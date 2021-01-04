package com.github.systeminvecklare.badger.impl.gdx.shader;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.systeminvecklare.badger.core.graphics.components.core.IDrawCycle;
import com.github.systeminvecklare.badger.core.graphics.components.shader.IShader;
import com.github.systeminvecklare.badger.impl.gdx.GdxDrawCycle;
import com.github.systeminvecklare.badger.impl.gdx.store.ShaderStore;

public class GdxShader implements IShader {
	private static final IShaderConfigurator DEFAULT_SHADER_CONFIGURATOR = new IShaderConfigurator() {
		@Override
		public void configure(ShaderProgram shaderProgram) {
		}
	};
	
	private final String vertexShaderName;
	private final String fragmentShaderName;
	private final IShaderConfigurator configurator;

	public GdxShader(String vertexShaderName, String fragmentShaderName, IShaderConfigurator configurator) {
		this.vertexShaderName = vertexShaderName;
		this.fragmentShaderName = fragmentShaderName;
		this.configurator = configurator;
	}
	
	public GdxShader(String vertexShaderName, String fragmentShaderName) {
		this(vertexShaderName, fragmentShaderName, DEFAULT_SHADER_CONFIGURATOR);
	}
	
	public GdxShader(String fragmentShaderName) {
		this(fragmentShaderName, DEFAULT_SHADER_CONFIGURATOR);
	}
	
	public GdxShader(String fragmentShaderName, IShaderConfigurator configurator) {
		this(null, fragmentShaderName, configurator);
	}

	@Override
	public void onBind(IDrawCycle drawCycle) {
		SpriteBatch batch = ((GdxDrawCycle) drawCycle).getSpriteBatch();
		ShaderProgram sp = getShaderProgram();
		batch.setShader(sp);
		getShaderConfigurator().configure(sp);
	}
	
	public String getVertexShaderName() {
		return vertexShaderName;
	}
	
	public String getFragmentShaderName() {
		return fragmentShaderName;
	}
	
	public IShaderConfigurator getShaderConfigurator() {
		return configurator;
	}

	private ShaderProgram getShaderProgram() {
		String currentVertexShaderName = getVertexShaderName();
		String currentFragmentShaderName = getFragmentShaderName();
		if(currentVertexShaderName == null) {
			return ShaderStore.getFragmentShader(currentFragmentShaderName);
		} else {
			return ShaderStore.getShader(currentVertexShaderName, currentFragmentShaderName);
		}
	}
}
