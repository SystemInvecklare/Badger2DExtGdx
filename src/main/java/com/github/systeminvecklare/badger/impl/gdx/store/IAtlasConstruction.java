package com.github.systeminvecklare.badger.impl.gdx.store;

import com.badlogic.gdx.graphics.Pixmap;

public interface IAtlasConstruction {
	void add(String name, Pixmap pixmap, int x, int y, int padding);
}
