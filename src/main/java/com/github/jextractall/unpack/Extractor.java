package com.github.jextractall.unpack;

import java.nio.file.Path;

public interface Extractor {

	ExtractionResult extractArchive(Path pathToarchive, ExtractorCallback callback);

	void cancel();
	
	boolean canExtract(Path pathToArchive);

    String[] getSupportedExtensions();
}
