package com.github.systeminvecklare.badger.impl.gdx.load;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.ITic;
import com.github.systeminvecklare.badger.core.load.ILoadManager;
import com.github.systeminvecklare.badger.core.widget.WidgetClip;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.ITexture;
import com.github.systeminvecklare.badger.impl.gdx.store.TextureStore;

public class LoadingBar extends WidgetClip {
	private final ILoadManager loadManager;
	private final String textureName;
	private float progress = 0f;
	
	public LoadingBar(String textureName, ILoadManager loadManager) {
		super(0,0);
		this.textureName = textureName;
		this.loadManager = loadManager;
		ITexture texture = TextureStore.getTexture(textureName);
		setSize(texture.getWidth(), texture.getHeight()/2);
	}
	
	@Override
	public void think(ITic tic) {
		this.progress = Math.min(1f, loadManager.getCompletedWork() / loadManager.getTotalWork());
		super.think(tic);
	}
	
	@Override
	public void drawWithoutTransform(IDrawCycle drawCycle) {
		super.drawWithoutTransform(drawCycle);
		GdxDrawCycle gdxDrawCycle = ((GdxDrawCycle) drawCycle);
		gdxDrawCycle.updateSpriteBatchTransform();
		SpriteBatch spriteBatch = gdxDrawCycle.getSpriteBatch();
		spriteBatch.setColor(Color.WHITE);
		ITexture texture = TextureStore.getTexture(textureName);
		int width = texture.getWidth();
		int height = texture.getHeight()/2;
		texture.draw(spriteBatch, 0, 0, width, height, 0, 0, width, height, false, false);
		int progressWidth = (int) (width*progress);
		texture.draw(spriteBatch, 0, 0, progressWidth, height, 0, height, progressWidth, height, false, false);
	}
}
