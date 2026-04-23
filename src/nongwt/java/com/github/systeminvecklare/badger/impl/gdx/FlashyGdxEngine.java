package com.github.systeminvecklare.badger.impl.gdx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.pooling.IPoolManager;

public class FlashyGdxEngine extends AbstractFlashyGdxEngine {
	private ThreadLocal<IPoolManager> poolManager = new ThreadLocal<IPoolManager>();
	
	public void initPoolManagerOnThread() {
		poolManager.set(newPoolManager());
	}
	
	public void disposePoolManagerOnThread() {
		poolManager.set(null);
		poolManager.remove();
	}

	@Override
	public IPoolManager getPoolManager() {
		return poolManager.get();
	}
	
	public ExecutorService newSingleThreadExecutorWithPoolManager() {
		return Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				return Executors.defaultThreadFactory().newThread(new Runnable() {
					@Override
					public void run() {
						FlashyGdxEngine.this.initPoolManagerOnThread();
						try {
							r.run();
						} finally {
							FlashyGdxEngine.this.disposePoolManagerOnThread();
						}
					}
				});
			}
		});
	}
	
	public static FlashyGdxEngine get() {
		return (FlashyGdxEngine) FlashyEngine.get();
	}
}