package Reika.SatisfactoryPlanner.GUI.Components;

import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import fxexpansions.SizedControllerBase;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ItemSinkPointsController extends SizedControllerBase {

	private final HBox mainPanel = new HBox();
	private final Label value = new Label();

	private final ItemRateController itemCount;

	public ItemSinkPointsController(Item c, float rate) {
		itemCount = new ItemRateController(c, rate, false);
		mainPanel.getChildren().add(itemCount.getRootNode());

		mainPanel.setSpacing(12);
		mainPanel.setMaxHeight(Double.MAX_VALUE);
		mainPanel.setMaxWidth(Double.MAX_VALUE);

		value.setText(String.format("%.0f/min", c.sinkValue*rate));

		mainPanel.getChildren().add(value);

		mainPanel.minWidthProperty().bind(value.widthProperty().add(mainPanel.spacingProperty()).add(itemCount.getWidth()));
		mainPanel.setPrefHeight(32);
		mainPanel.setMinHeight(mainPanel.getPrefHeight());
		mainPanel.setMaxHeight(mainPanel.getPrefHeight());
		GuiUtil.sizeToContent(value);
	}

	@Override
	public double getHeight() {
		return Math.max(40, mainPanel.getHeight());
	}

	@Override
	public double getWidth() {
		return value.getMinWidth()+mainPanel.getSpacing()+40;
	}

	@Override
	public Parent getRootNode() {
		return mainPanel;
	}

}

