package Reika.SatisfactoryPlanner.Util;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Recipe;

public interface FactoryListener {

	public void onAddRecipe(Recipe r);
	public void onRemoveRecipe(Recipe r);
	public void onAddProduct(Consumable c);
	public void onRemoveProduct(Consumable c);

}
