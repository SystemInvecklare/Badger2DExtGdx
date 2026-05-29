package com.github.systeminvecklare.badger.impl.gdx.fbo;

public interface IHookableFboManager extends IFboManager {
	void onAfterSceneDraw();
	void onScreenResize(int width, int height);
}