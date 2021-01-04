package com.github.systeminvecklare.badger.impl.gdx.experimental;

import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayer;
import com.github.systeminvecklare.badger.core.graphics.components.layer.ILayerVisitor;
import com.github.systeminvecklare.badger.core.graphics.components.scene.IScene;
import com.github.systeminvecklare.badger.core.graphics.components.transform.IReadableTransform;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.framework.engine.SceneManager;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.impl.gdx.util.ScaledLayer;

public class CameraLayer extends ScaledLayer {
	private Position cameraPos;
	
	@Override
	public void init() {
		cameraPos = FlashyEngine.get().getPoolManager().getPool(Position.class).obtain();
		super.init();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		cameraPos.free();
		cameraPos = null;
	}
	
	@Override
	public IReadableTransform getTransform() {
		ITransform transform = (ITransform) super.getTransform();
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			transform.setPosition(0, 0);
			ITransform temp = ep.obtain(ITransform.class).setToIdentity().setPosition(cameraPos).addToPosition(SceneManager.get().getWidth()/2, SceneManager.get().getHeight()/2);
			temp.multLeft(transform);
			return transform.setTo(temp);
		} finally {
			ep.freeAllAndSelf();
		}
	}
	
	public void setCameraPosition(IReadablePosition position) {
		cameraPos.setTo(-position.getX(), -position.getY());
	}

	public static void updateCameralayers(IScene scene, IReadablePosition newCameraPos) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			final IReadablePosition data = newCameraPos.copy(ep);
			scene.visitLayers(new ILayerVisitor() {
				@Override
				public void visit(ILayer layer) {
					if(layer instanceof CameraLayer) {
						((CameraLayer) layer).setCameraPosition(data);
					}
				}
			});
		} finally {
			ep.freeAllAndSelf();
		}
	}
}
