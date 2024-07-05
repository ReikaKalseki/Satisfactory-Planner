package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class GeneratorRowController extends ControllerBase {

	@FXML
	private Spinner<Integer> counter;

	@FXML
	private HBox fuelCostBar;

	@FXML
	private ImageView icon;

	@FXML
	private Label powerGenText;

	private Generator generator;

	private Consumer<Integer> callback;

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		GuiUtil.setupCounter(counter, 0, 9999, 0, true);
		counter.valueProperty().addListener((val, old, nnew) -> {
			this.setCount(nnew, old != nnew);
		});
		powerGenText.setText(String.format("%.3fMW", 0F));
	}

	public void setGenerator(Generator g) {
		icon.setImage(g.createIcon(64));
		generator = g;
		this.setCount(0, false);
	}

	public void setCount(int c, boolean notify) {
		fuelCostBar.getChildren().clear();
		counter.getValueFactory().setValue(c);
		powerGenText.setText(String.format("%.3fMW", generator.powerGenerationMW*c));
		for (Fuel f : generator.getFuels()) {
			try {
				HBox wrapper = new HBox();
				wrapper.setPadding(new Insets(2));
				GuiInstance gui = this.loadNestedFXML("ItemView", wrapper);
				((ItemViewController)gui.controller).setItem(f.item, generator.getBurnRate(f.item)*c);
				if (f.secondaryItem != null) {
					gui = this.loadNestedFXML("ItemView", wrapper);
					((ItemViewController)gui.controller).setItem(f.secondaryItem, f.secondaryItemRatio*c);
				}
				fuelCostBar.getChildren().add(wrapper);
				wrapper.setStyle("-fx-border-width: 2; -fx-border-color: #aaa; -fx-border-radius: 3;");
			}
			catch (Exception e) {
				GuiUtil.showException(e);
			}
			Label lb = new Label("OR");
			lb.setPadding(new Insets(0, 8, 0, 8));
			lb.setStyle(GuiSystem.getFontStyle(FontModifier.SEMIBOLD)+" -fx-font-size:16;");
			fuelCostBar.getChildren().add(lb);
		}
		if (fuelCostBar.getChildren().size() > 1) //remove last trailing OR
			fuelCostBar.getChildren().removeLast();
		if (notify && callback != null)
			callback.accept(c);
	}

	public void setCallback(Consumer<Integer> call) {
		callback = call;
	}

}

