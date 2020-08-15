package com.github.systeminvecklare.badger.impl.gdx.font;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.impl.gdx.FontStore;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;

public class FlashyFont implements IFlashyFont {
	private String fontName;
	private int size;
	
	public static float getOversample()
	{
		return Gdx.graphics.getHeight()/SceneManager.get().getHeight();
	}
	
	public FlashyFont(String fontName, int size) {
		this.fontName = fontName;
		this.size = size;
	}
	
	@Override
	public float getWidth(String text)
	{
		return new GlyphLayout(getFont(), text).width;
	}
	
	@Override
	public float getHeight(String text)
	{
		return new GlyphLayout(getFont(), text).height;
	}
	
	@Override
	public void preloadFont()
	{
		getFont();
	}

	private BitmapFont getFont() {
		return FontStore.getFont(fontName, size);
	}
	

	@Override
	public void draw(IDrawCycle drawCycle, String text, int x, int y, Color color) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		float scale = 1f/FlashyFont.getOversample();
		drawCycle.getTransform().multiplyScale(scale, scale);
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		BitmapFont font = FontStore.getFont(fontName, (int) (size*FlashyFont.getOversample()));
		font.setColor(color);
		font.draw(spriteBatch, text,  x, y);
	}

}
