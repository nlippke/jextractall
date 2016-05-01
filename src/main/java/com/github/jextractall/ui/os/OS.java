package com.github.jextractall.ui.os;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;

public class OS {
	
	private Set<File> openFiles = new HashSet<>();
	private OpenFileHandler handler = null;
	
	
	private static OS INSTANCE;
	
	public void init(String[] args) {
		openFiles.addAll(Arrays.stream(args)
			.map(s -> new File(s))
			.filter(f -> f.canRead())
			.collect(Collectors.toSet()));
	}
	
	protected void openFile(File file) {
		if (handler != null) {
			handler.openFile(file);
		} else {
			openFiles.add(file);
		}
	}

	public void registerHandler(OpenFileHandler handler) {
		this.handler = handler;
		openFiles.stream().forEach(f -> openFile(f));
		openFiles.clear();
	}
	
	public boolean haveBufferedFiles() {
		return openFiles.size() > 0;
	}
	
	private static boolean isMacOs() {
		return System.getProperty("os.name").toLowerCase().contains("os x");
	}
	
	public static OS getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		if (isMacOs()) {
			INSTANCE = new MacOS();
		} else {
			INSTANCE = new OS();
		}
		return INSTANCE;
	}

	public void placeSystemMenu(Parent root, Menu defaultApplicationMenu) {
		BorderPane bp = (BorderPane) root;
		MenuBar bar = new MenuBar();
		bar.getMenus().add(defaultApplicationMenu);
		bp.setTop(bar);
	}
	
}
