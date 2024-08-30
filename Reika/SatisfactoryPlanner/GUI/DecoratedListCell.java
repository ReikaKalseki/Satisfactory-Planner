package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;

abstract class DecoratedListCell<T> extends ListCell<T> {

	private final String prompt;

	protected DecoratedListCell(String ptext, boolean isButton) {
		super();

		prompt = ptext;

		//this.setGraphicTextGap(24);
		this.setContentDisplay(ContentDisplay.RIGHT);
		this.setMinHeight(Region.USE_PREF_SIZE);
		if (isButton) {
			this.setPadding(new Insets(2, 4, 2, 4));
			this.getStyleClass().add("button-cell");
		}
		else {
			this.setPadding(new Insets(2, 12, 2, 6));
			this.getStyleClass().add("dropdown-cell");
		}
		Insets in = this.getInsets();
		this.setPrefHeight(32+in.getTop()+in.getBottom());
		this.setStyle(GuiSystem.getFontStyle());
	}

	@Override
	protected final void updateItem(T r, boolean empty) {
		super.updateItem(r, empty);
		if (empty || r == null) {
			this.setText(prompt);
			this.setGraphic(null);
		}
		else {
			//this.setText(r.name);
			this.setText("");
			this.setGraphic(this.createCellContent(r));
		}
	}

	protected abstract Node createDecoration(T obj);

	protected abstract String getString(T obj);

	private Node createCellContent(T r) {
		Node n = this.getCachedCellContent(r);
		if (n != null)
			return n;
		Label lb = new Label(this.getString(r));
		lb.setStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD)+" -fx-text-fill: #222");
		n = GuiUtil.createSpacedHBox(lb, this.createDecoration(r), null);
		this.onCreateCellContent(r, n);
		return n;
	}

	protected void onCreateCellContent(T obj, Node graphic) {

	}

	protected Node getCachedCellContent(T obj) {
		return null;
	}

}