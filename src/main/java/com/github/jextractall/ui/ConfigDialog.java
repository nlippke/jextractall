package com.github.jextractall.ui;

import java.io.IOException;

import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class ConfigDialog extends Dialog<ConfigModel> {

    ConfigController controller;

    public ConfigDialog(Stage parent, ConfigModel model) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("config.fxml"),
                Messages.getResourceBundle());
        Parent root = (Parent) fxmlLoader.load();
        this.controller = fxmlLoader.getController();
        getDialogPane().setContent(root);
        setTitle(Messages.getMessage("config.title"));
        setResizable(false);
        controller.setModel(model);
        controller.stage = parent;
        initStyle(StageStyle.DECORATED);
        initOwner(parent);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(
                controller.getValidationResultProperty());

        setResultConverter(new Callback<ButtonType, ConfigModel>() {
            @Override
            public ConfigModel call(ButtonType param) {
                if (param == ButtonType.OK) {
                    return controller.getModel();
                }
                return null;
            }});
    }

}
