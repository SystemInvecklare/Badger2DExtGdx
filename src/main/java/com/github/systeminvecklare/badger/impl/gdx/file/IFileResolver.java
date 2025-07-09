package com.github.systeminvecklare.badger.impl.gdx.file;

import com.badlogic.gdx.files.FileHandle;

public interface IFileResolver {
	/**
	 * @return A FileHandle (that may be pointing to a file that does not exist) or null if the IFileResolver decides it is not able to resolve the type-path combination.
	 */
	FileHandle resolve(IFileType fileType, String path);
}
