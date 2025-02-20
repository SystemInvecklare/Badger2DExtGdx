package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class NinePatchRepeatingBitmapGraphics implements IMovieClipLayer {
	private final String textureName;
	private final int inset;
	private final IRectangle rectangle;
	private final Color color;
	private boolean drawMiddle = true;
	
	public NinePatchRepeatingBitmapGraphics(String texture, int inset, IRectangle rectangle) {
		this(texture, inset, rectangle, Color.WHITE);
	}
	
	public NinePatchRepeatingBitmapGraphics(String texture, int inset, IRectangle rectangle, Color color) {
		this.textureName = texture;
		this.inset = inset;
		this.rectangle = rectangle;
		this.color = color;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		gdxDrawCycle.updateSpriteBatchTransform();
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		spriteBatch.setColor(color);
		ITexture texture = TextureStore.getTexture(getTextureName());
		int inset = getInset();
		
		texture.draw(spriteBatch, rectangle.getX(), rectangle.getY(), inset, inset, 0, texture.getHeight()-inset, inset, inset, false, false);
		texture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth()-inset, rectangle.getY(), inset, inset, texture.getWidth() - inset, texture.getHeight()-inset, inset, inset, false, false);
		texture.draw(spriteBatch, rectangle.getX(), rectangle.getY()+rectangle.getHeight() - inset, inset, inset, 0, 0, inset, inset, false, false);
		texture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth()-inset, rectangle.getY()+rectangle.getHeight() - inset, inset, inset, texture.getWidth() - inset, 0, inset, inset, false, false);
		
		{
			ITexture subTexture = texture.createSubTexture(inset, 0, texture.getWidth() - 2*inset, inset);
			subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			float drawWidth = rectangle.getWidth() - 2*inset;
			subTexture.draw(spriteBatch, rectangle.getX()+inset, rectangle.getY(), drawWidth, inset, 0, 0, drawWidth/subTexture.getWidth(), 1);
		}
		{
			ITexture subTexture = texture.createSubTexture(inset, texture.getHeight() - inset, texture.getWidth() - 2*inset, inset);
			subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			float drawWidth = rectangle.getWidth() - 2*inset;
			subTexture.draw(spriteBatch, rectangle.getX()+inset, rectangle.getY()+rectangle.getHeight() - inset, drawWidth, inset, 0, 0, drawWidth/subTexture.getWidth(), 1);
		}
		{
			ITexture subTexture = texture.createSubTexture(0, inset, inset, texture.getHeight() - 2*inset);
			subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			float drawHeight = rectangle.getHeight() - 2*inset;
			subTexture.draw(spriteBatch, rectangle.getX(), rectangle.getY()+inset, inset, drawHeight, 0, 0, 1, drawHeight/subTexture.getHeight());
		}
		{
			ITexture subTexture = texture.createSubTexture(texture.getWidth() - inset, inset, inset, texture.getHeight() - 2*inset);
			subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			float drawHeight = rectangle.getHeight() - 2*inset;
			subTexture.draw(spriteBatch, rectangle.getX()+rectangle.getWidth() - inset, rectangle.getY()+inset, inset, drawHeight, 0, 0, 1, drawHeight/subTexture.getHeight());
		}
		if(drawMiddle()) {
			ITexture subTexture = texture.createSubTexture(inset, inset, texture.getWidth() - 2*inset, texture.getHeight() - 2*inset);
			subTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			float drawWidth = rectangle.getWidth() - 2*inset;
			float drawHeight = rectangle.getHeight() - 2*inset;
			subTexture.draw(spriteBatch, rectangle.getX()+inset, rectangle.getY()+inset, drawWidth, drawHeight, 0, 0, drawWidth/subTexture.getWidth(), drawHeight/subTexture.getHeight());
		}
	}

	protected int getInset() {
		return inset;
	}

	protected String getTextureName() {
		return textureName;
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
}
