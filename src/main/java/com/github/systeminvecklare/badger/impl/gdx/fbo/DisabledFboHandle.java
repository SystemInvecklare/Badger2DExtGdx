package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

public class DisabledFboHandle implements IFboHandle {
	public static final IFboHandle INSTANCE = new DisabledFboHandle();

	@Override
	public boolean drawCached(IDrawCycle drawCycle) {
		return true; // Always draw
	}

	@Override
	public void done() {
	}

	@Override
	public void setDirty() {
	}
	
	@Override
	public boolean failed() {
		return false;
	}

	@Override
	public void setRectangleDirty() {
	}

	@Override
	public void setTint(Color tint) {
	}
}
