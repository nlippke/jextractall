package com.github.jextractall.ui.os;

import java.io.File;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.scene.Parent;
import javafx.scene.control.Menu;

public class MacOS extends OS {

	private com.apple.eawt.Application app;
		
	public void init(String[] args) {
		initInternal();
		super.init(args);
	}
	
	public void initInternal() {
		if (app != null) {
			return;
		}
		app = com.apple.eawt.Application.getApplication();
		app.setOpenFileHandler(new com.apple.eawt.OpenFilesHandler() {

			@Override
			public void openFiles(com.apple.eawt.AppEvent.OpenFilesEvent e) {
				for (Object oFile : e.getFiles()) {
					if (oFile instanceof File) {
						openFile((File) oFile);
					}
				}
			}
		});
	}

	@Override
	public void placeSystemMenu(Parent root, Menu applicationMenu) {
		applicationMenu.setText("Apple");
		MenuToolkit.toolkit().setApplicationMenu(applicationMenu);
	}
	
	
}
