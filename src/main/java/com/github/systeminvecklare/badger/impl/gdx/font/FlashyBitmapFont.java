package com.github.systeminvecklare.badger.impl.gdx.font;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.github.systeminvecklare.badger.core.font.IFlashyFont;
import com.github.systeminvecklare.badger.core.font.IFlashyText;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.util.FloatRectangle;
import com.github.systeminvecklare.badger.core.util.IFloatRectangle;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;

public class FlashyBitmapFont implements IFlashyFont<Color> {
	private final LazyBitmapFont fontHolder;
	
	public FlashyBitmapFont(BitmapFont font) {
		if(font == null) {
			throw new NullPointerException("font was null");
		}
		this.fontHolder = new LazyBitmapFont(null);
		this.fontHolder.font = font;
	}
	
	/**
	 * Internal path to .fnt file.
	 * @param fontFile
	 */
	public FlashyBitmapFont(String fontPath) {
		this(Gdx.files.internal(fontPath));
	}
	
	/**
	 * FileHandle to .fnt file.
	 * @param fontFile
	 */
	public FlashyBitmapFont(FileHandle fontFile) {
		this.fontHolder = new LazyBitmapFont(fontFile);
	}

	@Override
	public float getWidth(String text) {
		return new GlyphLayout(fontHolder.getFont(), text).width;
	}

	@Override
	public float getHeight(String text) {
		return new GlyphLayout(fontHolder.getFont(), text).height;
	}
	
	private FloatRectangle getBoundsFromGlyphLayout(GlyphLayout glyphLayout) {
		Float x = null;
		Float y = null;
		for(GlyphRun run : glyphLayout.runs) {
			if(x == null) {
				x = run.x;
			} else {
				x = Math.min(x, run.x);
			}
			if(y == null) {
				y = run.y;
			} else {
				y = Math.min(y, run.y);
			}
		}
		return new FloatRectangle(x != null ? x : 0, (y != null ? y : 0) - fontHolder.getFont().getCapHeight(), glyphLayout.width, glyphLayout.height);
	}
	
	@Override
	public FloatRectangle getBounds(String text) {
		return getBoundsFromGlyphLayout(new GlyphLayout(fontHolder.getFont(), text));
	}
	
	
	@Override
	public FloatRectangle getBounds(String text, float maxWidth) {
		return getBoundsFromGlyphLayout(new GlyphLayout(fontHolder.getFont(), text, Color.WHITE, maxWidth, Align.left, true));
	}
	
	@Override
	public FloatRectangle getBoundsCentered(String text, float maxWidth) {
		return getBoundsFromGlyphLayout(new GlyphLayout(fontHolder.getFont(), text, Color.WHITE, maxWidth, Align.center, true));
	}

	@Override
	public void preloadFont() {
		fontHolder.getFont();
	}
	
	private static SpriteBatch extractAndUpdateSpriteBatch(IDrawCycle drawCycle) {
		SpriteBatch spriteBatch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		((GdxDrawCycle) drawCycle).updateSpriteBatchTransform();
		return spriteBatch;
	}

	@Override
	public void draw(IDrawCycle drawCycle, String text, float x, float y, Color tint) {
		SpriteBatch spriteBatch = extractAndUpdateSpriteBatch(drawCycle);
		spriteBatch.setColor(Color.WHITE);
		BitmapFont bitmapFont = fontHolder.getFont();
		bitmapFont.setColor(tint);
		bitmapFont.draw(spriteBatch, text, x, y);
	}
	
	@Override
	public void drawWrapped(IDrawCycle drawCycle, String text, float x, float y, Color tint, float maxWidth) {
		SpriteBatch spriteBatch = extractAndUpdateSpriteBatch(drawCycle);
		spriteBatch.setColor(Color.WHITE);
		BitmapFont bitmapFont = fontHolder.getFont();
		bitmapFont.setColor(tint);
		bitmapFont.draw(spriteBatch, text, x, y, maxWidth, Align.left, true);
	}
	
	@Override
	public void drawWrappedCentered(IDrawCycle drawCycle, String text, float x, float y, Color tint, float maxWidth) {
		SpriteBatch spriteBatch = extractAndUpdateSpriteBatch(drawCycle);
		spriteBatch.setColor(Color.WHITE);
		BitmapFont bitmapFont = fontHolder.getFont();
		bitmapFont.setColor(tint);
		bitmapFont.draw(spriteBatch, text, x, y, maxWidth, Align.center, true);
	}
	
	@Override
	public IFlashyText createText(String text, Color tint) {
		BitmapFont bitmapFont = fontHolder.getFont();
		bitmapFont.setColor(tint);
		GlyphLayout glyphLayout = new GlyphLayout(bitmapFont, text);
		return new FlashyText(glyphLayout, getBoundsFromGlyphLayout(glyphLayout));
	}
	

	@Override
	public IFlashyText createTextWrapped(String text, Color tint, float maxWidth) {
		BitmapFont bitmapFont = fontHolder.getFont();
		GlyphLayout glyphLayout = new GlyphLayout(bitmapFont, text, tint, maxWidth, Align.left, true);
		return new FlashyText(glyphLayout, getBoundsFromGlyphLayout(glyphLayout));
	}

	@Override
	public IFlashyText createTextWrappedCentered(String text, Color tint, float maxWidth) {
		BitmapFont bitmapFont = fontHolder.getFont();
		GlyphLayout glyphLayout = new GlyphLayout(bitmapFont, text, tint, maxWidth, Align.center, true);
		return new FlashyText(glyphLayout, getBoundsFromGlyphLayout(glyphLayout));
	}

	private static class LazyBitmapFont {
		private BitmapFont font = null;
		private final FileHandle fileHandle;
		public LazyBitmapFont(FileHandle fileHandle) {
			this.fileHandle = fileHandle;
		}
		
		public BitmapFont getFont() {
			if(font == null) {
				//TODO This should probably use TextureStore and getBitmapFont(fontName)?
				font = new BitmapFont(fileHandle);
			}
			return font;
		}
	}
	
	private class FlashyText implements IFlashyText {
		private final GlyphLayout glyphLayout;
		private final FloatRectangle bounds;

		public FlashyText(GlyphLayout glyphLayout, FloatRectangle bounds) {
			this.glyphLayout = glyphLayout;
			this.bounds = bounds;
		}

		@Override
		public IFloatRectangle getBounds() {
			return bounds;
		}

		@Override
		public void draw(IDrawCycle drawCycle, float x, float y) {
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
			FlashyBitmapFont.this.fontHolder.getFont().draw(spriteBatch, glyphLayout, x, y);
		}
	}
}
