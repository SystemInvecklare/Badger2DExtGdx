package com.github.systeminvecklare.badger.impl.gdx.file;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public abstract class GdxFileResolver implements IFileResolver {
	public static final IFileResolver INTERNAL = new GdxFileResolver() {
		@Override
		public FileHandle resolve(IFileType fileType, String path) {
			return Gdx.files.internal(path);
		}
	};
	public static final IFileResolver EXTERNAL = new GdxFileResolver() {
		@Override
		public FileHandle resolve(IFileType fileType, String path) {
			return Gdx.files.external(path);
		}
	};
	public static final IFileResolver CLASSPATH = new GdxFileResolver() {
		@Override
		public FileHandle resolve(IFileType fileType, String path) {
			return Gdx.files.classpath(path);
		}
	};
	public static final IFileResolver ABSOLUTE = new GdxFileResolver() {
		@Override
		public FileHandle resolve(IFileType fileType, String path) {
			return Gdx.files.absolute(path);
		}
	};
	public static final IFileResolver LOCAL = new GdxFileResolver() {
		@Override
		public FileHandle resolve(IFileType fileType, String path) {
			return Gdx.files.local(path);
		}
	};
	
	private GdxFileResolver() {}
}
