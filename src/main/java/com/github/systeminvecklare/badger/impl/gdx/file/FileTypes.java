package com.github.systeminvecklare.badger.impl.gdx.file;

public class FileTypes {
	private FileTypes() {}
	
	public static IFileType IMAGE = new FileType();
	public static IFileType AUDIO = new FileType();
	public static IFileType SHADER = new FileType();
	public static IFileType FONT = new FileType();
	public static IFileType OTHER = new FileType();
	
	private static class FileType implements IFileType {
	}
}
