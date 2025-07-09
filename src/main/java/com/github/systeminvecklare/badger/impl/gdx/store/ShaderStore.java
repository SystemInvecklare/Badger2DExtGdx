package com.github.systeminvecklare.badger.impl.gdx.store;

import java.util.Locale;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.systeminvecklare.badger.impl.gdx.FlashyGdxEngine;
import com.github.systeminvecklare.badger.impl.gdx.file.FileTypes;

public class ShaderStore {
	private static final AbstractStore<ShaderKey, ShaderProgram> store = new AbstractStore<ShaderKey, ShaderProgram>() {
		@Override
		protected ShaderProgram loadItem(ShaderKey shaderKey) {
			
			ShaderProgram shaderProgram = new ShaderProgram(readShader(shaderKey.vertexShaderName), readShader(shaderKey.fragmentShaderName));
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
	
	private static final String DEFAULT_VERTEXT_SHADER_KEY = "DEFAULT_VERTEXT_SHADER_KEY";
	private static String DEFAULT_VERTEXT_SHADER = "#ifdef GL_ES\r\n" + 
			"    precision mediump float;\r\n" + 
			"#endif\r\n" + 
			"\r\n" + 
			"attribute vec4 a_position;\r\n" + 
			"attribute vec4 a_color;\r\n" + 
			"attribute vec2 a_texCoord0;\r\n" + 
			"\r\n" + 
			"uniform mat4 u_projTrans;\r\n" + 
			"\r\n" + 
			"varying vec4 v_color;\r\n" + 
			"varying vec2 v_texCoords;\r\n" + 
			"\r\n" + 
			"void main() {\r\n" + 
			"    v_color = a_color;\r\n" + 
			"    v_texCoords = a_texCoord0;\r\n" + 
			"    gl_Position = u_projTrans * a_position;\r\n" + 
			"}";
	
	public static ShaderProgram getFragmentShader(String fragmentShaderName) {
		return store.getItem(new ShaderKey(DEFAULT_VERTEXT_SHADER_KEY, fragmentShaderName));
	}
	

	public static ShaderProgram getShader(String vertexShaderName, String fragmentShaderName) {
		return store.getItem(new ShaderKey(vertexShaderName, fragmentShaderName));
	}
	
	private static String readShader(String shaderKey) {
		if(DEFAULT_VERTEXT_SHADER_KEY.equals(shaderKey)) {
			return DEFAULT_VERTEXT_SHADER;
		} else {
			return toFileHandle(shaderKey).readString();
		}
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
		return FlashyGdxEngine.get().getFileResolver().resolve(FileTypes.SHADER, pathName);
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
