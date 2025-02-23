package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.NinePatchDefinition;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class NinePatchBitmapGraphics implements IMovieClipLayer {
	private final NinePatchDefinition ninePatchDefinition;
	private final IRectangle rectangle;
	private final Color color;
	
	public NinePatchBitmapGraphics(String texture, int inset, IRectangle rectangle) {
		this(texture, inset, rectangle, Color.WHITE);
	}
	
	public NinePatchBitmapGraphics(String texture, int inset, IRectangle rectangle, Color color) {
		this(new NinePatchDefinition(texture, inset, inset, inset, inset), rectangle, color);
	}
	
	public NinePatchBitmapGraphics(NinePatchDefinition ninePatchDefinition, IRectangle rectangle) {
		this(ninePatchDefinition, rectangle, Color.WHITE);
	}

	public NinePatchBitmapGraphics(NinePatchDefinition ninePatchDefinition, IRectangle rectangle, Color color) {
		this.ninePatchDefinition = ninePatchDefinition;
		this.rectangle = rectangle;
		this.color = color;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		gdxDrawCycle.updateSpriteBatchTransform();
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		NinePatch ninePatch = TextureStore.getNinePatch(ninePatchDefinition);
		ninePatch.setColor(Color.WHITE);
		spriteBatch.setColor(color);
		
		ninePatch.draw(spriteBatch, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
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
