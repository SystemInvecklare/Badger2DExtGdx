package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.util.GeometryUtil;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.core.widget.Rectangle;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class StretchedBitmapGraphics implements IMovieClipLayer, IRectangle {
	private final String textureName;
	private final Color tint;
	private final IRectangle rectangle;
	private boolean hittable = false;
	
	public StretchedBitmapGraphics(String textureName, float x, float y, float width, float height) {
		this(textureName, Color.WHITE, x, y, width, height);
	}
	
	public StretchedBitmapGraphics(String textureName, IRectangle rectangle) {
		this(textureName, Color.WHITE, rectangle);
	}
	
	public StretchedBitmapGraphics(String textureName, Color tint, float x, float y, float width, float height) {
		this(textureName, tint, new Rectangle((int) x, (int) y, (int) width, (int) height));
	}
	
	public StretchedBitmapGraphics(String textureName, Color tint, IRectangle rectangle) {
		this.textureName = textureName;
		this.tint = tint;
		this.rectangle = rectangle;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(getTint());
		ITexture texture = TextureStore.getTexture(textureName);
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.draw(spriteBatch, getX(), getY(), getWidth(), getHeight());
	}
	
	public Color getTint() {
		return tint;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable) {
			return GeometryUtil.isInRectangle(p.getX(), p.getY(), rectangle);
		}
		return false;
	}

	@Override
	public void init() {
	}

	public StretchedBitmapGraphics makeHittable() {
		hittable = true;
		return this;
	}

	@Override
	public void dispose() {
	}

	@Override
	public int getX() {
		return rectangle.getX();
	}

	@Override
	public int getY() {
		return rectangle.getY();
	}

	@Override
	public int getWidth() {
		return rectangle.getWidth();
	}

	@Override
	public int getHeight() {
		return rectangle.getHeight();
	}
}