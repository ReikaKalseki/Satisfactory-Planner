package Reika.SatisfactoryPlanner.GUI;

import java.util.Map.Entry;
import java.util.Set;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;

public class OutputMatrix extends ScaledRecipeMatrix {

	public OutputMatrix(Factory f) {
		super(f, MatrixType.OUT);
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
