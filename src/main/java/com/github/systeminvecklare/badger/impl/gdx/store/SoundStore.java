package com.github.systeminvecklare.badger.impl.gdx.store;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.file.FileTypes;

public class SoundStore {
	private static AbstractStore<String, Sound> soundStore = new AbstractStore<String, Sound>() {
		@Override
		protected Sound loadItem(String itemName) {
			return Gdx.audio.newSound(FlashyGdxEngine.get().getFileResolver().resolve(FileTypes.AUDIO, itemName));
		}

		@Override
		protected void disposeItem(Sound item) {
			item.dispose();
		}
	};
	static {
		FlashyGdxEngine.get().registerStore(soundStore);
	}
	
	public static Sound getSound(String soundName) {
		return soundStore.getItem(soundName);
	}
}
