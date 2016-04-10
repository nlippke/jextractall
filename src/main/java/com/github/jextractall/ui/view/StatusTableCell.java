package com.github.jextractall.ui.view;

import com.github.jextractall.ui.model.ExtractorTask;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class StatusTableCell extends TableCell<ExtractorTask, Exception> {
	
	private VBox vb;
	private ImageView imgV;
	private Image errorImg = new Image("com/github/jextractall/ui/bullet_error.png");
	
	public StatusTableCell() {
		vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		imgV = new ImageView();
		imgV.setFitHeight(errorImg.getHeight());
		imgV.setFitWidth(errorImg.getWidth());
		vb.getChildren().addAll(imgV);
		setGraphic(vb);
	}
	
	public void updateItem(Exception ex, boolean empty) {
		if (!empty && ex != null) {
			imgV.setImage(errorImg);
//			ex.printStackTrace();
			setTooltip(new Tooltip(ex.getMessage()));
		} else {
			imgV.setImage(null);
			setTooltip(null);
		}
	}
}
