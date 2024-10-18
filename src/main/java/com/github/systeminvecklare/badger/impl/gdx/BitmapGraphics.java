package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.util.GeometryUtil;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class BitmapGraphics implements IMovieClipLayer {
	private String textureName;
	private Color tint;
	private float centerX = 0f;
	private float centerY = 0f;
	private Float width;
	private Float height;
	private TextureWrap xWrap = TextureWrap.ClampToEdge;
	private TextureWrap yWrap = TextureWrap.ClampToEdge;
	private boolean hittable = false;
	
	public BitmapGraphics(String textureName, Color tint) {
		this(textureName, tint, null, null);
	}
	
	public BitmapGraphics(String textureName, Color tint, Float width, Float height) {
		this.textureName = textureName;
		this.tint = tint;
		this.width = width;
		this.height = height;
	}

	public BitmapGraphics setCenter(float x, float y) {
		this.centerX = x;
		this.centerY = y;
		return this;
	}
	
	
	public BitmapGraphics setCenter(float xAndY) {
		return setCenter(xAndY, xAndY);
	}
	
	public BitmapGraphics setCenterRelative(float rx, float ry) {
		return setCenter(rx*getWidth(), ry*getHeight());
	}


	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(getTint());
		ITexture texture = TextureStore.getTexture(textureName);
		texture.setWrap(xWrap, yWrap);
		
		float theWidth = getWidth();
		float theHeight = getHeight();
		texture.draw(spriteBatch, -getCenterX(), -getCenterY(), theWidth, theHeight);
	}
	
	public Color getTint() {
		return tint;
	}

	public float getHeight() {
		ITexture texture = TextureStore.getTexture(textureName);
		float theHeight;
		if(width == null && height == null)
		{
			theHeight = texture.getHeight();
		}
		else if(width == null)
		{
			theHeight = height;
		}
		else if(height == null)
		{
			theHeight = texture.getHeight()*width/texture.getWidth();
		}
		else
		{
			theHeight = height;
		}
		return theHeight;
	}

	public float getWidth() {
		ITexture texture = TextureStore.getTexture(textureName);
		float theWidth;
		if(width == null && height == null)
		{
			theWidth = texture.getWidth();
		}
		else if(width == null)
		{
			theWidth = texture.getWidth()*height/texture.getHeight();
		}
		else if(height == null)
		{
			theWidth = width;
		}
		else
		{
			theWidth = width;
		}
		return theWidth;
	}
	
	public float getCenterX() {
		return centerX;
	}
	
	public float getCenterY() {
		return centerY;
	}

	public BitmapGraphics repeatX() {
		this.xWrap = TextureWrap.Repeat;
		return this;
	}
	
	public BitmapGraphics repeatY() {
		this.yWrap = TextureWrap.Repeat;
		return this;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable) {
			return GeometryUtil.isInRectangle(p.getX(), p.getY(), -getCenterX(), -getCenterY(), getWidth(), getHeight());
		}
		//TODO this needs to take into account scaling and center
//			Texture texture = TextureStore.getTexture(textureName);
//			
//			double x = p.getX();
//			double y = p.getY();
//			if(x >= 0 && y >= 0 && x < texture.getWidth() && y < texture.getHeight())
//			{
//				TextureData text = texture.getTextureData();
//				if(!text.isPrepared())
//				{
//					text.prepare();
//				}
//				Pixmap pixmap = text.consumePixmap();
//				try
//				{
//					int colorInt = pixmap.getPixel((int) x, texture.getHeight()-(int) y);
//					return new Color(colorInt).a > 0.0f;
//				}
//				finally
//				{
//					if(text.disposePixmap())
//					{
//						pixmap.dispose();
//					}
//				}
//			}
//			else
//			{
//				return false;
//			}
		return false;
	}


	@Override
	public void dispose() {
		textureName = null;
		tint = null;
	}

	@Override
	public void init() {
	}

	public BitmapGraphics makeHittable() {
		hittable = true;
		return this;
	}
}
