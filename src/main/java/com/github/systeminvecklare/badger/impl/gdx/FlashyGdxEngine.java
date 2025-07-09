package com.github.systeminvecklare.badger.impl.gdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayerDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.layer.Layer;
import com.github.systeminvecklare.badger.core.graphics.components.layer.LayerDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.IMovieClipDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.MovieClipDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.scene.ISceneDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.scene.Scene;
import com.github.systeminvecklare.badger.core.graphics.components.scene.SceneDelegate;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IFlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.framework.smartlist.ISmartList;
import com.github.systeminvecklare.badger.core.graphics.framework.smartlist.SmartList;
import com.github.systeminvecklare.badger.core.pooling.FlashyPoolManager;
import com.github.systeminvecklare.badger.core.pooling.IPool;
import com.github.systeminvecklare.badger.core.pooling.IPoolManager;
import com.github.systeminvecklare.badger.core.pooling.SimplePool;
import com.github.systeminvecklare.badger.core.util.PoolableArrayOf16Floats;
import com.github.systeminvecklare.badger.impl.gdx.audio.FlashySound;
import com.github.systeminvecklare.badger.impl.gdx.audio.IFlashySoundDelegate;
import com.github.systeminvecklare.badger.impl.gdx.audio.NonThreadedFlashySoundDelegate;
import com.github.systeminvecklare.badger.impl.gdx.file.GdxFileResolver;
import com.github.systeminvecklare.badger.impl.gdx.file.IFileResolver;
import com.github.systeminvecklare.badger.impl.gdx.file.OverloadingFileResolver;
import com.github.systeminvecklare.badger.impl.gdx.store.IStore;

public class FlashyGdxEngine implements IFlashyEngine {
	private IFileResolver fileResolver = GdxFileResolver.INTERNAL;
	
	private ThreadLocal<IPoolManager> poolManager = new ThreadLocal<IPoolManager>();
	private List<IStore> stores = new ArrayList<IStore>(0);
	private boolean regeristingStore = false;
	private List<IStore> queuedStores = new ArrayList<IStore>(0);
	
	public FlashyGdxEngine() {
	}
	
	public void initPoolManagerOnThread() {
		poolManager.set(newPoolManager());
	}
	
	public void disposePoolManagerOnThread() {
		poolManager.set(null);
		poolManager.remove();
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
	
	public static ExecutorService sameThreadExecutor() {
		return new AbstractExecutorService() {
			private boolean terminated = false;
			
			@Override
			public void execute(Runnable command) {
				if(!terminated) {
					command.run();
				}
			}
			
			@Override
			public List<Runnable> shutdownNow() {
				return Collections.emptyList();
			}
			
			@Override
			public void shutdown() {
				terminated = true;
			}
			
			@Override
			public boolean isTerminated() {
				return terminated;
			}
			
			@Override
			public boolean isShutdown() {
				return terminated;
			}
			
			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
				return true;
			}
		};
	}

	@Override
	public IPoolManager getPoolManager() {
		return poolManager.get();
	}
	
	protected IPoolManager newPoolManager() {
		FlashyPoolManager poolManager = new FlashyPoolManager() {
			private IPool<ITransform> transformPool = new SimplePool<ITransform>(10,30) {
				@Override
				public ITransform newObject() {
					return new GdxTransform(this);
				}
			};

			@SuppressWarnings("unchecked")
			@Override
			public <T> IPool<T> getPool(Class<T> poolableType) {
				if(ITransform.class.equals(poolableType))
				{
					return (IPool<T>) transformPool;
				}
				return super.getPool(poolableType);
			}
		};
		poolManager.registerPool(PoolableMatrix4.class, new SimplePool<PoolableMatrix4>(10,20) {
			@Override
			public PoolableMatrix4 newObject() {
				return new PoolableMatrix4(this);
			}
		});
		poolManager.registerPool(PoolableMatrix3.class, new SimplePool<PoolableMatrix3>(10,20) {
			@Override
			public PoolableMatrix3 newObject() {
				return new PoolableMatrix3(this);
			}
		});
		poolManager.registerPool(PoolableArrayOf16Floats.class, new SimplePool<PoolableArrayOf16Floats>(10,20) {
			@Override
			public PoolableArrayOf16Floats newObject() {
				return new PoolableArrayOf16Floats(this);
			}
		});
		poolManager.registerPool(Vector3.class, new SimplePool<Vector3>(10,30) {
			@Override
			public Vector3 newObject() {
				return new Vector3();
			}
		});
		poolManager.registerPool(Quaternion.class, new SimplePool<Quaternion>(10,20) {
			@Override
			public Quaternion newObject() {
				return new Quaternion();
			}
		});
		return poolManager;
	}

	@Override
	public ISceneDelegate newSceneDelegate(Scene wrapper) {
		return new SceneDelegate(wrapper);
	}

	@Override
	public ILayerDelegate newLayerDelegate(Layer wrapper) {
		return new LayerDelegate(wrapper);
	}

	@Override
	public IMovieClipDelegate newMovieClipDelegate(MovieClip wrapper) {
		return new MovieClipDelegate(wrapper);
	}
	
	public IFlashySoundDelegate newFlashySoundDelegate(FlashySound wrapper, String soundName) {
		return new NonThreadedFlashySoundDelegate(wrapper, soundName);
	}

	@Override
	public <T> ISmartList<T> newSmartList() {
		return new SmartList<T>();
	}
	
	@Override
	public void copyToClipboard(CharSequence text) {
		Gdx.app.getClipboard().setContents(text.toString());
	}
	
	@Override
	public String pasteFromClipboard() {
		return Gdx.app.getClipboard().getContents();
	}
	
	public void registerStore(IStore store) {
		if(regeristingStore) {
			queuedStores.add(store);
		} else {
			regeristingStore = true;
			List<IStore> depencencies = new ArrayList<IStore>();
			int insertIndex = stores.size();
			for(int i = 0; i < stores.size(); ++i) {
				IStore existingStore = stores.get(i);
				depencencies.clear();
				if(existingStore.getDependencies(depencencies).contains(store)) {
					insertIndex = i;
					break;
				}
			}
			stores.add(insertIndex, store);
			regeristingStore = false;
			if(!queuedStores.isEmpty()) {
				List<IStore> harvestedQueued = new ArrayList<IStore>(queuedStores);
				queuedStores.clear();
				for(IStore queued : harvestedQueued) {
					registerStore(queued);
				}
			}
		}
	}
	
	public void reloadStoreInventories() {
		for(IStore store : stores) {
			store.reloadInventory();
		}
	}
	
	public void disposeStoreInventories() {
		for(IStore store : stores) {
			store.disposeInventory();
		}
	}
	
	public void appendFileResolver(IFileResolver fileResolver) {
		if(this.fileResolver instanceof OverloadingFileResolver) {
			((OverloadingFileResolver) this.fileResolver).append(fileResolver);
		} else {
			this.fileResolver = new OverloadingFileResolver().append(this.fileResolver).append(fileResolver);
		}
	}
	
	public void setFileResolver(IFileResolver fileResolver) {
		this.fileResolver = fileResolver;
	}
	
	public IFileResolver getFileResolver() {
		return fileResolver;
	}
	
	public static FlashyGdxEngine get() {
		return (FlashyGdxEngine) FlashyEngine.get();
	}
}