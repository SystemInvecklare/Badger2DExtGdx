/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gwtref.client;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.math.Matrix4;
import com.google.gwt.core.client.GWT;

public class ReflectionCache {
	private static IReflectionCache instance = GWT.create(IReflectionCache.class);
	
	public static native void log(String msg) /*-{
    console.log(msg);
	}-*/;

	public static Type forName (String name) throws ClassNotFoundException {
		Type type = instance.forName(convert(name));
		if (type == null) {
			throw new RuntimeException("Couldn't find Type for class '" + name + "'");
		}
		return type;
	}

	public static Type getType (Class clazz) {
		if (clazz == null) return null;
		String className = clazz.getName();
		// This hits. So it seems like there is an obfuscation issue with GWT. clazz.getName() SHOULD return the correct string.
		// For now, let's just fix it 'manually'
		if(clazz == GlyphRun.class) {
			className = "com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun";
		} else if(clazz == Color.class) {
			className = "com.badlogic.gdx.graphics.Color";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.GlyphLayout.class) {
			className = "com.badlogic.gdx.graphics.g2d.GlyphLayout";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.TextureRegion.class) {
			className = "com.badlogic.gdx.graphics.g2d.TextureRegion";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.Sprite.class) {
			className = "com.badlogic.gdx.graphics.g2d.Sprite";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.BitmapFont.class) {
			className = "com.badlogic.gdx.graphics.g2d.BitmapFont";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.NinePatch.class) {
			className = "com.badlogic.gdx.graphics.g2d.NinePatch";
		} else if(clazz == com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion.class) {
			className = "com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion";
//		} else if(clazz == com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute.class) {
//			className = "com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute";
		} else if(clazz == com.badlogic.gdx.graphics.Color.class) {
			className = "com.badlogic.gdx.graphics.Color";
		} else if(clazz == com.badlogic.gdx.graphics.Texture.class) {
			className = "com.badlogic.gdx.graphics.Texture";
		} else if(clazz == com.badlogic.gdx.utils.Array.class) {
			className = "com.badlogic.gdx.utils.Array";
		} else if(clazz == com.badlogic.gdx.utils.Disposable.class) {
			className = "com.badlogic.gdx.utils.Disposable";
		} else if(clazz == com.badlogic.gdx.utils.Json.class) {
			className = "com.badlogic.gdx.utils.Json";
		} else if(clazz == com.badlogic.gdx.utils.ObjectMap.class) {
			className = "com.badlogic.gdx.utils.ObjectMap";
		} else if(clazz == com.badlogic.gdx.utils.OrderedMap.class) {
			className = "com.badlogic.gdx.utils.OrderedMap";
		} else if(clazz == com.badlogic.gdx.utils.Queue.class) {
			className = "com.badlogic.gdx.utils.Queue";
		} else if(clazz == com.badlogic.gdx.graphics.VertexAttribute.class) {
			className = "com.badlogic.gdx.graphics.VertexAttribute";
		} else if(clazz == com.badlogic.gdx.Net.class) {
			className = "com.badlogic.gdx.Net";
		} else if(clazz == com.badlogic.gdx.maps.MapObject.class) {
			className = "com.badlogic.gdx.maps.MapObject";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ParticleEffect";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ParticleController.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ParticleController";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ResourceData.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ResourceData";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData";
		} else if(clazz == com.badlogic.gdx.graphics.g3d.particles.ParallelArray.class) {
			className = "com.badlogic.gdx.graphics.g3d.particles.ParallelArray";
		} else if(clazz == java.util.Collection.class) {
			className = "java.util.Collection";
		} else if(clazz == java.util.List.class) {
			className = "java.util.List";
		} else if(clazz == java.util.ArrayList.class) {
			className = "java.util.ArrayList";
		} else if(clazz == java.util.Map.class) {
			className = "java.util.Map";
		} else if(clazz == java.util.HashMap.class) {
			className = "java.util.HashMap";
		} else if(clazz == java.lang.String.class) {
			className = "java.lang.String";
		} else if(clazz == java.lang.Boolean.class) {
			className = "java.lang.Boolean";
		} else if(clazz == java.lang.Byte.class) {
			className = "java.lang.Byte";
		} else if(clazz == java.lang.Short.class) {
			className = "java.lang.Short";
		} else if(clazz == java.lang.Character.class) {
			className = "java.lang.Character";
		} else if(clazz == java.lang.Integer.class) {
			className = "java.lang.Integer";
		} else if(clazz == java.lang.Float.class) {
			className = "java.lang.Float";
		} else if(clazz == java.lang.Double.class) {
			className = "java.lang.Double";
		} else if(clazz == java.lang.CharSequence.class) {
			className = "java.lang.CharSequence";
		} else if(clazz == java.lang.Enum.class) {
			className = "java.lang.Enum";
		} else if(clazz == java.lang.Object.class) {
			className = "java.lang.Object";
		}
		Type type = instance.forName(convert(className));
		if (type == null) {
			throw new RuntimeException("Couldn't find Type for class '" + clazz.getName() + "'");
		}
		return type;
	}

	private static String convert (String className) {
		if (className.startsWith("[")) {
			int dimensions = 0;
			char c = className.charAt(0);
			String suffix = "";
			while (c == '[') {
				dimensions++;
				suffix += "[]";
				c = className.charAt(dimensions);
			}
			char t = className.charAt(dimensions);
			switch (t) {
			case 'Z':
				return "boolean" + suffix;
			case 'B':
				return "byte" + suffix;
			case 'C':
				return "char" + suffix;
			case 'L':
				return className.substring(dimensions + 1, className.length() - 1).replace('$', '.') + suffix;
			case 'D':
				return "double" + suffix;
			case 'F':
				return "float" + suffix;
			case 'I':
				return "int" + suffix;
			case 'J':
				return "long" + suffix;
			case 'S':
				return "short" + suffix;
			default:
				throw new IllegalArgumentException("Couldn't transform '" + className + "' to qualified source name");
			}
		} else {
			return className.replace('$', '.');
		}
	}

	public static Object newArray (Class componentType, int size) {
		return instance.newArray(getType(componentType), size);
	}

	public static Object getFieldValue (Field field, Object obj) throws IllegalAccessException {
		return instance.get(field, obj);
	}

	public static void setFieldValue (Field field, Object obj, Object value) throws IllegalAccessException {
		instance.set(field, obj, value);
	}

	public static Object invoke (Method method, Object obj, Object[] params) {
		return instance.invoke(method, obj, params);
	}

	public static int getArrayLength (Type type, Object obj) {
		return instance.getArrayLength(type, obj);
	}

	public static Object getArrayElement (Type type, Object obj, int i) {
		return instance.getArrayElement(type, obj, i);
	}

	public static void setArrayElement (Type type, Object obj, int i, Object value) {
		instance.setArrayElement(type, obj, i, value);
	}
}
