package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Fuel;

import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import fxexpansions.WindowBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

public class ItemInOutViewController extends FXMLControllerBase {


	@FXML
	private Label inputLabel;

	@FXML
	private HBox inputs;

	@FXML
	private Label outputLabel;

	@FXML
	private HBox outputs;

	@FXML
	private GridPane rootGrid;

	private final HashMap<Consumable, GuiInstance<ItemRateController>> inputViews = new HashMap();
	private final HashMap<Consumable, GuiInstance<ItemRateController>> outputViews = new HashMap();

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

	}
	/*
	public void addInput(ItemViewController v) {
		inputs.getChildren().add(v.getRootNode());
	}

	public void addOutput(ItemViewController v) {
		outputViews.put(v.getItem(), v);
		outputs.getChildren().add(v.getRootNode());
	}
	 */
	public void addInput(Consumable in, float amt) throws IOException {
		GuiInstance<ItemRateController> gui = GuiUtil.createItemView(in, amt, inputs);
		inputViews.put(in, gui);
	}

	public void addOutput(Consumable out, float amt) throws IOException {
		GuiInstance<ItemRateController> gui = GuiUtil.createItemView(out, amt, outputs);
		outputViews.put(out, gui);
	}

	public void setFuel(Fuel f) throws IOException {
		this.addInput(f.item, f.primaryBurnRate);
		if (f.secondaryItem != null) {
			this.addInput(f.secondaryItem, f.secondaryBurnRate);
		}
		if (f.byproduct != null) {
			this.addOutput(f.byproduct, f.getByproductRate());
		}
		else {
			rootGrid.getRowConstraints().remove(1);
			rootGrid.getChildren().remove(outputLabel);
			rootGrid.getChildren().remove(outputs);
			rootGrid.getColumnConstraints().remove(0);
			rootGrid.getChildren().remove(inputLabel);
			rootGrid.setRowIndex(inputs, 0);
			rootGrid.setColumnIndex(inputs, 0);
		}

		this.setInputText("Fuel");
		this.setOutputText("Byproduct");
	}

	public int addRow() {
		RowConstraints cc = new RowConstraints();
		rootGrid.getRowConstraints().add(cc);
		cc.setMinHeight(Region.USE_COMPUTED_SIZE);
		cc.setPrefHeight(Region.USE_COMPUTED_SIZE);
		cc.setMaxHeight(Double.MAX_VALUE);
		return rootGrid.getRowCount()-1;
	}

	public int addColumn() {
		ColumnConstraints cc = new ColumnConstraints();
		rootGrid.getColumnConstraints().add(cc);
		cc.setMinWidth(Region.USE_COMPUTED_SIZE);
		cc.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cc.setMaxWidth(Double.MAX_VALUE);
		return rootGrid.getColumnCount()-1;
	}

	public void addNode(Node n, int row, int col) {
		rootGrid.add(n, col, row);
	}

	public void setScale(float sc) {
		for (GuiInstance<ItemRateController> v : inputViews.values()) {
			v.controller.setScale(sc);
		}
		for (GuiInstance<ItemRateController> v : outputViews.values()) {
			v.controller.setScale(sc);
		}
	}

	public void setInputText(String sg) {
		inputLabel.setText(sg);
	}

	public void setOutputText(String sg) {
		outputLabel.setText(sg);
	}

}

