package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.IKeyPressListener;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayer;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.IMovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.graphics.components.scene.Scene;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.standard.input.keyboard.IKeyPressEvent;
import com.github.systeminvecklare.badger.core.util.DragBehavior;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.AtlasStore;
import com.github.systeminvecklare.badger.impl.gdx.store.atlas.IAtlasBuilder;

public class AtlasDebugScene extends Scene {
	private final Texture texture = new Texture(1, 1, Format.RGB888);
	
	public AtlasDebugScene(IAtlasBuilder atlasBuilder) {
		ILayer layer = addLayer("test", new ScaledLayer());
		IMovieClip clip = new MovieClip();
		clip.addBehavior(new DragBehavior());
		final AtlasGraphics atlasGraphics = new AtlasGraphics(atlasBuilder);
		clip.addGraphics(atlasGraphics);
		layer.addMovieClip(clip);
		
		addKeyPressListener(new IKeyPressListener() {
			@Override
			public void onKeyPress(IKeyPressEvent event) {
				if(event.getKeyCode() == Input.Keys.PLUS) {
					atlasGraphics.overflowDepth++;
				} else if(event.getKeyCode() == Input.Keys.MINUS) {
					atlasGraphics.overflowDepth = Math.max(0, atlasGraphics.overflowDepth - 1);
				}
			}
		});
	}
	
	@Override
	public void draw(IDrawCycle drawCycle) {
		if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			GdxDrawCycle gdxDrawCycle = ((GdxDrawCycle) drawCycle);
			gdxDrawCycle.updateSpriteBatchTransform();
			gdxDrawCycle.getSpriteBatch().draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
		super.draw(drawCycle);
	}
	
	private class AtlasGraphics implements IMovieClipLayer {
		private final IAtlasBuilder atlasBuilder;
		private int overflowDepth = 0;
		
		public AtlasGraphics(IAtlasBuilder atlasBuilder) {
			this.atlasBuilder = atlasBuilder;
		}

		@Override
		public void draw(IDrawCycle drawCycle) {
			SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
			((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
			spriteBatch.setColor(Color.WHITE);
			Texture texture = AtlasStore.getAtlasDebugTextureIfExists(atlasBuilder, overflowDepth);
			System.out.println(overflowDepth);
			if(texture != null) {
				spriteBatch.draw(texture, 0, SceneManager.get().getHeight() - texture.getHeight(), texture.getWidth(), texture.getHeight());
			} else {
				spriteBatch.draw(AtlasDebugScene.this.texture, 0, 0, SceneManager.get().getWidth(), SceneManager.get().getHeight());
			}
		}

		@Override
		public boolean hitTest(IReadablePosition p) {
			return true;
		}

		@Override
		public void init() {
		}

		@Override
		public void dispose() {
		}
	}
}
