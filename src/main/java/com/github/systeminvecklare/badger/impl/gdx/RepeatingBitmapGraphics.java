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
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class RepeatingBitmapGraphics implements IMovieClipLayer {
	private String textureName;
	private Color tint;
	private final IRectangle targetRectangle;
	private boolean hittable = false;

	public RepeatingBitmapGraphics(String textureName, Color tint, int targetWidth, int targetHeight) {
		this(textureName, tint, new Rectangle(0, 0, targetWidth, targetHeight));
	}
	
	public RepeatingBitmapGraphics(String textureName, Color tint, IRectangle targetRectangle) {
		this.textureName = textureName;
		this.tint = tint;
		this.targetRectangle = targetRectangle;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		spriteBatch.setColor(tint);
		ITexture texture = TextureStore.getTexture(textureName);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		IRectangle targetRectangle = getTargetRectangle();
		float width = targetRectangle.getWidth();
		float height = targetRectangle.getHeight();
		texture.draw(spriteBatch, targetRectangle.getX(), targetRectangle.getY(), width, height, 0, height/texture.getHeight(), width/texture.getWidth(), 0);
	}
	
	public IRectangle getTargetRectangle() {
		return targetRectangle;
	}
	
	public RepeatingBitmapGraphics makeHittable() {
		this.hittable = true;
		return this;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable) {
			return GeometryUtil.isInRectangle(p.getX(), p.getY(), getTargetRectangle());
		}
		return false;
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
		textureName = null;
		tint = null;
	}
}
