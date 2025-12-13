package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class BitmapPartGraphics implements IMovieClipLayer {
	private String textureName;
	private Color tint;
	private int srcX;
	private int srcY;
	private int srcWidth;
	private int srcHeight;
	private float centerX = 0f;
	private float centerY = 0f;
	private float width;
	private float height;
	private TextureWrap xWrap = TextureWrap.ClampToEdge;
	private TextureWrap yWrap = TextureWrap.ClampToEdge;
	private boolean flipX = false;
	private boolean flipY = false;
	
	public BitmapPartGraphics(String textureName, Color tint, int srcX, int srcY, int srcWidth, int srcHeight) {
		this(textureName, tint, srcX, srcY, srcWidth, srcHeight,(float) srcWidth,(float) srcHeight);
	}
	
	public BitmapPartGraphics(String textureName, Color tint, int srcX, int srcY, int srcWidth, int srcHeight, Float width, Float height) {
		this.textureName = textureName;
		this.tint = tint;
		this.srcX = srcX;
		this.srcY = srcY;
		this.srcWidth = srcWidth;
		this.srcHeight = srcHeight;
		if(width == null)
		{
			if(height == null)
			{
				width = (float) srcWidth;
				height = (float) srcHeight;
			}
			else
			{
				width = (height*srcWidth)/srcHeight;
			}
		}
		else if(height == null)
		{
			height = (width*srcHeight)/srcWidth;
		}
		this.width = width;
		this.height = height;
	}

	public BitmapPartGraphics setCenter(float x, float y)
	{
		this.centerX = x;
		this.centerY = y;
		return this;
	}
	
	
	public BitmapPartGraphics setCenter(float xAndY)
	{
		return setCenter(xAndY, xAndY);
	}


	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(getTint());
		ITexture texture = TextureStore.getTexture(textureName);
		texture.setWrap(xWrap, yWrap);
		texture.draw(spriteBatch, -getCenterX(), -getCenterY(), getWidth(), getHeight(), getSrcX(), getSrcY(), getSrcWidth(), getSrcHeight(), getFlipX(), getFlipY());
	}
	

	public Color getTint() {
		return tint;
	}
	
	public int getSrcX() {
		return srcX;
	}
	
	public int getSrcY() {
		return srcY;
	}
	
	public int getSrcWidth() {
		return srcWidth;
	}
	
	public int getSrcHeight() {
		return srcHeight;
	}

	public float getCenterX() {
		return centerX;
	}
	
	public float getCenterY() {
		return centerY;
	}
	
	public boolean getFlipY() {
		return flipY;
	}

	public boolean getFlipX() {
		return flipX;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public BitmapPartGraphics setFlipX(boolean flipX) {
		this.flipX = flipX;
		return this;
	}
	
	public BitmapPartGraphics setFlipY(boolean flipY) {
		this.flipY = flipY;
		return this;
	}

	public BitmapPartGraphics repeatX() {
		this.xWrap = TextureWrap.Repeat;
		return this;
	}
	
	public BitmapPartGraphics repeatY() {
		this.yWrap = TextureWrap.Repeat;
		return this;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
//		Texture texture = TextureStore.getTexture(textureName);
//		
//		double x = p.getX();
//		double y = p.getY();
//		if(x >= 0 && y >= 0 && x < texture.getWidth() && y < texture.getHeight())
//		{
//			TextureData text = texture.getTextureData();
//			if(!text.isPrepared())
//			{
//				text.prepare();
//			}
//			Pixmap pixmap = text.consumePixmap();
//			try
//			{
//				int colorInt = pixmap.getPixel((int) x, texture.getHeight()-(int) y);
//				return new Color(colorInt).a > 0.0f;
//			}
//			finally
//			{
//				if(text.disposePixmap())
//				{
//					pixmap.dispose();
//				}
//			}
//		}
//		else
//		{
//			return false;
//		}
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
}
