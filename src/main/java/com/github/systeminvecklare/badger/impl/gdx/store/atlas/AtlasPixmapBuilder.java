package com.github.systeminvecklare.badger.impl.gdx.store.atlas;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class AtlasPixmapBuilder {
	public static Pixmap getAtlasPixmap(IAtlasBuilder atlasBuilder) {
		return new TextureAtlas(atlasBuilder).pixmap;
	}
	
	private static Pixmap getOverflowAtlas(TextureAtlas atlas, int overflowDepth) {
		if(atlas == null) {
			return null;
		}
		if(overflowDepth > 0) {
			return getOverflowAtlas(atlas.overflowAtlas, overflowDepth - 1);
		} else {
			return atlas.pixmap;
		}
	}
	
	public static Pixmap getAtlasPixmapIfExists(IAtlasBuilder atlasBuilder, int overflowDepth) {
		return getOverflowAtlas(new TextureAtlas(atlasBuilder), overflowDepth);
	}
	
	private static class TextureAtlas {
		private final Pixmap pixmap;
		private TextureAtlas overflowAtlas = null;

		public TextureAtlas(IAtlasBuilder builder) {
			this.pixmap = new Pixmap(builder.getAltasWidth(), builder.getAltasHeight(), Format.RGBA8888);
			this.pixmap.setFilter(Filter.NearestNeighbour);
			this.pixmap.setBlending(Blending.None);
			builder.build(new IAtlasConstruction() {
				@Override
				public void add(String name, Pixmap drawnPixmap, int x, int y, int padding) {
					if(x + drawnPixmap.getWidth() + 2*padding > pixmap.getWidth() || y + drawnPixmap.getHeight() + 2*padding > pixmap.getHeight()) {
						throw new IllegalArgumentException(name+" is too big to fit in pixmap!");
					}
					// Note: We do this to help with strange bleeding. But we can only do it if we have at least some padding. 
					if(padding > 0) {
						pixmap.drawPixmap(drawnPixmap, x + padding - 1, y + padding);
						pixmap.drawPixmap(drawnPixmap, x + padding + 1, y + padding);
						pixmap.drawPixmap(drawnPixmap, x + padding, y + padding - 1);
						pixmap.drawPixmap(drawnPixmap, x + padding, y + padding + 1);
					}
					drawnPixmap.drawPixmap(drawnPixmap, x + padding, y + padding);
				}

				@Override
				public void setOverflowAtlas(IAtlasBuilder overflowAtlasBuilder) {
					if(overflowAtlas != null) {
						overflowAtlas.dispose();
					}
					overflowAtlas = new TextureAtlas(overflowAtlasBuilder);
				}
			});
		}

		public void dispose() {
			pixmap.dispose();
			if(overflowAtlas != null) {
				overflowAtlas.dispose();
				overflowAtlas = null;
			}
		}
	}
}
