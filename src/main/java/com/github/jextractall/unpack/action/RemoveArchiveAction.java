package com.github.jextractall.unpack.action;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jextractall.unpack.ExtractionResult;

public class RemoveArchiveAction  implements PostAction{

	private boolean removeEmptyDirectory = false;

	public RemoveArchiveAction() {

	}

	public void run(ExtractionResult result) throws Exception {
		for (Path fileToBeDeleted : result.getArchiveVolumes()) {
			Files.deleteIfExists(fileToBeDeleted);
		}

		if (removeEmptyDirectory && result.getArchiveVolumes().size() > 0) {
			Path directory = result.getArchiveVolumes().get(0).getParent();
			if (Files.list(directory)
					.filter(p->!p.getFileName().toString().matches("\\.+"))
					.count() == 0) {
				Files.deleteIfExists(directory);
			}
		}
	}


	public boolean isRemoveEmptyDirectory() {
		return removeEmptyDirectory;
	}


	public void setRemoveEmptyDirectory(boolean removeEmptyDirectory) {
		this.removeEmptyDirectory = removeEmptyDirectory;
	}
}
