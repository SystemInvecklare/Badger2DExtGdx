package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.GdxVectorDrawer;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.IGdxVectorDrawer;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;

public class GdxDrawCycle implements IDrawCycle {
	private ITransform transform = FlashyEngine.get().getPoolManager().getPool(ITransform.class).obtain().setToIdentity();
	private IShader shader;
	private SpriteBatch spriteBatch = new SpriteBatch();
	private IGdxVectorDrawer vectorDrawer = new GdxVectorDrawer();

	@Override
	public ITransform getTransform() {
		return transform;
	}
	
	public GdxDrawCycle reset()
	{
		transform.setToIdentity();//.setPosition(new Position(null).setTo(0, SceneManager.get().getHeightSource().getFromSource())).setScale(new Vector(null).setTo(1, -1));
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
		shader = null;
		spriteBatch.setShader(null);
		return this;
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}
	
	@Override
	public void setShader(IShader shader) {
		this.shader = shader;
	}

	public void updateSpriteBatchTransform() {
		spriteBatch.setTransformMatrix(((GdxTransform) transform).getMatrix4());
		if(shader != null)
		{
			shader.onBind(this);
		}
		else
		{
			spriteBatch.setShader(null);
		}
	}

	public IGdxVectorDrawer updateAndGetVectorDrawer() {
		vectorDrawer.updateTransform(((GdxTransform) transform).getMatrix4());
		return vectorDrawer;
	}
}
