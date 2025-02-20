package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.store.atlas.IAtlasBuilder;

public class TextureStore {
	static {
		FlashyGdxEngine.get().registerStore(new IStore() {
			@Override
			public void reloadInventory() {
				reloadGraphics();
			}
			
			@Override
			public void disposeInventory() {
				disposeGraphics();
			}
			
			@Override
			public List<IStore> getDependencies(List<IStore> result) {
				result.add(AtlasStore.atlasStore);
				return result;
			}
		});
	}
	private static Map<NinePatchDefinition, NinePatch> ninepatches = new HashMap<NinePatchDefinition, NinePatch>();
	private static Map<String, ITexture> textures = new HashMap<String, ITexture>();
	private static List<Texture> managedTextures = new ArrayList<Texture>();
	private static Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	private static List<IAtlasBuilder> textureAtlases = new ArrayList<IAtlasBuilder>(0);
	
	public static void registerTextureAtlas(IAtlasBuilder atlasBuilder) {
		textureAtlases.add(atlasBuilder);
	}

	public static ITexture getTexture(String textureName) {
		ITexture texture = textures.get(textureName);
		if(texture == null)
		{
			texture = loadTexture(textureName);
		}
		return texture;
	}
	
	public static BitmapFont getBitmapFont(String fontName) {
		BitmapFont font = fonts.get(fontName);
		if(font == null)
		{
			font = loadFont(fontName);
		}
		return font;
	}
	
	public static NinePatch getNinePatch(NinePatchDefinition ninePatchDefinition) {
		NinePatch ninePatch = ninepatches.get(ninePatchDefinition);
		if(ninePatch == null)
		{
			ninePatch = loadNinePatch(ninePatchDefinition);
		}
		return ninePatch;
	}
	
	private static BitmapFont loadFont(String fontName) {
		BitmapFontData bitmapFontData = new BitmapFontData(Gdx.files.internal(fontName+".fnt"), false);
		Array<TextureRegion> pageRegions = new Array<TextureRegion>(bitmapFontData.imagePaths.length);
		for(String imagePath : bitmapFontData.imagePaths) {
			pageRegions.add(getTexture(imagePath).asTextureRegion());
		}
		BitmapFont font = new BitmapFont(bitmapFontData, pageRegions, true);
		fonts.put(fontName, font);
		return font;
	}

	private static ITexture loadTexture(String textureName) {
		ITexture result = null;
		
		for(IAtlasBuilder atlasBuilder : textureAtlases) {
			if(atlasBuilder.contains(textureName)) {
				result = AtlasStore.getAtlas(atlasBuilder).getTexture(textureName);
				if(result != null) {
//					System.out.println("Using "+textureName+" from atlas!");
					break;
				}
			}
		}
		
		if(result == null) {
//			System.out.println(textureName+" RAW");
			Texture texture = new Texture(textureName);
			managedTextures.add(texture);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);//TODO temp. Rather do a callback hook so you can do something to the texture based on name
			result = new SingleTextureWrapper(texture);
		}
		
		textures.put(textureName, result);
		return result;
	}
	
	private static NinePatch loadNinePatch(NinePatchDefinition definition)
	{
		NinePatch ninePatch = getTexture(definition.textureName).newNinepatch(definition.left,definition.right,definition.top,definition.bottom);
		ninepatches.put(definition, ninePatch);
		return ninePatch;
	}
	
	public static void reloadGraphics() {
		for(Texture texture : managedTextures) {
			texture.dispose();
		}
		managedTextures.clear();
		for(String textureName : textures.keySet()) {
			textures.put(textureName, loadTexture(textureName));
		}
		for(String fontName : fonts.keySet()) {
			fonts.put(fontName, loadFont(fontName));
		}
		for(NinePatchDefinition ninepatchDef : ninepatches.keySet()) {
			ninepatches.put(ninepatchDef, loadNinePatch(ninepatchDef));
		}
	}
	
	public static void disposeGraphics()
	{
		for(Texture texture : managedTextures) {
			texture.dispose();
		}
		managedTextures.clear();
		for(String textureName : textures.keySet()) {
			textures.put(textureName, null);
		}
		for(String fontName : fonts.keySet())
		{
			BitmapFont current = fonts.get(fontName);
			if(current != null)
			{
				try
				{
					current.dispose();
				}
				catch(NullPointerException e)
				{
					//Ignore . the dispose method from fonts can on occasion throw nullpointerexceptions...
				}
			}
			fonts.put(fontName, null);
		}
		for(NinePatchDefinition ninepatchDef : ninepatches.keySet())
		{
			NinePatch current = ninepatches.get(ninepatchDef);
			if(current != null)
			{
				//Ninepatches doesn't need to be disposed
			}
			ninepatches.put(ninepatchDef, null);
		}
	}
	
	public static class NinePatchDefinition {
		private String textureName;
		private int left;
		private int right;
		private int top;
		private int bottom;

		public NinePatchDefinition(String textureName, int left, int right, int top, int bottom) {
			this.textureName = textureName;
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}
		
		@Override
		public int hashCode() {
			return textureName.hashCode() ^ top ^ bottom ^ left ^ right;
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof NinePatchDefinition) && equalsOwn((NinePatchDefinition) obj);
		}

		private boolean equalsOwn(NinePatchDefinition obj) {
			return this.textureName.equals(obj.textureName)
					&& this.left == obj.left
					&& this.right == obj.right
					&& this.top == obj.top
					&& this.bottom == obj.bottom;
		}
	}
	
	private static class SingleTextureWrapper implements ITexture {
		private final Texture texture;
		
		public SingleTextureWrapper(Texture texture) {
			this.texture = texture;
		}

		@Override
		public int getWidth() {
			return texture.getWidth();
		}

		@Override
		public int getHeight() {
			return texture.getHeight();
		}
		
		@Override
		public NinePatch newNinepatch(int left, int right, int top, int bottom) {
			return new NinePatch(texture, left, right, top, bottom);
		}

		@Override
		public void setWrap(TextureWrap xWrap, TextureWrap yWrap) {
			if(texture.getUWrap() != xWrap || texture.getVWrap() != yWrap) {
				texture.setWrap(xWrap, yWrap);
			}
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height) {
			spriteBatch.draw(texture, x, y, width, height);
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, int srcX, int srcY,
				int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
			spriteBatch.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY); 
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y) {
			spriteBatch.draw(texture, x, y);
		}

		@Override
		public void draw(SpriteBatch spriteBatch, float x, float y, float width, float height, float u, float v,
				float u2, float v2) {
			spriteBatch.draw(texture, x, y, width, height, u, v, u2, v2);
		}

		@Override
		public TextureRegion asTextureRegion() {
			return new TextureRegion(texture);
		}

		@Override
		public ITexture createSubTexture(int x, int y, int width, int height) {
			return new AtlasStore.PackedTexture(texture, x, y, width, height);
		}
	}
}
