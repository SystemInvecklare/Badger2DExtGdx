package com.github.systeminvecklare.badger.impl.gdx.audio;

import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;

public class FlashySound implements IFlashySound {
	private IFlashySoundDelegate delegate;

	public FlashySound(String soundName) {
		this.delegate = FlashyGdxEngine.get().newFlashySoundDelegate(this,soundName);
	}

	/**
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#play()
	 */
	public long play() {
		return delegate.play();
	}

	/**
	 * @param volume
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#play(float)
	 */
	public long play(float volume) {
		return delegate.play(volume);
	}

	/**
	 * @param volume
	 * @param pitch
	 * @param pan
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#play(float, float, float)
	 */
	public long play(float volume, float pitch, float pan) {
		return delegate.play(volume, pitch, pan);
	}

	/**
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#loop()
	 */
	public long loop() {
		return delegate.loop();
	}

	/**
	 * @param volume
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#loop(float)
	 */
	public long loop(float volume) {
		return delegate.loop(volume);
	}

	/**
	 * @param volume
	 * @param pitch
	 * @param pan
	 * @return
	 * @see com.badlogic.gdx.audio.Sound#loop(float, float, float)
	 */
	public long loop(float volume, float pitch, float pan) {
		return delegate.loop(volume, pitch, pan);
	}

	/**
	 * 
	 * @see com.badlogic.gdx.audio.Sound#stop()
	 */
	public void stop() {
		delegate.stop();
	}

	/**
	 * 
	 * @see com.badlogic.gdx.audio.Sound#pause()
	 */
	public void pause() {
		delegate.pause();
	}

	/**
	 * 
	 * @see com.badlogic.gdx.audio.Sound#resume()
	 */
	public void resume() {
		delegate.resume();
	}

	/**
	 * 
	 * @see com.badlogic.gdx.audio.Sound#dispose()
	 */
	public void dispose() {
		delegate.dispose();
	}

	/**
	 * @param soundId
	 * @see com.badlogic.gdx.audio.Sound#stop(long)
	 */
	public void stop(long soundId) {
		delegate.stop(soundId);
	}

	/**
	 * @param soundId
	 * @see com.badlogic.gdx.audio.Sound#pause(long)
	 */
	public void pause(long soundId) {
		delegate.pause(soundId);
	}

	/**
	 * @param soundId
	 * @see com.badlogic.gdx.audio.Sound#resume(long)
	 */
	public void resume(long soundId) {
		delegate.resume(soundId);
	}

	/**
	 * @param soundId
	 * @param looping
	 * @see com.badlogic.gdx.audio.Sound#setLooping(long, boolean)
	 */
	public void setLooping(long soundId, boolean looping) {
		delegate.setLooping(soundId, looping);
	}

	/**
	 * @param soundId
	 * @param pitch
	 * @see com.badlogic.gdx.audio.Sound#setPitch(long, float)
	 */
	public void setPitch(long soundId, float pitch) {
		delegate.setPitch(soundId, pitch);
	}

	/**
	 * @param soundId
	 * @param volume
	 * @see com.badlogic.gdx.audio.Sound#setVolume(long, float)
	 */
	public void setVolume(long soundId, float volume) {
		delegate.setVolume(soundId, volume);
	}

	/**
	 * @param soundId
	 * @param pan
	 * @param volume
	 * @see com.badlogic.gdx.audio.Sound#setPan(long, float, float)
	 */
	public void setPan(long soundId, float pan, float volume) {
		delegate.setPan(soundId, pan, volume);
	}
}
