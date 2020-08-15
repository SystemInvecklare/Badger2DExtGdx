package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;

public interface IGdxVectorDrawer {
	public float[] calculatePolygon(IVectorShape shape, float fineness, float stepReductionFactor);
	public float[] calculatePolygon(IVectorShape shape);
	public ITriangulation triangulate(float[] vertices); 
	
	public void draw(float[] vertices, Color color);
	public void fill(ITriangulation triangulation, Color color);
	
	
	public void updateTransform(Matrix4 matrix4);
}
