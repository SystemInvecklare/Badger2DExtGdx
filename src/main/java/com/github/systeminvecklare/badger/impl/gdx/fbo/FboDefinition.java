package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.github.systeminvecklare.badger.core.widget.IRectangle;

public class FboDefinition implements IFboDefinition {
	private IRectangle rectangle;
	private String DEBUG_Name; //TODO remove

	public FboDefinition(IRectangle rectangle, String debugName) {
		if(rectangle == null) {
			throw new RuntimeException("rectangle is null");
		}
		this.rectangle = rectangle;
		this.DEBUG_Name = debugName;
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

	@Override
	public String DEBUG_name() {
		return DEBUG_Name;
	}
}
