package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial;

import com.badlogic.gdx.math.Vector2;
import com.github.systeminvecklare.badger.core.graphics.components.transform.IReadableTransform;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;

public class PolynomialFunction2D {
	private PolynomialFunction xFunc;
	private PolynomialFunction yFunc;
	
	public PolynomialFunction2D(PolynomialFunction xFunc, PolynomialFunction yFunc) {
		this.xFunc = xFunc;
		this.yFunc = yFunc;
	}

	public PolynomialFunction2D(float[] xcs, float[] ycs) {
		this(new PolynomialFunction(xcs), new PolynomialFunction(ycs));
	}

	public Vector2 eval(float at, Vector2 result) {
		result.x = xFunc.eval(at);
		result.y = yFunc.eval(at);
		return result;
	}
	
	public PolynomialFunction2D mult(PolynomialFunction other)
	{
		return new PolynomialFunction2D(xFunc.mult(other), yFunc.mult(other));
	}
	
	public PolynomialFunction2D add(PolynomialFunction2D other)
	{
		return new PolynomialFunction2D(this.xFunc.add(other.xFunc), this.yFunc.add(other.yFunc));
	}
	
	public PolynomialFunction2D sub(PolynomialFunction2D other) {
		return new PolynomialFunction2D(this.xFunc.sub(other.xFunc), this.yFunc.sub(other.yFunc));
	}
	
	public PolynomialFunction2D sub(Vector2 constant) {
		return new PolynomialFunction2D(this.xFunc.add(-constant.x), this.yFunc.add(-constant.y));
	}

	public PolynomialFunction2D differentiate() {
		return new PolynomialFunction2D(xFunc.differentiate(), yFunc.differentiate());
	}


	public PolynomialFunction length2() {
		return dot(this);
	}

	public PolynomialFunction dot(PolynomialFunction2D other) {
		return xFunc.mult(other.xFunc).add(yFunc.mult(other.yFunc));
	}
	
	public PolynomialFunction2D transform(float[] matrix2x2) {
		return new PolynomialFunction2D(xFunc.mult(matrix2x2[0]).add(yFunc.mult(matrix2x2[1])),xFunc.mult(matrix2x2[2]).add(yFunc.mult(+matrix2x2[3])));
	}

	@Deprecated
	public PolynomialFunction2D transform(IReadableTransform transform) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Position origo = ep.obtain(Position.class).setToOrigin(); 
			transform.transform(origo);
			Position xBase = ep.obtain(Position.class).setTo(1, 0); 
			transform.transform(xBase);
			Position yBase = ep.obtain(Position.class).setTo(0, 1); 
			transform.transform(yBase);
			
			float m11 = xBase.getX()-origo.getX();
			float m12 = xBase.getY()-origo.getY();
			float m13 = origo.getX();
			float m21 = yBase.getX()-origo.getX();
			float m22 = yBase.getY()-origo.getY();
			float m23 = origo.getY();
			
			PolynomialFunction newXFunc = xFunc.mult(m11).add(yFunc.mult(m12)).add(m13);
			PolynomialFunction newYFunc = xFunc.mult(m21).add(yFunc.mult(m22)).add(m23);
			
			return new PolynomialFunction2D(newXFunc, newYFunc);
		}
		finally
		{
			ep.freeAllAndSelf();
		}
	}
}
