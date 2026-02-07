package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.core.widget.PlaceholderWidget;
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
	private final PlaceholderWidget rectangleCache = new PlaceholderWidget();
	
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
		// Sample rectangle
		rectangleCache.setTo(rectangle);
		if(rectangleCache.getWidth() == 0 || rectangleCache.getHeight() == 0) {
			return;
		}
		
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		gdxDrawCycle.updateSpriteBatchTransform();
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		spriteBatch.setColor(color);
		final NinePatchDefinition ninePatch = getNinePatch();
		ITexture texture = TextureStore.getTexture(ninePatch.textureName);
		
		int ninePatchLeft = ninePatch.left;
		int ninePatchRight = ninePatch.right;
		int ninePatchBottom = ninePatch.bottom;
		int ninePatchTop = ninePatch.top;
		//TODO Optimization: Cache on rectangle (x,y,width,height,ninepatch,texture) and render to separate texture (with Color.WHITE). In most cases these won't change and we can reuse the prerendered texture! (and if we do this we won't need to have the CachedSubTextures)
		
		final int ninePatchHorizontal = ninePatchLeft + ninePatchRight;
		final int ninePatchVertical = ninePatchTop + ninePatchBottom;
		
		int horizontalInnerSpace = rectangleCache.getWidth() - ninePatchHorizontal;
		int verticalInnerSpace = rectangleCache.getHeight() - ninePatchVertical;
		
		int bottomCornersMaxHeight = ninePatchBottom - ninePatchBottom*(ninePatchVertical - rectangleCache.getHeight())/ninePatchVertical;
		int topCornersMaxHeight = rectangleCache.getHeight() - bottomCornersMaxHeight;
		int rightCornersMaxWidth = ninePatchRight - ninePatchRight*(ninePatchHorizontal - rectangleCache.getWidth())/ninePatchHorizontal;
		int leftCornersMaxWidth = rectangleCache.getWidth() - rightCornersMaxWidth;

		ninePatchLeft = Math.min(ninePatchLeft, leftCornersMaxWidth);
		ninePatchRight = Math.min(ninePatchRight, rightCornersMaxWidth);
		ninePatchBottom = Math.min(ninePatchBottom, bottomCornersMaxHeight);
		ninePatchTop = Math.min(ninePatchTop, topCornersMaxHeight);
		
		// Bottom left corner
		texture.draw(spriteBatch, rectangleCache.getX(), rectangleCache.getY(), ninePatchLeft, ninePatchBottom, 0, texture.getHeight()-ninePatchBottom, ninePatchLeft, ninePatchBottom, false, false);
		
		// Bottom right corner
		texture.draw(spriteBatch, rectangleCache.getX()+rectangleCache.getWidth()-ninePatchRight, rectangleCache.getY(), ninePatchRight, ninePatchBottom, texture.getWidth() - ninePatchRight, texture.getHeight()-ninePatchBottom, ninePatchRight, ninePatchBottom, false, false);
		
		// Top left corner
		texture.draw(spriteBatch, rectangleCache.getX(), rectangleCache.getY()+rectangleCache.getHeight() - ninePatchTop, ninePatchLeft, ninePatchTop, 0, 0, ninePatchLeft, ninePatchTop, false, false);
		
		// Top left corner
		texture.draw(spriteBatch, rectangleCache.getX()+rectangleCache.getWidth()-ninePatchRight, rectangleCache.getY()+rectangleCache.getHeight() - ninePatchTop, ninePatchRight, ninePatchTop, texture.getWidth() - ninePatchRight, 0, ninePatchRight, ninePatchTop, false, false);
		
		if(cacheKey == null || !cacheKey.equals(ninePatch)) {
			cacheKey = ninePatch;
			cachedSubTextures = new CachedSubTextures(cacheKey, texture);
		}
		
		cachedSubTextures.invalidateIfTextureChanged(texture);
		
		if(horizontalInnerSpace > 0) {
			{
				ITexture subTexture = cachedSubTextures.getBottom(texture);
				float drawWidth = rectangleCache.getWidth() - ninePatchHorizontal;
				float drawHeight = Math.min(ninePatchBottom, bottomCornersMaxHeight);
				subTexture.draw(spriteBatch, rectangleCache.getX()+ninePatchLeft, rectangleCache.getY(), drawWidth, drawHeight, 0, 1, drawWidth/subTexture.getWidth(), 1f - drawHeight/subTexture.getHeight());
			}
			{
				ITexture subTexture = cachedSubTextures.getTop(texture);
				float drawWidth = rectangleCache.getWidth() - ninePatchHorizontal;
				float drawHeight = Math.min(ninePatchTop, topCornersMaxHeight);
				subTexture.draw(spriteBatch, rectangleCache.getX()+ninePatchLeft, rectangleCache.getY()+rectangleCache.getHeight() - drawHeight, drawWidth, drawHeight, 0, drawHeight/subTexture.getHeight(), drawWidth/subTexture.getWidth(), 0);
			}
		}
		if(verticalInnerSpace > 0) {
			{
				ITexture subTexture = cachedSubTextures.getLeft(texture);
				float drawWidth = Math.min(ninePatchLeft, leftCornersMaxWidth);
				float drawHeight = rectangleCache.getHeight() - ninePatchVertical;
				subTexture.draw(spriteBatch, rectangleCache.getX(), rectangleCache.getY()+ninePatchBottom, drawWidth, drawHeight, 0, 0, (drawWidth/subTexture.getWidth()), -drawHeight/subTexture.getHeight());
			}
			{
				ITexture subTexture = cachedSubTextures.getRight(texture);
				float drawWidth = Math.min(ninePatchRight, rightCornersMaxWidth);
				float drawHeight = rectangleCache.getHeight() - ninePatchVertical;
				subTexture.draw(spriteBatch, rectangleCache.getX()+rectangleCache.getWidth() - drawWidth, rectangleCache.getY()+ninePatchBottom, drawWidth, drawHeight, 1f - (drawWidth/subTexture.getWidth()), 0, 1, -drawHeight/subTexture.getHeight());
			}
		}
		if(drawMiddle() && verticalInnerSpace > 0 && horizontalInnerSpace > 0) {
			ITexture subTexture = cachedSubTextures.getMiddle(texture);
			float drawWidth = rectangleCache.getWidth() - ninePatchHorizontal;
			float drawHeight = rectangleCache.getHeight() - ninePatchVertical;
			subTexture.draw(spriteBatch, rectangleCache.getX()+ninePatchLeft, rectangleCache.getY()+ninePatchBottom, drawWidth, drawHeight, 0, 0, drawWidth/subTexture.getWidth(), -drawHeight/subTexture.getHeight());
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
		
		private ITexture textureKey;
		
		private ITexture bottom = null;
		private ITexture top = null;
		private ITexture left = null;
		private ITexture right = null;
		private ITexture middle = null;

		
		public CachedSubTextures(NinePatchDefinition ninePatch, ITexture texture) {
			this.ninePatch = ninePatch;
			this.ninePatchHorizontal = ninePatch.left + ninePatch.right;
			this.ninePatchVertical = ninePatch.top + ninePatch.bottom;
			this.textureKey = texture;
		}
		
		public void invalidateIfTextureChanged(ITexture textureKey) {
			if(this.textureKey != textureKey) {
				this.textureKey = textureKey;
				
				bottom = null;
				top = null;
				left = null;
				right = null;
				middle = null;
			}
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
