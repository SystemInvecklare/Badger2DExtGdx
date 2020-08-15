package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial;

import com.badlogic.gdx.math.Vector2;

public interface ILineDeviationMeasurer {
	public float lineDeviation(PolynomialFunction2D func, float from, float to, Vector2 getValueAtTo);
	public void setTransform(float a11, float a12, float a21, float a22);
}
