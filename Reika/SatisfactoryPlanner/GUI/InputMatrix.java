package Reika.SatisfactoryPlanner.GUI;

import java.util.Map.Entry;
import java.util.Set;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController.WarningState;

import fxexpansions.GuiInstance;

public class InputMatrix extends ScaledRecipeMatrix {

	public InputMatrix(Factory f) {
		super(f, MatrixType.IN);
	}

	@Override
	protected boolean sumAtTop() {
		return true;
	}

	@Override
	public double getAmount(Consumable c) {
		return owner.getTotalConsumption(c);
	}
	/*
	@Override
	protected final void computeIO() {
		items = new ArrayList(owner.getAllIngredients());
		Collections.sort(items);
	}
	 */
	@Override
	public void updateStatuses(Consumable c) {
		GuiInstance<ItemRateController> gui = sumEntries.get(c);
		if (gui != null) {
			double total = owner.getTotalConsumption(c);
			gui.controller.setAmount(total);
			gui.controller.setState(total > owner.getTotalProduction(c)+owner.getExternalInput(c, false)+0.0001 ? WarningState.INSUFFICIENT : WarningState.NONE);
		}
	}

	@Override
	protected Set<Entry<Consumable, Double>> getItemRates(ItemConsumerProducer r) {
		return r.getIngredientsPerMinute().entrySet();
	}

	@Override
	protected String getItemHeader() {
		return "Consuming";
	}

	@Override
	public void onSetCount(Generator g, Fuel fuel, double old, double count) {
		super.onSetCount(g, fuel, old, count);
		if (count > 0 && recipeEntries.containsKey(fuel))
			recipeEntries.get(fuel).setScale(count);
		this.updateStatuses(fuel);
	}

}
