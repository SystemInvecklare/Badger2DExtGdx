package com.github.systeminvecklare.badger.impl.gdx;

import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.pooling.IPoolManager;

public class FlashyGdxEngine extends AbstractFlashyGdxEngine {
	private final IPoolManager poolManager;
	
	public FlashyGdxEngine() {
		this.poolManager = newPoolManager();
	}
	
	public void initPoolManagerOnThread() {
		// No-op
	}
	
	public void disposePoolManagerOnThread() {
		// No-op
	}
	
	@Override
	public IPoolManager getPoolManager() {
		return poolManager;
	}
	
	public static FlashyGdxEngine get() {
		return (FlashyGdxEngine) FlashyEngine.get();
	}
}