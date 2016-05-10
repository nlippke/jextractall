package com.github.jextractall.ui.model;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import com.github.jextractall.ui.model.ConfigModel.ExtractorModel;
import com.github.jextractall.ui.model.ConfigModel.PostExtractionModel;
import com.github.jextractall.ui.model.ConfigModel.ScannerModel;

public class ConfigModelFactory {
	
    private static final String SCANNER_GLOB_TO_IGNORE = "scanner.globToIgnore";
	private static final String SCANNER_FILE_TYPES = "scanner.fileTypes";
	private static final String POST_REMOVE_ARCHIVED = "post.removeArchived";
	private static final String POST_AUTO_SCAN = "post.autoScan";
	private static final String POST_CLOSE_APPLICATION = "post.closeApplication";
	private static final String EXTRACT_SKIP_EXISTING = "extract.skipExisting";
	private static final String EXTRACT_GLOB_TO_IGNORE = "extract.globToIgnore";
	private static final String EXTRACT_TO_SUBDIRECTORY = "extract.subDirectory";
	private static final String EXTRACT_TO_DIRECTORY = "extract.directory";


	public static ConfigModel defaults() {
        ConfigModel model = new ConfigModel();
        model.getExtractorModel().setExtractToSameDirectory(true);
        model.getExtractorModel().setOverrideExisting(true);
        model.getScannerModel().setFileTypes(ExtractorTaskFactory.getSupportedFileTypes());
        return model;
    }

	public static ConfigModel commandLine() {
		ConfigModel model = defaults();
		model.getPostExtractionModel().setCloseApplication(true);
		return model;
	}
	
    public static ConfigModel load() throws ConfigurationException {
        PropertiesConfiguration config = createConfiguration();
        File configFile = getConfigurationFile();
        ConfigModel model = defaults();
        if (!configFile.exists()) {
            return model;
        }
        config.load(configFile);
        
        ExtractorModel extractor = model.getExtractorModel();
        ScannerModel scanner = model.getScannerModel();
        PostExtractionModel postExtraction = model.getPostExtractionModel();
        
        if (StringUtils.isNotEmpty(config.getString(EXTRACT_TO_DIRECTORY))) {
        	extractor.setDirectory(config.getString(EXTRACT_TO_DIRECTORY));
        	extractor.setExtractToDirectoy(true);
        } else if (StringUtils.isNotEmpty(config.getString(EXTRACT_TO_SUBDIRECTORY))) {
        	extractor.setSubdirectory(config.getString(EXTRACT_TO_SUBDIRECTORY));
        	extractor.setExtractToSubdirectoy(true);
        }
        extractor.setExtractToSameDirectory(
        		!(extractor.getExtractToDirectoy() || extractor.getExtractToSubdirectoy()));
        if (StringUtils.isNotEmpty(config.getString(EXTRACT_GLOB_TO_IGNORE))) {
        	extractor.setGlobToIgnore(Arrays
        			.stream(config.getStringArray(EXTRACT_GLOB_TO_IGNORE))
        			.collect(Collectors.joining(",")));
        	extractor.setIgnoreCreateFilesMatchingGlob(true);
        }
        extractor.setSkipExisting(config.getBoolean(EXTRACT_SKIP_EXISTING, false));
        extractor.setOverrideExisting(!extractor.getSkipExisting());
        postExtraction.setRemoveArchivedFiles(config.getBoolean(POST_REMOVE_ARCHIVED, false));
        postExtraction.setScanExtracted(config.getBoolean(POST_AUTO_SCAN, false));
        postExtraction.setCloseApplication(config.getBoolean(POST_CLOSE_APPLICATION, false));
        scanner.setFileTypes(config.getStringArray(SCANNER_FILE_TYPES));
        if (StringUtils.isNotEmpty(config.getString(SCANNER_GLOB_TO_IGNORE))) {
        	scanner.setGlobToIgnore(Arrays
        			.stream(config.getStringArray(SCANNER_GLOB_TO_IGNORE))
        			.collect(Collectors.joining(",")));
        }
        return model;
    }

    public static void save(ConfigModel model) throws ConfigurationException {
    	
    	ExtractorModel extractor = model.getExtractorModel();
        ScannerModel scanner = model.getScannerModel();
        PostExtractionModel postExtraction = model.getPostExtractionModel();
    	
        PropertiesConfiguration config = createConfiguration();
        if (extractor.getExtractToDirectoy()) {
           config.addProperty(EXTRACT_TO_DIRECTORY, extractor.getDirectory());
        }
        if (extractor.getExtractToSubdirectoy()) {
            config.addProperty(EXTRACT_TO_SUBDIRECTORY, extractor.getSubdirectory());
        }
        if (extractor.getIgnoreCreateFilesMatchingGlob()) {
            config.addProperty(EXTRACT_GLOB_TO_IGNORE,extractor.getGlobToIgnore());
        }
        config.addProperty(POST_REMOVE_ARCHIVED,postExtraction.getRemoveArchivedFiles());
        config.addProperty(POST_AUTO_SCAN, postExtraction.getScanExtracted());
        config.addProperty(POST_CLOSE_APPLICATION, postExtraction.getCloseApplication());
        config.addProperty(EXTRACT_SKIP_EXISTING,extractor.getSkipExisting());
        config.addProperty(SCANNER_FILE_TYPES, scanner.getFileTypes());
        config.addProperty(SCANNER_GLOB_TO_IGNORE, scanner.getGlobToIgnore());
        config.save(getConfigurationFile());

    }

    private static PropertiesConfiguration createConfiguration() throws ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration();
        return config;
    }

    private static File getConfigurationFile() {

        File dataDir = null, configDir = null, configFile = null;

        if (System.getenv("APPDATA") != null) {
            dataDir = new File(System.getenv("APPDATA"));
            configDir = new File(dataDir, "/jextractall");
        }

        if (dataDir == null || !dataDir.exists()) {
            dataDir = new File(System.getProperty("user.home"));
            configDir = new File(dataDir, "/.jextractall");
        }

        configFile = new File(configDir, "jextractall.properties");
        return configFile;
    }

}
