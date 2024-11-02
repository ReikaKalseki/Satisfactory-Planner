package Reika.SatisfactoryPlanner.GUI.Components;

import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;

import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GeneratorRowController extends FXMLControllerBase {

	@FXML
	private VBox countContainer;

	@FXML
	private HBox fuelBar;

	@FXML
	private ImageView icon;

	@FXML
	private Label powerGenText;

	private Generator generator;

	private Factory factory;

	private final TreeMap<Fuel, FuelBlock> fuels = new TreeMap();

	private final HashSet<Fuel> settingValue = new HashSet();

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		powerGenText.setText(String.format("%.0fMW", 0F));
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
			Label lb = new Label("+");
			lb.setPadding(new Insets(0, 8, 0, 8));
			lb.setStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD)+" -fx-font-size:24; -fx-text-fill:#E69344;");
			fuelBar.getChildren().add(lb);
		}
		if (fuelBar.getChildren().size() > 1) //remove last trailing +
			fuelBar.getChildren().removeLast();
	}

	public void reset() {
		this.setCount(0);
	}

	public void setCount(double c) {
		for (Fuel f : generator.getFuels())
			this.setCount(f, c);
	}

	public void setCount(Fuel f, double c) {
		if (settingValue.contains(f))
			return;
		settingValue.add(f);
		//Logging.instance.log(generator.displayName+" x "+c);
		//Thread.dumpStack();
		fuels.get(f).counter.getValueFactory().setValue(c);
		//Logging.instance.log(generator.displayName+" x "+c+" > "+powerGenText.getText());
		double sum = factory.getCount(generator);
		powerGenText.setText(String.format("%.0fMW", generator.powerGenerationMW*sum));
		countContainer.getChildren().clear();
		countContainer.getChildren().add(Setting.FRACTION.getCurrentValue().format(sum, false, false));
		settingValue.remove(f);
	}
	/*
	public void setWidths(double countW, double powerW) {
		countSumText.setMinWidth(countW);
		powerGenText.setMinWidth(powerW);
	}

	public double getCountWidth() {
		return GuiUtil.getWidth(countSumText);
	}

	public double getPowerWidth() {
		return GuiUtil.getWidth(powerGenText);
	}
	 */
	private class FuelBlock {

		private final GuiInstance<ItemInOutViewController> gui;
		private final Fuel fuel;
		private final Spinner<Double> counter;

		private FuelBlock(GuiInstance<ItemInOutViewController> g, Fuel f) {
			gui = g;
			fuel = f;

			int row = gui.controller.addRow();
			counter = new Spinner<Double>();//new Button("Choose");
			GuiUtil.setupCounter(counter, 0, 9999, 0, true, true);
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

