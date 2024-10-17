package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayer;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.IMovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.graphics.components.scene.Scene;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.util.DragBehavior;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.AtlasStore;
import com.github.systeminvecklare.badger.impl.gdx.store.IAtlasBuilder;

public class AtlasDebugScene extends Scene {
	public AtlasDebugScene(IAtlasBuilder atlasBuilder) {
		ILayer layer = addLayer("test", new ScaledLayer());
		IMovieClip clip = new MovieClip();
		clip.addBehavior(new DragBehavior());
		clip.addGraphics(new AtlasGraphics(atlasBuilder));
		layer.addMovieClip(clip);
	}
	
	private static class AtlasGraphics implements IMovieClipLayer {
		private final IAtlasBuilder atlasBuilder;
		
		public AtlasGraphics(IAtlasBuilder atlasBuilder) {
			this.atlasBuilder = atlasBuilder;
		}

		@Override
		public void draw(IDrawCycle drawCycle) {
			SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
			((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
			spriteBatch.setColor(Color.WHITE);
			Texture texture = AtlasStore.getAtlasDebugTexture(atlasBuilder);

			spriteBatch.draw(texture, 0, SceneManager.get().getHeight() - texture.getHeight(), texture.getWidth(), texture.getHeight());
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
