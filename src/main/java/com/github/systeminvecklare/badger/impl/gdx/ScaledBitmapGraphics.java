package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;

public class ScaledBitmapGraphics implements IMovieClipLayer {
	private String textureName;
	private Color tint;
	private int srcWidth;
	private int srcHeight;
	private float centerX = 0f;
	private float centerY = 0f;
	private float width;
	private float height;
	
	public ScaledBitmapGraphics(String textureName, Color tint) {
		this(textureName, tint, null, null);
	}
	
	public ScaledBitmapGraphics(String textureName, Color tint, Float side)
	{
		this(textureName, tint, side, side);
	}
	
	public ScaledBitmapGraphics(String textureName, Color tint, Float width, Float height) {
		this.textureName = textureName;
		this.tint = tint;
		Texture texture = TextureStore.getTexture(textureName);
		this.srcWidth = texture.getWidth();
		this.srcHeight = texture.getHeight();
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

	public ScaledBitmapGraphics setCenter(float x, float y)
	{
		this.centerX = x;
		this.centerY = y;
		return this;
	}
	
	public ScaledBitmapGraphics setCenter(float xAndY)
	{
		return setCenter(xAndY, xAndY);
	}


	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(tint);
		spriteBatch.draw(TextureStore.getTexture(textureName), -centerX, -centerY, width, height);
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
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
