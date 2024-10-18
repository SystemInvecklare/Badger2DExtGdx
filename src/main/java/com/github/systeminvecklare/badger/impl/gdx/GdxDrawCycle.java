package com.github.systeminvecklare.badger.impl.gdx;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.GdxVectorDrawer;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.IGdxVectorDrawer;

public class GdxDrawCycle implements IDrawCycle {
	private ITransform transform = FlashyEngine.get().getPoolManager().getPool(ITransform.class).obtain().setToIdentity();
	private final float[] lastBoundTransform = new float[16];
	private IShader shader;
	private IShader lastBoundShader;
	private SpriteBatch spriteBatch = new SpriteBatch() {
		protected void setupMatrices() {
			if(ShaderProgram.pedantic) {
				ShaderProgram shaderProgram = getShader();
				if(shaderProgram != null) {
					if(!shaderProgram.hasUniform("u_texture")) {
						ShaderProgram.pedantic = false;
						try {
							super.setupMatrices();
							return;
						} finally {
							ShaderProgram.pedantic = true;
						}
					}
				}
			}
			super.setupMatrices();
		}
	};
	private IGdxVectorDrawer vectorDrawer = new GdxVectorDrawer();

	@Override
	public ITransform getTransform() {
		return transform;
	}
	
	public GdxDrawCycle reset()
	{
		transform.setToIdentity();//.setPosition(new Position(null).setTo(0, SceneManager.get().getHeightSource().getFromSource())).setScale(new Vector(null).setTo(1, -1));
		Arrays.fill(lastBoundTransform, 0f);
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
		shader = null;
		lastBoundShader = null;
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
		Matrix4 gdxMatrix = ((GdxTransform) transform).getMatrix4();
		if(!Arrays.equals(lastBoundTransform, gdxMatrix.val)) {
			spriteBatch.setTransformMatrix(gdxMatrix);
			System.arraycopy(gdxMatrix.val, 0, lastBoundTransform, 0, 16);
		}
		if(shader != lastBoundShader) {
			if(shader != null) {
				shader.onBind(this);
			} else {
				spriteBatch.setShader(null);
			}
			lastBoundShader = shader;
		}
	}

	public IGdxVectorDrawer updateAndGetVectorDrawer() {
		vectorDrawer.updateTransform(((GdxTransform) transform).getMatrix4());
		return vectorDrawer;
	}
}
