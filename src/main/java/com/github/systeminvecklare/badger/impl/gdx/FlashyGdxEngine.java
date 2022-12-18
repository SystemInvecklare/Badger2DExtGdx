package com.github.systeminvecklare.badger.impl.gdx;

import java.util.ArrayList;
import java.util.List;

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
import com.github.systeminvecklare.badger.impl.gdx.store.IStore;

public class FlashyGdxEngine implements IFlashyEngine {
	private FlashyPoolManager poolManager;
	private List<IStore> stores = new ArrayList<IStore>();
	
	public FlashyGdxEngine() {
		this.poolManager = new FlashyPoolManager()
		{
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
	}

	@Override
	public IPoolManager getPoolManager() {
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
		stores.add(store);
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
	
	public static FlashyGdxEngine get() {
		return (FlashyGdxEngine) FlashyEngine.get();
	}
}