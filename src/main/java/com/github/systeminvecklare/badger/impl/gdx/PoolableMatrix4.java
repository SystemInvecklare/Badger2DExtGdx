package com.github.systeminvecklare.badger.impl.gdx;

import com.badlogic.gdx.math.Matrix4;
import com.github.systeminvecklare.badger.core.pooling.IPool;
import com.github.systeminvecklare.badger.core.pooling.IPoolable;

public class PoolableMatrix4 implements IPoolable {
	private IPool<PoolableMatrix4> pool;
	private Matrix4 matrix4;
	
	
	public PoolableMatrix4(IPool<PoolableMatrix4> pool) {
		this.pool = pool;
		this.matrix4 = new Matrix4();
	}

	@Override
	public void free() {
		pool.free(this);
	}

	@Override
	public IPool<PoolableMatrix4> getPool() {
		return pool;
	}
	
	public Matrix4 getMatrix4() {
		return matrix4;
	}

}
