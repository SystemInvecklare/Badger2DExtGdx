package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.systeminvecklare.badger.core.math.Mathf;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.store.atlas.IAtlasBuilder;
import com.github.systeminvecklare.badger.impl.gdx.store.atlas.IAtlasConstruction;
import com.github.systeminvecklare.badger.impl.gdx.store.atlas.ITextureAtlas;

public class AtlasStore {
	private static final TextureWrap DEFAULT_WRAP = TextureWrap.ClampToEdge;
	
	private static final SharedUtils SHARED_UTILS = new SharedUtils();
	
	/*package-protected*/ static AbstractStore<IAtlasBuilder, TextureAtlas> atlasStore = new AbstractStore<IAtlasBuilder, TextureAtlas>() {
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
	
	private static Texture getOverflowAtlas(TextureAtlas atlas, int overflowDepth) {
		if(atlas == null) {
			return null;
		}
		if(overflowDepth > 0) {
			return getOverflowAtlas(atlas.overflowAtlas, overflowDepth - 1);
		} else {
			return atlas.texture;
		}
	}
	
	public static Texture getAtlasDebugTextureIfExists(IAtlasBuilder atlasBuilder, int overflowDepth) {
		return getOverflowAtlas(atlasStore.getItem(atlasBuilder), overflowDepth);
	}
	
	private static class TextureAtlas implements ITextureAtlas {
		private final Texture texture;
		private final Map<String, PackedTexture> regions = new HashMap<String, PackedTexture>();
		private TextureAtlas overflowAtlas = null;

		public TextureAtlas(IAtlasBuilder builder) {
			this.texture = new Texture(builder.getAltasWidth(), builder.getAltasHeight(), Format.RGBA8888);
			this.texture.setFilter(builder.getMinFilter(), builder.getMagFilter());
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
			texture.dispose();
			if(overflowAtlas != null) {
				overflowAtlas.dispose();
				overflowAtlas = null;
			}
		}

		@Override
		public ITexture getTexture(String texture) {
			if(overflowAtlas != null) {
				ITexture found = overflowAtlas.getTexture(texture);
				if(found != null) {
					return found;
				}
			}
			return regions.get(texture);
		}
	}
	
	private static class TextureRegionKey {
		private float xTex1;
		private float xTex2;
		private float yTex1;
		private float yTex2;
		
		public void set(DimensionRangedDraw xRange, DimensionRangedDraw yRange) {
			this.xTex1 = xRange.tex1;
			this.xTex2 = xRange.tex2;
			this.yTex1 = yRange.tex1;
			this.yTex2 = yRange.tex2;
		}
		
		public TextureRegionKey copy() {
			TextureRegionKey copied = new TextureRegionKey();
			copied.xTex1 = this.xTex1;
			copied.xTex2 = this.xTex2;
			copied.yTex1 = this.yTex1;
			copied.yTex2 = this.yTex2;
			return copied;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(xTex1);
			result = prime * result + Float.floatToIntBits(xTex2);
			result = prime * result + Float.floatToIntBits(yTex1);
			result = prime * result + Float.floatToIntBits(yTex2);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TextureRegionKey other = (TextureRegionKey) obj;
			if (Float.floatToIntBits(xTex1) != Float.floatToIntBits(other.xTex1))
				return false;
			if (Float.floatToIntBits(xTex2) != Float.floatToIntBits(other.xTex2))
				return false;
			if (Float.floatToIntBits(yTex1) != Float.floatToIntBits(other.yTex1))
				return false;
			if (Float.floatToIntBits(yTex2) != Float.floatToIntBits(other.yTex2))
				return false;
			return true;
		}
	}
	
	private static abstract class Cache<K, V> {
		private static final int MAX_SIZE = 10;
		
		private Map<K, V> cached = new HashMap<K, V>();
		
		public V get(K key) {
			V value = cached.get(key);
			if(value == null) {
				value = calculate(key);
				if(cached.size() < MAX_SIZE) {
					cached.put(snapshot(key), value);
				}
			}
			return value;
		}
		protected K snapshot(K key) {
			return key;
		}
		
		protected abstract V calculate(K key);
	}
	
	/*package-protected*/ static class PackedTexture implements ITexture {
		private final TextureRegion region;
		private TextureWrap xWrap = DEFAULT_WRAP;
		private TextureWrap yWrap = DEFAULT_WRAP;
		
		private final Cache<TextureRegionKey, TextureRegion> cache = new Cache<AtlasStore.TextureRegionKey, TextureRegion>() {
			@Override
			protected TextureRegion calculate(TextureRegionKey key) {
				float subU = Mathf.lerp(key.xTex1, region.getU(), region.getU2());
				float subU2 = Mathf.lerp(key.xTex2, region.getU(), region.getU2());
				float subV = Mathf.lerp(key.yTex1, region.getV(), region.getV2());
				float subV2 = Mathf.lerp(key.yTex2, region.getV(), region.getV2());
				return new TextureRegion(region.getTexture(), subU, subV2, subU2, subV);
			}
			
			protected TextureRegionKey snapshot(TextureRegionKey key) {
				return key.copy();
			}
		};

		public PackedTexture(Texture texture, int x, int y, int width, int height) {
			this(new TextureRegion(texture, x, y, width, height));
		}
		
		private PackedTexture(TextureRegion region) {
			this.region = region;
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
				
				for(int i = 0; i < xRanger.rangerLength; ++i) {
					for(int j = 0; j < yRanger.rangerLength; ++j) {
						DimensionRangedDraw xRange = xRanger.ranges[i];
						DimensionRangedDraw yRange = yRanger.ranges[j];
						TextureRegionKey key = SHARED_UTILS.utilKey;
						key.set(xRange, yRange);
						spriteBatch.draw(cache.get(key), xRange.start, yRange.start, xRange.length, yRange.length);
					}
				}
			} finally {
				SHARED_UTILS.free();
			}
		}

		@Override
		public TextureRegion asTextureRegion() {
			return region;
		}
		
		@Override
		public ITexture createSubTexture(int x, int y, int width, int height) {
			if(x < 0 || y < 0 || x + width > region.getRegionWidth() || y + height > region.getRegionHeight()) {
				throw new IllegalArgumentException("Invalid subtexture region (x,y,width,height) == ("+x+","+y+","+width+","+height+") for region of size "+region.getRegionWidth()+" x "+region.getRegionHeight()+".");
			}
			TextureRegion subRegion = new TextureRegion(region);
			subRegion.setRegion(region.getRegionX()+x, region.getRegionY()+y, width, height);
			return new PackedTexture(subRegion);
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
		
		public DimensionRangedDraw copy() {
			DimensionRangedDraw copy = new DimensionRangedDraw();
			copy.set(start, length, tex1, tex2);
			return copy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(length);
			result = prime * result + Float.floatToIntBits(start);
			result = prime * result + Float.floatToIntBits(tex1);
			result = prime * result + Float.floatToIntBits(tex2);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DimensionRangedDraw other = (DimensionRangedDraw) obj;
			if (Float.floatToIntBits(length) != Float.floatToIntBits(other.length))
				return false;
			if (Float.floatToIntBits(start) != Float.floatToIntBits(other.start))
				return false;
			if (Float.floatToIntBits(tex1) != Float.floatToIntBits(other.tex1))
				return false;
			if (Float.floatToIntBits(tex2) != Float.floatToIntBits(other.tex2))
				return false;
			return true;
		}
	}
	
	private static class Ranger implements WrapHelper.IDrawSink {
		private static final int MAX_POOL = 200;
		
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
		private final TextureRegionKey utilKey = new TextureRegionKey();
		
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
