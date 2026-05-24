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
import com.github.systeminvecklare.badger.core.math.Mathf;
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
		AdhocFbo adhocFbo = fbos.get(definition);
		if(adhocFbo == null) {
			adhocFbo = new AdhocFbo(definition);
			fbos.put(definition, adhocFbo);
		}
		adhocFbo.renderedThisFrame = true;
		
		if(adhocFbo.fbo.isEmpty()) {
			return false; // Fbo is empty. No need to render anything
		}
		
		if(!adhocFbo.dirty) {
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
//			spriteBatch.setColor(adhocFbo.color);
			spriteBatch.setColor(new Color(Mathf.random(), Mathf.random(), Mathf.random(), 1)); //TODO remove
			spriteBatch.draw(adhocFbo.textureRegion, adhocFbo.x, adhocFbo.y);
			return false;
		}
		
		
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
		spriteBatch.flush();
		
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.spriteBatch.flush();
			currentRendering.parentRendering.adhocFbo.fbo.end();
		}
		
		currentRendering.originalProjectionMatrix.set(spriteBatch.getProjectionMatrix());
		currentRendering.originalTransformMatrix.set(spriteBatch.getTransformMatrix());
		
		currentRendering.original.setTo(drawCycle.getTransform());
		drawCycle.getTransform().setToIdentity();
		gdxDrawCycle.updateSpriteBatchTransform();
		
		spriteBatch.setProjectionMatrix(utilMatrix.setToOrtho2D(currentRendering.adhocFbo.x, currentRendering.adhocFbo.y, currentRendering.adhocFbo.fbo.getWidth(), currentRendering.adhocFbo.fbo.getHeight()));
		spriteBatch.setTransformMatrix(utilMatrix.idt());
		adhocFbo.fbo.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
//		Gdx.gl.glClearColor(Mathf.random(), Mathf.random(), Mathf.random(), 0.5f); // Useful for a visual representation of when things are redrawn
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
		FboRendering currentRendering = renderingStack.pop();
		currentRendering.spriteBatch.flush();
		currentRendering.adhocFbo.fbo.end();
		currentRendering.adhocFbo.dirty = false;
		
		if(currentRendering.parentRendering != null) {
			currentRendering.parentRendering.adhocFbo.fbo.begin();
		}
		
		currentRendering.spriteBatch.setProjectionMatrix(currentRendering.originalProjectionMatrix);
		currentRendering.spriteBatch.setTransformMatrix(currentRendering.originalTransformMatrix);
		currentRendering.drawCycle.getTransform().setTo(currentRendering.original);
		currentRendering.drawCycle.updateSpriteBatchTransform();
		currentRendering.spriteBatch.setColor(currentRendering.adhocFbo.color);
		currentRendering.spriteBatch.draw(currentRendering.adhocFbo.textureRegion, currentRendering.adhocFbo.x, currentRendering.adhocFbo.y);
		
		currentRendering.clear();
	}
	

	@Override
	public void onAfterSceneDraw() {
		//TODO make sure rendering stack is empty. Otherwise throw
		
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
	
	private static class FboRendering implements IPoolable {
		private final IPool<FboRendering> pool;
		
		private AdhocFbo adhocFbo;
		private GdxDrawCycle drawCycle;
		private SpriteBatch spriteBatch;
		private FboRendering parentRendering;
		
		private final Matrix4 originalProjectionMatrix = new Matrix4();
		private final Matrix4 originalTransformMatrix = new Matrix4();
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
		
		public AdhocFbo(IFboDefinition definition) {
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
		}

		@Override
		public void end() {
		}

		@Override
		public void setupRegion(TextureRegion textureRegion, int width, int height) {
		}

		@Override
		public void dispose() {
		}
	}
}
