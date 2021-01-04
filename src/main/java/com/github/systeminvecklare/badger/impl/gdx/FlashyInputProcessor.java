package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.InputProcessor;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IInputHandler;

public class FlashyInputProcessor implements InputProcessor {
	private IInputHandler handler;

	public FlashyInputProcessor(IInputHandler handler) {
		this.handler = handler;
	}

	@Override
	public boolean keyDown(int keycode) {
		return handler.registerKeyDown(keycode);
	}


	@Override
	public boolean keyUp(int keycode) {
		return handler.registerKeyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return handler.registerPointerDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return handler.registerPointerUp(screenX, screenY, pointer, button);
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return handler.registerPointerDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
