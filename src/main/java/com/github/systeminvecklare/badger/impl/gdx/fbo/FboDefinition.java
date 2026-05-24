package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.github.systeminvecklare.badger.core.widget.IRectangle;

public class FboDefinition implements IFboDefinition {
	private IRectangle rectangle;

	public FboDefinition(IRectangle rectangle) {
		if(rectangle == null) {
			throw new RuntimeException("rectangle is null");
		}
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
}
