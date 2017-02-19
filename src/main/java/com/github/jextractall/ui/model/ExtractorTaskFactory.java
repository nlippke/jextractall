package com.github.jextractall.ui.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.jextractall.unpack.Extractor;

public class ExtractorTaskFactory {

	private static HashMap<String, Class<? extends Extractor>> allExtractors = new HashMap<>();

	public ExtractorTaskFactory() {
	}


	public static String[] getSupportedFileTypes() {
	    return allExtractors.keySet().toArray(new String[0]);
	}

	public static void register(Class<? extends Extractor> extractorClass) {
        try {
            Extractor ex = extractorClass.newInstance();
            for (String s : ex.getSupportedExtensions()) {
                allExtractors.put(s, extractorClass);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Extractor is required to have a default constructor");
        }
	}

	public static void scanForfiles(Path start, List<ExtractorTask> store, String globToInclude,
	        String globToExclude) throws IOException {

		if (globToInclude != null && !globToInclude.startsWith("glob:")) {
			globToInclude = "glob:" + globToInclude;
		}

		if (globToExclude != null && !globToExclude.startsWith("glob:")) {
			globToExclude = "glob:{" + globToExclude + "}";
		}

	    final PathMatcher includeMatcher = globToInclude != null ?
	            FileSystems.getDefault().getPathMatcher(globToInclude) : null;

	    final PathMatcher excludeMatcher = globToExclude != null ?
	                    FileSystems.getDefault().getPathMatcher(globToExclude) : null;

		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

			    if (includeMatcher == null || !includeMatcher.matches(file.getFileName())) {
			        return FileVisitResult.CONTINUE;
			    }

			    if (excludeMatcher != null && excludeMatcher.matches(file)) {
			        return FileVisitResult.CONTINUE;
			    }

			    try {
					ExtractorTask newTask = createFromPath(file);
					if (!store.contains(newTask)) {
						store.add(newTask);
					}
				} catch (Exception e) {
				}

				return super.visitFile(file, attrs);
			}

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                if (excludeMatcher != null && excludeMatcher.matches(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return super.preVisitDirectory(dir, attrs);
            }

		});
	}

	public static ExtractorTask createFromPath(Path path) throws InstantiationException, IllegalAccessException {
		String[] splitter = path.toString().split("[.]");
		Optional<String> suffix = Stream.of(splitter).reduce((first, second) -> second);
		if (suffix.isPresent() && allExtractors.containsKey(suffix.get())) {
			Class<? extends Extractor> clazz = allExtractors.get(suffix.get());
			Extractor extractor = clazz.newInstance();
//			if (extractor.canExtract(path)) {
				return new ExtractorTask(extractor, path);
//			} 
		}
		return  new ExtractorTask(null, path);
//		throw new InvalidArchiveException(Messages.getMessage("error.extraction"));
	}
	
	
}
