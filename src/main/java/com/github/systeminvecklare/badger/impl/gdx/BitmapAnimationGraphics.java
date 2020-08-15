package com.github.systeminvecklare.badger.impl.gdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.ISource;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.pooling.IPool;

public class BitmapAnimationGraphics implements IMovieClipLayer {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IPool<List<BitmapPartGraphics>> arrayListpool = (IPool<List<BitmapPartGraphics>>) (IPool) FlashyEngine.get().getPoolManager().getPool(ArrayList.class);
	private List<BitmapPartGraphics> frames;
	
	private String textureName;
	private Color tint;
	private int tilesX;
	private int tilesY;
	private PlayMode playMode = PlayMode.LOOP;
	private ISource<Float> stateTime;
	private float fps;
	private Number width;
	private Number height;

	public BitmapAnimationGraphics(String textureName, Color tint, int tilesX, int tilesY, Number width, Number height, float fps,ISource<Float> stateTime) {
		this.textureName = textureName;
		this.tint = tint;
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.fps = fps;
		this.stateTime = stateTime;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void draw(IDrawCycle drawCycle) {
		frames.get(getKeyFrameIndex(stateTime.getFromSource())).draw(drawCycle);
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		return false;
	}

	@Override
	public void init() {
		frames = arrayListpool.obtain();
		Texture texture = TextureStore.getTexture(textureName);
		
		int srcWidth = texture.getWidth()/tilesX;
		int srcHeight = texture.getHeight()/tilesY;
		
		Float drawnWidth = width == null ? null : width.floatValue();
		Float drawnHeight = height == null ? null : height.floatValue();
		
		for(int y = 0; y < tilesY; ++y)
		{
			for(int x = 0; x < tilesX; ++x)
			{
				BitmapPartGraphics frame = new BitmapPartGraphics(textureName, tint, x*srcWidth, y*srcHeight, srcWidth, srcHeight,drawnWidth,drawnHeight);
				frames.add(frame);
				frame.init();
			}
		}
	}
	
	public BitmapAnimationGraphics setPlayMode(PlayMode playMode) {
		if(playMode == PlayMode.LOOP_RANDOM)
		{
			throw new UnsupportedOperationException("LOOP_RANDOM not supported.");
		}
		this.playMode = playMode;
		return this;
	}

	@Override
	public void dispose() {
		for(int i = 0; i< frames.size(); ++i)
		{
			frames.get(i).dispose();
		}
		frames.clear();
		arrayListpool.free(frames);
		frames = null;
	}
	
	private int getKeyFrameIndex (float stateTime) {
		if (frames.size() == 1) return 0;

		int frameNumber = (int)(stateTime * fps);
		switch (playMode) {
		case NORMAL:
			frameNumber = Math.min(frames.size() - 1, frameNumber);
			break;
		case LOOP:
			frameNumber = frameNumber % frames.size();
			break;
		case LOOP_PINGPONG:
			frameNumber = frameNumber % ((frames.size() * 2) - 2);
			if (frameNumber >= frames.size()) frameNumber = frames.size() - 2 - (frameNumber - frames.size());
			break;
		case REVERSED:
			frameNumber = Math.max(frames.size() - frameNumber - 1, 0);
			break;
		case LOOP_REVERSED:
			frameNumber = frameNumber % frames.size();
			frameNumber = frames.size() - frameNumber - 1;
			break;
		}

		return frameNumber;
	}
}
