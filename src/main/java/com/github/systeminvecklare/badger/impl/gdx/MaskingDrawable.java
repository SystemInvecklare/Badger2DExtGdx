package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawable;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.core.widget.IRectangle;

public class MaskingDrawable implements IDrawable {
	private final IRectangle rectangle;
	private final IDrawable wrapped;
	private final Rectangle scissor = new Rectangle();
	
	public MaskingDrawable(IRectangle rectangle, IDrawable wrapped) {
		this.rectangle = rectangle;
		this.wrapped = wrapped;
	}

	@Override
	public void draw(IDrawCycle drawCycle) {
		ITransform transform = drawCycle.getTransform();
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			Position a = ep.obtain(Position.class).setTo(rectangle.getX(), rectangle.getY());
			Position b = ep.obtain(Position.class).setTo(rectangle.getX()+rectangle.getWidth(), rectangle.getY());
			Position c = ep.obtain(Position.class).setTo(rectangle.getX()+rectangle.getWidth(), rectangle.getY()+rectangle.getHeight());
			Position d = ep.obtain(Position.class).setTo(rectangle.getX(), rectangle.getY()+rectangle.getHeight());
			transform.transform(a);
			transform.transform(b);
			transform.transform(c);
			transform.transform(d);
			float minX = min4(a.getX(), b.getX(), c.getX(), d.getX());
			float minY = min4(a.getY(), b.getY(), c.getY(), d.getY());
			float maxX = max4(a.getX(), b.getX(), c.getX(), d.getX());
			float maxY = max4(a.getY(), b.getY(), c.getY(), d.getY());
			scissor.set(minX, minY, maxX - minX, maxY - minY);
		} finally {
			ep.freeAllAndSelf();
		}
		GdxDrawCycle gdxDrawCycle = (GdxDrawCycle) drawCycle;
		gdxDrawCycle.getSpriteBatch().flush();
		boolean pushed = ScissorStack.pushScissors(scissor);
		if(pushed) {
			wrapped.draw(drawCycle);
			gdxDrawCycle.getSpriteBatch().flush();
			ScissorStack.popScissors();
		}
	}
	
	private static float max4(float a, float b, float c, float d) {
		return Math.max(Math.max(a, b), Math.max(c, d));
	}
	
	private static float min4(float a, float b, float c, float d) {
		return Math.min(Math.min(a, b), Math.min(c, d));
	}
}
