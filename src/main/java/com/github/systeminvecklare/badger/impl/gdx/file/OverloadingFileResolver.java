package com.github.systeminvecklare.badger.impl.gdx.file;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;

public class OverloadingFileResolver implements IFileResolver {
	private final List<IFileResolver> fileResolvers = new ArrayList<IFileResolver>();

	@Override
	public FileHandle resolve(IFileType fileType, String path) {
		FileHandle result = null;
		for(int i = fileResolvers.size() - 1; i >= 0; i--) {
			result = fileResolvers.get(i).resolve(fileType, path);
			if(result != null) {
				return result;
			}
		}
		return result;
	}

	public OverloadingFileResolver append(IFileResolver fileResolver) {
		fileResolvers.add(fileResolver);
		return this;
	}
}
