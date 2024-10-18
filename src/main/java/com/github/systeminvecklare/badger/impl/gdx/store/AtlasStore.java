package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.systeminvecklare.badger.core.math.Mathf;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;

public class AtlasStore {
	private static final TextureWrap DEFAULT_WRAP = TextureWrap.ClampToEdge;
	
	private static final SharedUtils SHARED_UTILS = new SharedUtils();
	
	private static AbstractStore<IAtlasBuilder, TextureAtlas> atlasStore = new AbstractStore<IAtlasBuilder, TextureAtlas>() {
		@Override
		protected TextureAtlas loadItem(IAtlasBuilder itemName) {
			return new TextureAtlas(itemName);
		}

		@Override
		protected void disposeItem(TextureAtlas item) {
			item.dispose();
		}
	};
	static {
		FlashyGdxEngine.get().registerStore(atlasStore);
	}
	
	public static ITextureAtlas getAtlas(IAtlasBuilder atlasBuilder) {
		return atlasStore.getItem(atlasBuilder);
	}
	

	public static Texture getAtlasDebugTexture(IAtlasBuilder atlasBuilder) {
		return atlasStore.getItem(atlasBuilder).texture;
	}
	
	private static class TextureAtlas implements ITextureAtlas {
		private final Texture texture;
		private final Map<String, PackedTexture> regions = new HashMap<String, PackedTexture>();

		public TextureAtlas(IAtlasBuilder builder) {
			this.texture = new Texture(builder.getAltasWidth(), builder.getAltasHeight(), Format.RGBA8888);
			this.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			this.texture.setWrap(DEFAULT_WRAP, DEFAULT_WRAP);
			builder.build(new IAtlasConstruction() {
				@Override
				public void add(String name, Pixmap pixmap, int x, int y, int padding) {
					if(x + pixmap.getWidth() + 2*padding > texture.getWidth() || y + pixmap.getHeight() + 2*padding > texture.getHeight()) {
						throw new IllegalArgumentException(name+" is too big to fit in pixmap!");
					}
					// Note: We do this to help with strange bleeding. But we can ony do it if we have at least some padding. 
					if(padding > 0) {
						texture.draw(pixmap, x + padding - 1, y + padding);
						texture.draw(pixmap, x + padding + 1, y + padding);
						texture.draw(pixmap, x + padding, y + padding - 1);
						texture.draw(pixmap, x + padding, y + padding + 1);
					}
					texture.draw(pixmap, x + padding, y + padding);
					if(regions.containsKey(name)) {
						throw new IllegalArgumentException("Texture "+name+" added twice to the atlas!");
					}
					regions.put(name, new PackedTexture(texture, x + padding, y + padding, pixmap.getWidth(), pixmap.getHeight()));
				}
			});
		}

		public void dispose() {
			texture.dispose();
		}

		@Override
		public ITexture getTexture(String texture) {
			return regions.get(texture);
		}
	}
	
	private static class PackedTexture implements ITexture {
		private final TextureRegion region;
		private TextureWrap xWrap = DEFAULT_WRAP;
		private TextureWrap yWrap = DEFAULT_WRAP;

		public PackedTexture(Texture texture, int x, int y, int width, int height) {
			this.region = new TextureRegion(texture, x, y, width, height);
		}

		@Override
		public int getWidth() {
			return region.getRegionWidth();
		}

		@Override
		public int getHeight() {
			return region.getRegionHeight();
		}

		@Override
		public NinePatch newNinepatch(int left, int right, int top, int bottom) {
			return new NinePatch(region, left, right, top, bottom);
		}

		@Override
		public void setWrap(TextureWrap xWrap, TextureWrap yWrap) {
			this.xWrap = xWrap;
			this.yWrap = yWrap;
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y) {
			spriteBatch.draw(region, x, y);
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height) {
			spriteBatch.draw(region, x, y, width, height);
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, int srcX, int srcY,
				int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
			SHARED_UTILS.claim();
			try {
				TextureRegion subRegion = SHARED_UTILS.utilRegion;
				subRegion.setRegion(region, srcX, srcY, srcWidth, srcHeight);
				subRegion.flip(flipX, flipY);
				spriteBatch.draw(subRegion, x, y, width, height);
			} finally {
				SHARED_UTILS.free();
			}
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, float u, float v,
				float u2, float v2) {
			SHARED_UTILS.claim();
			try {
				Ranger xRanger = SHARED_UTILS.xRanger;
				Ranger yRanger = SHARED_UTILS.yRanger;
				
				WrapHelper.draw(x, width, u, u2, xWrap, region.getRegionWidth(), xRanger);
				WrapHelper.draw(y, height, v, v2, yWrap, region.getRegionHeight(), yRanger);
				
				TextureRegion subRegion = SHARED_UTILS.utilRegion;
				
				for(int i = 0; i < xRanger.rangerLength; ++i) {
					for(int j = 0; j < yRanger.rangerLength; ++j) {
						DimensionRangedDraw xRange = xRanger.ranges[i];
						DimensionRangedDraw yRange = yRanger.ranges[j];
						float subU = Mathf.lerp(xRange.tex1, region.getU(), region.getU2());
						float subU2 = Mathf.lerp(xRange.tex2, region.getU(), region.getU2());
						float subV = Mathf.lerp(yRange.tex1, region.getV(), region.getV2());
						float subV2 = Mathf.lerp(yRange.tex2, region.getV(), region.getV2());
						subRegion.setTexture(region.getTexture());
						subRegion.setRegion(subU, subV2, subU2, subV);
						spriteBatch.draw(subRegion, xRange.start, yRange.start, xRange.length, yRange.length);
					}
				}
			} finally {
				SHARED_UTILS.free();
			}
		}
	}
	
	private static class DimensionRangedDraw {
		private float start;
		private float length;
		private float tex1;
		private float tex2;

		public void set(float start, float length, float tex1, float tex2) {
			this.start = start;
			this.length = length;
			this.tex1 = tex1;
			this.tex2 = tex2;
		}
	}
	
	private static class Ranger implements WrapHelper.IDrawSink {
		private static final int MAX_POOL = 10;
		
		private int rangerLength = 0;
		private DimensionRangedDraw[] ranges = new DimensionRangedDraw[0];

		@Override
		public void draw(float start, float length, float tex1, float tex2) {
			if(ranges.length <= rangerLength) {
				grow(1);
			}
			ranges[rangerLength].set(start, length, tex1, tex2);
			rangerLength++;
		}
		
		private void grow(int grow) {
			DimensionRangedDraw[] oldRanged = ranges;
			ranges = new DimensionRangedDraw[oldRanged.length + grow];
			if(oldRanged.length > 0) {
				System.arraycopy(oldRanged, 0, ranges, 0, oldRanged.length);
			}
			if(grow == 1) {
				ranges[oldRanged.length] = new DimensionRangedDraw();
			} else {
				for(int i = 0; i < grow; ++i) {
					ranges[oldRanged.length + i] = new DimensionRangedDraw();
				}
			}
		}
		
		public void clear() {
			rangerLength = 0;
			if(ranges.length > MAX_POOL) {
				DimensionRangedDraw[] oldRanged = ranges;
				ranges = new DimensionRangedDraw[MAX_POOL];
				System.arraycopy(oldRanged, 0, ranges, 0, ranges.length);
			}
		}
	}
	
	private static class SharedUtils {
		private boolean claimed = false;
		private final Ranger xRanger = new Ranger();
		private final Ranger yRanger = new Ranger();
		private final TextureRegion utilRegion = new TextureRegion();
		
		public void claim() {
			if(claimed) {
				throw new RuntimeException("Utils already shared!");
			}
			claimed = true;
			xRanger.clear();
			yRanger.clear();
			utilRegion.setTexture(null);
		}
		
		public void free() {
			claimed = false;
			xRanger.clear();
			yRanger.clear();
			utilRegion.setTexture(null);
		}
	}
}
