package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial;

public class PolynomialFunction {
	private float[] coeficcients;

	public PolynomialFunction(float ... coeficcients) {
		this.coeficcients = coeficcients;
	}
	
	public float eval(float value) {
		if(value == 0f)
		{
			return coeficcients.length > 0 ? coeficcients[0] : 0f;
		}
		else if(value == 1f)
		{
			float sum = 0f;
			for(int c = 0; c < coeficcients.length; ++c)
			{
				sum += coeficcients[c];
			}
			return sum;
		}
		return evalAtOffset(value, 0);
	}
	
	
	private float evalAtOffset(float value, int offset) {
		if(offset >= coeficcients.length)
		{
			return 0f;
		}
		float nextEval = evalAtOffset(value, offset+1);
		if(nextEval == 0f)
		{
			return coeficcients[offset];
		}
		else
		{
			return coeficcients[offset]+value*nextEval;
		}
	}
	
	private PolynomialFunction trim()
	{
		//Find highest coordinate not equal to 0
		int highestNotZero = -1;
		for(int i = coeficcients.length-1; i >= 0; i--)
		{
			if(coeficcients[i] != 0.0f)
			{
				highestNotZero = i;
				break;
			}
		}
		if(highestNotZero+1 < coeficcients.length)
		{
			float[] trimmed = new float[highestNotZero+1];
			System.arraycopy(coeficcients, 0, trimmed, 0, highestNotZero+1);
			coeficcients = trimmed;
		}
		return this;
	}
	
	public PolynomialFunction mult(PolynomialFunction other)
	{
		if(this.isClearlyZero() || other.isClearlyZero())
		{
			return new PolynomialFunction();
		}
		float[] newCoords = new float[coeficcients.length + other.coeficcients.length - 1];
		for(int mc = 0; mc < coeficcients.length; ++mc)
		{
			for(int oc = 0; oc < other.coeficcients.length; ++oc)
			{
				newCoords[mc+oc] += coeficcients[mc]*other.coeficcients[oc];
			}
		}
		return new PolynomialFunction(newCoords);//.trim();
	}
	
	public PolynomialFunction mult(float factor)
	{
		if(this.isClearlyZero() || factor == 0f)
		{
			return new PolynomialFunction();
		}
		float[] newCoords = new float[coeficcients.length];
		for(int mc = 0; mc < coeficcients.length; ++mc)
		{
			newCoords[mc] = factor*coeficcients[mc];
		}
		return new PolynomialFunction(newCoords);//.trim();
	}
	
	public PolynomialFunction add(float constant)
	{
		if(constant == 0f)
		{
			return this;
		}
		else if(this.isClearlyZero())
		{
			return new PolynomialFunction(constant);
		}
		float[] newCoords = new float[coeficcients.length];
		System.arraycopy(coeficcients, 0, newCoords, 0, coeficcients.length);
		newCoords[0] += constant;
		return new PolynomialFunction(newCoords).trim();
	}
	
	public PolynomialFunction add(PolynomialFunction other)
	{
		if(other.isClearlyZero())
		{
			return this;
		}
		else if(this.isClearlyZero())
		{
			return other;
		}
		float[] newCoords = new float[Math.max(coeficcients.length, other.coeficcients.length)];
		float myCoord;
		float otherCoord;
		for(int i = 0; i < newCoords.length; ++i)
		{
			myCoord = i < coeficcients.length ? coeficcients[i] : 0;
			otherCoord = i < other.coeficcients.length ? other.coeficcients[i] : 0;
			newCoords[i] = myCoord + otherCoord;
		}
		return new PolynomialFunction(newCoords).trim();
	}
	
	public PolynomialFunction sub(PolynomialFunction other)
	{
		if(other.isClearlyZero())
		{
			return this;
		}
		float[] newCoords = new float[Math.max(coeficcients.length, other.coeficcients.length)];
		float myCoord;
		float otherCoord;
		for(int i = 0; i < newCoords.length; ++i)
		{
			myCoord = i < coeficcients.length ? coeficcients[i] : 0;
			otherCoord = i < other.coeficcients.length ? other.coeficcients[i] : 0;
			newCoords[i] = myCoord - otherCoord;
		}
		return new PolynomialFunction(newCoords).trim();
	}
	
	private boolean isClearlyZero() {
		return coeficcients.length == 0 || (coeficcients.length == 1 && coeficcients[0] == 0f);
	}

	
	public PolynomialFunction differentiate() {
		if(coeficcients.length <= 1)
		{
			return new PolynomialFunction();
		}
		float[] diffCoef = new float[coeficcients.length-1];
		for(int c = 1; c < coeficcients.length; ++c)
		{
			diffCoef[c-1] = c*coeficcients[c];
		}
		return new PolynomialFunction(diffCoef);
	}

	public PolynomialFunction primitive() {
		if(this.isClearlyZero())
		{
			return new PolynomialFunction();
		}
		float[] primCoef = new float[coeficcients.length+1];
		for(int c = 0; c < coeficcients.length; ++c)
		{
			primCoef[c+1] = coeficcients[c]/(c+1);
		}
		return new PolynomialFunction(primCoef);//.trim();
	}
}
