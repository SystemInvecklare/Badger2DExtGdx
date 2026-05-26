package com.github.systeminvecklare.badger.impl.gdx;

import com.github.systeminvecklare.badger.impl.gdx.fbo.FboManager;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IHookableFboManager;
import com.github.systeminvecklare.badger.impl.gdx.fbo.IFboManager;

/*package-proected*/ class GwtSafeFlashyGdxEngineBase {
	private IHookableFboManager fboManager;
	
	protected IHookableFboManager newFboManager() {
		return new FboManager();
	}
	
	public final IFboManager getFboManager() {
		if(fboManager == null) {
			synchronized (this) {
				if(fboManager != null) {
					return fboManager;
				}
			}
			throw new RuntimeException("FboManager not initialized (or wrong thread)!");
		}
		return fboManager;
	}
	
	public final synchronized void initFromRenderThread() {
		fboManager = newFboManager();
		if(fboManager == null) {
			throw new RuntimeException("newFboManager() returned null!");
		}
	}
}
