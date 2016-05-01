package com.github.jextractall.ui;

import java.io.IOException;

import com.github.jextractall.ui.i18n.Messages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class PasswordDialog extends Dialog<String> {

    PasswordController controller;

    public PasswordDialog(Stage parent, String label) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("password.fxml"),
                Messages.getResourceBundle());
        Parent root = (Parent) fxmlLoader.load();
        this.controller = fxmlLoader.getController();
        this.controller.passwordLabel.setText(Messages.getMessage("password.label", label));
        getDialogPane().setContent(root);
        setTitle(Messages.getMessage("password.title"));
        setResizable(false);
        initStyle(StageStyle.DECORATED);
        initOwner(parent);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType param) {
                if (param == ButtonType.OK) {
                    return controller.getPassword();
                }
                return null;
            }});
    }

}
