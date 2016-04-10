package com.github.jextractall.ui;

import java.util.Optional;

import org.controlsfx.dialog.ExceptionDialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class DialogBuilder {

    Dialog<ButtonType> dialog;

    private DialogBuilder(Dialog<ButtonType> dialog) {
        this.dialog = dialog;
    }

    public static DialogBuilder exception(Exception ex) {
        return new DialogBuilder(new ExceptionDialog(ex));
    }

    public static DialogBuilder information() {
        return new DialogBuilder(new Alert(AlertType.INFORMATION));
    }

    public static DialogBuilder warn() {
        return new DialogBuilder(new Alert(AlertType.WARNING));
    }

    public DialogBuilder withTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public DialogBuilder withMessage(String message) {
        dialog.setContentText(message);
        return this;
    }

    public DialogBuilder withHeader(String header) {
        dialog.setHeaderText(header);
        return this;
    }

    public Optional<ButtonType> show() {
        return dialog.showAndWait();
    }
}
