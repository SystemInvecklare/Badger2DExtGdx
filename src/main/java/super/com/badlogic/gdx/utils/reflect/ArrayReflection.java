package com.badlogic.gdx.utils.reflect;

import com.badlogic.gdx.Gdx;

// LibGDX fails. This fixes it.
public final class ArrayReflection {
    public static Object newInstance(Class c, int size) {
        return new Object[size];
    }
    
    public static int getLength(Object array) {
        return ((Object[])array).length;
    }
    
    public static Object get(Object array, int index) {
        return ((Object[])array)[index];
    }
    
    public static void set(Object array, int index, Object value) {
        ((Object[])array)[index] = value;
    }
}