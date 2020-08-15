package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.math.Matrix3;
import com.github.systeminvecklare.badger.core.pooling.IPool;
import com.github.systeminvecklare.badger.core.pooling.IPoolable;

public class PoolableMatrix3 implements IPoolable {
	private IPool<PoolableMatrix3> pool;
	private Matrix3 matrix3;
	
	
	public PoolableMatrix3(IPool<PoolableMatrix3> pool) {
		this.pool = pool;
		this.matrix3 = new Matrix3();
	}

	@Override
	public void free() {
		pool.free(this);
	}

	@Override
	public IPool<PoolableMatrix3> getPool() {
		return pool;
	}
	
	public Matrix3 getMatrix3() {
		return matrix3;
	}

}
