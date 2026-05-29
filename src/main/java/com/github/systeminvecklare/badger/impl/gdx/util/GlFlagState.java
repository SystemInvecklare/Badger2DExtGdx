package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.Gdx;

public class GlFlagState {
	private final int flagEnum;
	private boolean storedState;
	private boolean changed = false;

	public GlFlagState(int flagEnum) {
		this.flagEnum = flagEnum;
	}
	
	public void storeState() {
		storedState = Gdx.gl.glIsEnabled(flagEnum);
		changed = false;
	}

	public void setEnabled(boolean enabled) {
		if(enabled != storedState) {
			changed = true;
			if(enabled) {
				Gdx.gl.glEnable(flagEnum);
			} else {
				Gdx.gl.glDisable(flagEnum);
			}
		}
	}
	
	public void restoreState() {
		if(changed) {
			if(storedState) {
				Gdx.gl.glEnable(flagEnum);
			} else {
				Gdx.gl.glDisable(flagEnum);
			}
		}
	}
}
