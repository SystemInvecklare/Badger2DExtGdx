package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

public interface IFboHandle {
	boolean drawCached(IDrawCycle drawCycle);
	void done();
	boolean failed();
	void setDirty();
	void setRectangleDirty();
	void setTint(Color tint);
}
