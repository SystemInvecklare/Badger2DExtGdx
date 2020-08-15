package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ShortArray;

public class Triangulation implements ITriangulation {
	private float[] vertices;
	private ShortArray arrRes;
	

	public Triangulation(float[] vertices, ShortArray arrRes) {
		this.vertices = vertices;
		this.arrRes = new ShortArray(arrRes);
	}


	@Override
	public void draw(ShapeRenderer renderer) {
		for (int i = 0; i < arrRes.size - 2; i = i + 3)
		{
			float x1 = vertices[arrRes.get(i) * 2];
			float y1 = vertices[(arrRes.get(i) * 2) + 1];

			float x2 = vertices[(arrRes.get(i + 1)) * 2];
			float y2 = vertices[(arrRes.get(i + 1) * 2) + 1];

			float x3 = vertices[arrRes.get(i + 2) * 2];
			float y3 = vertices[(arrRes.get(i + 2) * 2) + 1];

			renderer.triangle(x1, y1, x2, y2, x3, y3);
		}	
	}

}
