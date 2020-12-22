package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.HashMap;
import java.util.Map;

/*package-protected*/ abstract class AbstractStore<K, T> implements IStore {
	private Map<K, T> items = new HashMap<K, T>();
	
	public T getItem(K itemName) {
		T item = items.get(itemName);
		if(item == null) {
			item = loadItem(itemName);
			items.put(itemName, item);
		}
		return item;
	}
	
	protected abstract T loadItem(K itemName);

	protected abstract void disposeItem(T item);

	@Override
	public void reloadInventory() {
		for(K itemName : items.keySet()) {
			T item = items.get(itemName);
			if(item != null) {
				disposeItem(item);
			}
			items.put(itemName, loadItem(itemName));
		}
	}

	@Override
	public void disposeInventory() {
		for(K itemName : items.keySet()) {
			T item = items.get(itemName);
			if(item != null) {
				disposeItem(item);
			}
			items.put(itemName, null);
		}
	}
}
