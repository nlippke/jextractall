package com.github.jextractall.ui.view;

import com.github.jextractall.ui.model.ExtractorTask;
import com.github.jextractall.unpack.ExtractionResult;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class StatusTableCell extends TableCell<ExtractorTask, ExtractionResult> {
	
	private VBox vb;
	private ImageView imgV;
	private Image errorImg = new Image("com/github/jextractall/ui/bullet_error.png");
	private Image okImg = new Image("com/github/jextractall/ui/bullet_ok.png");
	
	public StatusTableCell() {
		vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		imgV = new ImageView();
		imgV.setFitHeight(errorImg.getHeight());
		imgV.setFitWidth(errorImg.getWidth());
		vb.getChildren().addAll(imgV);
		setGraphic(vb);
	}
	
	public void updateItem(ExtractionResult result, boolean empty) {
		if (empty || result == null) {
			imgV.setImage(null);
			setTooltip(null);
			super.updateItem(result, empty);
			return;
		}
		if (result.getException() != null) {
			imgV.setImage(errorImg);
//			ex.printStackTrace();
			setTooltip(new Tooltip(result.getException().getMessage()));
		} else {
			imgV.setImage(okImg);
			setTooltip(null);
		}
	}
}
