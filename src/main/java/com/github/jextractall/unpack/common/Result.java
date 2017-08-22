package com.github.jextractall.unpack.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.jextractall.unpack.ExtractionResult;

public class Result implements ExtractionResult {

	final ExtractionResult.STATUS status;
	final List<Path> extractedFiles;
	final List<Path> volumes;
	final Exception exception;

	public static class ResultBuilder {

		ExtractionResult.STATUS status;
		ArrayList<Path> extractedFiles;
		ArrayList<Path> volumeFiles;
		Exception exception;

		private ResultBuilder() {
			extractedFiles = new ArrayList<>();
			volumeFiles = new ArrayList<>();
		}

		public static ResultBuilder newInstance() {
			return new ResultBuilder().withResult(STATUS.OK);
		}

		public ResultBuilder withResult(ExtractionResult.STATUS status) {
			this.status = status;
			return this;
		}

		public ResultBuilder withExtractedFile(Path file) {
			if (!extractedFiles.contains(file)) {
				extractedFiles.add(file);
			}
			return this;
		}

		public ResultBuilder withVolumneFile(Path file) {
			if (!volumeFiles.contains(file)) {
				volumeFiles.add(file);
			}
			return this;
		}

		public ResultBuilder withException(Exception ex) {
			this.exception = ex;
			this.status = STATUS.FAILURE;
			return this;
		}

		public boolean isOK() {
			return exception == null;
		}

		public ExtractionResult create() {
			return new Result(status, extractedFiles, volumeFiles, exception);
		}
	}


	public Result(STATUS status, ArrayList<Path> extractedFiles, ArrayList<Path> volumeFiles, Exception exception) {
		this.status = status;
		this.extractedFiles = Collections.unmodifiableList(extractedFiles);
		this.volumes = Collections.unmodifiableList(volumeFiles);
		this.exception = exception;
	}


	public List<Path> getExtractedFiles() {
		return extractedFiles;
	}

	public List<Path> getArchiveVolumes() {
		return volumes;
	}

	public Exception getException() {
		return exception;
	}


	public STATUS getStatus() {
		return status;
	}
}
