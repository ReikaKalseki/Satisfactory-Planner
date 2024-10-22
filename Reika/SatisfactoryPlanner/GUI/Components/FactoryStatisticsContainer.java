package Reika.SatisfactoryPlanner.GUI.Components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Setting.InputInOutputOptions;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.Data.Objects.Milestone;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.FontModifier;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController.WarningState;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.JavaUtil;

import fxexpansions.ExpandingTilePane;
import fxexpansions.FXMLControllerBase;
import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public abstract class FactoryStatisticsContainer extends FXMLControllerBase {

	@FXML
	protected ExpandingTilePane<ItemCountController> buildCostBar;

	@FXML
	protected ExpandingTilePane<ItemCountController> buildingBar;

	@FXML
	protected ExpandingTilePane<ItemRateController> netConsumptionBar;

	@FXML
	protected ExpandingTilePane<ItemRateController> netProductBar;

	@FXML
	protected ExpandingTilePane<ItemRateController> deficiencyBar;

	@FXML
	protected Label powerProduction;

	@FXML
	protected ExpandingTilePane<PowerBreakdownEntryController> powerBreakdown;

	@FXML
	protected ExpandingTilePane<ItemRateController> sinkBreakdown;

	@FXML
	protected Label sinkPoints;

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

	private boolean anyWarnings;

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

		this.bindRowHeight(2, netConsumptionBar);
		this.bindRowHeight(3, netProductBar);
		this.bindRowHeight(4, deficiencyBar);
		if (powerBreakdown != null)
			this.bindRowHeight(6, powerBreakdown);
		if (sinkBreakdown != null)
			this.bindRowHeight(7, sinkBreakdown);

		tierBar.minRowHeight = 32;
		if (powerBreakdown != null)
			powerBreakdown.minRowHeight = 32;
		if (sinkBreakdown != null)
			sinkBreakdown.minRowHeight = 40;
		buildingBar.minRowHeight = 40;
		buildCostBar.minRowHeight = 40;
		netConsumptionBar.minRowHeight = 40;
		netProductBar.minRowHeight = 40;
		deficiencyBar.minRowHeight = 40;
	}

	private void bindRowHeight(int idx, ExpandingTilePane p) {
		RowConstraints rc = statisticsGrid.getRowConstraints().get(idx);
		rc.prefHeightProperty().bind(p.heightProperty());
		rc.minHeightProperty().bind(rc.prefHeightProperty());
		rc.maxHeightProperty().bind(rc.prefHeightProperty());
		rc.setValignment(VPos.CENTER);
		p.setAlignment(Pos.CENTER_LEFT);
	}

	protected final void setupTierBar() {
		tierBar.clear();
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
		anyWarnings = !li.isEmpty();
		Collections.sort(li);
		warningList.getChildren().clear();
		for (Warning w : li) {
			warningList.getChildren().add(w.createUI());
		}
		if (!anyWarnings) {
			Label lb = new Label("None - Your Factory Is Perfectly Efficient!");
			lb.setFont(GuiSystem.getFont());
			lb.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.OKAY_COLOR)+";");
			warningList.getChildren().add(lb);
		}
	}

	public final void updateStats() {
		this.updateStats(EnumSet.allOf(StatFlags.class));
	}

	public final void updateStats(StatFlags... flags) {
		if (flags.length > 0)
			this.updateStats(EnumSet.copyOf(JavaUtil.makeListFrom(flags)));
	}

	public void updateStats(EnumSet<StatFlags> flags) {
		if (flags.contains(StatFlags.WARNINGS))
			this.updateWarnings();

		if (flags.contains(StatFlags.BUILDINGS)) {
			buildCostBar.clear();
			buildingBar.clear();

			CountMap<Item> cost = new CountMap();
			CountMap<Building> bc = factory.getBuildings();
			for (Building b : JavaUtil.sorted(bc.keySet())) {
				int amt = bc.get(b);
				GuiUtil.addIconCount(b, amt, 5, false, buildingBar);

				for (Entry<Item, Integer> e : b.getConstructionCost().entrySet()) {
					cost.increment(e.getKey(), e.getValue()*amt);
				}
			}

			for (Item i : JavaUtil.sorted(cost.keySet(), (i1, i2) -> i1.compareTo(i2))) {
				GuiUtil.addIconCount(i, cost.get(i), 5, false, buildCostBar);
			}
		}

		if (flags.contains(StatFlags.TIER)) {
			int max = factory.getMaxTier();
			for (int i = 0; i < tierLamps.length; i++) {
				tierLamps[i].controller.setState(i <= max);
			}
		}

		if (flags.contains(StatFlags.SINK)) {
			TreeMap<Item, Integer> breakdown = sinkBreakdown == null ? null : new TreeMap();
			int points = factory.computeSinkPoints(breakdown);
			if (sinkPoints != null)
				sinkPoints.setText(String.format("%d / min", points));
			if (sinkBreakdown != null) {
				sinkBreakdown.getChildren().clear();
				for (Entry<Item, Integer> e : breakdown.entrySet()) {
					//ItemSinkPointsController c = new ItemSinkPointsController(e.getKey(), e.getValue());
					//GuiInstance<ItemSinkPointsController> gui = new GuiInstance<ItemSinkPointsController>(c.getRootNode(), c);
					this.center(sinkBreakdown, GuiUtil.createItemView(e.getKey(), e.getValue().floatValue(), sinkBreakdown));
				}
			}
		}

		if (flags.contains(StatFlags.POWER)) {
			float[] avgMinMax = new float[3];
			TreeMap<FunctionalBuilding, Float> breakdown = powerBreakdown == null ? null : new TreeMap();
			factory.computeNetPowerProduction(avgMinMax, breakdown);
			String text = String.format("%.2fMW", avgMinMax[0]);
			if (Math.abs(avgMinMax[1]-avgMinMax[2]) > 0.1) {
				text = String.format("%s average (%.2fMW to %.2fMW range)", text, avgMinMax[1], avgMinMax[2]);
			}
			if (powerProduction != null) {
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
			if (powerBreakdown != null) {
				powerBreakdown.getChildren().clear();
				for (Entry<FunctionalBuilding, Float> e : breakdown.entrySet()) {
					PowerBreakdownEntryController c = new PowerBreakdownEntryController(e.getKey(), e.getValue());
					GuiInstance<PowerBreakdownEntryController> gui = new GuiInstance<PowerBreakdownEntryController>(c.getRootNode(), c);
					powerBreakdown.addEntry(gui);
				}
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

		if (flags.contains(StatFlags.CONSUMPTION)) {
			netConsumptionBar.clear();
			deficiencyBar.clear();

			for (Consumable c : factory.getAllIngredients()) {
				float sup = factory.getExternalInput(c, false);
				float ship = sup-factory.getTotalProduction(c);
				if (sup > 0 && ship > 0.0001 && ship >= Setting.IOTHRESH.getCurrentValue().floatValue()) {
					this.center(netConsumptionBar, GuiUtil.createItemView(c, ship, netConsumptionBar));
				}
				float deficiency = factory.getTotalConsumption(c)-factory.getTotalProduction(c)-factory.getExternalInput(c, false);
				if (deficiency > 0.0001 && deficiency >= Setting.IOTHRESH.getCurrentValue().floatValue()) {
					this.center(deficiencyBar, GuiUtil.createItemView(c, deficiency, deficiencyBar)).controller.setState(WarningState.INSUFFICIENT);
				}
			}
		}

		if (flags.contains(StatFlags.PRODUCTION)) {
			netProductBar.clear();
			HashSet<Consumable> set = new HashSet(factory.getAllProducedItems());
			if (Setting.INOUT.getCurrentValue() != InputInOutputOptions.EXCLUDE)
				set.addAll(factory.getAllMinedItems());
			if (Setting.INOUT.getCurrentValue() == InputInOutputOptions.ALL)
				set.addAll(factory.getAllSuppliedItems());
			ArrayList<Consumable> li = new ArrayList(set);
			Collections.sort(li);
			for (Consumable c : li) {
				float amt = factory.getTotalProduction(c)-factory.getTotalConsumption(c);
				if (Setting.INOUT.getCurrentValue() != InputInOutputOptions.EXCLUDE)
					amt += factory.getExternalInput(c, Setting.INOUT.getCurrentValue() == InputInOutputOptions.ALL ? false : true);
				if (amt > 0.0001 && amt >= Setting.IOTHRESH.getCurrentValue().floatValue()) {
					GuiInstance<ItemRateController> gui = this.center(netProductBar, GuiUtil.createItemView(c, amt, netProductBar));
					if (!factory.getDesiredProducts().contains(c))
						gui.controller.setState(WarningState.LEFTOVER);
				}
			}
		}

		this.getRootNode().layout();
	}

	private GuiInstance<ItemRateController> center(ExpandingTilePane exp, GuiInstance<ItemRateController> gui) {
		//exp.setMargin(gui.rootNode, new Insets(-8, -4, -8, 4));
		//exp.setVgap(Math.max(exp.getVgap(), 16+8));
		exp.setPadding(new Insets(0, 4, 0, 4));
		return gui;
	}

	protected final EnumSet<StatFlags> getAllExcept(StatFlags... flags) {
		EnumSet<StatFlags> set = EnumSet.allOf(StatFlags.class);
		if (flags.length == 1)
			set.remove(flags[0]);
		else if (flags.length > 0)
			set.removeAll(JavaUtil.makeListFrom(flags));
		return set;
	}

	protected final boolean areWarningsActive() {
		return anyWarnings;
	}

	protected static enum StatFlags {
		WARNINGS,
		BUILDINGS,
		PRODUCTION,
		CONSUMPTION,
		LOCALSUPPLY,
		POWER,
		TIER,
		SINK,
		;
	}
}

