package com.github.systeminvecklare.badger.impl.gdx;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundStore {
	private static Map<String, Sound> sounds = new HashMap<String, Sound>();

	public static Sound getSound(String soundName) {
		Sound sound = sounds.get(soundName);
		if(sound == null)
		{
			sound = loadSound(soundName);
		}
		return sound;
	}
	
	private static Sound loadSound(String soundName)
	{
		Sound sound = Gdx.audio.newSound(Gdx.files.internal(soundName));
		sounds.put(soundName, sound);
		return sound;
	}
	
	public static void reloadSounds()
	{
		for(String soundName : sounds.keySet())
		{
			Sound current = sounds.get(soundName);
			if(current != null)
			{
				current.dispose();
			}
			sounds.put(soundName, loadSound(soundName));
		}
	}

	public static void disposeSounds() {
		for(String soundName : sounds.keySet())
		{
			Sound current = sounds.get(soundName);
			if(current != null)
			{
				current.dispose();
			}
			sounds.put(soundName, null);
		}
	}
}
