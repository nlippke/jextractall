package com.github.jextractall.ui;

import java.util.Locale;

import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModelFactory;
import com.github.jextractall.ui.model.ExtractorTaskFactory;
import com.github.jextractall.ui.os.OS;
import com.github.jextractall.unpack.SevenZipExtractor;

import de.codecentric.centerdevice.MenuToolkit;
import de.codecentric.centerdevice.dialogs.about.AboutStageBuilder;
import de.codecentric.centerdevice.labels.LabelMaker;
import de.codecentric.centerdevice.labels.LabelName;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	private static final Image ICON = new Image(Main.class.getResourceAsStream("/jextractall.png"));

	public static final String APP_NAME = "jExtractAll";
	public static final String APP_VERSION = "1.1";

	private MainController controller;
		
	@Override
	public void init() throws Exception {		
		
	}

	@Override
	public void start(Stage primaryStage) {
		ExtractorTaskFactory.register(SevenZipExtractor.class);
		primaryStage.setTitle(APP_NAME);
		primaryStage.getIcons().add(ICON);
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("main.fxml"), Messages.getResourceBundle());
			Parent root = (Parent)loader.load();
			createSystemMenu(root);
			controller = loader.getController();
			controller.setStage(primaryStage);
			Scene scene = new Scene(root,400,400);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			boolean needQuickStart = OS.getInstance().haveBufferedFiles();
			OS.getInstance().registerHandler( f -> controller.addFilesToTaskList(f));
	
			if (needQuickStart) {
				controller.setConfigModel(ConfigModelFactory.commandLine());
				controller.onExtract();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void createSystemMenu(Parent root) {
		MenuToolkit tk = MenuToolkit.toolkit(Locale.getDefault());
		Menu defaultApplicationMenu = new Menu(new LabelMaker(Locale.getDefault()).getLabel(LabelName.FILE), null, 
				createAboutMenuItem(APP_NAME), 
				new SeparatorMenuItem(), 
				tk.createQuitMenuItem(APP_NAME));
		OS.getInstance().placeSystemMenu(root, defaultApplicationMenu);
	}

	public MenuItem createAboutMenuItem(String appName) {
		String label = new LabelMaker(Locale.getDefault()).getLabel(LabelName.ABOUT, appName);
		MenuItem about = new MenuItem(label);
		AboutStageBuilder stageBuilder = AboutStageBuilder.start(label)
				.withCloseOnFocusLoss()
				.withAppName(appName)
				.withVersionString(APP_VERSION);

		stageBuilder.withImage(ICON);

		Stage aboutStage = stageBuilder.build();
		about.setOnAction(event -> aboutStage.show());
		return about;
	}

	public static void main(String[] args) throws Exception {
		OS.getInstance().init(args);
		launch(args);
	}
}
