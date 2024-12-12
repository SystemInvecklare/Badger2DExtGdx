package com.github.systeminvecklare.badger.impl.gdx.shader;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.util.GeometryUtil;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;

public class GdxShaderGraphics implements IMovieClipLayer {
	private static final Texture EMPTY_TEXTURE = new Texture(1, 1, Format.RGBA8888);
	private final IShader shader;
	private final float width;
	private final float height;
	private final IShaderConfigurator shaderConfigurator;
	private boolean hittable = false;

	public GdxShaderGraphics(String fragmentShader, float width, float height, IShaderConfigurator shaderConfigurator) {
		this.shader = new GdxShader(fragmentShader, shaderConfigurator) {
			@Override
			public IShaderConfigurator getShaderConfigurator() {
				return GdxShaderGraphics.this.getShaderConfigurator();
			}
		};
		this.width = width;
		this.height = height;
		this.shaderConfigurator = shaderConfigurator;
	}
	
	public GdxShaderGraphics makeHittable() {
		this.hittable = true;
		return this;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		drawCycle.setShader(shader);
		gdxDrawCycle.updateSpriteBatchTransform();
		gdxDrawCycle.getSpriteBatch().draw(getTexture(), 0, 0, getWidth(), getHeight());
		drawCycle.setShader(null);
	}
	
	public Texture getTexture() {
		return EMPTY_TEXTURE;
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable) {
			return GeometryUtil.isInRectangle(p.getX(), p.getY(), 0, 0, getWidth(), getHeight());
		}
		return false;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public IShaderConfigurator getShaderConfigurator() {
		return shaderConfigurator;
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
	}
}
