package com.github.jextractall.ui.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.StringUtils;

import com.github.jextractall.exceptions.IncorrectPasswordException;
import com.github.jextractall.exceptions.InvalidDestination;
import com.github.jextractall.ui.MainController;
import com.github.jextractall.ui.PasswordDialog;
import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModel.ExtractorModel;
import com.github.jextractall.unpack.ExtractionResult;
import com.github.jextractall.unpack.ExtractionResult.STATUS;
import com.github.jextractall.unpack.Extractor;
import com.github.jextractall.unpack.ExtractorCallback;
import com.github.jextractall.unpack.action.RemoveArchiveAction;
import com.github.jextractall.unpack.common.FileAdvisor;
import com.github.jextractall.unpack.common.FileUtils;
import com.github.jextractall.unpack.common.Result.ResultBuilder;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

public class ExtractorTask extends Task<Void> implements ExtractorCallback {

    private Extractor extractor;
    private Path pathToArchive;
    private Path outDir;
    private Iterator<String> knownPasswordIterator;

    private final ObjectProperty<ExtractionResult> result = new SimpleObjectProperty<>(null);

    public final ReadOnlyObjectProperty<ExtractionResult> resultProperty() {
        return result;
    }

    private ConfigModel config;
    private PathMatcher ignoreExtractedFilesMatcher;
    private boolean cancelFlag = false;

    private PasswordModel password = null;
    private StringProperty statusProperty = new SimpleStringProperty(null);

    public final ReadOnlyStringProperty statusProperty() {
        return statusProperty;
    }

    public ExtractorTask(Extractor extractor, Path pathToArchive) {
        this.extractor = extractor;
        this.pathToArchive = pathToArchive;
        updateProgress(0, 1);
        setConfig(ConfigModelFactory.defaults());
        knownPasswordIterator = config.getExtractorModel().getPasswords().iterator();
    }

    private Path determineTargetDirectory() {

        ExtractorModel ec = config.getExtractorModel();

        if (ec.getExtractToSameDirectory()) {
            return pathToArchive.getParent();
        }
        if (ec.getExtractToSubdirectoy() && StringUtils.isNotEmpty(ec.getSubdirectory())) {

            return FileSystems.getDefault().getPath(pathToArchive.getParent().toString(), ec.getSubdirectory());
        }
        if (ec.getExtractToDirectoy() && StringUtils.isNotEmpty(ec.getDirectory())) {
            return Paths.get(ec.getDirectory(), pathToArchive.getParent().getFileName().toString());
        }
        return null;
    }

    @Override
    protected Void call() throws Exception {

        cancelFlag = false;

        if (extractor == null) {
            result.set(ResultBuilder.newInstance().withResult(STATUS.FAILURE)
                    .withException(new Exception(Messages.getMessage("error.unknownArchive"))).create());
        } else {
            outDir = determineTargetDirectory();

            if (!FileUtils.canWriteIntoDirectory(outDir)) {
                throw new InvalidDestination(Messages.getMessage("error.extractInto", outDir.toAbsolutePath()));
            }
            knownPasswordIterator = config.getExtractorModel().getPasswords().iterator();

            ExtractionResult extractionResult = null;
            while (extractionResult == null || extractionResult.getException() instanceof IncorrectPasswordException) {
                password = null;
            		extractionResult = extractor.extractArchive(pathToArchive, this);
            }    
            result.set(extractionResult);
        }

        if (result.get().getStatus() == STATUS.OK) {
            if (config.getPostExtractionModel().getRemoveArchivedFiles()) {
                new RemoveArchiveAction().run(result.get());
            }
            statusProperty.set(Messages.getMessage("main.taskview.ok"));
            if (password != null && password.remember()) {
                config.getExtractorModel().addPassword(password.getPassword());
            }
        } else {
            statusProperty.set(Messages.getMessage(cancelFlag ? "main.taskview.cancel" : "main.taskview.nok"));
            throw result.get().getException();
        }

        return null;
    }

    public List<Path> getExtractedFiles() {
        return result.get().getExtractedFiles();
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

    public boolean isValid() {
        return extractor != null && extractor.canExtract(pathToArchive);
    }

    @Override
    public void volumeProgress(Path currentVolume, long current, long total) {
        if (!cancelFlag) {
            updateProgress((double) current / total, 1);
        }
        if (current == total) {
            statusProperty.set(Messages.getMessage("main.taskview.ok"));
        }
    }

    @Override
    public FileAdvisor advice(String fileName) {

        Path fileToCreate = FileUtils.adjustPath(outDir, fileName);

        if (cancelFlag) {
            return FileAdvisor.skip(fileToCreate);
        }

        statusProperty.setValue(fileName);

        if (ignoreExtractedFilesMatcher != null && ignoreExtractedFilesMatcher.matches(fileToCreate)) {
            return FileAdvisor.skip(fileToCreate);
        }

        if (Files.exists(fileToCreate)) {
            if (Files.isDirectory(fileToCreate)) {
                return FileAdvisor.skip(fileToCreate);
            } else {
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
                    .getPathMatcher("glob:{" + config.getExtractorModel().getGlobToIgnore() + "}");
        } else {
            ignoreExtractedFilesMatcher = null;
        }
    }

    public ExtractionResult getResult() {
        return result.get();
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

    @Override
    public String getPassword() {

    		if (password != null) {
    			return password.getPassword();
    		}
    	
        if (knownPasswordIterator.hasNext()) {
            password = new PasswordModel(knownPasswordIterator.next(), false);
            return password.getPassword();
        }

        final FutureTask<PasswordModel> askForPassword = new FutureTask<>(() -> {
            try {
                PasswordDialog dialog;
                dialog = new PasswordDialog(MainController.getStage(), pathToArchive.getFileName().toString());
                Optional<PasswordModel> result = dialog.showAndWait();
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (IOException e) {
            }
            return null;
        });

        Platform.runLater(askForPassword);
        try {
            this.password = askForPassword.get();
        } catch (Exception e) {
        }
        return password.getPassword();
    }

    public String getStatus() {
        return statusProperty.get();
    }

    public Optional<String> getNewPassword() {
        if (password == null || !password.remember()) {
            return Optional.ofNullable(null);
        }
        return Optional.of(password.getPassword());
    }
}
