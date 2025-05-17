package com.github.systeminvecklare.badger.impl.gdx;

import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawable;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.graphics.components.transform.NonInvertibleMatrixException;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.core.util.GeometryUtil;
import com.github.systeminvecklare.badger.core.widget.RectangleUtil;
import com.github.systeminvecklare.badger.core.widget.WidgetClip;

public class MaskingWidgetClip extends WidgetClip {
	private final MaskingDrawable masker;

	public MaskingWidgetClip(int width, int height) {
		super(width, height);
		this.masker = new MaskingDrawable(RectangleUtil.justSize(this), new IDrawable() {
			@Override
			public void draw(IDrawCycle drawCycle) {
				drawWithoutTransformMasked(drawCycle);
			}
		});
	}
	
	@Override
	public final boolean hitTest(IReadablePosition p) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try {
			ITransform transform = getTransform(ep.obtain(ITransform.class));
			try {
				transform = transform.invert();
			} catch(NonInvertibleMatrixException e) {
				return false;
			}
			Position transformedPosition = ep.obtain(Position.class);
			transform.transform(transformedPosition.setTo(p));
			
			if(!GeometryUtil.isInRectangle(transformedPosition.getX(), transformedPosition.getY(), 0, 0, this.getWidth(), this.getHeight())) {
				return false;
			}
		} finally {
			ep.freeAllAndSelf();
		}
		return hitTestMasked(p);
	}
	

	@Override
	public final void drawWithoutTransform(IDrawCycle drawCycle) {
		masker.draw(drawCycle);
	}
	
	public void drawWithoutTransformMasked(IDrawCycle drawCycle) {
		super.drawWithoutTransform(drawCycle);
	}
	
	public boolean hitTestMasked(IReadablePosition p) {
		return super.hitTest(p);
	}
}
