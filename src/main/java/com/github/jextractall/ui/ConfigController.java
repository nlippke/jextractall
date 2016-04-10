package com.github.jextractall.ui;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.controlsfx.control.CheckListView;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModel;
import com.github.jextractall.ui.model.ExtractorTaskFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class ConfigController implements Initializable {

    @FXML RadioButton extractToSameDirectoryOption;
    @FXML ToggleGroup directoryGroup;
    @FXML RadioButton extractToSubdirectoryOption;
    @FXML TextField subDirectory;
    @FXML RadioButton extractToDirectoryOption;
    @FXML TextField directory;
    @FXML RadioButton overrideExistingOption;
    @FXML ToggleGroup overrideGroup;
    @FXML RadioButton skipExistingOption;
    @FXML CheckBox ignoreFilesMatchingGlobOption;
    @FXML TextField ignoreMatchingGlob;
    @FXML CheckBox removeArchivedFilesOption;
    @FXML CheckBox searchForNestedArchiveOption;
    @FXML Button directoryChooser;
    @FXML CheckListView<String> fileTypesView;
    @FXML TextField ignoreGlobOnScan;

    private ConfigModel model;
    private ValidationSupport validationSupport = new ValidationSupport();
    Stage stage;


    private void setControlBindings() {
        subDirectory.disableProperty().bind(extractToSubdirectoryOption.selectedProperty().not());
        directory.disableProperty().bind(extractToDirectoryOption.selectedProperty().not());
        directoryChooser.disableProperty().bind(extractToDirectoryOption.selectedProperty().not());
        ignoreMatchingGlob.disableProperty().bind(ignoreFilesMatchingGlobOption.selectedProperty().not());
    }

    public void setModel(ConfigModel model) {
        if (this.model != null) {
            unbindModel();
        }
        this.model = model;
        bindModel();
    }

    public ConfigModel getModel() {
        return model;
    }

    private void unbindModel() {
        Bindings.unbindBidirectional(extractToDirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToDirectoryProperty());
        Bindings.unbindBidirectional(extractToSubdirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToSubdirectoryProperty());
        Bindings.unbindBidirectional(extractToSameDirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToSameDirectoryProperty());
        Bindings.unbindBidirectional(subDirectory.textProperty(),
                model.getExtractorModel().subdirectoryProperty());
        Bindings.unbindBidirectional(directory.textProperty(),
                model.getExtractorModel().directoryProperty());
        Bindings.unbindBidirectional(overrideExistingOption.selectedProperty(),
                model.getExtractorModel().overrideExistingProperty());
        Bindings.unbindBidirectional(skipExistingOption.selectedProperty(),
                model.getExtractorModel().skipExistingProperty());
        Bindings.unbindBidirectional(ignoreFilesMatchingGlobOption.selectedProperty(),
                model.getExtractorModel().ignoreCreateFilesMatchingGlobProperty());
        Bindings.unbindBidirectional(ignoreMatchingGlob.textProperty(),
                model.getExtractorModel().globToIgnoreProperty());
        Bindings.unbindBidirectional(removeArchivedFilesOption.selectedProperty(),
                model.getPostExtractionModel().removeArchivedFilesProperty());
        Bindings.unbindBidirectional(searchForNestedArchiveOption.selectedProperty(),
                model.getPostExtractionModel().scanExtractedProperty());
        Bindings.unbindBidirectional(ignoreGlobOnScan.textProperty(),
                model.getScannerModel().globToIgnoreProperty());
        model.getScannerModel().fileTypesProperty().unbind();
    }

    private void bindModel() {
        Bindings.bindBidirectional(extractToDirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToDirectoryProperty());
        Bindings.bindBidirectional(extractToSubdirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToSubdirectoryProperty());
        Bindings.bindBidirectional(extractToSameDirectoryOption.selectedProperty(),
                model.getExtractorModel().extractToSameDirectoryProperty());
        Bindings.bindBidirectional(subDirectory.textProperty(),
                model.getExtractorModel().subdirectoryProperty());
        Bindings.bindBidirectional(directory.textProperty(),
                model.getExtractorModel().directoryProperty());
        Bindings.bindBidirectional(overrideExistingOption.selectedProperty(),
                model.getExtractorModel().overrideExistingProperty());
        Bindings.bindBidirectional(skipExistingOption.selectedProperty(),
                model.getExtractorModel().skipExistingProperty());
        Bindings.bindBidirectional(ignoreFilesMatchingGlobOption.selectedProperty(),
                model.getExtractorModel().ignoreCreateFilesMatchingGlobProperty());
        Bindings.bindBidirectional(ignoreMatchingGlob.textProperty(),
                model.getExtractorModel().globToIgnoreProperty());
        Bindings.bindBidirectional(removeArchivedFilesOption.selectedProperty(),
                model.getPostExtractionModel().removeArchivedFilesProperty());
        Bindings.bindBidirectional(searchForNestedArchiveOption.selectedProperty(),
                model.getPostExtractionModel().scanExtractedProperty());
        Bindings.bindBidirectional(ignoreGlobOnScan.textProperty(),
                model.getScannerModel().globToIgnoreProperty());
        Stream.of(model.getScannerModel().getFileTypes()).forEach(t -> fileTypesView.getCheckModel().check(t));
        fileTypesView.getCheckModel().getCheckedItems().addListener((Change<? extends String> c) ->
        model.getScannerModel().setFileTypes(fileTypesView.getCheckModel().getCheckedItems().toArray(new String[0])));
    }

    private void addMultiSelectionFeatureTo(CheckListView<?> listView) {
        fileTypesView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(
                    javafx.collections.ListChangeListener.Change<? extends String> c) {
                fileTypesView.getCheckModel().getCheckedItems().removeListener(this);
                try {
                    if (c.next()) {
                        if (c.wasAdded() && c.getAddedSize() == 1) {
                            for (String s : fileTypesView.getSelectionModel().getSelectedItems()) {
                                fileTypesView.getCheckModel().check(s);
                            }
                            return;
                        }
                        if (c.wasRemoved() && c.getRemovedSize() == 1) {
                            for (String s : fileTypesView.getSelectionModel().getSelectedItems()) {
                                fileTypesView.getCheckModel().clearCheck(s);
                            }
                            return;
                        }
                    }
                } finally {
                    fileTypesView.getCheckModel().getCheckedItems().addListener(this);
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setControlBindings();
        final ObservableList<String> fileTypes = FXCollections.observableArrayList();
        fileTypes.addAll(ExtractorTaskFactory.getSupportedFileTypes());
        fileTypesView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        FXCollections.sort(fileTypes);
        fileTypesView.setItems(fileTypes);
        addMultiSelectionFeatureTo(fileTypesView);
        addValidationSupport();
    }

    private void addValidationSupport() {
        Validator<String> emptyValidatorIfEnabled = (Control c, String v) ->
              ValidationResult.fromErrorIf(c,
                      Messages.getMessage("config.validation.emptyField"),
                      !c.isDisabled() && (v == null || v.isEmpty()));

        Validator<String> globPatternValidator = (Control c, String v) ->
              ValidationResult.fromErrorIf(c,
                      Messages.getMessage("config.validation.invalidExpression"),
                      isGlobPatternInvalid(v));

        validationSupport.registerValidator(ignoreGlobOnScan, false, globPatternValidator);

        Tooltip  tip = new Tooltip();
        tip.setText(Messages.getMessage("config.tooltip.glob"));

        ignoreGlobOnScan.setTooltip(tip);
        ignoreMatchingGlob.setTooltip(tip);
        validationSupport.registerValidator(ignoreMatchingGlob, false, globPatternValidator);
        validationSupport.registerValidator(directory, false, emptyValidatorIfEnabled);
        validationSupport.registerValidator(subDirectory, false, emptyValidatorIfEnabled);

        directory.disableProperty().addListener(
            (observable, oldValue, newValue) -> {
                validationSupport.initInitialDecoration();
                String text = directory.getText();
                directory.setText("");
                directory.setText(text);
            }
        );

        subDirectory.disableProperty().addListener(
                (observable, oldValue, newValue) -> {
                    validationSupport.initInitialDecoration();
                    String text = subDirectory.getText();
                    subDirectory.setText("");
                    subDirectory.setText(text);
                }
            );
    }

    private boolean isGlobPatternInvalid(String v) {
        if (StringUtils.isEmpty(v)) {
            return false;
        }
        try {
            FileSystems.getDefault().getPathMatcher("glob:{"+v+"}");
            return false;
        } catch (PatternSyntaxException pse) {
            return true;
        }
    }

    @FXML
    public void openDirectoryDialog() {
        MainController.DIRECTORY_CHOOSER.setTitle(
                Messages.getMessage("config.directoryChooser.title"));
        if (model.getExtractorModel().getDirectory() != null) {
            File f = new File(model.getExtractorModel().getDirectory());
            if (f.exists() && f.isDirectory()) {
                MainController.DIRECTORY_CHOOSER.setInitialDirectory(
                        new File(model.getExtractorModel().getDirectory()));
            }
        }
        File dir = MainController.DIRECTORY_CHOOSER.showDialog(stage);
        if (dir == null) {
            return;
        }
        model.getExtractorModel().setDirectory(dir.getAbsolutePath());
    }

    public ReadOnlyBooleanProperty getValidationResultProperty() {
        return validationSupport.invalidProperty();
    }
}
