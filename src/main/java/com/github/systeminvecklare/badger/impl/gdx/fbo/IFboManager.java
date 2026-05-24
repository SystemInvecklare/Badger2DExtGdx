package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

public interface IFboManager {
	boolean drawCached(IDrawCycle drawCycle, IFboDefinition definition);
	void done();
	void setDirty(IFboDefinition definition);
	void setRectangleDirty(IFboDefinition definition);
	void setTint(IFboDefinition definition, Color tint);
}