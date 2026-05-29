package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;

/**
 * Usage:
 * <br>
 * <pre>
 * <code>
 * if(fboManager.drawCached(drawCycle, fboDef)) {
 *   // Normal drawing here
 *   fboManager.done(); // Required
 * }
 * </code>
 * </pre>
 * @author Mattias Selin
 */
public interface IFboManager {
	boolean drawCached(IDrawCycle drawCycle, IFboDefinition definition);
	void done();
	/**
	 * Important: Maybe ONLY be called inside "drawCached->done" block
	 * <pre><code>
	 * if(fboManager.drawCached(...)) {
	 *   &#47;* here *&#47;
	 *   fboManager.done()
	 * }
	 * </code></pre>
	 * @return true if current draw failed to construct FBO and is currently drawing as normal.
	 */
	boolean failed();
	void setDirty(IFboDefinition definition);
	void setRectangleDirty(IFboDefinition definition);
	void setTint(IFboDefinition definition, Color tint);
}