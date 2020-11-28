package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;

public class FontStore {
	static {
		FlashyGdxEngine.get().registerStore(new IStore() {
			@Override
			public void reloadInventory() {
				reloadFonts();
			}
			
			@Override
			public void disposeInventory() {
				disposeFonts();
			}
		});
	}
	private static Map<FontKey, BitmapFont> fonts = new HashMap<FontKey, BitmapFont>();

	public static BitmapFont getFont(String fontName, int size) {
		FontKey key = new FontKey(fontName, size);
		BitmapFont font = fonts.get(key);
		if(font == null)
		{
			font = loadFont(key);
		}
		return font;
	}
	

	private static BitmapFont loadFont(FontKey key) {
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/"+key.name+".ttf"));
		BitmapFont font = gen.generateFont(key.newParameter());
		gen.dispose();
		fonts.put(key, font);
		return font;
	}
	
	
	public static void reloadFonts()
	{
		for(FontKey fontKey : fonts.keySet())
		{
			BitmapFont current = fonts.get(fontKey);
			if(current != null)
			{
				current.dispose();
			}
			fonts.put(fontKey, loadFont(fontKey));
		}
	}

	public static void disposeFonts() {
		for(FontKey fontKey : fonts.keySet())
		{
			BitmapFont current = fonts.get(fontKey);
			if(current != null)
			{
				current.dispose();
			}
			fonts.put(fontKey, null);
		}
	}
	
	private static class FontKey {
		private String name;
		private int size;
		
		public FontKey(String name, int size) {
			this.name = name;
			this.size = size;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() ^ size;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FontKey)
			{
				return this.name.equals(((FontKey) obj).name) && this.size == ((FontKey) obj).size;
			}
			return false;
		}
		
		public FreeTypeFontParameter newParameter()
		{
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = this.size;
			return parameter;
		}
	}
	
//	private static class FontKey {
//		private String name;
//		private FreeTypeFontParameter parameter;
//		
//		public FontKey(String name, FreeTypeFontParameter parameter) {
//			this.name = name;
//			this.parameter = parameter;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if(obj instanceof FontKey)
//			{
//				FontKey fontKeyObj = (FontKey) obj;
//				return equalsInt(this.parameter.size,fontKeyObj.parameter.size)
//						&& equalsBoolean(this.parameter.mono,fontKeyObj.parameter.mono)
//						&& equalsHinting(this.parameter.hinting,fontKeyObj.parameter.hinting)
//						&& equalsColor(this.parameter.color,fontKeyObj.parameter.color)
//						&& equalsFloat(this.parameter.gamma,fontKeyObj.parameter.gamma)
//						&& equalsInt(this.parameter.renderCount,fontKeyObj.parameter.renderCount)
//						&& equalsFloat(this.parameter.borderWidth,fontKeyObj.parameter.borderWidth)
//						&& equalsColor(this.parameter.borderColor,fontKeyObj.parameter.borderColor)
//						&& equalsBoolean(this.parameter.borderStraight,fontKeyObj.parameter.borderStraight)
//						&& equalsFloat(this.parameter.borderGamma,fontKeyObj.parameter.borderGamma)
//						&& equalsInt(this.parameter.shadowOffsetX,fontKeyObj.parameter.shadowOffsetX)
//						&& equalsInt(this.parameter.shadowOffsetY,fontKeyObj.parameter.shadowOffsetY)
//						&& equalsColor(this.parameter.shadowColor,fontKeyObj.parameter.shadowColor)
//						&& equalsInt(this.parameter.spaceX,fontKeyObj.parameter.spaceX)
//						&& equalsInt(this.parameter.spaceY,fontKeyObj.parameter.spaceY)
//						&& equalsString(this.parameter.characters,fontKeyObj.parameter.characters)
//						&& equalsBoolean(this.parameter.kerning,fontKeyObj.parameter.kerning)
////						&& equalsPixmapPacker(this.parameter.packer,fontKeyObj.parameter.packer)
//						&& equalsBoolean(this.parameter.flip,fontKeyObj.parameter.flip)
//						&& equalsBoolean(this.parameter.genMipMaps,fontKeyObj.parameter.genMipMaps)
//						&& equalsTextureFilter(this.parameter.minFilter,fontKeyObj.parameter.minFilter)
//						&& equalsTextureFilter(this.parameter.magFilter,fontKeyObj.parameter.magFilter)
//						&& equalsBoolean(this.parameter.incremental,fontKeyObj.parameter.incremental);
//			}
//			else
//			{
//				return false;
//			}
//		}
//		
//		private boolean equalsTextureFilter(TextureFilter minFilter, TextureFilter minFilter2) {
//			return minFilter == minFilter2;
//		}
//
//		private boolean equalsString(String characters, String characters2) {
//			return characters == null ? (characters2 == null) : characters.equals(characters2);
//		}
//
//		private boolean equalsFloat(float gamma, float gamma2) {
//			return gamma == gamma2;
//		}
//
//		private boolean equalsColor(Color color, Color color2) {
//			return color == null ? (color2 == null) : color.equals(color2);
//		}
//
//		private boolean equalsHinting(Hinting hinting, Hinting hinting2) {
//			return hinting == hinting2;
//		}
//
//		private boolean equalsBoolean(boolean mono, boolean mono2) {
//			return mono == mono2;
//		}
//
//		private boolean equalsInt(int size, int size2) {
//			return size == size2;
//		}
//
//		@Override
//		public int hashCode() {
//			int hashCode = name.hashCode();
//			hashCode = hashCode ^ hashCodeInt(parameter.size);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.mono);
//			hashCode = hashCode ^ hashCodeHinting(parameter.hinting);
//			hashCode = hashCode ^ hashCodeColor(parameter.color);
//			hashCode = hashCode ^ hashCodeFloat(parameter.gamma);
//			hashCode = hashCode ^ hashCodeInt(parameter.renderCount);
//			hashCode = hashCode ^ hashCodeFloat(parameter.borderWidth);
//			hashCode = hashCode ^ hashCodeColor(parameter.borderColor);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.borderStraight);
//			hashCode = hashCode ^ hashCodeFloat(parameter.borderGamma);
//			hashCode = hashCode ^ hashCodeInt(parameter.shadowOffsetX);
//			hashCode = hashCode ^ hashCodeInt(parameter.shadowOffsetY);
//			hashCode = hashCode ^ hashCodeColor(parameter.shadowColor);
//			hashCode = hashCode ^ hashCodeInt(parameter.spaceX);
//			hashCode = hashCode ^ hashCodeInt(parameter.spaceY);
//			hashCode = hashCode ^ hashCodeString(parameter.characters);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.kerning);
////			hashCode = hashCode ^ hashCodePixmapPacker(parameter.packer);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.flip);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.genMipMaps);
//			hashCode = hashCode ^ hashCodeTextureFilter(parameter.minFilter);
//			hashCode = hashCode ^ hashCodeTextureFilter(parameter.magFilter);
//			hashCode = hashCode ^ hashCodeBoolean(parameter.incremental);
//			return hashCode;
//		}
//
//		private int hashCodeTextureFilter(TextureFilter minFilter) {
//			return minFilter == null ? 0 : minFilter.hashCode();
//		}
//
//		private int hashCodeString(String characters) {
//			return characters == null ? 0 : characters.hashCode();
//		}
//
//		private int hashCodeFloat(float gamma) {
//			return Float.valueOf(gamma).hashCode();
//		}
//
//		private int hashCodeColor(Color color) {
//			return color == null ? 0 : color.hashCode();
//		}
//
//		private int hashCodeHinting(Hinting hinting) {
//			return hinting.hashCode();
//		}
//
//		private int hashCodeBoolean(boolean mono) {
//			return Boolean.valueOf(mono).hashCode();
//		}
//
//		private int hashCodeInt(int size) {
//			return size;
//		}
//	}
}
