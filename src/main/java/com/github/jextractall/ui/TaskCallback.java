package com.github.jextractall.ui;

import com.github.jextractall.ui.model.ExtractorTask;

public interface TaskCallback {

	void onComplete(ExtractorTask task);
	void onCancelled(ExtractorTask task);
	void onFailure(ExtractorTask task);
	
}
