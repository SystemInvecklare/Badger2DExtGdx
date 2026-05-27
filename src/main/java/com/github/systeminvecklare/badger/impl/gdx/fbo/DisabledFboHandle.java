package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

public class DisabledFboHandle implements IFboHandle {
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
	public void setRectangleDirty() {
	}

	@Override
	public void setTint(Color tint) {
	}
}
