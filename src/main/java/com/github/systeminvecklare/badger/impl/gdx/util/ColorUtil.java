package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.graphics.Color;
import com.github.systeminvecklare.badger.core.math.Mathf;

public class ColorUtil {
	private static final float[] CHANNEL_WEIGHTS = new float[] {0.2989f, 0.5870f, 0.1141f};
	private ColorUtil() {}
	
	public static float getBrightness(Color color) {
		float squareBrightness = color.r*color.r*CHANNEL_WEIGHTS[0];
		squareBrightness += color.g*color.g*CHANNEL_WEIGHTS[1];
		squareBrightness += color.b*color.b*CHANNEL_WEIGHTS[2];
		return Mathf.sqrt(squareBrightness);
	}
	
	public static Color setBrightness(Color color, float targetBrightness, Color result) {
		float currentBrightness = getBrightness(color);
		if(targetBrightness == currentBrightness) {
			return result.set(color);
		}
		// at^2 + bt + c' = targetBrightness^2,
		// Let c = c' - targetBrightness^2
		// at^2 + bt + c = 0
		
		float cw = color.r*CHANNEL_WEIGHTS[0] + color.g*CHANNEL_WEIGHTS[1] + color.b*CHANNEL_WEIGHTS[2];
		
		float c = currentBrightness*currentBrightness - targetBrightness*targetBrightness;
		float b = 2f*cw;
		float a = 1f - currentBrightness*currentBrightness - cw;
		
		if(a == 0) {
			//bt + c = 0
			// t = -c/b
			if(b == 0) {
				// Only happens when color == Black OR invalid color (negative coefs)
				// So We assume black. --> a == 1 - 0 - 0 = 1
				// So only way we get here is with invalid color.
				throw new IllegalArgumentException("Color is " + color);
			}
			return lerp(-c/b, color, Color.WHITE, result);
		}
		b /= a;
		c /= a;
		// t^2 + bt + c = 0
		// (t + b/2)^2 - b^2/4 + c = 0
		// (t + b/2)^2 = b^2/4 - c
		float rhs = b*b*0.25f  - c;
		if(rhs < 0) {
			// No solutions. Return a gray-scale with correct brightness
			return result.set(Color.BLACK).lerp(Color.WHITE, targetBrightness);
		}
		rhs = Mathf.sqrt(rhs);
		float t1 = -b*0.5f + rhs;
		float t2 = -b*0.5f - rhs;
		boolean t1Valid = t1 >= 0 && t1 <= 1;
		boolean t2Valid = t2 >= 0 && t2 <= 1;
		if(t1Valid) {
			return result.set(color).lerp(Color.WHITE, t1);
		} else if(t2Valid) {
			return result.set(color).lerp(Color.WHITE, t2);
		} else {
			// Maybe it's just a rounding error. Do best effort
			float t1Adjusted = Mathf.clamp(t1, 0, 1);
			float t2Adjusted = Mathf.clamp(t2, 0, 1);
			
			float t1Diff = Math.abs(t1 - t1Adjusted);
			float t2Diff = Math.abs(t2 - t2Adjusted);
			if(t1Diff <= t2Diff) {
				return result.set(color).lerp(Color.WHITE, t1Adjusted);
			} else {
				return result.set(color).lerp(Color.WHITE, t2Adjusted);
			}
		}
	}
	
	public static Color lerp(float t, Color a, Color b) {
		if(t == 0) {
			return a;
		} else if(t == 1) {
			return b;
		} else {
			Color result = new Color(a);
			result.lerp(b, t);
			return result;
		}
	}
	
	public static Color lerp(float t, Color a, Color b, Color result) {
		if(t == 0) {
			result.set(a);
		} else if(t == 1) {
			result.set(b);
		} else {
			result.set(a);
			result.lerp(b, t);
		}
		return result;
	}
}
