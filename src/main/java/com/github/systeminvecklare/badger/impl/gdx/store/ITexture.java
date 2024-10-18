package com.github.systeminvecklare.badger.impl.gdx.store;

import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ITexture {
	int getWidth();
	int getHeight();
	NinePatch newNinepatch(int left, int right, int top, int bottom);
	TextureRegion asTextureRegion();
	void setWrap(TextureWrap xWrap, TextureWrap yWrap);
	void draw(SpriteBatch spriteBatch, float x, float y);
	void draw(SpriteBatch spriteBatch, float x, float y, float width, float height);
	void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
			int srcHeight, boolean flipX, boolean flipY);
	void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, float u, float v, float u2, float v2);
}
