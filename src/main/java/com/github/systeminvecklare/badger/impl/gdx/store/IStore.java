package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.List;

public interface IStore {
	void reloadInventory();
	void disposeInventory();
	List<IStore> getDependencies(List<IStore> result);
}
