package com.github.jextractall.unpack.action;

import com.github.jextractall.unpack.ExtractionResult;

public interface PostAction {
	public void run(ExtractionResult result) throws Exception;
}
