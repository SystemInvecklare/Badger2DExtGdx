package com.github.systeminvecklare.badger.impl.gdx.font;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.ISource;
import com.github.systeminvecklare.badger.core.graphics.components.moviecliplayer.IMovieClipLayer;
import com.github.systeminvecklare.badger.core.graphics.components.util.ConstantSource;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;

public class TextGraphics implements IMovieClipLayer {
	private String chachedText;
	private Color color;
	private IFlashyFont font;
	private boolean hittable = false;
	private int offsetX;
	private int offsetY;
	private float anchorX = 0f;
	private float anchorY = 0f;
	private float width;
	private float height;
	private ISource<String> textSource;
	
	public TextGraphics(IFlashyFont font, String text) {
		this(font, text, Color.BLACK);
	}
	
	public TextGraphics(IFlashyFont font, String text, Color color) {
		this(font, new ConstantSource<String>(text), color);
	}
	
	public TextGraphics(IFlashyFont font, ISource<String> textSource) {
		this(font, textSource, Color.BLACK);
	}
	
	public TextGraphics(IFlashyFont font, ISource<String> textSource, Color color) {
		this.font = font; 
		this.textSource = textSource;
		this.color = color;
		this.chachedText = textSource.getFromSource();
		updateDimensions();
	}

	private void updateDimensions() {
		width = font.getWidth(chachedText);
		height = font.getHeight(chachedText);
	}
	
	public TextGraphics makeHittable()
	{
		hittable = true;
		return this;
	}
	
	@Override
	public void draw(IDrawCycle drawCycle) {
		
		String currentText = textSource.getFromSource();
		if(currentText == null)
		{
			width = 0f;
			height= 0f;
			return;
		}
		else
		{
			if(!currentText.equals(chachedText))
			{
				chachedText = currentText;
				updateDimensions();
			}
		}
		
		font.draw(drawCycle, chachedText,  -getTotalOffsetX(), -getTotalOffsetY(), color);
	}
	
	public TextGraphics setCenter(double x, double y)
	{
		this.offsetX = (int) x;
		this.offsetY = (int) y;
		return this;
	}
	
	
	public TextGraphics setCenter(double xAndY)
	{
		return setCenter(xAndY, xAndY);
	}
	
	private int getTotalOffsetX()
	{
		return (int) ((offsetX+anchorX*width)*FlashyFont.getOversample());
	}
	
	private int getTotalOffsetY()
	{
		return (int) ((offsetY-anchorY*height)*FlashyFont.getOversample());
	}

	@Override
	public boolean hitTest(IReadablePosition p) {
		if(hittable)
		{
			double px = p.getX()+getTotalOffsetX();
			double py = p.getY()+getTotalOffsetY();
			return px >= 0 && py <= 0 && py > -font.getHeight(chachedText) && px < font.getWidth(chachedText);
		}
		else
		{
			return false;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
		font = null;
		chachedText = null;
		color = null;
		textSource = null;
	}

	public TextGraphics setAnchors(float anchorX, float anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		return this;
	}

}
