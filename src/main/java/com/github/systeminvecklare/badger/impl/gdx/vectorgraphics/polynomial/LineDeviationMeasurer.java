package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial;

import com.badlogic.gdx.math.Vector2;

public class LineDeviationMeasurer implements ILineDeviationMeasurer {
	private float[] A = new float[]{1,0,0,1};
	
	private float[] alpha = new float[4]; //Reused in lineDeviation
	private Vector2 a = new Vector2(); //Reused in lineDeviation
	private Vector2 fromV = new Vector2(); //Reused in lineDeviation
//	private Vector2 toV = new Vector2(); //Reused in lineDeviation
	
	@Override
	public void setTransform(float a11, float a12,float a21, float a22)
	{
		A[0] = a11;
		A[1] = a12;
		A[2] = a21;
		A[3] = a22;
	}
	
	@Override
	public float lineDeviation(PolynomialFunction2D func, float from, float to, Vector2 getValueAtTo) {
		fromV = func.eval(from, fromV);
		
		PolynomialFunction2D Ag = func.sub(fromV).transform(A);
		
		a = transform(A, a.set(func.eval(to, getValueAtTo)).sub(fromV));
		
		a.nor(); //This is now aHat (i.e 'a' normalized)
		
		alpha[0] = 1-a.x*a.x;
		alpha[1] = -a.x*a.y;
		alpha[2] = alpha[1]; //Same value
		alpha[3] = 1-a.y*a.y;
		
		PolynomialFunction dist2 = Ag.dot(Ag.transform(alpha));
		PolynomialFunction primitive = dist2.primitive();
		return primitive.eval(to)-primitive.eval(from);
	}

	private Vector2 transform(float[] matrix, Vector2 vector) {
		float newX = matrix[0]*vector.x+matrix[1]*vector.y;
		float newY = matrix[2]*vector.x+matrix[3]*vector.y;
		return vector.set(newX, newY);
	}
}
