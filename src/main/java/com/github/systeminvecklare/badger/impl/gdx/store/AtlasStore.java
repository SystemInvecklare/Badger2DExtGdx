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

public class AtlasStore {
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
			this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			builder.build(new IAtlasConstruction() {
				@Override
				public void add(String name, Pixmap pixmap, int x, int y) {
					if(x + pixmap.getWidth() > texture.getWidth() || y + pixmap.getHeight() > texture.getHeight()) {
						throw new IllegalArgumentException(name+" is too big to fit in pixmap!");
					}
					texture.draw(pixmap, x, y);
					if(regions.containsKey(name)) {
						throw new IllegalArgumentException("Texture "+name+" added twice to the atlas!");
					}
					regions.put(name, new PackedTexture(texture, x, y, pixmap.getWidth(), pixmap.getHeight()));
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
			// Not sure how this will play out...
			Texture texture = region.getTexture();
			if(texture.getUWrap() != xWrap || texture.getVWrap() != yWrap) {
				texture.setWrap(xWrap, yWrap);
			}
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
			//TODO reuse TextureRegion object?
			TextureRegion subRegion = new TextureRegion(region, srcX, srcY, srcWidth, srcHeight);
			subRegion.flip(flipX, flipY);
			spriteBatch.draw(subRegion, x, y, width, height);
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, float u, float v,
				float u2, float v2) {
			//TODO we need to chop up this into multiple draws. if u < 0 and/or u2 > 1. We can use the xWrap and yWrap to do the logic.
			//TODO BUT! In that case, don't apply the wrap to the root-texture as we are doing now... 
			float subU = Mathf.lerp(u, region.getU(), region.getU2());
			float subU2 = Mathf.lerp(u2, region.getU(), region.getU2());
			float subV = Mathf.lerp(v, region.getV(), region.getV2());
			float subV2 = Mathf.lerp(v2, region.getV(), region.getV2());
			//TODO reuse util region and then do "set texture followed by set u v etc..."
			TextureRegion subRegion = new TextureRegion(region.getTexture(), subU, subV, subU2, subV2);
			spriteBatch.draw(subRegion, x, y, width, height);
		}
	}
}
