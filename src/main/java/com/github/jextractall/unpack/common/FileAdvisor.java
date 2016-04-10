package com.github.jextractall.unpack.common;

import java.nio.file.Path;

public class FileAdvisor {
	enum OPERATION { SKIP, CREATE, OVERRIDE };

	private OPERATION operation;
	private Path pathToFile;

	private FileAdvisor(Path pathToFile, OPERATION op) {
		this.operation = op;
		this.pathToFile = pathToFile;
	}

	public static FileAdvisor override(Path p) {
		return new FileAdvisor(p, OPERATION.OVERRIDE);
	}

	public static FileAdvisor create(Path p) {
		return new FileAdvisor(p, OPERATION.CREATE);
	}

	public static FileAdvisor skip(Path p) {
		return new FileAdvisor(p, OPERATION.SKIP);
	}

	public boolean create() {
		return operation == OPERATION.CREATE;
	}

	public boolean override() {
		return operation == OPERATION.OVERRIDE;
	}

	public boolean skip() {
		return operation == OPERATION.SKIP;
	}

	public Path getPath() {
		return pathToFile;
	}


}
