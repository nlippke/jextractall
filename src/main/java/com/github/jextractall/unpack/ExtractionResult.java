package com.github.jextractall.unpack;

import java.nio.file.Path;
import java.util.List;

public interface ExtractionResult {

	enum STATUS { OK, ABORT, FAILURE };

	List<Path> getExtractedFiles();
	List<Path> getArchiveVolumes();
	Exception getException();
	STATUS getStatus();

}
