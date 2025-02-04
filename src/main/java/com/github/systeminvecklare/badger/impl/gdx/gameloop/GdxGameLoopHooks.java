package com.github.systeminvecklare.badger.impl.gdx.gameloop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IPixelTranslator;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoopHooksAdapter;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoopHooks;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;

public class GdxGameLoopHooks extends GameLoopHooksAdapter implements IGameLoopHooks {
	private final Texture texture = new Texture(1, 1, Format.RGB888);
	private final IPixelTranslator pixelTranslator;
	private final boolean useLetterboxing;
	
	private ITransform originalTransform = null;

	public GdxGameLoopHooks(IPixelTranslator pixelTranslator, boolean useLetterboxing) {
		this.pixelTranslator = pixelTranslator;
		this.useLetterboxing = useLetterboxing;
	}

	@Override
	public void onBeforeDraw() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	@Override
	public void onBeforeSceneDraw(IDrawCycle drawCycle) {
		ITransform transform = drawCycle.getTransform();
		this.originalTransform = transform.copy(FlashyEngine.get().getPoolManager());
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			Position bottomLeft = pixelTranslator.translate(0, Gdx.graphics.getHeight(), ep.obtain(Position.class));
			Position topRight = pixelTranslator.translate(Gdx.graphics.getWidth(), 0, ep.obtain(Position.class));
			
			float widthScale = (topRight.getX()-bottomLeft.getX())/Gdx.graphics.getWidth();
			float heightScale = (topRight.getY()-bottomLeft.getY())/Gdx.graphics.getHeight();
			transform.multiplyScale(1f/widthScale, 1f/heightScale).addToPosition(-bottomLeft.getX()/widthScale, -bottomLeft.getY()/heightScale);
		} finally {
			ep.freeAllAndSelf();
		}
	}
	
	@Override
	public void onAfterSceneDraw(IDrawCycle drawCycle) {
		if(originalTransform != null) {
			drawCycle.getTransform().setTo(originalTransform);
			originalTransform.free();
			originalTransform = null;
			
			if(!useLetterboxing) {
				return;
			}
			
			GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
			gdxDrawCycle.updateSpriteBatchTransform();
			SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
			spriteBatch.setShader(null);
			//TODO we should create a simple shader we can use that doesn't need a texture.... And render "natively"
			spriteBatch.setColor(Color.BLACK);
			
			EasyPooler ep = EasyPooler.obtainFresh();
			try {
				final int gdxWidth = Gdx.graphics.getWidth();
				final int gdxHeight = Gdx.graphics.getHeight();
				
				Position bottomLeft = pixelTranslator.translate(0, gdxHeight, ep.obtain(Position.class));
				Position topRight = pixelTranslator.translate(gdxWidth, 0, ep.obtain(Position.class));
				
				float xScale = gdxWidth/(topRight.getX() - bottomLeft.getX());
				float yScale = gdxHeight/(topRight.getY() - bottomLeft.getY());
				
				float leftBorder = -bottomLeft.getX()*xScale;
				float rightBorder = gdxWidth - xScale*(gdxWidth - bottomLeft.getX());
				
				float bottomBorder = -bottomLeft.getY()*yScale;
				float topBorder = gdxHeight - yScale*(gdxHeight - bottomLeft.getY());
				
				if(leftBorder > 0) {
					spriteBatch.draw(texture, 0, 0, leftBorder, gdxHeight);
				}
				if(rightBorder > 0) {
					spriteBatch.draw(texture, gdxWidth - rightBorder, 0, rightBorder, gdxHeight);
				}
				if(bottomBorder > 0) {
					spriteBatch.draw(texture, 0, 0, gdxWidth, bottomBorder);
				}
				if(topBorder > 0) {
					spriteBatch.draw(texture, 0, gdxHeight - topBorder, gdxWidth, topBorder);
				}
			} finally {
				ep.freeAllAndSelf();
			}
		}
	}
}
