package Reika.SatisfactoryPlanner.GUI;

import java.util.Map.Entry;
import java.util.Set;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;

public class InputMatrix extends ScaledRecipeMatrix {

	public InputMatrix(Factory f) {
		super(f, MatrixType.IN);
	}
	/*
	@Override
	protected final void computeIO() {
		items = new ArrayList(owner.getAllIngredients());
		Collections.sort(items);
	}
	 */

	@Override
	protected Set<Entry<Consumable, Double>> getItemRates(ItemConsumerProducer r) {
		return r.getIngredientsPerMinute().entrySet();
	}

	@Override
	protected String getItemHeader() {
		return "Consuming";
	}

}
