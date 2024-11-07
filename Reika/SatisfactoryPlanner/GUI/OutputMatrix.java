package Reika.SatisfactoryPlanner.GUI;

import java.util.Map.Entry;
import java.util.Set;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController.WarningState;

import fxexpansions.GuiInstance;

public class OutputMatrix extends ScaledRecipeMatrix {

	public OutputMatrix(Factory f) {
		super(f, MatrixType.OUT);
	}

	@Override
	protected boolean sumAtTop() {
		return false;
	}

	@Override
	public double getAmount(Consumable c) {
		return this.getAvailable(c);
	}
	/*
	@Override
	protected final void computeIO() {
		HashSet<Consumable> out = new HashSet(owner.getAllProducedItems());
		if (owner.resourceMatrixRule != InclusionPattern.EXCLUDE)
			out.addAll(owner.getAllSuppliedItems());
		items = new ArrayList(out);
		Collections.sort(items);
	}
	 */
	@Override
	public void updateStatuses(Consumable c) {
		GuiInstance<ItemRateController> gui = sumEntries.get(c);
		if (gui != null) {
			gui.controller.setAmount(this.getAvailable(c));
			gui.controller.setState(owner.isExcess(c) ? WarningState.LEFTOVER : WarningState.NONE);
		}
	}

	@Override
	protected Set<Entry<Consumable, Double>> getItemRates(ItemConsumerProducer r) {
		return r.getProductsPerMinute().entrySet();
	}

	@Override
	protected String getItemHeader() {
		return "Producing Or Available";
	}

	@Override
	public void onUpdateSupply(ResourceSupply r) {
		switch (owner.resourceMatrixRule) {
			case EXCLUDE:
				break;
			case MERGE:
				Consumable item = r.getResource();
				int sum = 0;
				for (ResourceSupply r2 : owner.getSupplies()) {
					if (r2.getResource() == item)
						sum += r2.getYield();
				}
				recipeEntries.get(supplyGroup).setAmount(item, sum);
				break;
			case INDIVIDUAL:
				recipeEntries.get(r).setAmount(r.getResource(), r.getYield());
				break;
		}
	}

}
