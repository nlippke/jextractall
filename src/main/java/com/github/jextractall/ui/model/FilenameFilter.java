package com.github.jextractall.ui.model;

import java.util.function.Predicate;

public class FilenameFilter implements Predicate<ExtractorTask> {

	private String filter;
	private String lowerCaseFilter;
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		if (filter != null) {
			lowerCaseFilter = filter;
		}
	}

	@Override
	public boolean test(ExtractorTask task) {
		if (filter == null || filter.isEmpty()) {
            return true;
        }

        if (task.getFileName().toLowerCase().contains(lowerCaseFilter)) {
            return true; // Filter matches first name.
        } 
        return false; // Does not match.
	}

}
