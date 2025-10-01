package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.Vector;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.core.util.GeometryUtil;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class BeamGraphics implements IMovieClipLayer {
	private final IReadablePosition start;
	private final IReadablePosition end;
	private final String texture;
	private final Color color;
	private boolean hittable = false;
	
	public BeamGraphics(IReadablePosition start, IReadablePosition end, String texture, Color color) {
		this.start = start;
		this.end = end;
		this.texture = texture;
		this.color = color;
	}
	
	public BeamGraphics(IReadablePosition start, IReadablePosition end, String texture) {
		this(start, end, texture, Color.WHITE);
	}

	public Color getColor() {
		return color;
	}
	
	public IReadablePosition getStart(EasyPooler ep) {
		return start;
	}
	
	public IReadablePosition getEnd(EasyPooler ep) {
		return end;
	}
	
	public float getOffset() {
		return 0f;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public BeamGraphics makeHittable() {
		hittable = true;
		return this;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		spriteBatch.setColor(getColor());
		ITexture texture = TextureStore.getTexture(getTexture());
		texture.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
		
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			IReadablePosition start = getStart(ep);
			IReadablePosition end = getEnd(ep);
			
			gdxDrawCycle.getTransform().mult(ep.obtain(ITransform.class).setToIdentity().setRotation(start.vectorTo(end, ep.obtain(Vector.class)).getRotationTheta()).setPosition(start));
			gdxDrawCycle.updateSpriteBatchTransform();
			
			float width = Math.round(start.distance(end));
			float height = texture.getHeight();
			float offset = getOffset();
			texture.draw(spriteBatch, 0, -height/2, width, height, -offset, height/texture.getHeight(), -offset + width/texture.getWidth(), 0);
		} finally {
			ep.freeAllAndSelf();
		}
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable) {
			EasyPooler ep = EasyPooler.obtainFresh();
			try {
				IReadablePosition start = getStart(ep);
				IReadablePosition end = getEnd(ep);
				ITexture texture = TextureStore.getTexture(getTexture());
				return GeometryUtil.isInBeam(p.getX(), p.getY(), start, end, texture.getHeight(), ep);
			} finally {
				ep.freeAllAndSelf();
			}
		} else {
			return false;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
	}
}
