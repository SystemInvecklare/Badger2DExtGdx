package com.github.systeminvecklare.badger.impl.gdx.vectorgraphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial.ILineDeviationMeasurer;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial.LineDeviationMeasurer;
import com.github.systeminvecklare.badger.impl.gdx.vectorgraphics.polynomial.PolynomialFunction2D;

public class GdxVectorDrawer implements IGdxVectorDrawer {
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	private EarClippingTriangulator ear = new EarClippingTriangulator();
	private ILineDeviationMeasurer deviationMeasurer = new LineDeviationMeasurer();
	
	private float defaultFineness = 0.01f;
	private float defaultStepReductionFactor = 0.618f;
	
	private Vector2 temp = new Vector2(); //Resued in calculatePolygon
	
	private float initialWidth = Gdx.graphics.getWidth(); //This is a float to that the division in updateTransform becomes a float.
	private float initialHeight = Gdx.graphics.getHeight(); //Same here
	
	private Matrix4 test = new Matrix4(); //TODO remove
	
	@Override
	public void updateTransform(Matrix4 matrix4) {
		if(test.equals(matrix4))
		{
			System.out.println("SAME MATRIX!");
		}
		else
		{
			test.set(matrix4);
		}
		float sx = initialWidth/Gdx.graphics.getWidth();
		float sy = initialHeight/Gdx.graphics.getHeight();
		deviationMeasurer.setTransform(sx*matrix4.val[Matrix4.M00], sx*matrix4.val[Matrix4.M01], sy*matrix4.val[Matrix4.M10], sy*matrix4.val[Matrix4.M11]);
		shapeRenderer.getTransformMatrix().setToScaling(sx, sy, 1).mul(matrix4);
		shapeRenderer.updateMatrices();
	}

	@Override
	public float[] calculatePolygon(IVectorShape shape, float fineness, float stepReductionFactor) {
		FloatArray array = new FloatArray();
		for(PolynomialFunction2D path : shape)
		{
			path.eval(0, temp);
			array.addAll(temp.x, temp.y);

			float start = 0;
			while(start < 1) {
				float step = 1-start;
				while(deviationMeasurer.lineDeviation(path, start, start+step, temp) > fineness)
				{
					step *= stepReductionFactor;
				}
//				path.eval(start+step, temp); //Done in deviationMeasurer.lineDeviation(path, start, start+step, temp)-call
				array.addAll(temp.x, temp.y);

				start += step;
			}
		}
		return array.shrink();
	}

	@Override
	public float[] calculatePolygon(IVectorShape shape) {
		return calculatePolygon(shape, defaultFineness, defaultStepReductionFactor);
	}

	@Override
	public void draw(float[] vertices, Color color) {
		//TODO Throw exception if called inside batch.begin() batch.end() (i.e. if batch.isDrawing())
		//Note: The shapeRenderer has already been transformed.
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.polygon(vertices);
		shapeRenderer.end();
	}
	
	@Override
	public ITriangulation triangulate(float[] vertices) {
		ShortArray arrRes = ear.computeTriangles(vertices);
		//Note that this ShortArray belongs to EarClippingTriangulator, so we need to copy it if we wanna save it.
		return new Triangulation(vertices, arrRes);
	}
	
	@Override
	public void fill(ITriangulation triangulation, Color color) {
		//TODO Throw exception if called inside batch.begin() batch.end() (i.e. if batch.isDrawing())
		boolean faceCullingEnabled = Gdx.gl.glIsEnabled(GL20.GL_CULL_FACE);
		if(faceCullingEnabled)
		{
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		}
		try
		{
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			//Note: The shapeRenderer has already been transformed.
			triangulation.draw(shapeRenderer);
			shapeRenderer.end();
		}
		finally
		{
			if(faceCullingEnabled)
			{
				Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			}
		}
		
	}
}
