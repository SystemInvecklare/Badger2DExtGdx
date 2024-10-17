package com.github.systeminvecklare.badger.impl.gdx.store;

public interface IAtlasBuilder {
	void build(IAtlasConstruction construction);
	int getAltasWidth();
	int getAltasHeight();
	boolean contains(String texture);
}
