package com.github.systeminvecklare.badger.impl.gdx.gameloop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IPixelTranslator;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoopHooksAdapter;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoopHooks;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IFboManager;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IHookableFboManager;
import com.github.systeminvecklare.badger.impl.gdx.util.GlFlagState;

public class GdxGameLoopHooks extends GameLoopHooksAdapter implements IGameLoopHooks {
	private final IPixelTranslator pixelTranslator;
	private final boolean useLetterboxing;
	private final IHookableFboManager fboManager;
	{
		IFboManager fboManagerMaybe = FlashyGdxEngine.get().getFboManager();
		if(fboManagerMaybe instanceof IHookableFboManager) {
			fboManager = (IHookableFboManager) fboManagerMaybe;
		} else {
			fboManager = null;
		}
	}
	private final GlFlagState GL_BLEND = new GlFlagState(GL20.GL_BLEND);
	private final GlFlagState GL_SCISSOR_TEST = new GlFlagState(GL20.GL_SCISSOR_TEST);
	
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
			
			((GdxDrawCycle) drawCycle).getSpriteBatch().flush();
			if(useLetterboxing) {
				EasyPooler ep = EasyPooler.obtainFresh();
				try {
					final int gdxWidth = Gdx.graphics.getWidth();
					final int gdxHeight = Gdx.graphics.getHeight();
					
					Position bottomLeft = pixelTranslator.translate(0, gdxHeight, ep.obtain(Position.class));
					Position topRight = pixelTranslator.translate(gdxWidth, 0, ep.obtain(Position.class));
					
					float xScale = gdxWidth/(topRight.getX() - bottomLeft.getX());
					float yScale = gdxHeight/(topRight.getY() - bottomLeft.getY());
					
					int leftBorder = (int) Math.ceil(-bottomLeft.getX()*xScale);
					int rightBorder = (int) Math.ceil(gdxWidth - xScale*(gdxWidth - bottomLeft.getX()));
					
					int bottomBorder = (int) Math.ceil(-bottomLeft.getY()*yScale);
					int topBorder = (int) Math.ceil(gdxHeight - yScale*(gdxHeight - bottomLeft.getY()));
					
					GL_BLEND.storeState();
					GL_SCISSOR_TEST.storeState();
					
					GL_BLEND.setEnabled(false);
					GL_SCISSOR_TEST.setEnabled(true);
					Gdx.gl.glClearColor(0, 0, 0, 1);
					
					if (leftBorder > 0) {
					    Gdx.gl.glScissor(0, 0, leftBorder, gdxHeight);
					    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					}
					if (rightBorder > 0) {
					    Gdx.gl.glScissor(gdxWidth - rightBorder, 0, rightBorder, gdxHeight);
					    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					}
					if (bottomBorder > 0) {
					    Gdx.gl.glScissor(0, 0, gdxWidth, bottomBorder);
					    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					}
					if (topBorder > 0) {
					    Gdx.gl.glScissor(0, gdxHeight - topBorder, gdxWidth, topBorder);
					    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					}
					
					GL_BLEND.restoreState();
					GL_SCISSOR_TEST.restoreState();
				} finally {
					ep.freeAllAndSelf();
				}
			}
			
			if(fboManager != null) {
				fboManager.onAfterSceneDraw();
			}
		}
	}
}
