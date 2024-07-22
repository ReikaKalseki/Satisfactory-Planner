package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ItemInOutViewController extends ControllerBase {


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

	private final HashMap<Consumable, GuiInstance<ItemViewController>> inputViews = new HashMap();
	private final HashMap<Consumable, GuiInstance<ItemViewController>> outputViews = new HashMap();

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
		GuiInstance<ItemViewController> gui = this.loadNestedFXML("ItemView", inputs);
		gui.controller.setItem(in, amt);
		inputViews.put(in, gui);
	}

	public void addOutput(Consumable out, float amt) throws IOException {
		GuiInstance<ItemViewController> gui = this.loadNestedFXML("ItemView", outputs);
		gui.controller.setItem(out, amt);
		outputViews.put(out, gui);
	}

	public void setFuel(Fuel f) throws IOException {
		this.addInput(f.item, f.primaryBurnRate);
		if (f.secondaryItem != null) {
			this.addInput(f.secondaryItem, f.secondaryBurnRate);
		}
		if (f.byproduct != null) {
			this.addOutput(f.byproduct, f.byproductAmount*f.primaryBurnRate);
		}
		else {
			rootGrid.getRowConstraints().remove(1);
			rootGrid.getChildren().remove(outputLabel);
			rootGrid.getChildren().remove(outputs);
			rootGrid.getColumnConstraints().remove(0);
			rootGrid.getChildren().remove(inputLabel);
		}


		this.setInputText("Fuel");
		this.setOutputText("Byproduct");
	}

	public void setScale(float sc) {
		for (GuiInstance<ItemViewController> v : inputViews.values()) {
			v.controller.setScale(sc);
		}
		for (GuiInstance<ItemViewController> v : outputViews.values()) {
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

