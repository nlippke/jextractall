package com.github.jextractall.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.sun.javafx.scene.control.skin.TextFieldSkin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PasswordController implements Initializable {

	@FXML Label passwordLabel;
	@FXML TextField passwordTextField;
	@FXML CheckBox showClearTextOption;
	@FXML CheckBox rememberPasswordOption;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		final PasswordSkin skin = new PasswordSkin(passwordTextField);
		passwordTextField.setSkin(skin);
		
		showClearTextOption.selectedProperty().addListener((obj, oldV, newV) -> {
			skin.setMask(!newV);
			passwordTextField.setText(passwordTextField.getText());
		});
	}

	public String getPassword() {
		return passwordTextField.getText();
	}
	
	/**
	 * Hack: Override skin to customize masking.
	 */
	class PasswordSkin extends TextFieldSkin {

		public static final char BULLET = '\u2022';
		
		private boolean mask = true;
		
		public void setMask(boolean mask) {
			this.mask = mask;
		}
		
		public PasswordSkin(TextField textField) {
			super(textField);
		}

		@Override
		protected String maskText(String txt) {
			return mask ? txt.replaceAll(".", ""+ BULLET) : txt;
		}
		
		
	}
}
