package com.github.systeminvecklare.badger.impl.gdx.font;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

public interface IFlashyFont {
	public float getWidth(String text);
	public float getHeight(String text);
	public void preloadFont();
	public void draw(IDrawCycle drawCycle, String text, int x, int y, Color color);
}
