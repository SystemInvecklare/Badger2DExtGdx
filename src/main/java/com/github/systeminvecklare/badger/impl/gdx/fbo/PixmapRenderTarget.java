package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.github.systeminvecklare.badger.core.widget.IRectangle;

/*package-protected*/ class PixmapRenderTarget implements IFboDefinition {
	private final IRectangle rectangle;
	
	public PixmapRenderTarget(IRectangle rectangle) {
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
