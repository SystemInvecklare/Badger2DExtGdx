package com.github.systeminvecklare.badger.impl.gdx.audio;

import com.badlogic.gdx.audio.Sound;
import com.github.systeminvecklare.badger.impl.gdx.SoundStore;

public class NonThreadedFlashySoundDelegate implements IFlashySoundDelegate {
	private String soundName;
	private FlashySound wrapper;

	public NonThreadedFlashySoundDelegate(FlashySound wrapper, String soundName) {
		this.wrapper = wrapper;
		this.soundName = soundName;
	}
	
	@Override
	public FlashySound getWrapper() {
		return wrapper;
	}
	
	private Sound getReal()
	{
		return SoundStore.getSound(soundName);
	}

	@Override
	public long play() {
		return getReal().play();
	}

	@Override
	public long play(float volume) {
		return getReal().play(volume);
	}

	@Override
	public long play(float volume, float pitch, float pan) {
		return getReal().play(volume, pitch, pan);
	}

	@Override
	public long loop() {
		return getReal().loop();
	}

	@Override
	public long loop(float volume) {
		return getReal().loop(volume);
	}

	@Override
	public long loop(float volume, float pitch, float pan) {
		return getReal().loop(volume, pitch, pan);
	}

	@Override
	public void stop() {
		getReal().stop();
	}

	@Override
	public void pause() {
		getReal().pause();
	}

	@Override
	public void resume() {
		getReal().resume();
	}

	@Override
	public void dispose() {
		//This is done in the SoundStore.
	}

	@Override
	public void stop(long soundId) {
		getReal().stop(soundId);
	}

	@Override
	public void pause(long soundId) {
		getReal().pause(soundId);
	}

	@Override
	public void resume(long soundId) {
		getReal().resume(soundId);
	}

	@Override
	public void setLooping(long soundId, boolean looping) {
		getReal().setLooping(soundId, looping);
	}

	@Override
	public void setPitch(long soundId, float pitch) {
		getReal().setPitch(soundId, pitch);
	}

	@Override
	public void setVolume(long soundId, float volume) {
		getReal().setVolume(soundId, volume);
	}

	@Override
	public void setPan(long soundId, float pan, float volume) {
		getReal().setPan(soundId, pan, volume);
	}
}
