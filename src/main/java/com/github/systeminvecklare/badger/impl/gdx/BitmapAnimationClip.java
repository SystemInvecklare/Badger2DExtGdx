package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.github.systeminvecklare.badger.core.graphics.components.core.ISource;
import com.github.systeminvecklare.badger.core.graphics.components.core.ITic;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClip;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;

public class BitmapAnimationClip extends MovieClip {
	private float stateTime = 0;
	private BitmapAnimationGraphics graphics;
	
	public BitmapAnimationClip(String textureName, Color tint, int tilesX, int tilesY, Number width, Number height, float fps) {
		this.graphics = new BitmapAnimationGraphics(textureName, tint, tilesX, tilesY, width, height, fps, new ISource<Float>() {
			@Override
			public Float getFromSource() {
				return stateTime;
			}
		});
		addGraphics(graphics);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		graphics = null;
	}
	
	public BitmapAnimationClip setPlayMode(PlayMode playMode) {
		graphics.setPlayMode(playMode);
		return this;
	}
	
	@Override
	public void think(ITic tic) {
		super.think(tic);
		stateTime += SceneManager.get().getStep();
	}
}
