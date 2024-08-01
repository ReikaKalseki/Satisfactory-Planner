package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;

import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import fxexpansions.WindowBase;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class GeneratorRowController extends FXMLControllerBase {

	@FXML
	private Label countSumText;

	@FXML
	private HBox fuelBar;

	@FXML
	private ImageView icon;

	@FXML
	private Label powerGenText;

	private Generator generator;

	private Factory factory;

	private final HashMap<Fuel, FuelBlock> fuels = new HashMap();

	private final HashSet<Fuel> settingValue = new HashSet();

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		powerGenText.setText(String.format("%.3fMW", 0F));
	}

	public void setFactory(Factory f) {
		factory = f;
	}

	public void setGenerator(Generator g) {
		icon.setImage(g.createIcon(64));
		generator = g;
		//this.setCount(0, false);
		fuelBar.getChildren().clear();

		for (Fuel f : generator.getFuels()) {
			try {/*
				HBox wrapper = new HBox();
				wrapper.setPadding(new Insets(2));
				((ItemViewController)gui.controller).setItem(f.item, f.primaryBurnRate*c);
				if (f.secondaryItem != null) {
					gui = this.loadNestedFXML("ItemView", wrapper);
					((ItemViewController)gui.controller).setItem(f.secondaryItem, f.secondaryBurnRate*c);
				}
				fuelCostBar.getChildren().add(wrapper);
				wrapper.setStyle("-fx-border-width: 2; -fx-border-color: #aaa; -fx-border-radius: 3;");*/

				//if (f.byproduct != null) {
				GuiInstance<ItemInOutViewController> gui = this.loadNestedFXML("ItemInOutView", fuelBar);
				gui.controller.setFuel(f);
				gui.controller.setScale(0);

				fuels.put(f, new FuelBlock(gui, f));
				//}
				/*else {
					GuiInstance gui = this.loadNestedFXML("ItemView", fuelBar);
					((ItemViewController)gui.controller).setItem(f.item, f.primaryBurnRate*c);*//*
				}*/
			}
			catch (Exception e) {
				GuiUtil.showException(e);
			}
			Label lb = new Label("OR");
			lb.setPadding(new Insets(0, 8, 0, 8));
			lb.setStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD)+" -fx-font-size:16;");
			fuelBar.getChildren().add(lb);
		}
		if (fuelBar.getChildren().size() > 1) //remove last trailing OR
			fuelBar.getChildren().removeLast();
	}

	public void reset() {
		this.setCount(0);
	}

	public void setCount(int c) {
		for (Fuel f : generator.getFuels())
			this.setCount(f, c);
	}

	public void setCount(Fuel f, int c) {
		if (settingValue.contains(f))
			return;
		settingValue.add(f);
		//Logging.instance.log(generator.displayName+" x "+c);
		//Thread.dumpStack();
		fuels.get(f).counter.getValueFactory().setValue(c);
		powerGenText.setText(String.format("%.3fMW", generator.powerGenerationMW*c));
		countSumText.setText(String.valueOf(factory.getCount(generator)));
		settingValue.remove(f);
	}

	private class FuelBlock {

		private final GuiInstance<ItemInOutViewController> gui;
		private final Fuel fuel;
		private final Spinner<Integer> counter;

		private FuelBlock(GuiInstance<ItemInOutViewController> g, Fuel f) {
			gui = g;
			fuel = f;

			int row = gui.controller.addRow();
			counter = new Spinner<Integer>();//new Button("Choose");
			GuiUtil.setupCounter(counter, 0, 9999, 0, true);
			counter.valueProperty().addListener((val, old, nnew) -> {
				factory.setCount(generator, fuel, nnew == null ? 0 : nnew);
				gui.controller.setScale(nnew);
			});
			counter.setMaxHeight(Double.POSITIVE_INFINITY);
			counter.setMaxWidth(Double.POSITIVE_INFINITY);
			gui.controller.addNode(counter, row, 0);
			GridPane.setRowSpan(counter, 1);
			GridPane.setColumnSpan(counter, GridPane.REMAINING);
		}

	}

}

