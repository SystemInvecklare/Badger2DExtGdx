package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.Locale;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.systeminvecklare.badger.impl.gdx.Badger2DExtGdx;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;

public class ShaderStore {
	private static final AbstractStore<ShaderKey, ShaderProgram> store = new AbstractStore<ShaderKey, ShaderProgram>() {
		@Override
		protected ShaderProgram loadItem(ShaderKey shaderKey) {
			ShaderProgram shaderProgram = new ShaderProgram(toFileHandle(shaderKey.vertexShaderName), toFileHandle(shaderKey.fragmentShaderName));
			if(!shaderProgram.isCompiled()) {
				throw new RuntimeException(shaderProgram.getLog());
			}
			return shaderProgram;
		}
		
		@Override
		protected void disposeItem(ShaderProgram item) {
			item.dispose();
		}
	};
	static {
//		ShaderProgram.pedantic = false;
		FlashyGdxEngine.get().registerStore(store);
	}
	
	private static final String DEFAULT_VERTEXT_SHADER = Badger2DExtGdx.classpath("shaders/vertex.glsl");//"classpath:res/badger2dextgdx/shaders/vertex.glsl";
	
	public static ShaderProgram getFragmentShader(String fragmentShaderName) {
		return store.getItem(new ShaderKey(DEFAULT_VERTEXT_SHADER, fragmentShaderName));
	}
	

	public static ShaderProgram getShader(String vertexShaderName, String fragmentShaderName) {
		return store.getItem(new ShaderKey(vertexShaderName, fragmentShaderName));
	}
	
	private static FileHandle toFileHandle(String pathName) {
		int colonPos = pathName.indexOf(":");
		if(colonPos != -1) {
			String fileType = pathName.substring(0, colonPos);
			fileType = fileType.toLowerCase(Locale.ENGLISH);
			if(fileType.length() >= 1) {
				fileType = fileType.substring(0, 1).toUpperCase(Locale.ENGLISH)+fileType.substring(1);
				FileType fileTypeEnum;
				try {
					fileTypeEnum = FileType.valueOf(fileType);
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
					fileTypeEnum = null;
				}
				if(fileTypeEnum != null) {
					String path = pathName.substring(colonPos+1);
					if(fileTypeEnum == FileType.Classpath) {
						return Gdx.files.classpath(path); //Workaround for bug in LibGDX Gwt-module
					}
					return Gdx.files.getFileHandle(path, fileTypeEnum);
				}
			}
		}
		return Gdx.files.internal(pathName);
	}
	
	private static class ShaderKey {
		private final String vertexShaderName;
		private final String fragmentShaderName;
		
		public ShaderKey(String vertexShaderName, String fragmentShaderName) {
			this.vertexShaderName = vertexShaderName;
			this.fragmentShaderName = fragmentShaderName;
		}

		@Override
		public int hashCode() {
			return Objects_hashCode(vertexShaderName)*31 ^ Objects_hashCode(fragmentShaderName);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ShaderKey) {
				ShaderKey other = (ShaderKey) obj;
				return Objects_equals(this.vertexShaderName, other.vertexShaderName)
					&& Objects_equals(this.fragmentShaderName, other.fragmentShaderName);
			} else {
				return false;
			}
		}
	}
	
	private static int Objects_hashCode(Object object) {
		return object == null ? 0 : object.hashCode();
	}
	
	private static boolean Objects_equals(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}
}
