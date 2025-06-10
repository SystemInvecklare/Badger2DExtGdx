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
import com.github.systeminvecklare.badger.core.graphics.framework.engine.IPixelTranslator;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.ISceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.GameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoop;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.gameloop.IGameLoopHooks;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.FlashyInputHandler;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.HeightSourcePixelTranslator;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IInputHandler;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.inputprocessor.IWindowCanvas;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.FlashyInputProcessor;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;

public abstract class AbstractGdxGameApplicationAdapter extends ApplicationAdapter implements ISceneManager {
	private IScene currentScene;
	private Collection<IScene> trashCan = new ArrayList<IScene>();
	private ApplicationContext applicationContext;
	private GdxDrawCycle drawCycle;
	private final float step;
	private final IWindowCanvas windowCanvas = new GdxWindowCanvas();
	
	protected boolean maintainScaledAspectRatio = false;
	
	private int startWidth;
	private int startHeight;
	
	private IGameLoop gameLoop;
	private IInputHandler inputHandler;
	private IPixelTranslator pixelTranslator;
	
	public AbstractGdxGameApplicationAdapter(FlashyGdxEngine flashyGdxEngine) {
		this(flashyGdxEngine ,60);
	}
	
	public AbstractGdxGameApplicationAdapter(FlashyGdxEngine flashyGdxEngine, int stepsPerSeconds) {
		FlashyEngine.set(flashyGdxEngine);
		this.applicationContext = new ApplicationContext();
		SceneManager.set(this);
		this.step = 1f/stepsPerSeconds; //Use SceneManager.get().getStep() to reach
	}
	
	public AbstractGdxGameApplicationAdapter maintainScaledAspectRatio() {
		maintainScaledAspectRatio = true;
		return this;
	}
	
	@Override
	public void create () {
		FlashyGdxEngine.get().initPoolManagerOnThread();
		resume();
		this.startWidth = windowCanvas.getWidth();
		this.startHeight = windowCanvas.getHeight();
		
		this.applicationContext.init();
		
		this.pixelTranslator = newPixelTranslator();
		
		this.inputHandler = newFlashyInputHandler(pixelTranslator, maintainScaledAspectRatio ? windowCanvas : null);
		
		this.currentScene = getInitialScene();
		
		this.gameLoop = new GameLoop(inputHandler, applicationContext, newGameLoopHooks(pixelTranslator, maintainScaledAspectRatio)) {
			
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
	
	protected IPixelTranslator newPixelTranslator() {
		if(maintainScaledAspectRatio) {
			return new IPixelTranslator() {
				@Override
				public Position translate(int x, int y, Position result) {
					ISceneManager sceneManager = SceneManager.get();
					float sceneManagerAr = sceneManager.getWidth()/sceneManager.getHeight();
					float gdxAr = ((float) windowCanvas.getWidth())/windowCanvas.getHeight();
					
					float relativeWidth = gdxAr/sceneManagerAr;
					
					float xInverseScale = 1f;
					float yInverseScale = 1f;
					if(relativeWidth > 1f) {
						xInverseScale = relativeWidth;
					} else if(relativeWidth < 1f) {
						yInverseScale = 1f/relativeWidth;
					}
					
					result.setTo(x*xInverseScale, (windowCanvas.getHeight() - y)*yInverseScale);
					result.add(windowCanvas.getWidth()*(1f - xInverseScale)/2f, windowCanvas.getHeight()*(1f - yInverseScale)/2f);
					return result;
				}
			};
		} else {
			return new HeightSourcePixelTranslator(new IIntSource() {
				@Override
				public int getFromSource() {
					return windowCanvas.getHeight();
				}
			});
		}
	}

	protected IInputHandler newFlashyInputHandler(IPixelTranslator pixelTranslator, IWindowCanvas requireInsideOrNull) {
		return new FlashyInputHandler(pixelTranslator, requireInsideOrNull);
	}
	
	protected IGameLoopHooks newGameLoopHooks(IPixelTranslator pixelTranslator, boolean useLetterBoxing) {
		return new GdxGameLoopHooks(pixelTranslator, useLetterBoxing);
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
	public void changeScene(IScene newScene, boolean initScene) {
		changeScene(newScene);
		if(initScene) {
			newScene.init();
		}
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
	
	private Position getPointer(Position position) {
		if(pixelTranslator != null) {
			return pixelTranslator.translate(Gdx.input.getX(), Gdx.input.getY(), position);
		} else {
			return position.setTo(Gdx.input.getX(), Gdx.input.getY());
		}
	}
	
	@Override
	public float getPointerX() {
		Position position = FlashyEngine.get().getPoolManager().getPool(Position.class).obtain();
		try {
			return getPointer(position).getX();
		} finally {
			position.free();
		}
	}
	
	@Override
	public float getPointerY() {
		Position position = FlashyEngine.get().getPoolManager().getPool(Position.class).obtain();
		try {
			return getPointer(position).getY();
		} finally {
			position.free();
		}
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
