package com.github.jextractall.unpack;

import java.nio.file.Path;

public class DummyExtractor implements Extractor {

    private String[] supportedExtensions;
    private ExtractionResult result;

    public DummyExtractor(ExtractionResult result, String... supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
        this.result = result;
    }

    /** {@inheritDoc} */
    @Override
    public ExtractionResult extractArchive(Path pathToarchive, ExtractorCallback callback) {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canExtract(Path pathToArchive) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String[] getSupportedExtensions() {
        return supportedExtensions;
    }

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

}
