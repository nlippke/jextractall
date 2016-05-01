package com.github.jextractall.unpack;

import java.nio.file.Path;

import com.github.jextractall.unpack.common.FileAdvisor;

public interface ExtractorCallback {
	void volumeProgress(Path currentVolume, long current, long total);
	FileAdvisor advice(String fileName);
	String getPassword();
}
