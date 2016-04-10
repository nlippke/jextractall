package com.github.jextractall.ui.model;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.github.jextractall.exceptions.InvalidDestination;
import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModel.ExtractorModel;
import com.github.jextractall.unpack.ExtractionResult;
import com.github.jextractall.unpack.ExtractionResult.STATUS;
import com.github.jextractall.unpack.Extractor;
import com.github.jextractall.unpack.ExtractorCallback;
import com.github.jextractall.unpack.action.RemoveArchiveAction;
import com.github.jextractall.unpack.common.FileAdvisor;
import com.github.jextractall.unpack.common.FileUtils;

import javafx.concurrent.Task;

public class ExtractorTask extends Task<Void> implements ExtractorCallback {

	private Extractor extractor;
	private Path pathToArchive;
	private Path outDir;
	private ExtractionResult result;
	private ConfigModel config;
	private PathMatcher ignoreExtractedFilesMatcher;
	private boolean cancelFlag = false;

	public ExtractorTask(Extractor extractor, Path pathToArchive) {
		this.extractor = extractor;
		this.pathToArchive = pathToArchive;
		updateProgress(0,1);
		setConfig(ConfigModelFactory.defaults());
	}

	private Path determineTargetDirectory() {

		ExtractorModel ec = config.getExtractorModel();

	    if (ec.getExtractToSameDirectory()) {
	        return pathToArchive.getParent();
	    }
	    if (ec.getExtractToSubdirectoy() && StringUtils.isNotEmpty(ec.getSubdirectory())) {

	        return FileSystems.getDefault().getPath(pathToArchive.getParent().toString(),
	                ec.getSubdirectory());
	    }
	    if (ec.getExtractToDirectoy() && StringUtils.isNotEmpty(ec.getDirectory())) {
	        return Paths.get(ec.getDirectory());
	    }
	    return null;
	}

	@Override
	protected Void call() throws Exception {
		cancelFlag = false;
	    outDir = determineTargetDirectory();

		if (!FileUtils.canWriteIntoDirectory(outDir)) {
			throw new InvalidDestination(Messages.getMessage("error.extractInto",outDir.toAbsolutePath()));
		}

		result = extractor.extractArchive(pathToArchive, this);

		if (result.getStatus() == STATUS.OK) {
		    if (config.getPostExtractionModel().getRemoveArchivedFiles()) {
		        new RemoveArchiveAction().run(result);
		    }
		} else {
			throw result.getException();
		}
		
		return null;
	}

	public List<Path> getExtractedFiles() {
	    return result.getExtractedFiles();
	}

	public Path getArchive() {
		return pathToArchive;
	}

	public Path getOutDir() {
		return outDir;
	}

	public String getFileName() {
		return pathToArchive.getFileName().toString();
	}

	@Override
	public void volumeProgress(Path currentVolume, long current, long total) {
		if (!cancelFlag) {
			updateProgress((double) current / total, 1);
		}
	}

	@Override
	public FileAdvisor advice(String fileName) {
		
		Path fileToCreate = FileUtils.adjustPath(outDir, fileName);

		if (cancelFlag) {
			return FileAdvisor.skip(fileToCreate);
		}
		
		if (ignoreExtractedFilesMatcher != null
		        && ignoreExtractedFilesMatcher.matches(fileToCreate)) {
		    return FileAdvisor.skip(fileToCreate);
		}

		if (Files.exists(fileToCreate)) {
			if (Files.isDirectory(fileToCreate)) {
				return FileAdvisor.skip(fileToCreate);
			} else{
				return FileAdvisor.override(fileToCreate);
			}
		}
		return FileAdvisor.create(fileToCreate);
	}

    public void setConfig(ConfigModel config) {
        this.config = config;

        if (config.getExtractorModel().getIgnoreCreateFilesMatchingGlob()
                && StringUtils.isNotEmpty(config.getExtractorModel().getGlobToIgnore())) {
            ignoreExtractedFilesMatcher = FileSystems.getDefault()
                    .getPathMatcher("glob:{"+config.getExtractorModel().getGlobToIgnore()+"}");
        } else {
            ignoreExtractedFilesMatcher = null;
        }
    }

    public ExtractionResult getResult() {
        return result;
    }

    public ConfigModel getConfig() {
        return config;
    }

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancelFlag = true;
		extractor.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

	
    public boolean isCancellable() {
    	return getState() == State.READY || getState() == State.SCHEDULED || getState() == State.RUNNING;
    }
    
    public ExtractorTask copyTask() {
    	return new ExtractorTask(extractor, pathToArchive);
    }
}
