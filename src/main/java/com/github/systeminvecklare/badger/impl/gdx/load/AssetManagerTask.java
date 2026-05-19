package com.github.systeminvecklare.badger.impl.gdx.load;

import com.badlogic.gdx.assets.AssetManager;
import com.github.systeminvecklare.badger.core.load.IProgressingLoadTask;

public abstract class AssetManagerTask implements IProgressingLoadTask {
	private final AssetManager assetManager;
	private float workScale;
	private boolean initialized = false;
	
	public AssetManagerTask(AssetManager assetManager) {
		this(assetManager, 1f);
	}
	
	public AssetManagerTask(AssetManager assetManager, float workScale) {
		this.assetManager = assetManager;
		this.workScale = workScale;
	}

	@Override
	public boolean load() {
		if(!initialized) {
			initialized = true;
			setup(assetManager);
			return false;
		}
		return assetManager.update();
	}

	protected abstract void setup(AssetManager assetManager);
	
	public AssetManagerTask setWorkScale(float workScale) {
		this.workScale = workScale;
		return this;
	}
	
	@Override
	public float getTotalWork() {
		return workScale;
	}
	
	@Override
	public float getCompletedWork() {
		return workScale*assetManager.getProgress();
	}
}
