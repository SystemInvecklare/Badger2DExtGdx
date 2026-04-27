package com.github.systeminvecklare.badger.impl.gdx.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ScreenUtils;
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
			if(itemName instanceof FileLoadedAtlas) {
				return TextureAtlas.fromFile((FileLoadedAtlas) itemName);
			} else {
				return new TextureAtlas(itemName);
			}
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
	
	private static class FileLoadedAtlas implements IAtlasBuilder {
		public final FileHandle texturePath;
		public final TextureFilter minFilter;
		public final TextureFilter magFilter;
		public final Map<String, PackedTextureTemplate> regions = new LinkedHashMap<String, PackedTextureTemplate>();
		public TextureAtlas overflowAtlas;

		public FileLoadedAtlas(FileHandle texturePath, TextureFilter minFilter, TextureFilter magFilter) {
			this.texturePath = texturePath;
			this.minFilter = minFilter;
			this.magFilter = magFilter;
		}

		@Override
		public void build(IAtlasConstruction construction) {
		}

		@Override
		public int getAltasWidth() {
			Texture texture = new Texture(texturePath);
			int result = texture.getWidth();
			texture.dispose();
			return result;
		}

		@Override
		public int getAltasHeight() {
			Texture texture = new Texture(texturePath);
			int result = texture.getHeight();
			texture.dispose();
			return result;
		}

		@Override
		public TextureFilter getMinFilter() {
			return minFilter;
		}

		@Override
		public TextureFilter getMagFilter() {
			return magFilter;
		}

		@Override
		public boolean contains(String texture) {
			return regions.containsKey(texture);
		}
	}
	
	public static void saveAtlasToFile(FileHandle path, String atlasName, IAtlasBuilder atlasBuilder) throws IOException {
		atlasStore.getItem(atlasBuilder).save(path, atlasName);
	}
	
	public static IAtlasBuilder loadAtlasFromFile(FileHandle path, String atlasName) throws IOException {
		FileHandle file = path.child(atlasName+"_0.json");
		return loadAtlasFromFile(path, file);
	}
	
	private static FileLoadedAtlas loadAtlasFromFile(FileHandle path, FileHandle file) throws IOException {
		JsonValue jsonReader = new JsonReader().parse(file);
		
		FileLoadedAtlas overflowAtlas = null;
		String overflowAtlasLink = jsonReader.getString("overflow_atlas", null);
		if(overflowAtlasLink != null) {
			overflowAtlas = loadAtlasFromFile(path, path.child(overflowAtlasLink));
		}
		
		FileHandle imageTexturePath = path.child(jsonReader.getString("image"));
		if(!imageTexturePath.exists()) {
			throw new IOException(file.toString()+" referenced "+imageTexturePath.toString()+" as \"image\", but it does not exist!");
		}
		FileLoadedAtlas fileLoadedAtlas = new FileLoadedAtlas(imageTexturePath, loadFilter(jsonReader, "min_filter"), loadFilter(jsonReader, "mag_filter"));
		JsonValue regions = jsonReader.get("regions");
		if(regions != null) {
			JsonValue entry = regions.child;
			while(entry != null) {
				String name = entry.name;
				fileLoadedAtlas.regions.put(name, PackedTexture.loadFromJson(entry));
				entry = entry.next;
			}
		}
		if(overflowAtlas != null) {
			fileLoadedAtlas.overflowAtlas = TextureAtlas.fromFile(overflowAtlas);
		}
		return fileLoadedAtlas;
	}
	
	private static void saveFilter(JsonWriter writer, String name, TextureFilter textureFilter) throws IOException {
		writer.set(name, textureFilter.ordinal());
	}
	
	private static TextureFilter loadFilter(JsonValue reader, String name) {
		return TextureFilter.values()[reader.getInt(name)];
	}
	
	private static class TextureAtlas implements ITextureAtlas {
		private final Texture texture;
		private final Map<String, PackedTexture> regions = new HashMap<String, PackedTexture>();
		private TextureAtlas overflowAtlas = null;
		
		public TextureAtlas(FileLoadedAtlas atlas) {
			this.texture = new Texture(atlas.texturePath);
			this.texture.setWrap(DEFAULT_WRAP, DEFAULT_WRAP);
			this.texture.setFilter(atlas.minFilter, atlas.magFilter);
			for(Entry<String, PackedTextureTemplate> entry : atlas.regions.entrySet()) {
				regions.put(entry.getKey(), entry.getValue().create(this.texture));
			}
			this.overflowAtlas = atlas.overflowAtlas;
		}

		public static TextureAtlas fromFile(FileLoadedAtlas atlas) {
			return new TextureAtlas(atlas);
		}

		public TextureAtlas(IAtlasBuilder builder) {
			if(builder instanceof FileLoadedAtlas) {
				throw new IllegalArgumentException("Incorrect TextureAtlas constructor used");
			}
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

		public void save(FileHandle path, String atlasName) throws IOException {
			saveOverflowChain(path, atlasName, 0);
		}
		
		private String saveOverflowChain(FileHandle path, String atlasName, int index) throws IOException {
			String imageName = atlasName+"_"+index+".png";
			String jsonName = atlasName+"_"+index+".json";
			String overflowAtlasNameOrNull = null;
			if(overflowAtlas != null) {
				overflowAtlasNameOrNull = overflowAtlas.saveOverflowChain(path, atlasName, index + 1);
			}
			FileHandle parentDir = path.child(atlasName).parent();
			if(!parentDir.exists()) {
				parentDir.mkdirs();
			}
			save(path, imageName, jsonName, overflowAtlasNameOrNull);
			return jsonName;
		}
		
		private void save(FileHandle path, String imageName, String jsonName, String overflowAtlasJsonNameOrNull) throws IOException {
			saveTexture(path.child(imageName));
			
			FileHandle jsonFilePath = path.child(jsonName);
			JsonWriter writer = new JsonWriter(jsonFilePath.writer(false, "utf-8"));
			writer.setOutputType(JsonWriter.OutputType.json);
			try {
				writer.object();
				writer.set("image", imageName);
				saveFilter(writer, "min_filter", texture.getMinFilter());
				saveFilter(writer, "mag_filter", texture.getMagFilter());
				
				writer.object("regions");
				for(Entry<String, PackedTexture> entry : regions.entrySet()) {
					writer.name(entry.getKey());
					PackedTexture packedTexture = entry.getValue();
					writer.object();
					packedTexture.writeToJson(writer);
					writer.pop();
				}
				writer.pop(); //pop regions object
				if(overflowAtlasJsonNameOrNull != null) {
					writer.set("overflow_atlas", overflowAtlasJsonNameOrNull);
				}
				writer.pop();
				writer.flush();
			} finally {
				writer.close();
			}
		}
		
		private void saveTexture(FileHandle imageFile) {
		    int w = texture.getWidth();
		    int h = texture.getHeight();

		    FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
		    SpriteBatch batch = new SpriteBatch();
		    
		    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		    
		    fbo.begin();

		    Gdx.gl.glViewport(0, 0, w, h);

		    Gdx.gl.glClearColor(0, 0, 0, 0);
		    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		    batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, w, h));

		    batch.begin();

		    batch.draw(texture, 0, 0, w, h);

		    batch.end();

		    Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, w, h);

		    fbo.end();

		    Pixmap flipped = new Pixmap(w, h, pixmap.getFormat());

		    for (int y = 0; y < h; y++) {
		        for (int x = 0; x < w; x++) {
		            flipped.drawPixel(x, h - 1 - y, pixmap.getPixel(x, y));
		        }
		    }

		    pixmap.dispose();

		    PixmapIO.writePNG(imageFile, flipped);
		    flipped.dispose();

		    batch.dispose();
		    fbo.dispose();
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
	
	private static class PackedTextureTemplate {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final boolean flipX;
		private final boolean flipY;
		private final TextureWrap xWrap;
		private final TextureWrap yWrap;

		public PackedTextureTemplate(int x, int y, int width, int height, boolean flipX, boolean flipY,
				TextureWrap xWrap, TextureWrap yWrap) {
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;
				this.flipX = flipX;
				this.flipY = flipY;
				this.xWrap = xWrap;
				this.yWrap = yWrap;
		}

		public PackedTexture create(Texture texture) {
			TextureRegion region = new TextureRegion(texture, x, y, width, height);
			region.flip(flipX, flipY);
			PackedTexture packedTexture = new PackedTexture(region);
			packedTexture.xWrap = xWrap;
			packedTexture.yWrap = yWrap;
			return packedTexture;
		}
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
		
		private void writeToJson(JsonWriter writer) throws IOException {
			writer.set("x", region.getRegionX());
			writer.set("y", region.getRegionY());
			writer.set("width", region.getRegionWidth());
			writer.set("height", region.getRegionHeight());
			if(region.isFlipX()) {
				writer.set("flip_x", true);
			}
			if(region.isFlipY()) {
				writer.set("flip_y", true);
			}
			if(xWrap != TextureWrap.ClampToEdge) {
				writer.set("x_wrap", xWrap.name());
			}
			if(yWrap != TextureWrap.ClampToEdge) {
				writer.set("y_wrap", yWrap.name());
			}
		}
		
		private static PackedTextureTemplate loadFromJson(JsonValue data) {
			int x = data.getInt("x");
			int y = data.getInt("y");
			int width = data.getInt("width");
			int height = data.getInt("height");
			
			boolean flipX = data.getBoolean("flip_x", false);
			boolean flipY = data.getBoolean("flip_y", false);
			
			TextureWrap xWrap = TextureWrap.ClampToEdge;
			TextureWrap yWrap = TextureWrap.ClampToEdge;
			if(data.has("x_wrap")) {
				xWrap = TextureWrap.valueOf(data.getString("x_wrap"));
			}
			if(data.has("y_wrap")) {
				yWrap = TextureWrap.valueOf(data.getString("y_wrap"));
			}
			
			return new PackedTextureTemplate(x, y, width, height, flipX, flipY, xWrap, yWrap);
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
