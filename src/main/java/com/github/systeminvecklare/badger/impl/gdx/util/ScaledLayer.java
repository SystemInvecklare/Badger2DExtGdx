package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.Gdx;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.layer.Layer;
import com.github.systeminvecklare.badger.core.graphics.components.transform.IReadableTransform;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;

public class ScaledLayer extends Layer {
	private ITransform transform;
	
	@Override
	public IReadableTransform getTransform() {
		transform./*setToIdentity().*/setScale(Gdx.graphics.getWidth()/SceneManager.get().getWidth(), Gdx.graphics.getHeight()/SceneManager.get().getHeight());
		return transform;
	}
	
	@Override
	public void init() {
		this.transform = FlashyEngine.get().getPoolManager().getPool(ITransform.class).obtain().setToIdentity();
		super.init();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if(transform != null)
		{
			transform.free();
			transform = null;
		}
	}
}
