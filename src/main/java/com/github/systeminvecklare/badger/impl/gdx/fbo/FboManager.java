package com.github.systeminvecklare.badger.impl.gdx.fbo;

import java.util.HashMap;
import java.util.Iterator;
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

public class FboManager implements IHookableFboManager {
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

	//TODO Would be good if we could pass a Color also. That way FlashyBitmapFont could be written smartly and wouldn't need to re-render when color changes.
	@Override
	public boolean drawCached(IDrawCycle drawCycle, IFboDefinition definition) {
		FBO_DEBUG.println("Begin drawCached "+definition.DEBUG_name());
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
		
		if(adhocFbo.fbo.isEmpty()) {
			FBO_DEBUG.println("It's empty");
			FBO_DEBUG.println("drawCached DONE");
			return false; // Fbo is empty. No need to render anything
		}
		
		if(!adhocFbo.dirty) {
			FBO_DEBUG.println("It's clean! Let's redraw texture!");
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
			FBO_DEBUG.println("Drawing texture with "+FBO_DEBUG.getSpritebatchCombined(spriteBatch));
			//TODO reuse color object
			Color previousColor = new Color(spriteBatch.getColor());
			spriteBatch.setColor(adhocFbo.color);
			adhocFbo.draw(spriteBatch);
//			spriteBatch.draw(adhocFbo.textureRegion, adhocFbo.x, adhocFbo.y);
			spriteBatch.setColor(previousColor);
			FBO_DEBUG.println("drawCached DONE");
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
		
		gdxDrawCycle.updateSpriteBatchTransform(); //TODO only for debug
		FBO_DEBUG.println("Stored state "+FBO_DEBUG.getSpritebatchCombined(spriteBatch));
		
		// 3. flush + end previous fbo OR to nothing (if to screen).
		spriteBatch.flush();
		
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.spriteBatch.flush();
			currentRendering.parentRendering.adhocFbo.fbo.end();
			FBO_DEBUG.println("Ended parent rendering.");
		}
		
		// 4. begin current fbo
		FBO_DEBUG.println("Begin new fbo");
		adhocFbo.fbo.begin();
		
		// 5. set current state to clean-slate
		spriteBatch.setProjectionMatrix(utilMatrix.setToOrtho2D(currentRendering.adhocFbo.x, currentRendering.adhocFbo.y, currentRendering.adhocFbo.fbo.getWidth(), currentRendering.adhocFbo.fbo.getHeight()));
		gdxDrawCycle.getTransform().setToIdentity();
		gdxDrawCycle.updateSpriteBatchTransform();
		
		FBO_DEBUG.println("Set clean-slate state: "+FBO_DEBUG.getSpritebatchCombined(spriteBatch));
		
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// 6. render
		return true;
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
			fbo.refreshRectangle(definition);
		}
	}
	
	@Override
	public void setTint(IFboDefinition definition, Color tint) {
		if(tint == null) {
			throw new NullPointerException("tint is null");
		}
		AdhocFbo fbo = fbos.get(definition);
		if(fbo == null) {
			fbo = new AdhocFbo(definition);
			fbos.put(definition, fbo);
		}
		fbo.color = tint;
	}

	@Override
	public void done() {
		// 7. flush + end current fbo
		FboRendering currentRendering = renderingStack.pop();
		currentRendering.spriteBatch.flush();
		currentRendering.adhocFbo.fbo.end();
		currentRendering.adhocFbo.dirty = false;
		FBO_DEBUG.println("Ended rendering.");
		
		// 8. begin previous fbo
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.adhocFbo.fbo.begin();
			FBO_DEBUG.println("Began parent rendering (again).");
		}
		
		// 9. restore state
		currentRendering.spriteBatch.setProjectionMatrix(currentRendering.originalProjectionMatrix);
		currentRendering.drawCycle.getTransform().setTo(currentRendering.original);
		currentRendering.drawCycle.updateSpriteBatchTransform();
		
		FBO_DEBUG.println("Restored state to: "+FBO_DEBUG.getSpritebatchCombined(currentRendering.spriteBatch));
		
		
		// 10. draw rendered texture
		//TODO reuse Color object
		Color previousColor = new Color(currentRendering.spriteBatch.getColor());
		currentRendering.spriteBatch.setColor(currentRendering.adhocFbo.color);
		FBO_DEBUG.println("Drawing newly rendered");
		currentRendering.adhocFbo.draw(currentRendering.spriteBatch);
//		currentRendering.spriteBatch.draw(currentRendering.adhocFbo.textureRegion, currentRendering.adhocFbo.x, currentRendering.adhocFbo.y);
		currentRendering.spriteBatch.setColor(previousColor);
		
		currentRendering.clear();
		FBO_DEBUG.println("drawCached DONE");
	}
	

	@Override
	public void onAfterSceneDraw() {
		//TODO make sure rendering stack is empty. Otherwise throw

		FBO_DEBUG.println("---cleaning up!---");
		
		Iterator<Entry<IFboDefinition, AdhocFbo>> iterator = fbos.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<IFboDefinition, AdhocFbo> entry = iterator.next();
			AdhocFbo adhocFbo = entry.getValue();
			if(!adhocFbo.renderedThisFrame) {
				adhocFbo.dispose();
				iterator.remove();
				FBO_DEBUG.println("Removed "+adhocFbo.DEBUG_def.DEBUG_name());
			} else {
				adhocFbo.renderedThisFrame = false;
			}
		}
		FBO_DEBUG.println("----FRAME END---");
		FBO_DEBUG.commit();
	}
	
	private static class FboRendering implements IPoolable {
		private final IPool<FboRendering> pool;
		
		private AdhocFbo adhocFbo;
		private GdxDrawCycle drawCycle;
		private SpriteBatch spriteBatch;
		private FboRendering parentRendering;
		
		private final Matrix4 originalProjectionMatrix = new Matrix4();
		private final ITransform original = new GdxTransform(null);
		
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
		private boolean disposed = false; //TODO needed?
		private final IFboDefinition DEBUG_def;
		
		public AdhocFbo(IFboDefinition definition) {
			this.DEBUG_def = definition;
			//TODO we should allow for fbos to change size (also sets dirty)
			int width = definition.getWidth();
			int height = definition.getHeight();
			this.x = definition.getX();
			this.y = definition.getY();
			
			int potWidth = nextPowerOfTwo(width);
			int potHeight = nextPowerOfTwo(height);
			
			//TODO clean up and redraw when android minimizes (register a store)
			this.fbo = newFrameBuffer(potWidth, potHeight);
			
			fbo.setupRegion(textureRegion, width, height);
			
			this.textureRegion.flip(false, true);
		}
		
		public void draw(SpriteBatch spriteBatch) {
			if(disposed) {
				throw new RuntimeException("FBO is disposed!");
			}
			if(fbo.isEmpty()) {
				throw new RuntimeException("FBO is empty! Should never have gotten to this");
			}
			spriteBatch.draw(textureRegion, x, y);
		}

		public void refreshRectangle(IFboDefinition definition) {
			//TODO
			throw new RuntimeException("TODO don't do this immediately. Wait until start of next frame or something. Or before we actually try to use this. We need to make sure no FBO is bound.");
//			int newX = definition.getX();
//			int newY = definition.getY();
//			int newWidth = definition.getWidth();
//			int newHeight = definition.getHeight();
//			if(newWidth != textureRegion.getRegionWidth() || newHeight != textureRegion.getRegionHeight() ||  x != newX || y != newY) {
//				// Change detected
//				dirty = true;
//				int potWidth = nextPowerOfTwo(newWidth);
//				int potHeight = nextPowerOfTwo(newHeight);
//				if(potWidth != fbo.getWidth() || potHeight != fbo.getHeight()) {
//					fbo.dispose();
//					fbo = newFrameBuffer(potWidth, potHeight);
//					
//					fbo.setupRegion(textureRegion, newWidth, newHeight);
//				}
//				textureRegion.setRegionWidth(newWidth);
//				textureRegion.setRegionWidth(newHeight);
//				this.x = newX;
//				this.y = newY;
//			}
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
			disposed = true;
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
}
