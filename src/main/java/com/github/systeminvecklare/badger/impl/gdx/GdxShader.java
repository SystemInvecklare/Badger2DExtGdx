package com.github.systeminvecklare.badger.impl.gdx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;

public class GdxShader implements IShader {
	private ShaderProgram shaderProgram;
	private String name;
	private Map<String,IGdxShaderParam> parameters = new HashMap<String, IGdxShaderParam>();

	public GdxShader(String name, ShaderProgram shaderProgram) {
		this.shaderProgram = shaderProgram;
		this.name = name;
	}

	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void onBind(IDrawCycle drawCycle) {
		SpriteBatch batch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		ShaderProgram sp = getShaderProgram();
		batch.setShader(sp);
		for(Entry<String, IGdxShaderParam> parameter : parameters.entrySet())
		{
			parameter.getValue().apply(sp,parameter.getKey());
		}
	}

	@Override
	public GdxShader bindUniformf(String name, IUniformFloat var) {
		IGdxShaderParam param = parameters.get(name);
		if(param != null)
		{
			if(!(param instanceof UniformFloatParam))
			{
				throw new IllegalArgumentException(String.valueOf(name)+" is already bound to an other type.");
			}
		}
		parameters.put(name, new UniformFloatParam(var));
		return this;
	}

	@Override
	public GdxShader bindUniformi(String name, IUniformInteger var) {
		IGdxShaderParam param = parameters.get(name);
		if(param != null)
		{
			if(!(param instanceof UniformIntParam))
			{
				throw new IllegalArgumentException(String.valueOf(name)+" is already bound to an other type.");
			}
		}
		parameters.put(name, new UniformIntParam(var));
		return this;
	}

	@Override
	public GdxShader bindUniform2fv(String name, IUniformVector2 var) {
		IGdxShaderParam param = parameters.get(name);
		if(param != null)
		{
			if(!(param instanceof UniformVec2Param))
			{
				throw new IllegalArgumentException(String.valueOf(name)+" is already bound to an other type.");
			}
		}
		parameters.put(name, new UniformVec2Param(var));
		return this;
	}
	
	@Override
	public IShader bindUniformfArray(String name, int length, IUniformFloatArray var) {
		IGdxShaderParam param = parameters.get(name);
		if(param != null)
		{
			if(!(param instanceof UniformFloatArrayParam))
			{
				throw new IllegalArgumentException(String.valueOf(name)+" is already bound to an other type.");
			}
		}
		parameters.put(name, new UniformFloatArrayParam(var,length));
		return this;
	}
	
	@Override
	public IShader bindUniform2fvArray(String name, int length, IUniformVector2 var) {
		IGdxShaderParam param = parameters.get(name);
		if(param != null)
		{
			if(!(param instanceof UniformFloatArrayParam))
			{
				throw new IllegalArgumentException(String.valueOf(name)+" is already bound to an other type.");
			}
		}
		parameters.put(name, new UniformVec2ArrayParam(var,length*2));
		return this;
	}
	
	private static interface IGdxShaderParam {
		public void apply(ShaderProgram sp, String name);
	}
	
	private static class UniformFloatParam implements IGdxShaderParam {
		private IUniformFloat inner;

		public UniformFloatParam(IUniformFloat uniformFloat) {
			this.inner = uniformFloat;
		}

		@Override
		public void apply(ShaderProgram sp, String name) {
			int location = sp.getUniformLocation(name);
			sp.setUniformf(location, inner.getValue());
		}
	}
	
	private static class UniformIntParam implements IGdxShaderParam {
		private IUniformInteger inner;

		public UniformIntParam(IUniformInteger uniformFloat) {
			this.inner = uniformFloat;
		}

		@Override
		public void apply(ShaderProgram sp, String name) {
			int location = sp.getUniformLocation(name);
			sp.setUniformi(location, inner.getValue());
		}
	}
	
	private static class UniformVec2Param implements IGdxShaderParam {
		private float[] temp = new float[2];
		private IUniformVector2 inner;

		public UniformVec2Param(IUniformVector2 uniformFloat) {
			this.inner = uniformFloat;
		}

		@Override
		public void apply(ShaderProgram sp, String name) {
			int location = sp.getUniformLocation(name);
			inner.getVec2(temp);
			sp.setUniform2fv(location, temp, 0, 2);
		}
	}
	
	private static class UniformFloatArrayParam implements IGdxShaderParam {
		private float[] temp;
		private IUniformFloatArray inner;

		public UniformFloatArrayParam(IUniformFloatArray uniformFloatArray, int length) {
			this.inner = uniformFloatArray;
			this.temp = new float[length];
		}

		@Override
		public void apply(ShaderProgram sp, String name) {
			int location = sp.getUniformLocation(name);
			inner.getArray(temp);
			sp.setUniform1fv(location, temp, 0, temp.length);
		}
	}
	
	private static class UniformVec2ArrayParam implements IGdxShaderParam {
		private float[] temp;
		private IUniformVector2 inner;

		public UniformVec2ArrayParam(IUniformVector2 uniformVec2Array, int length) {
			this.inner = uniformVec2Array;
			this.temp = new float[length];
		}

		@Override
		public void apply(ShaderProgram sp, String name) {
			int location = sp.getUniformLocation(name);
			inner.getVec2(temp);
			sp.setUniform2fv(location, temp, 0, temp.length);
		}
	}
}
