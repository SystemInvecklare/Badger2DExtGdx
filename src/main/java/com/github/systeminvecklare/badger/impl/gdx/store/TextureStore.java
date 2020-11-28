package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;

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
		});
	}
	private static Map<NinePatchDefinition, NinePatch> ninepatches = new HashMap<NinePatchDefinition, NinePatch>();
	private static Map<String, Texture> textures = new HashMap<String, Texture>();
	private static Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();

	public static Texture getTexture(String textureName) {
		Texture texture = textures.get(textureName);
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
//		BitmapFont font = new BitmapFont(Gdx.files.internal(fontName+".fnt"), Gdx.files.internal(fontName+"_0.png"), false);
		TextureRegion texture = new TextureRegion(new Texture(Gdx.files.internal(fontName+"_0.png")));
		texture.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		BitmapFont font = new BitmapFont(Gdx.files.internal(fontName+".fnt"), texture, false);
		fonts.put(fontName, font);
		return font;
	}

	private static Texture loadTexture(String textureName)
	{
		Texture texture = new Texture(textureName);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		textures.put(textureName, texture);
//		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);//TODO temp. Rather do a callback hook so you can do something to the texture based on name
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);//TODO temp. Rather do a callback hook so you can do something to the texture based on name
		return texture;
	}
	
	private static NinePatch loadNinePatch(NinePatchDefinition definition)
	{
		NinePatch ninePatch = new NinePatch(getTexture(definition.textureName),definition.left,definition.right,definition.top,definition.bottom);
		ninepatches.put(definition, ninePatch);
		return ninePatch;
	}
	
	public static void reloadGraphics()
	{
		for(String textureName : textures.keySet())
		{
			Texture current = textures.get(textureName);
			if(current != null)
			{
				current.dispose();
			}
			textures.put(textureName, loadTexture(textureName));
		}
		for(String fontName : fonts.keySet())
		{
			BitmapFont current = fonts.get(fontName);
			if(current != null)
			{
				current.dispose();
			}
			fonts.put(fontName, loadFont(fontName));
		}
		for(NinePatchDefinition ninepatchDef : ninepatches.keySet())
		{
			NinePatch current = ninepatches.get(ninepatchDef);
			if(current != null)
			{
				//Ninepatches doesn't need to be disposed
			}
			ninepatches.put(ninepatchDef, loadNinePatch(ninepatchDef));
		}
	}
	
	public static void disposeGraphics()
	{
		for(String textureName : textures.keySet())
		{
			Texture current = textures.get(textureName);
			if(current != null)
			{
				current.dispose();
			}
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
}
