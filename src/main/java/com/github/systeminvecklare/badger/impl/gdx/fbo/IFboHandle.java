package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

/**
 * Usage:
 * <br>
 * <pre>
 * <code>
 * if(fboHandle.drawCached(drawCycle)) {
 *   // Normal drawing here
 *   fboHandle.done(); // Required
 * }
 * </code>
 * </pre>
 * @author Mattias Selin
 */
public interface IFboHandle {
	boolean drawCached(IDrawCycle drawCycle);
	void done();
	boolean failed();
	void setDirty();
	void setRectangleDirty();
	void setTint(Color tint);
}
