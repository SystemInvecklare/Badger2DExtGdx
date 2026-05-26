package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.widget.IRectangle;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;

public class SimpleFboDefinition implements IFboDefinition {
	private final IFboManager fboManager;
	private IRectangle rectangle;
	
	public SimpleFboDefinition(IRectangle rectangle) {
		this(FlashyGdxEngine.get().getFboManager(), rectangle);
	}
	
	public SimpleFboDefinition(IFboManager fboManager,  IRectangle rectangle) {
		if(fboManager == null) {
			throw new RuntimeException("fboManager is null");
		}
		if(rectangle == null) {
			throw new RuntimeException("rectangle is null");
		}
		this.fboManager = fboManager;
		this.rectangle = rectangle;
	}
	
	public void setRectangle(IRectangle rectangle) {
		if(rectangle == null) {
			throw new RuntimeException("rectangle is null");
		}
		this.rectangle = rectangle;
	}

	@Override
	public int getX() {
		return rectangle.getX();
	}

	@Override
	public int getY() {
		return rectangle.getY();
	}

	@Override
	public int getWidth() {
		return rectangle.getWidth();
	}

	@Override
	public int getHeight() {
		return rectangle.getHeight();
	}
	
	public void setDirty() {
		fboManager.setDirty(this);
	}
	
	public void setRectangleDirty() {
		fboManager.setRectangleDirty(this);
	}

	public boolean drawCached(IDrawCycle drawCycle) {
		return fboManager.drawCached(drawCycle, this);
	}

	public void done() {
		fboManager.done();
	}
}
