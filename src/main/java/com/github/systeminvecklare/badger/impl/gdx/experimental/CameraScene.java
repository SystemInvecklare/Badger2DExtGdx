package com.github.systeminvecklare.badger.impl.gdx.experimental;

import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.core.ITic;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayer;
import com.github.systeminvecklare.badger.core.graphics.components.movieclip.IMovieClip;
import com.github.systeminvecklare.badger.core.graphics.components.scene.Scene;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.components.transform.NonInvertibleMatrixException;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.core.pooling.IPoolManager;

public class CameraScene extends Scene {
	private Position cameraPos;
	private IMovieClip target = null;
	private Position trackPos;
	
	@Override
	public void init() {
		IPoolManager pm = FlashyEngine.get().getPoolManager();
		cameraPos = pm.getPool(Position.class).obtain().setToOrigin();
		trackPos = pm.getPool(Position.class).obtain().setToOrigin();
		super.init();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		cameraPos.free();
		cameraPos = null;
		trackPos.free();
		trackPos = null;
	}
	
	/**
	 * Don't override. Override {@code normalThink(ITic)} instead.
	 */
	@Override
	public void think(ITic tic) {
		normalThink(tic);
		updateTrackPosition();
		updateCameraPos(trackPos, cameraPos);
		updateCameraLayers(cameraPos);
	}
	
	protected void updateTrackPosition() {
		if(target != null) {
			ILayer layer = target.getLayer();
			if(layer != null) {
				EasyPooler ep = EasyPooler.obtainFresh();
				try {
					trackPos.setTo(layer.toLocalTransform(target.toGlobalTransform(ep.obtain(ITransform.class).setToIdentity())).getPosition());
				} catch (NonInvertibleMatrixException e) {
					// Non-invertible -> don't update trackPos
					e.printStackTrace();
				} finally {
					ep.freeAllAndSelf();
				}
			}
		}
	}
	
	protected void updateCameraPos(IReadablePosition trackPosition, Position cameraPos) {
		cameraPos.setTo((cameraPos.getX()*9f+trackPosition.getX())/10f, (cameraPos.getY()*9f+trackPosition.getY())/10f);
	}

	protected void updateCameraLayers(IReadablePosition cameraPos) {
		CameraLayer.updateCameralayers(this, cameraPos);
	}

	protected void normalThink(ITic tic) {
		super.think(tic);
	}
	
	public void setCameraTarget(IMovieClip target) {
		//TODO here we could add a special behavior to the target.
		//     This behavior would be used to listen for when the target is disposed --> set target to null!
		//     And if a new camera target is set before, then remove behavior etc...
		this.target = target;
	}
}
