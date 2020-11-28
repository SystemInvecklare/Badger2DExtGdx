package com.github.systeminvecklare.badger.impl.gdx.gameloop;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.IIntSource;
import com.github.systeminvecklare.badger.core.graphics.components.scene.IScene;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.ApplicationContext;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IApplicationContext;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.ISceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.FlashyInputHandler;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IInputHandler;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.FlashyInputProcessor;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;

public abstract class AbstractGdxGameApplicationAdapter extends ApplicationAdapter implements ISceneManager {
	private IScene currentScene;
	private Collection<IScene> trashCan = new ArrayList<IScene>();
	private ApplicationContext applicationContext;
	private GdxDrawCycle drawCycle;
	private final float step;
	
	private int startWidth;
	private int startHeight;
	
	private IGameLoop gameLoop;
	private IInputHandler inputHandler;
	
	public AbstractGdxGameApplicationAdapter(FlashyGdxEngine flashyGdxEngine) {
		this(flashyGdxEngine ,60);
	}
	
	public AbstractGdxGameApplicationAdapter(FlashyGdxEngine flashyGdxEngine, int stepsPerSeconds) {
		FlashyEngine.set(flashyGdxEngine);
		this.applicationContext = new ApplicationContext();
		SceneManager.set(this);
		this.step = 1f/stepsPerSeconds; //Use SceneManager.get().getStep() to reach
	}
	
	@Override
	public void create () {
		resume();
		this.startWidth = Gdx.graphics.getWidth();
		this.startHeight = Gdx.graphics.getHeight();
		
		this.applicationContext.init();
		
		this.inputHandler = new FlashyInputHandler(new IIntSource() {
			@Override
			public int getFromSource() {
				return Gdx.graphics.getHeight();
			}
		});
		
		this.currentScene = getInitialScene();
		
		this.gameLoop = new GameLoop(inputHandler, applicationContext, new GdxGameLoopHooks()) {
			
			@Override
			protected IScene getCurrentScene() {
				return currentScene;
			}
			
			@Override
			protected IDrawCycle newDrawCycle() {
				return drawCycle.reset();
			}
			
			@Override
			protected void closeDrawCycle() {
				drawCycle.getSpriteBatch().end();
			}
		};
		
		this.currentScene.init();
		
		Gdx.input.setInputProcessor(new FlashyInputProcessor(inputHandler));
	}

	protected abstract IScene getInitialScene();

	@Override
	public void render () {
		gameLoop.execute(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void skipQueuedUpdates() {
		gameLoop.skipQueuedUpdates();
	}
	
	@Override
	public void changeScene(IScene newScene) {
		this.currentScene = newScene;
	}
	
	@Override
	public void emptyTrashCan() {
		synchronized (trashCan) {
			for(IScene doomedScene : trashCan)
			{
				doomedScene.dispose();
			}
			trashCan.clear();
		}
	}
	
	@Override
	public float getHeight() {
		return 480f;
	}
	
	@Override
	public float getWidth() {
		return startWidth*this.getHeight()/startHeight;
	}
	
	
	@Override
	public void sendToTrashCan(IScene sceneToBeDisposed) {
		synchronized (trashCan) {
			trashCan.add(sceneToBeDisposed);
		}
	}
	
	@Override
	public void resume() {
		// Android app regained focus
		super.resume();
		FlashyGdxEngine.get().reloadStoreInventories();
		this.drawCycle  = new GdxDrawCycle();
	}
	
	@Override
	public void pause() {
		super.pause();
		// Android app lost focus
		FlashyGdxEngine.get().disposeStoreInventories();
	}
	
	@Override
	public void dispose() {
		this.applicationContext.dispose(); //Do this?
		this.applicationContext = null;
		super.dispose();
	}
	
	@Override
	public float getStep() {
		return step;
	}
	
	@Override
	public IApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
