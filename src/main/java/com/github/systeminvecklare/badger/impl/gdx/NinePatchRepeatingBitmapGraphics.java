package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.NinePatchDefinition;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class NinePatchRepeatingBitmapGraphics implements IMovieClipLayer {
	private final NinePatchDefinition ninePatch;
	private final IRectangle rectangle;
	private final Color color;
	private boolean drawMiddle = true;
	
	private NinePatchDefinition cacheKey = null;
	private CachedSubTextures cachedSubTextures = null;
	
	public NinePatchRepeatingBitmapGraphics(String texture, int inset, IRectangle rectangle) {
		this(texture, inset, rectangle, Color.WHITE);
	}
	
	public NinePatchRepeatingBitmapGraphics(String texture, int inset, IRectangle rectangle, Color color) {
		this(new NinePatchDefinition(texture, inset), rectangle, color);
	}
	
	public NinePatchRepeatingBitmapGraphics(NinePatchDefinition ninePatchDefinition, IRectangle rectangle) {
		this(ninePatchDefinition, rectangle, Color.WHITE);
	}
	
	public NinePatchRepeatingBitmapGraphics(NinePatchDefinition ninePatchDefinition, IRectangle rectangle, Color color) {
		this.ninePatch = ninePatchDefinition;
		this.rectangle = rectangle;
		this.color = color;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		gdxDrawCycle.updateSpriteBatchTransform();
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		spriteBatch.setColor(color);
		final NinePatchDefinition ninePatch = getNinePatch();
		ITexture texture = TextureStore.getTexture(ninePatch.textureName);
		
		//TODO Optimization: Cache on rectangle (x,y,width,height,ninepatch,texture) and render to separate texture (with Color.WHITE). In most cases these won't change and we can reuse the prerendered texture! (and if we do this we won't need to have the CachedSubTextures)
		
		texture.draw(spriteBatch, rectangle.getX(), rectangle.getY(), ninePatch.left, ninePatch.bottom, 0, texture.getHeight()-ninePatch.bottom, ninePatch.left, ninePatch.bottom, false, false);
		texture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth()-ninePatch.right, rectangle.getY(), ninePatch.right, ninePatch.bottom, texture.getWidth() - ninePatch.right, texture.getHeight()-ninePatch.bottom, ninePatch.right, ninePatch.bottom, false, false);
		texture.draw(spriteBatch, rectangle.getX(), rectangle.getY()+rectangle.getHeight() - ninePatch.top, ninePatch.left, ninePatch.top, 0, 0, ninePatch.left, ninePatch.top, false, false);
		texture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth()-ninePatch.right, rectangle.getY()+rectangle.getHeight() - ninePatch.top, ninePatch.right, ninePatch.top, texture.getWidth() - ninePatch.right, 0, ninePatch.right, ninePatch.top, false, false);
		
		if(cacheKey == null || !cacheKey.equals(ninePatch)) {
			cacheKey = ninePatch;
			cachedSubTextures = new CachedSubTextures(cacheKey);
		}
		final int ninePatchHorizontal = ninePatch.left + ninePatch.right;
		final int ninePatchVertical = ninePatch.top + ninePatch.bottom;
		{
			ITexture subTexture = cachedSubTextures.getBottom(texture);
			float drawWidth = rectangle.getWidth() - ninePatchHorizontal;
			subTexture.draw(spriteBatch, rectangle.getX()+ninePatch.left, rectangle.getY(), drawWidth, ninePatch.bottom, 0, 1, drawWidth/subTexture.getWidth(), 0);
		}
		{
			ITexture subTexture = cachedSubTextures.getTop(texture);
			float drawWidth = rectangle.getWidth() - ninePatchHorizontal;
			subTexture.draw(spriteBatch, rectangle.getX()+ninePatch.left, rectangle.getY()+rectangle.getHeight() - ninePatch.top, drawWidth, ninePatch.top, 0, 1, drawWidth/subTexture.getWidth(), 0);
		}
		{
			ITexture subTexture = cachedSubTextures.getLeft(texture);
			float drawHeight = rectangle.getHeight() - ninePatchVertical;
			subTexture.draw(spriteBatch, rectangle.getX(), rectangle.getY()+ninePatch.bottom, ninePatch.left, drawHeight, 0, 0, 1, -drawHeight/subTexture.getHeight());
		}
		{
			ITexture subTexture = cachedSubTextures.getRight(texture);
			float drawHeight = rectangle.getHeight() - ninePatchVertical;
			subTexture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth() - ninePatch.right, rectangle.getY()+ninePatch.bottom, ninePatch.right, drawHeight, 0, 0, 1, -drawHeight/subTexture.getHeight());
		}
		if(drawMiddle()) {
			ITexture subTexture = cachedSubTextures.getMiddle(texture);
			float drawWidth = rectangle.getWidth() - ninePatchHorizontal;
			float drawHeight = rectangle.getHeight() - ninePatchVertical;
			subTexture.draw(spriteBatch, rectangle.getX()+ninePatch.left, rectangle.getY()+ninePatch.bottom, drawWidth, drawHeight, 0, 0, drawWidth/subTexture.getWidth(), -drawHeight/subTexture.getHeight());
		}
	}


	protected NinePatchDefinition getNinePatch() {
		return ninePatch;
	}
	
	protected boolean drawMiddle() {
		return drawMiddle;
	}
	
	public NinePatchRepeatingBitmapGraphics setDrawMiddle(boolean drawMiddle) {
		this.drawMiddle = drawMiddle;
		return this;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		return false;
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
	}
	
	private static class CachedSubTextures {
		private final NinePatchDefinition ninePatch;
		private final int ninePatchHorizontal;
		private final int ninePatchVertical;
		
		private ITexture bottom = null;
		private ITexture top = null;
		private ITexture left = null;
		private ITexture right = null;
		private ITexture middle = null;

		
		public CachedSubTextures(NinePatchDefinition ninePatch) {
			this.ninePatch = ninePatch;
			this.ninePatchHorizontal = ninePatch.left + ninePatch.right;
			this.ninePatchVertical = ninePatch.top + ninePatch.bottom;
		}

		public ITexture getMiddle(ITexture texture) {
			if(middle == null) {
				ITexture subTexture = texture.createSubTexture(ninePatch.left, ninePatch.top, texture.getWidth() - ninePatchHorizontal, texture.getHeight() - ninePatchVertical);
				subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				middle = subTexture;
			}
			return middle;
		}

		public ITexture getRight(ITexture texture) {
			if(right == null) {
				ITexture subTexture = texture.createSubTexture(texture.getWidth() - ninePatch.right, ninePatch.top, ninePatch.right, texture.getHeight() - ninePatchVertical);
				subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				right = subTexture;
			}
			return right;
		}

		public ITexture getLeft(ITexture texture) {
			if(left == null) {
				ITexture subTexture = texture.createSubTexture(0, ninePatch.top, ninePatch.left, texture.getHeight() - ninePatchVertical);
				subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				left = subTexture;
			}
			return left;
		}

		public ITexture getTop(ITexture texture) {
			if(top == null) {
				ITexture subTexture = texture.createSubTexture(ninePatch.left, 0, texture.getWidth() - ninePatchHorizontal, ninePatch.top);
				subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				top = subTexture;
			}
			return top;
		}

		public ITexture getBottom(ITexture texture) {
			if(bottom == null) {
				ITexture subTexture = texture.createSubTexture(ninePatch.left, texture.getHeight() - ninePatch.bottom, texture.getWidth() - ninePatchHorizontal, ninePatch.bottom);
				subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				bottom = subTexture;
			}
			return bottom;
		}
	}
}
