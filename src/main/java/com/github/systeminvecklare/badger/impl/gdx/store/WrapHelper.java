package com.github.systeminvecklare.badger.impl.gdx.store;

import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.github.systeminvecklare.badger.core.math.Mathf;

/*package-protected*/ class WrapHelper {
	public static void draw(float start, float length, float tex1, float tex2, TextureWrap wrap, float textureSize, IDrawSink drawSink) {
		if(length < 0) {
			start += length;
			length = -length;
			float swap = tex1;
			tex1 = tex2;
			tex2 = swap;
		}
		
	    if (tex1 >= 0 && tex2 <= 1 && tex1 <= 1 && tex2 >= 0) {
	    	drawSink.draw(start, length, tex1, tex2);
	        return;
	    }

	    switch (wrap) {
	        case ClampToEdge:
	            drawClampToEdge(start, length, tex1, tex2, textureSize, drawSink);
	            break;
	        case Repeat:
	            drawRepeat(false, start, length, tex1, tex2, drawSink);
	            break;
	        case MirroredRepeat:
	        	drawRepeat(true, start, length, tex1, tex2, drawSink);
	            break;
	    }
	}
	
	private static void drawRepeat(boolean mirrorRepeat, float start, float length, float tex1, float tex2, IDrawSink drawSink) {
		float m1 = start + (-tex1*length)/(tex2 - tex1);
		float m2 = start + ((1f-tex1)*length)/(tex2 - tex1);
		float middleU1 = 0f;
		float middleU2 = 1f;
		
		if(m2 < m1) {
			float swap = m1;
			m1 = m2;
			m2 = swap;
			swap = middleU1;
			middleU1 = middleU2;
			middleU2 = swap;
		}
		
		float mLength = m2 - m1;
		if(mLength > 0f) {
			// Protection from loop going crazy
			if(mLength < 1f) {
				mLength = 1f;
			}
			int N = Mathf.ceilToInt((m2 - start)/mLength);
			float startM1 = m2 - mLength*N;
			
			boolean mirrored = Mathf.mod(N, 2) == 0;
			
			int copies = Mathf.ceilToInt((start + length - startM1)/mLength);
			for(int i = 0; i < copies; ++i) {
				if(mirrorRepeat && mirrored) {
					drawVisible(startM1 + mLength*i, mLength, middleU2, middleU1, start, length, drawSink);
				} else {
					drawVisible(startM1 + mLength*i, mLength, middleU1, middleU2, start, length, drawSink);
				}
				mirrored = !mirrored;
			}
		}
	}
	
	private static void drawClampToEdge(float start, float length, float tex1, float tex2, float textureSize, IDrawSink drawSink) {
		float m1 = start + (-tex1*length)/(tex2 - tex1);
		float m2 = start + ((1f-tex1)*length)/(tex2 - tex1);
		float middleU1 = 0f;
		float middleU2 = 1f;
		
		if(m2 < m1) {
			float swap = m1;
			m1 = m2;
			m2 = swap;
			swap = middleU1;
			middleU1 = middleU2;
			middleU2 = swap;
		}
		
		// Draw middle
		drawVisible(m1, m2 - m1, middleU1, middleU2, start, length, drawSink);
		
		// Draw pre
		if(m1 > start) {
			float uL = Mathf.lerp(1f/(2f*textureSize), middleU1, middleU2);
			drawVisible(start, m1 - start, uL, uL, start, length, drawSink);
		}
		
		// Draw post
		if(m2 < start + length) {
			float uR = Mathf.lerp(1f-(1f/(2f*textureSize)), middleU1, middleU2);
			drawVisible(m2, start + length - m2, uR, uR, start, length, drawSink);
		}
	}
	
	private static void drawVisible(float segStart, float segWidth, float tex1, float tex2, float start, float length, IDrawSink drawSink) {
		if(segWidth > 0) {
			float t1 = (start - segStart)/segWidth;
			float t2 = (start + length - segStart)/segWidth;
			t1 = Math.max(0, t1);
			t2 = Math.min(1, t2);
			if(t2 > t1) {
				float p1 = Mathf.lerp(t1, tex1, tex2);
				float p2 = Mathf.lerp(t2, tex1, tex2);
				drawSink.draw(segStart + t1*segWidth, (t2 - t1)*segWidth, p1, p2);
			}
		}
	}
	
	public interface IDrawSink {
		void draw(float start, float length, float tex1, float tex2);
	}
}
