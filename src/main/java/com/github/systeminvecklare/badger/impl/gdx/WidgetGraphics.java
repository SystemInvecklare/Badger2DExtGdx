package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.widget.AbstractWidget;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class WidgetGraphics extends AbstractWidget implements IMovieClipLayer {
	private int x;
	private int y;
	private String textureName;
	private Color tint;
	
	public WidgetGraphics(String textureName) {
		this(textureName, Color.WHITE);
	}
	
	public WidgetGraphics(String textureName, Color tint) {
		this.textureName = textureName;
		this.tint = tint;
	}
	
	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(getTint());
		ITexture texture = TextureStore.getTexture(textureName);
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		
		float theWidth = getWidth();
		float theHeight = getHeight();
		texture.draw(spriteBatch, x, y, theWidth, theHeight);
	}

	protected Color getTint() {
		return tint;
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

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return TextureStore.getTexture(textureName).getWidth();
	}

	@Override
	public int getHeight() {
		return TextureStore.getTexture(textureName).getHeight();
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void addToPosition(int dx, int dy) {
		this.x += dx;
		this.y += dy;
	}
}