package com.github.systeminvecklare.badger.impl.gdx.load;

import com.badlogic.gdx.assets.AssetManager;
import com.github.systeminvecklare.badger.core.load.ILoadTask;

public abstract class AssetManagerTask implements ILoadTask {
	private final AssetManager assetManager;
	private boolean initialized = false;
	
	public AssetManagerTask(AssetManager assetManager) {
		this.assetManager = assetManager;
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
}
