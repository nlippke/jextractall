package com.github.jextractall.ui.model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConfigModel implements Cloneable {

	public class ScannerModel implements Cloneable {

		private StringProperty fileTypes = new SimpleStringProperty();
		public StringProperty fileTypesProperty() { return fileTypes; }; 

		private StringProperty globToIgnore = new SimpleStringProperty();
		public StringProperty globToIgnoreProperty() { return globToIgnore; };

		public String[] getFileTypes() {
			if (fileTypes.get() != null) {
				return fileTypes.get().split(",");
			}
			return new String[0];
		}

		public void setFileTypes(String[] fileTypes) {
			if (fileTypes == null) {
				return;
			}
			this.fileTypes.set(Stream.of(fileTypes)
					.collect(Collectors.joining(",")));
		}

		public void setGlobToIgnore(String glob) {
			this.globToIgnore.set(glob);
		}
		
		public String getGlobToIgnore() {
			return this.globToIgnore.get();
		}
		
		public String convertFileTypesToGlob() {
			return "*.{"+fileTypes.get()+"}";
		}
		
		public Object clone() {
			ScannerModel model = new ScannerModel();
			model.setFileTypes(getFileTypes());
			model.setGlobToIgnore(getGlobToIgnore());
			return scannerModel;
		}
	}

	public class ExtractorModel implements Cloneable {

		private BooleanProperty extractToSameDirectoy = new SimpleBooleanProperty();
		public BooleanProperty extractToSameDirectoryProperty() { return extractToSameDirectoy; };

		private BooleanProperty extractToSubdirectoy = new SimpleBooleanProperty();
		public BooleanProperty extractToSubdirectoryProperty() { return extractToSubdirectoy; };

		private StringProperty subdirectory = new SimpleStringProperty();
		public StringProperty subdirectoryProperty() { return subdirectory; };

		private BooleanProperty extractToDirectoy = new SimpleBooleanProperty();
		public BooleanProperty extractToDirectoryProperty() { return extractToDirectoy; };

		private StringProperty directory = new SimpleStringProperty();
		public StringProperty directoryProperty() { return directory; };

		private BooleanProperty overrideExisting = new SimpleBooleanProperty();
		public BooleanProperty overrideExistingProperty() { return overrideExisting; };

		private BooleanProperty ignoreCreateFilesMatchingGlob = new SimpleBooleanProperty();
		public BooleanProperty ignoreCreateFilesMatchingGlobProperty() { return ignoreCreateFilesMatchingGlob; };

		private StringProperty globToIgnore = new SimpleStringProperty();
		public StringProperty globToIgnoreProperty() { return globToIgnore; };

		private BooleanProperty skipExisting = new SimpleBooleanProperty();
		public BooleanProperty skipExistingProperty() { return skipExisting; };

		public boolean getExtractToSameDirectory() {
			return extractToSameDirectoy.get();
		}

		public void setExtractToSameDirectory(boolean value) {
			extractToSameDirectoy.set(value);
		}

		public boolean getExtractToSubdirectoy() {
			return extractToSubdirectoy.get();
		}

		public void setExtractToSubdirectoy(boolean value) {
			extractToSubdirectoy.set(value);
		}

		public String getSubdirectory() {
			return subdirectory.get();
		}

		public void setSubdirectory(String subdirectory) {
			this.subdirectory.set(subdirectory);
		}

		public boolean getExtractToDirectoy() {
			return extractToDirectoy.get();
		}

		public void setExtractToDirectoy(boolean value) {
			this.extractToDirectoy.set(value);
		}

		public String getDirectory() {
			return directory.get();
		}

		public void setDirectory(String directory) {
			this.directory.set(directory);
		}

		public boolean getOverrideExisting() {
			return overrideExisting.get();
		}

		public void setOverrideExisting(boolean value) {
			this.overrideExisting.set(value);
		}

		public boolean getIgnoreCreateFilesMatchingGlob() {
			return ignoreCreateFilesMatchingGlob.get();
		}

		public void setIgnoreCreateFilesMatchingGlob(boolean value) {
			this.ignoreCreateFilesMatchingGlob.set(value);
		}

		public String getGlobToIgnore() {
			return globToIgnore.get();
		}

		public void setGlobToIgnore(String globToIgnore) {
			this.globToIgnore.set(globToIgnore);
		}


		public boolean getSkipExisting() {
			return this.skipExisting.get();
		}

		public void setSkipExisting(boolean value) {
			this.skipExisting.set(value);
		}

		public Object clone() {
			ExtractorModel model = new ExtractorModel();
			model.setDirectory(getDirectory());
			model.setExtractToDirectoy(getExtractToDirectoy());
			model.setExtractToSameDirectory(getExtractToSameDirectory());
			model.setExtractToSubdirectoy(getExtractToSubdirectoy());
			model.setGlobToIgnore(getGlobToIgnore());
			model.setIgnoreCreateFilesMatchingGlob(getIgnoreCreateFilesMatchingGlob());
			model.setOverrideExisting(getOverrideExisting());
			model.setSkipExisting(getSkipExisting());
			model.setSubdirectory(getSubdirectory());
			return model;
		}

	}

	public class PostExtractionModel implements Cloneable {

		private BooleanProperty removeArchivedFiles = new SimpleBooleanProperty();
		public BooleanProperty removeArchivedFilesProperty() { return removeArchivedFiles; };

		private BooleanProperty scanExtracted = new SimpleBooleanProperty();
		public BooleanProperty scanExtractedProperty() { return scanExtracted; };

		private BooleanProperty closeApplication = new SimpleBooleanProperty();
		public BooleanProperty closeApplicationProperty() { return closeApplication; };

		
		public boolean getRemoveArchivedFiles() {
			return removeArchivedFiles.get();
		}

		public void setRemoveArchivedFiles(boolean value) {
			this.removeArchivedFiles.set(value);
		}

		public void setScanExtracted(boolean value) {
			scanExtractedProperty().set(value);
		}

		public boolean getScanExtracted() {
			return scanExtractedProperty().get();
		}

		public void setCloseApplication(boolean value) {
			closeApplication.set(value);
		}
		
		public boolean getCloseApplication() {
			return closeApplication.get();
		}
		
		public Object clone() {
			PostExtractionModel model = new PostExtractionModel();
			model.setRemoveArchivedFiles(getRemoveArchivedFiles());
			model.setScanExtracted(getScanExtracted());
			model.setCloseApplication(getCloseApplication());
			return model;
		}
	}


	private ScannerModel scannerModel = new ScannerModel();
	private ExtractorModel extractorModel = new ExtractorModel();
	private PostExtractionModel postExtractionModel = new PostExtractionModel();

	public ScannerModel getScannerModel() {
		return scannerModel;
	}
	
	public ExtractorModel getExtractorModel() {
		return extractorModel;
	}
	
	public PostExtractionModel getPostExtractionModel() {
		return postExtractionModel;
	}

	@Override
	public Object clone() {
		ConfigModel model = new ConfigModel();
		model.extractorModel = (ExtractorModel) extractorModel.clone();
		model.postExtractionModel = (PostExtractionModel) postExtractionModel.clone();
		model.scannerModel = (ScannerModel) scannerModel.clone();
		return model;
	}
}

