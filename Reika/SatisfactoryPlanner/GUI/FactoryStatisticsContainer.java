package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Setting.InputInOutputOptions;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Item;
import Reika.SatisfactoryPlanner.Data.Milestone;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;

import fxexpansions.ExpandingTilePane;
import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public abstract class FactoryStatisticsContainer extends FXMLControllerBase {

	@FXML
	protected ExpandingTilePane<ItemCountController> buildCostBar;

	@FXML
	protected ExpandingTilePane<ItemCountController> buildingBar;

	@FXML
	protected ExpandingTilePane<ItemCountController> netConsumptionBar;

	@FXML
	protected ExpandingTilePane<ItemCountController> netProductBar;

	@FXML
	protected Label powerProduction;

	@FXML
	protected GridPane statisticsGrid;

	@FXML
	protected ExpandingTilePane<TierLampController> tierBar;

	@FXML
	protected VBox warningList;

	@FXML
	protected TitledPane warningPanel;

	@FXML
	protected TitledPane wrapperPanel;

	private GuiInstance<TierLampController>[] tierLamps = null;

	protected Factory factory;

	public void setFactory(Factory f) {
		factory = f;
	}

	public final Factory getFactory() {
		return factory;
	}

	@Override
	public void init(HostServices services) throws IOException {
		this.setupTierBar();
	}

	protected final void setupTierBar() {
		tierBar.getChildren().clear();
		tierLamps = new GuiInstance[Milestone.getMaxTier()+1];
		for (int i = 0; i < tierLamps.length; i++) {
			TierLampController c = new TierLampController(i);
			GuiInstance<TierLampController> gui = new GuiInstance<TierLampController>(c.getRootNode(), c);
			tierBar.addEntry(gui);
			tierLamps[i] = gui;
		}
	}

	private void updateWarnings() {
		ArrayList<Warning> li = new ArrayList();
		factory.getWarnings(w -> li.add(w));
		boolean any = !li.isEmpty();
		Collections.sort(li);
		warningList.getChildren().clear();
		for (Warning w : li) {
			warningList.getChildren().add(w.createUI());
		}
		if (!any) {
			Label lb = new Label("None - Your Factory Is Perfectly Efficient!");
			lb.setFont(GuiSystem.getFont());
			lb.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			warningList.getChildren().add(lb);
		}
	}

	public final void updateStats(boolean all) {
		this.updateStats(all, all, all, all, all, all, all);
	}

	public void updateStats(boolean warnings, boolean buildings, boolean production, boolean consuming, boolean local, boolean power, boolean tier) {
		if (warnings)
			this.updateWarnings();

		if (buildings) {
			buildCostBar.getChildren().clear();
			buildingBar.getChildren().clear();

			CountMap<Item> cost = new CountMap();
			CountMap<FunctionalBuilding> bc = factory.getBuildings();
			for (FunctionalBuilding b : bc.keySet()) {

				int amt = bc.get(b);
				GuiUtil.addIconCount(b, amt, 5, buildingBar);

				for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
					cost.increment(e.getKey(), e.getValue()*amt);
				}
			}

			for (Item i : cost.keySet()) {
				GuiUtil.addIconCount(i, cost.get(i), 5, buildCostBar);
			}
		}

		if (tier) {
			int max = factory.getMaxTier();
			for (int i = 0; i < tierLamps.length; i++) {
				tierLamps[i].controller.setState(i <= max);
			}
		}

		if (power) {
			float[] avgMinMax = new float[3];
			factory.computeNetPowerProduction(avgMinMax);
			String text = String.format("%.2fMW", avgMinMax[0]);
			if (Math.abs(avgMinMax[1]-avgMinMax[2]) > 0.1) {
				text = String.format("%s average (%.2fMW to %.2fMW range)", text, avgMinMax[1], avgMinMax[2]);
			}
			powerProduction.setText(text);
			if (avgMinMax[0] > 0) {
				powerProduction.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			}
			else if (avgMinMax[0] < 0) {
				powerProduction.setStyle(GuiSystem.getFontStyle(FontModifier.BOLD)+" -fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR)+";");
			}
			else {
				powerProduction.setStyle("");
			}
		}

		/*
		if (consuming || production) {
			Collection<Consumable> all = factory.getAllRelevantItems();
			if (consuming)
				netConsumptionBar.getChildren().clear();
			if (production)
				netProductBar.getChildren().clear();
			for (Consumable c : all) {
				float amt = factory.getFlow(c).getNetYield();
				if (amt < 0 && consuming)
					GuiUtil.addIconCount(netConsumptionBar, c, -amt);
				if (amt > 0 && consuming)
					GuiUtil.addIconCount(netProductBar, c, amt);
			}
		}*/
		if (consuming) {
			netConsumptionBar.getChildren().clear();
			for (Consumable c : factory.getAllIngredients()) {
				float amt = factory.getTotalConsumption(c)-factory.getTotalProduction(c)-factory.getExternalInput(c, true);
				if (amt > 0)
					GuiUtil.addIconCount(c, amt, 5, netConsumptionBar);
			}
		}
		if (production) {
			netProductBar.getChildren().clear();
			HashSet<Consumable> set = new HashSet(factory.getAllProducedItems());
			if (Setting.INOUT.getCurrentValue() != InputInOutputOptions.EXCLUDE)
				set.addAll(factory.getAllMinedItems());
			if (Setting.INOUT.getCurrentValue() == InputInOutputOptions.ALL)
				set.addAll(factory.getAllSuppliedItems());
			for (Consumable c : set) {
				float amt = factory.getTotalProduction(c)-factory.getTotalConsumption(c);
				if (Setting.INOUT.getCurrentValue() != InputInOutputOptions.EXCLUDE)
					amt += factory.getExternalInput(c, Setting.INOUT.getCurrentValue() == InputInOutputOptions.ALL ? false : true);
				if (amt > 0) {
					ItemCountController gui = GuiUtil.addIconCount(c, amt, 5, netProductBar).controller;
					if (!factory.getDesiredProducts().contains(c))
						gui.setWarning();
				}
			}
		}

		this.getRootNode().layout();
	}
}

