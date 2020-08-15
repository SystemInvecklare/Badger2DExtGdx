package com.github.systeminvecklare.badger.impl.gdx.gameloop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoopHooksAdapter;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoopHooks;

public class GdxGameLoopHooks extends GameLoopHooksAdapter implements IGameLoopHooks {

	@Override
	public void onBeforeDraw() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
}
