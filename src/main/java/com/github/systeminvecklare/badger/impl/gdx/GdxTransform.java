package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.github.systeminvecklare.badger.core.graphics.components.FlashyEngine;
import com.github.systeminvecklare.badger.core.graphics.components.transform.IReadableTransform;
import com.github.systeminvecklare.badger.core.graphics.components.transform.ITransform;
import com.github.systeminvecklare.badger.core.math.AbstractPosition;
import com.github.systeminvecklare.badger.core.math.AbstractRotation;
import com.github.systeminvecklare.badger.core.math.IReadableDeltaRotation;
import com.github.systeminvecklare.badger.core.math.IReadablePosition;
import com.github.systeminvecklare.badger.core.math.IReadableRotation;
import com.github.systeminvecklare.badger.core.math.IReadableVector;
import com.github.systeminvecklare.badger.core.math.Mathf;
import com.github.systeminvecklare.badger.core.math.Position;
import com.github.systeminvecklare.badger.core.math.Rotation;
import com.github.systeminvecklare.badger.core.math.Vector;
import com.github.systeminvecklare.badger.core.pooling.EasyPooler;
import com.github.systeminvecklare.badger.core.pooling.IPool;

/**
 * Checked at 2015-03-15 17:39
 * 
 * @author Matte
 *
 */
public class GdxTransform implements ITransform {
	private IPool<ITransform> pool; 
	private Matrix4 matrix4;
	private TransPosition myPos = new TransPosition();
	private TransRotation myRot = new TransRotation();
	private TransScale myScale = new TransScale();
	
	public GdxTransform(IPool<ITransform> pool) {
		this.pool = pool;
		this.matrix4 = new Matrix4();
	}

	@Override
	public void free() {
		pool.free(this);
	}

	@Override
	public IPool<ITransform> getPool() {
		return pool;
	}

	@Override
	public IReadablePosition getPosition() {
		return myPos;
	}

	@Override
	public IReadableRotation getRotation() {
		return myRot;
	}

	@Override
	public IReadableVector getScale() {
		return myScale;
	}

	@Override
	public float getShear() {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
			Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
			Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			matrix.idt();
			matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
			matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
			matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
			matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
			decompose(matrix, rot, scaleShear);
			return scaleShear.val[Matrix3.M01];
		}
		finally
		{
			ep.freeAllAndSelf();
		}
	}

	@Override
	public void transform(Position argumentAndResult) {
		float[] values = matrix4.getValues();
		float x = argumentAndResult.getX();
		float y = argumentAndResult.getY();
		argumentAndResult.setTo(values[Matrix4.M00]*x+values[Matrix4.M01]*y+values[Matrix4.M03],
								values[Matrix4.M10]*x+values[Matrix4.M11]*y+values[Matrix4.M13]);
	}
	
	@Override
	public ITransform setTo(IReadablePosition position, IReadableVector scale,
			IReadableRotation rotation, float shear) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3().setToRotationRad(rotation.getTheta());
			
			Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3().idt();
			scaleShear.val[Matrix3.M00] = scale.getX();
			scaleShear.val[Matrix3.M11] = scale.getY();
			scaleShear.val[Matrix3.M01] = shear;
			
			Matrix4 result = ep.obtain(PoolableMatrix4.class).getMatrix4();
			
			result.idt().set(rot.mul(scaleShear));
			result.val[Matrix4.M03] = position.getX();
			result.val[Matrix4.M13] = position.getY();
			
			this.matrix4.set(result);
			return this;
		}
		finally
		{
			ep.freeAllAndSelf();
		}
	}

	@Override
	public ITransform setTo(IReadableTransform other) {
		if(other instanceof GdxTransform)
		{
			matrix4.set(((GdxTransform) other).matrix4);
			return this;
		}
		else
		{
			return setTo(other.getPosition(),other.getScale(),other.getRotation(),other.getShear());
		}
	}

	@Override
	public ITransform setToIdentity() {
		matrix4.idt();
		return this;
	}

	@Override
	public ITransform setPosition(IReadablePosition position) {
		return setPosition(position.getX(), position.getY());
	}

	@Override
	public ITransform setRotation(IReadableRotation rotation) {
		return setRotation(rotation.getTheta());
	}

	@Override
	public ITransform setScale(IReadableVector scale) {
		return setScale(scale.getX(), scale.getY());
	}

	@Override
	public ITransform setShear(float shear) {
		return setTo(getPosition(), getScale(),  getRotation(), shear);
	}

	@Override
	public ITransform mult(IReadableTransform other) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix4 temp = ep.obtain(PoolableMatrix4.class).getMatrix4();
			temp.set(matrix4).mul(((GdxTransform) other).matrix4);
			matrix4.set(temp);
		}
		finally
		{
			ep.freeAllAndSelf();
		}
		return this;
	}

	@Override
	public ITransform multLeft(IReadableTransform other) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix4 temp = ep.obtain(PoolableMatrix4.class).getMatrix4();
			temp.set(matrix4).mulLeft(((GdxTransform) other).matrix4);
			matrix4.set(temp);
		}
		finally
		{
			ep.freeAllAndSelf();
		}
		return this;
	}

	@Override
	public ITransform invert() {
		matrix4.inv();
		return this;
	}
	
	@Override
	public ITransform setPosition(float x, float y) {
		matrix4.val[Matrix4.M03] = x;
		matrix4.val[Matrix4.M13] = y;
		return this;
	}
	
	@Override
	public ITransform setRotation(float theta) {
		return addToRotation(theta-getRotation().getTheta());
	}
	
	@Override
	public ITransform setScale(float x, float y) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
			matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
			matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
			matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
			matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
			
			Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
			Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			decompose(matrix, rot, scaleShear);
			
			scaleShear.val[Matrix3.M00] = x;
			scaleShear.val[Matrix3.M11] = y;
			
			rot.mul(scaleShear);
			//rot is now the whole matrix.
			matrix4.val[Matrix4.M00] = rot.val[Matrix3.M00];
			matrix4.val[Matrix4.M01] = rot.val[Matrix3.M01];
			matrix4.val[Matrix4.M10] = rot.val[Matrix3.M10];
			matrix4.val[Matrix4.M11] = rot.val[Matrix3.M11];
		}
		finally
		{
			ep.freeAllAndSelf();
		}
		
		return this;
	}
	
	@Override
	public ITransform addToPosition(float dx, float dy) {
		matrix4.val[Matrix4.M03] += dx;
		matrix4.val[Matrix4.M13] += dy;
		return this;
	}
	
	@Override
	public ITransform addToPosition(IReadableVector dvector) {
		return addToPosition(dvector.getX(), dvector.getY());
	}
	
	@Override
	public ITransform addToRotation(float dtheta) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
			
			matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
			matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
			matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
			matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
			
			Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
			Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			decompose(matrix, rot, scaleShear);
			
			rot.setToRotationRad(rot.getRotationRad()+dtheta);
			
			rot.mul(scaleShear);
			//rot is now the whole matrix.
			matrix4.val[Matrix4.M00] = rot.val[Matrix3.M00];
			matrix4.val[Matrix4.M01] = rot.val[Matrix3.M01];
			matrix4.val[Matrix4.M10] = rot.val[Matrix3.M10];
			matrix4.val[Matrix4.M11] = rot.val[Matrix3.M11];
		}
		finally
		{
			ep.freeAllAndSelf();
		}
		
		return this;
	}
	
	@Override
	public ITransform addToRotation(IReadableDeltaRotation drotation) {
		return addToRotation(drotation.getTheta());
	}
	
	@Override
	public ITransform multiplyScale(float sx, float sy) {
		EasyPooler ep = EasyPooler.obtainFresh();
		try
		{
			Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
			
			matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
			matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
			matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
			matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
			
			Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
			Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			decompose(matrix, rot, scaleShear);
			
			scaleShear.val[Matrix3.M00] *= sx;
			scaleShear.val[Matrix3.M11] *= sy;
			
			rot.mul(scaleShear);
			//rot is now the whole matrix.
			matrix4.val[Matrix4.M00] = rot.val[Matrix3.M00];
			matrix4.val[Matrix4.M01] = rot.val[Matrix3.M01];
			matrix4.val[Matrix4.M10] = rot.val[Matrix3.M10];
			matrix4.val[Matrix4.M11] = rot.val[Matrix3.M11];
		}
		finally
		{
			ep.freeAllAndSelf();
		}
		
		return this;
	}
	
	@Override
	public ITransform multiplyScale(IReadableVector dscale) {
		return multiplyScale(dscale.getX(), dscale.getY());
	}
	
	
	private class TransPosition extends AbstractPosition implements IReadablePosition {

		@Override
		public float getX() {
			return matrix4.getValues()[Matrix4.M03];
		}

		@Override
		public float getY() {
			return matrix4.getValues()[Matrix4.M13];
		}
	}
	
	private class TransRotation extends AbstractRotation implements IReadableRotation {

		@Override
		public float getTheta() {
			EasyPooler ep = EasyPooler.obtainFresh();
			try
			{
				Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			
				matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
				matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
				matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
				matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
					
				decompose(matrix, rot, scaleShear);
				return Rotation.rotationClamp(rot.getRotationRad());
			}
			finally
			{
				ep.freeAllAndSelf();
			}
		}
	}
	
	private class TransScale implements IReadableVector {

		@Override
		public float getX() {
			EasyPooler ep = EasyPooler.obtainFresh();
			try
			{
				Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
			
				matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
				matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
				matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
				matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
				
				decompose(matrix, rot, scaleShear);
				return scaleShear.val[Matrix3.M00];
			}
			finally
			{
				ep.freeAllAndSelf();
			}
		}

		@Override
		public float getY() {
			EasyPooler ep = EasyPooler.obtainFresh();
			try
			{
				Matrix3 matrix = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 rot = ep.obtain(PoolableMatrix3.class).getMatrix3();
				Matrix3 scaleShear = ep.obtain(PoolableMatrix3.class).getMatrix3();
				
				matrix.val[Matrix3.M00] = matrix4.val[Matrix4.M00];
				matrix.val[Matrix3.M10] = matrix4.val[Matrix4.M10];
				matrix.val[Matrix3.M01] = matrix4.val[Matrix4.M01];
				matrix.val[Matrix3.M11] = matrix4.val[Matrix4.M11];
				
				decompose(matrix, rot, scaleShear);
				return scaleShear.val[Matrix3.M11];
			}
			finally
			{
				ep.freeAllAndSelf();
			}
		}

		@Override
		public float length() {
			return Mathf.sqrt(length2());
		}

		@Override
		public float length2() {
			float x = getX();
			float y = getY();
			return x*x+y*y;
		}

		@Override
		public float dot(IReadableVector other) {
			return Vector.dot(this, other);
		}

		@Override
		public float cross(IReadableVector other) {
			return Vector.cross(this, other);
		}

		@Override
		public float getRotationTheta() {
			return Vector.getRotationTheta(this);
		}
	}
	
	private static void decompose(Matrix3 matrix, Matrix3 rot, Matrix3 scaleShear)
	{
		float m00 = matrix.val[Matrix3.M00];
		float m10 = matrix.val[Matrix3.M10];
		IPool<Vector> vectorPool = FlashyEngine.get().getPoolManager().getPool(Vector.class);
		Vector v = vectorPool.obtain();
		try
		{
			v.setTo(m00-Mathf.sqrt(m00*m00+m10*m10),m10);
			if(v.length2() < 0.000001f)
			{
				v.setTo(m00+Mathf.sqrt(m00*m00+m10*m10),m10);
			}
			v.normalize();
			
			rot.idt();
			rot.val[Matrix3.M00] += -2*v.getX()*v.getX();
			rot.val[Matrix3.M01] += -2*v.getX()*v.getY();
			rot.val[Matrix3.M10] += -2*v.getX()*v.getY();
			rot.val[Matrix3.M11] += -2*v.getY()*v.getY();
		}
		finally
		{
			vectorPool.free(v);
		}
		
		
		scaleShear.set(rot).mul(matrix);
		
		if(rot.det() < 0)
		{
			rot.val[Matrix3.M00] *= -1;
			rot.val[Matrix3.M10] *= -1;
			rot.val[Matrix3.M20] *= -1;
			
			scaleShear.val[Matrix3.M00] *= -1;
			scaleShear.val[Matrix3.M01] *= -1;
			scaleShear.val[Matrix3.M02] *= -1;
		}
		
		if(scaleShear.val[Matrix3.M00] < 0 && scaleShear.val[Matrix3.M11] < 0)
//		if(scaleShear.val[Matrix3.M11] < 0)
		{
			rot.val[Matrix3.M00] *= -1;
			rot.val[Matrix3.M10] *= -1;
			rot.val[Matrix3.M01] *= -1;
			rot.val[Matrix3.M11] *= -1;
			
			scaleShear.val[Matrix3.M00] *= -1;
			scaleShear.val[Matrix3.M10] *= -1;
			scaleShear.val[Matrix3.M01] *= -1;
			scaleShear.val[Matrix3.M11] *= -1;
		}
	}
	
	public Matrix4 getMatrix4() {
		return matrix4;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pos: "+this.getPosition().getX()).append(",").append(this.getPosition().getY());
		builder.append("\n");
		builder.append("Rotation: ").append(this.getRotation().getTheta());
		builder.append("\n");
		builder.append("Scale: ").append(this.getScale().getX()).append(",").append(this.getScale().getY());
		builder.append("\n");
		builder.append("Shear: ").append(this.getShear());
		builder.append("\n");
		return builder.toString();
	}
}
