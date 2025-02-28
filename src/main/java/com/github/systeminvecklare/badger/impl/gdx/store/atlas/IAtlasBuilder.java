package com.github.systeminvecklare.badger.impl.gdx.store.atlas;

import com.badlogic.gdx.graphics.Texture.TextureFilter;

public interface IAtlasBuilder {
	void build(IAtlasConstruction construction);
	int getAltasWidth();
	int getAltasHeight();
	TextureFilter getMinFilter();
	TextureFilter getMagFilter();
	boolean contains(String texture);
}
