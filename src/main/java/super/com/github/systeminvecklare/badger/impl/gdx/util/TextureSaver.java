package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class TextureSaver {
	public static void saveTexture(FileHandle file, Texture texture) {
	    Gdx.app.error("TextureSaver", "void saveTexture(FileHandle file, Texture texture) not supported on web!");
	}
}
