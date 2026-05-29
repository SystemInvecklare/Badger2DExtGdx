package com.github.systeminvecklare.badger.impl.gdx.fbo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.pooling.IPool;
import com.github.systeminvecklare.badger.core.pooling.IPoolable;
import com.github.systeminvecklare.badger.core.pooling.SimplePool;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.GdxTransform;
import com.github.systeminvecklare.badger.impl.gdx.store.IStore;
import com.github.systeminvecklare.badger.impl.gdx.util.GlFlagState;

public class FboManager implements IHookableFboManager, IStore {
	private final IPool<FboRendering> renderingPool = new SimplePool<FboRendering>(2, 10) {
		@Override
		public FboRendering newObject() {
			return new FboRendering(this);
		}
		
		@Override
		public void free(FboRendering poolable) {
			poolable.clear();
			super.free(poolable);
		}
	};
	private final Map<IFboDefinition, AdhocFbo> fbos = new HashMap<IFboDefinition, AdhocFbo>();
	private final Array<FboRendering> renderingStack = new Array<FboRendering>();
	
	private final Matrix4 utilMatrix = new Matrix4();
	private final BlendFunction utilBlendFucnction = new BlendFunction(0, 0);
	private final Color utilColor = new Color();

	@Override
	public boolean drawCached(IDrawCycle drawCycle, IFboDefinition definition) {
		AdhocFbo adhocFbo = fbos.get(definition);
		if(adhocFbo == null) {
			FboRendering ongoingRendering = null;
			if(!renderingStack.isEmpty()) {
				ongoingRendering = renderingStack.peek();
				ongoingRendering.spriteBatch.flush();
				ongoingRendering.adhocFbo.fbo.end(); // Pause fbo
			}
			
			// No fbo bound, so we can safely construct new fbo
			adhocFbo = new AdhocFbo(definition);
			
			if(ongoingRendering != null) {
				ongoingRendering.adhocFbo.fbo.begin(); // Resume fbo
			}
			fbos.put(definition, adhocFbo);
		}
		adhocFbo.renderedThisFrame = true;
		
		// Handle queued refresh rectangle
		if(adhocFbo.rectangleRefreshRequested) {
			adhocFbo.rectangleRefreshRequested = false;
			FboRendering ongoingRendering = null;
			if(!renderingStack.isEmpty()) {
				ongoingRendering = renderingStack.peek();
				ongoingRendering.spriteBatch.flush();
				ongoingRendering.adhocFbo.fbo.end(); // Pause fbo
			}
			
			// No fbo bound, so we can safely run code that potentially creates new fbo
			adhocFbo.refreshRectangle(definition);
			
			if(ongoingRendering != null) {
				ongoingRendering.adhocFbo.fbo.begin(); // Resume fbo
			}
		}
		
		if(adhocFbo.fbo.isEmpty()) {
			return false; // Fbo is empty. No need to render anything
		}
		
		if(!adhocFbo.dirty) {
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
			
			utilBlendFucnction.copyFrom(spriteBatch);
			
			utilColor.set(spriteBatch.getColor());
			spriteBatch.setColor(adhocFbo.color);
			BlendFunction.BLEND_FBO.apply(spriteBatch);
			adhocFbo.draw(spriteBatch);
			spriteBatch.setColor(utilColor);
			utilBlendFucnction.apply(spriteBatch);
			return false;
		}
		
		// 1. Create new FboRendering on top
		FboRendering currentRendering = renderingPool.obtain();
		if(!renderingStack.isEmpty()) {
			currentRendering.parentRendering = renderingStack.peek();
		}
		renderingStack.add(currentRendering); // Push to stack
		
		currentRendering.adhocFbo = adhocFbo;
		currentRendering.drawCycle = (GdxDrawCycle) drawCycle;
		
		GdxDrawCycle gdxDrawCycle = currentRendering.drawCycle;
		currentRendering.spriteBatch = gdxDrawCycle.getSpriteBatch();
		SpriteBatch spriteBatch = currentRendering.spriteBatch;
		
		
		// 2. store current state
		currentRendering.originalProjectionMatrix.set(spriteBatch.getProjectionMatrix());
		currentRendering.original.setTo(gdxDrawCycle.getTransform());
		currentRendering.originalBlendFunc.copyFrom(spriteBatch);
		currentRendering.originalScissorTest.storeState();
		
		// 3. flush + end previous fbo OR to nothing (if to screen).
		spriteBatch.flush();
		
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.spriteBatch.flush();
			currentRendering.parentRendering.adhocFbo.fbo.end();
		}
		
		// 4. begin current fbo
		adhocFbo.fbo.begin();
		
		// 5. set current state to clean-slate
		spriteBatch.setProjectionMatrix(utilMatrix.setToOrtho2D(currentRendering.adhocFbo.x, currentRendering.adhocFbo.y, currentRendering.adhocFbo.fbo.getWidth(), currentRendering.adhocFbo.fbo.getHeight()));
		gdxDrawCycle.getTransform().setToIdentity();
		gdxDrawCycle.updateSpriteBatchTransform();
		BlendFunction.BLEND_INTO_FBO.apply(spriteBatch);
		currentRendering.originalScissorTest.setEnabled(false);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// 6. render
		return true;
	}
	
	@Override
	public void done() {
		// 7. flush + end current fbo
		FboRendering currentRendering = renderingStack.pop();
		currentRendering.spriteBatch.flush();
		currentRendering.adhocFbo.fbo.end();
		currentRendering.adhocFbo.dirty = false;
		
		// 8. begin previous fbo
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.adhocFbo.fbo.begin();
		}
		
		// 9. restore state
		currentRendering.spriteBatch.setProjectionMatrix(currentRendering.originalProjectionMatrix);
		currentRendering.drawCycle.getTransform().setTo(currentRendering.original);
		currentRendering.drawCycle.updateSpriteBatchTransform();
		currentRendering.originalBlendFunc.apply(currentRendering.spriteBatch);
		currentRendering.originalScissorTest.restoreState();
		
		// 10. draw rendered texture
		utilColor.set(currentRendering.spriteBatch.getColor());
		utilBlendFucnction.copyFrom(currentRendering.spriteBatch);
		BlendFunction.BLEND_FBO.apply(currentRendering.spriteBatch);
		currentRendering.spriteBatch.setColor(currentRendering.adhocFbo.color);
		currentRendering.adhocFbo.draw(currentRendering.spriteBatch);
		currentRendering.spriteBatch.setColor(utilColor);
		utilBlendFucnction.apply(currentRendering.spriteBatch);
		
		currentRendering.clear();
	}
	

	@Override
	public void setDirty(IFboDefinition definition) {
		AdhocFbo fbo = fbos.get(definition);
		if(fbo != null) {
			fbo.dirty = true;
		}
	}
	
	@Override
	public void setRectangleDirty(IFboDefinition definition) {
		AdhocFbo fbo = fbos.get(definition);
		if(fbo != null) {
			fbo.rectangleRefreshRequested = true;
		}
	}
	
	@Override
	public void setTint(IFboDefinition definition, Color tint) {
		if(tint == null) {
			throw new NullPointerException("tint is null");
		}
		AdhocFbo fbo = fbos.get(definition);
		if(fbo != null) {
			if(fbo.color == Color.WHITE) {
				fbo.color = new Color();
			}
			fbo.color.set(tint);
			fbo.color.a = 1;
		}
	}
	
	/**
	 * Override and clear Array instead if you want to disable crash. Could be good for production.
	 */
	protected void onInvalidRenderStack(Array<?> renderingStack) {
		throw new RuntimeException("Rendering stack not empty after scene draw! Note: if fboManager.drawCached() returns true, then fboManager.done() MUST be called. And never if shall it be so if fboManager.drawCached() returned false.");
	}

	@Override
	public void onAfterSceneDraw() {
		if(!renderingStack.isEmpty()) {
			onInvalidRenderStack(renderingStack);
		}

		Iterator<Entry<IFboDefinition, AdhocFbo>> iterator = fbos.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<IFboDefinition, AdhocFbo> entry = iterator.next();
			AdhocFbo adhocFbo = entry.getValue();
			if(!adhocFbo.renderedThisFrame) {
				adhocFbo.dispose();
				iterator.remove();
			} else {
				adhocFbo.renderedThisFrame = false;
			}
		}
	}
	
	@Override
	public List<IStore> getDependencies(List<IStore> result) {
		return result;
	}
	
	@Override
	public void disposeInventory() {
		for(Entry<IFboDefinition, AdhocFbo> fbo : fbos.entrySet()) {
			fbo.getValue().fbo.dispose();
		}
		fbos.clear();
	}
	
	@Override
	public void reloadInventory() {
	}
	
	private static class FboRendering implements IPoolable {
		private final IPool<FboRendering> pool;
		
		private AdhocFbo adhocFbo;
		private GdxDrawCycle drawCycle;
		private SpriteBatch spriteBatch;
		private FboRendering parentRendering;
		
		private final Matrix4 originalProjectionMatrix = new Matrix4();
		private final ITransform original = new GdxTransform(null);
		private final BlendFunction originalBlendFunc = new BlendFunction(0, 0);
		private final GlFlagState originalScissorTest = new GlFlagState(GL20.GL_SCISSOR_TEST);
		// Might want to cache state for
		// GL_DEPTH_TEST (should be disabled during FBO rendering)
		// GL_STENCIL_TEST (should be disabled during FBO rendering)
		// GL_CULL_FACE (should be disabled during FBO rendering)
		// GL_BLEND (should be ENALBED during FBO rendering)
		// glColorMask (all channels should be ENABLED during FBO rendering)
		
		public FboRendering(IPool<FboRendering> pool) {
			this.pool = pool;
		}
		
		@Override
		public void free() {
			pool.free(this);
		}

		public void clear() {
			adhocFbo = null;
			drawCycle = null;
			spriteBatch = null;
			parentRendering = null;
		}
	}
	
	private static class AdhocFbo {
		private Color color = Color.WHITE;
		private IFrameBuffer fbo;
		private final TextureRegion textureRegion = new TextureRegion();
		private int x;
		private int y;
		private boolean dirty = true;
		private boolean renderedThisFrame = false;
		private boolean rectangleRefreshRequested = false;
		
		public AdhocFbo(IFboDefinition definition) {
			int width = definition.getWidth();
			int height = definition.getHeight();
			this.x = definition.getX();
			this.y = definition.getY();
			
			int potWidth = nextPowerOfTwo(width);
			int potHeight = nextPowerOfTwo(height);
			
			this.fbo = newFrameBuffer(potWidth, potHeight);
			
			fbo.setupRegion(textureRegion, width, height);
			
			this.textureRegion.flip(false, true);
		}
		
		public void draw(SpriteBatch spriteBatch) {
			if(fbo == null) {
				throw new RuntimeException("FBO is disposed! (null)");
			}
			if(fbo.isEmpty()) {
				throw new RuntimeException("FBO is empty! Should never have gotten to this");
			}
			spriteBatch.draw(textureRegion, x, y);
		}

		public void refreshRectangle(IFboDefinition definition) {
			int newX = definition.getX();
			int newY = definition.getY();
			int newWidth = definition.getWidth();
			int newHeight = definition.getHeight();
			if(newWidth != textureRegion.getRegionWidth() || newHeight != textureRegion.getRegionHeight() ||  x != newX || y != newY) {
				// Change detected
				dirty = true;
				int potWidth = nextPowerOfTwo(newWidth);
				int potHeight = nextPowerOfTwo(newHeight);
				if(potWidth != fbo.getWidth() || potHeight != fbo.getHeight()) {
					fbo.dispose();
					fbo = newFrameBuffer(potWidth, potHeight);
					
					fbo.setupRegion(textureRegion, newWidth, newHeight);
				}
				textureRegion.setRegionWidth(newWidth);
				textureRegion.setRegionWidth(newHeight);
				this.x = newX;
				this.y = newY;
			}
		}

		private static int nextPowerOfTwo(int n) {
			// Haxx from the internietz
		    n--;
		    n |= n >> 1;
		    n |= n >> 2;
		    n |= n >> 4;
		    n |= n >> 8;
		    n |= n >> 16;
		    return n + 1;
		}
		
		private IFrameBuffer newFrameBuffer(int width, int height) {
			if(width <= 0 || height <= 0) {
				return new EmptyFrameBuffer(width, height);
			}
			return new RealFrameBuffer(new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false));
		}
		
		public void dispose() {
			fbo.dispose();
			fbo = null;
			textureRegion.setTexture(null);
		}
	}
	
	private interface IFrameBuffer {
		int getWidth();
		int getHeight();
		boolean isEmpty();
		void begin();
		void end();
		void setupRegion(TextureRegion textureRegion, int width, int height);
		void dispose();
	}
	
	private static class RealFrameBuffer implements IFrameBuffer {
		private final FrameBuffer real;

		public RealFrameBuffer(FrameBuffer real) {
			this.real = real;
		}

		@Override
		public void setupRegion(TextureRegion textureRegion, int width, int height) {
			textureRegion.setRegion(real.getColorBufferTexture());
			textureRegion.setRegion(0, 0, width, height);
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public int getWidth() {
			return real.getWidth();
		}

		@Override
		public int getHeight() {
			return real.getHeight();
		}
		
		@Override
		public void begin() {
			real.begin();
		}
		
		@Override
		public void end() {
			real.end();
		}

		@Override
		public void dispose() {
			real.dispose();
		}
	}
	
	private static class EmptyFrameBuffer implements IFrameBuffer {
		private final int width;
		private final int height;
		
		public EmptyFrameBuffer(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public void begin() {
			throw new IllegalStateException();
		}

		@Override
		public void end() {
			throw new IllegalStateException();
		}

		@Override
		public void setupRegion(TextureRegion textureRegion, int width, int height) {
		}

		@Override
		public void dispose() {
		}
	}
	
	private static class BlendFunction {
		public static final BlendFunction BLEND_FBO = new BlendFunction(GL20.GL_ONE, GL20.GL_SRC_ALPHA, GL20.GL_ZERO, GL20.GL_SRC_ALPHA) {
			@Override
			public BlendFunction setTo(int srcFunc, int dstFunc, int srcFuncAlpha, int dstFuncAlpha) {
				throw new UnsupportedOperationException("Don't overwrite static field BLEND_FBO!");
			}
		};
		public static final BlendFunction BLEND_INTO_FBO = new BlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA) {
			@Override
			public BlendFunction setTo(int srcFunc, int dstFunc, int srcFuncAlpha, int dstFuncAlpha) {
				throw new UnsupportedOperationException("Don't overwrite static field BLEND_INTO_FBO!");
			}
		};
		
		private int srcFunc;
		private int dstFunc;
		private int srcFuncAlpha;
		private int dstFuncAlpha;
		
		public BlendFunction(int srcFunc, int dstFunc) {
			this(srcFunc, dstFunc, srcFunc, dstFunc);
		}
		
		public BlendFunction(int srcFunc, int dstFunc, int srcFuncAlpha, int dstFuncAlpha) {
			this.srcFunc = srcFunc;
			this.dstFunc = dstFunc;
			this.srcFuncAlpha = srcFuncAlpha;
			this.dstFuncAlpha = dstFuncAlpha;
		}

		public void apply(SpriteBatch spriteBatch) {
			spriteBatch.setBlendFunctionSeparate(srcFunc, dstFunc, srcFuncAlpha, dstFuncAlpha);
		}
		
		public void copyFrom(SpriteBatch sampled) {
			setTo(sampled.getBlendSrcFunc(), sampled.getBlendDstFunc(), sampled.getBlendSrcFuncAlpha(), sampled.getBlendDstFuncAlpha());
		}
		
		public BlendFunction setTo(int srcFunc, int dstFunc, int srcFuncAlpha, int dstFuncAlpha) {
			this.srcFunc = srcFunc;
			this.dstFunc = dstFunc;
			this.srcFuncAlpha = srcFuncAlpha;
			this.dstFuncAlpha = dstFuncAlpha;
			return this;
		}
	}
}
