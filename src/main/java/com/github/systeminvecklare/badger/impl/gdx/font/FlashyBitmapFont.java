package com.github.systeminvecklare.badger.impl.gdx.font;

import java.util.List;
import java.util.function.Supplier;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.github.systeminvecklare.badger.core.font.EmbellishmentTextSegment;
import com.github.systeminvecklare.badger.core.font.IFlashyFont;
import com.github.systeminvecklare.badger.core.font.IFlashyText;
import com.github.systeminvecklare.badger.core.font.TransformedFlashyText;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.util.FloatRectangle;
import com.github.systeminvecklare.badger.core.util.IFloatRectangle;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IFboDefinition;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IFboManager;
import com.github.systeminvecklare.badger.impl.gdx.file.FileTypes;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class FlashyBitmapFont implements IFlashyFont<Color> {
	private final LazyBitmapFont fontHolder;
	private final float lineHeightScale;
	
	public FlashyBitmapFont(BitmapFont font) {
		this(font, 1f);
	}
	
	public FlashyBitmapFont(BitmapFont font, float lineHeightScale) {
		if(font == null) {
			throw new NullPointerException("font was null");
		}
		this.lineHeightScale = lineHeightScale;
		this.fontHolder = new LazyBitmapFont(null);
		this.fontHolder.font = font;
	}
	
	/**
	 * Internal path to .fnt file.
	 * @param fontFile
	 */
	public FlashyBitmapFont(String fontPath) {
		this(FlashyGdxEngine.get().getFileResolver().resolve(FileTypes.FONT, fontPath), 1f);
	}
	
	public FlashyBitmapFont(String fontPath, float lineHeightScale) {
		this(FlashyGdxEngine.get().getFileResolver().resolve(FileTypes.FONT, fontPath), lineHeightScale);
	}
	
	/**
	 * FileHandle to .fnt file.
	 * @param fontFile
	 */
	public FlashyBitmapFont(FileHandle fontFile) {
		this(fontFile, 1f);
	}
	
	public FlashyBitmapFont(FileHandle fontFile, float lineHeightScale) {
		this.fontHolder = new LazyBitmapFont(fontFile);
		this.lineHeightScale = lineHeightScale;
	}
	
	private GlyphLayout newGlyphLayout(BitmapFont font, String text) {
		if(lineHeightScale == 1f) {
			return new GlyphLayout(font, text);
		}
		float lineHeight = font.getLineHeight();
		GlyphLayout layout;
		try {
			font.getData().setLineHeight(lineHeight*lineHeightScale);
			layout = new GlyphLayout(font, text);
		} finally {
			font.getData().setLineHeight(lineHeight);
		}
		return layout;
	}
	

	private GlyphLayout newGlyphLayout(BitmapFont font, String text, Color color, float maxWidth, int align, boolean wrap) {
		if(lineHeightScale == 1f) {
			return new GlyphLayout(font, text, color, maxWidth, align, wrap);
		}
		float lineHeight = font.getLineHeight();
		GlyphLayout layout;
		try {
			font.getData().setLineHeight(lineHeight*lineHeightScale);
			layout = new GlyphLayout(font, text, color, maxWidth, align, wrap);
		} finally {
			font.getData().setLineHeight(lineHeight);
		}
		return layout;
	}

	@Override
	public float getWidth(String text) {
		return newGlyphLayout(fontHolder.getFont(), text).width;
	}

	@Override
	public float getHeight(String text) {
		return newGlyphLayout(fontHolder.getFont(), text).height;
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
		return getBoundsFromGlyphLayout(newGlyphLayout(fontHolder.getFont(), text));
	}
	
	
	@Override
	public FloatRectangle getBounds(String text, float maxWidth) {
		return getBoundsFromGlyphLayout(newGlyphLayout(fontHolder.getFont(), text, Color.WHITE, maxWidth, Align.left, true));
	}

	@Override
	public FloatRectangle getBoundsCentered(String text, float maxWidth) {
		return getBoundsFromGlyphLayout(newGlyphLayout(fontHolder.getFont(), text, Color.WHITE, maxWidth, Align.center, true));
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
	public IFlashyText createText(final String text, Color tint) {
		return new FlashyText(text, tint) {
			@Override
			protected GlyphLayout createLayout(BitmapFont bitmapFont, String text) {
				return newGlyphLayout(bitmapFont, text);
			}
		};
	}
	

	@Override
	public IFlashyText createTextWrapped(String text, Color tint, final float maxWidth) {
		return new FlashyText(text, tint) {
			@Override
			protected GlyphLayout createLayout(BitmapFont bitmapFont, String text) {
				return newGlyphLayout(bitmapFont, text, bitmapFont.getColor(), maxWidth, Align.left, true);
			}
		};
	}

	@Override
	public IFlashyText createTextWrappedCentered(String text, Color tint, final float maxWidth) {
		return new FlashyText(text, tint) {
			@Override
			protected GlyphLayout createLayout(BitmapFont bitmapFont, String text) {
				return newGlyphLayout(bitmapFont, text, bitmapFont.getColor(), maxWidth, Align.center, true);
			}
		};
	}
	
	public FlashyBitmapFont withLineHeightScale(float scale) {
		return new FlashyBitmapFont(fontHolder.fileHandle, scale);
	}
	
	public float getLineHeightScale() {
		return lineHeightScale;
	}

	private static class LazyBitmapFont {
		private BitmapFont font = null;
		private final FileHandle fileHandle;
		
		public LazyBitmapFont(FileHandle fileHandle) {
			this.fileHandle = fileHandle;
		}
		
		public BitmapFont getFont() {
			if(font != null) {
				return font;
			}
			return TextureStore.getBitmapFont(fileHandle.pathWithoutExtension());
		}
	}
	
	private abstract class FlashyText implements IFlashyText {
		private final IFboManager fboManager = FlashyGdxEngine.get().getFboManager();
		private final String text;
		private final Color color;
		private final Color colorCacheKey = new Color();
		private GlyphLayout glyphLayout;
		private FloatRectangle bounds;
		private boolean initialized = false;
		private final IFboDefinition TEMP_test = new IFboDefinition() {
			@Override
			public int getX() {
				//TODO round down or something
				return (int) bounds.getX();
			}
			
			@Override
			public int getY() {
				return (int) bounds.getY();
			}
			
			@Override
			public int getWidth() {
				return (int) bounds.getWidth();
			}
			
			@Override
			public int getHeight() {
				return (int) bounds.getHeight();
			}
		};

		public FlashyText(String text, Color color) {
			this.text = text;
			this.color = color;
		}
		
		private void refresh() {
			BitmapFont bitmapFont = fontHolder.getFont();
			bitmapFont.setColor(color);
			this.glyphLayout = createLayout(bitmapFont, text);
			colorCacheKey.set(color);
			this.bounds = getBoundsFromGlyphLayout(glyphLayout);
			fboManager.setRectangleDirty(TEMP_test);
			fboManager.setDirty(TEMP_test);
		}
		
		protected abstract GlyphLayout createLayout(BitmapFont bitmapFont, String text);
		
		private void assertInitialized() {
			if(!initialized) {
				initialized = true;
				refresh();
			}			
		}
		
		private void assertFresh() {
			assertInitialized();
			if(!colorCacheKey.equals(color)) {
				refresh();
			}
		}

		@Override
		public IFloatRectangle getBounds() {
			assertInitialized();
			return bounds;
		}

		@Override
		public void draw(IDrawCycle drawCycle, float x, float y) {
			assertFresh();
			//TODO Hmm.... When enabling this for text, other stuff gets fucked up. Why?
			//     Does it have to do with the mult on the transform?
//			drawCycle.getTransform().mult(drawCycle.borrowUtility().setToIdentity().setPosition(x,y));
//			if(fboManager.drawCached(drawCycle, TEMP_test)) {
//				GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
//				gdxDrawCycle.updateSpriteBatchTransform();
//				SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
//				FlashyBitmapFont.this.fontHolder.getFont().draw(spriteBatch, glyphLayout, 0, 0);
//				fboManager.done();
//			}
			
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
			FlashyBitmapFont.this.fontHolder.getFont().draw(spriteBatch, glyphLayout, x, y);
		}
		
		@Override
		public List<EmbellishmentTextSegment> getEmbellishments(List<EmbellishmentTextSegment> result,
				Supplier<TransformedFlashyText> transfromedTextSupplier) {
			return result;
		}
	}
}
