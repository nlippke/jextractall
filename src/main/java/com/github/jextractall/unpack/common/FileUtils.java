package com.github.jextractall.unpack.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

	public static boolean canWriteIntoDirectory(Path destination) {
	    if (Files.notExists(destination)) {
	        try {
	            Files.createDirectories(destination);
	        } catch (IOException ioe) {
	            return false;
	        }
	    }
        if (!Files.isDirectory(destination)) {
            return false;
        }

        return Files.isWritable(destination);
    }

	public static Path adjustPath(Path root, String ancestor) {
	    return root.getFileSystem().getPath(root.toString(), ancestor);
	}
}
