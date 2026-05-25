package com.github.systeminvecklare.badger.impl.gdx.fbo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class FBO_DEBUG {
	private static String lastPrint = null;
	private static StringBuilder currentPrint = new StringBuilder();
	
	private static boolean enabled = false;
	
	public static void println(String log) {
		currentPrint.append(log).append("\n");
	}
	
	public static void commit() {
		String newPrint = currentPrint.toString();
		currentPrint.setLength(0);
		if(!enabled) {	
			newPrint = null;
		}
		if(lastPrint == null || !lastPrint.equals(newPrint)) {
			if(newPrint != null) {
				System.out.println(newPrint);
			}
		}
		lastPrint = newPrint;
	}

	public static void setEnabled(boolean enabled) {
		FBO_DEBUG.enabled = enabled;
	}

	public static boolean isEnabled() {
		return enabled;
	}
	
	public static String getSpritebatchCombined(SpriteBatch spriteBatch) {
		Matrix4 combinedMatrix = new Matrix4();
		combinedMatrix.set(spriteBatch.getProjectionMatrix()).mul(spriteBatch.getTransformMatrix());
		Vector3 translation = combinedMatrix.getTranslation(new Vector3());
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append("scale="+combinedMatrix.getScaleX()+", "+combinedMatrix.getScaleY()+"... pos: "+translation);
		builder.append("]");
		return builder.toString();
	}
}
