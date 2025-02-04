package com.github.systeminvecklare.badger.impl.gdx.gameloop;

import com.badlogic.gdx.Gdx;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IWindowCanvas;

/*package-protected*/ class GdxWindowCanvas implements IWindowCanvas {
	@Override
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}

	@Override
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
}
