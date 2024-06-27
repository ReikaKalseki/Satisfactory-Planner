package Reika.SatisfactoryPlanner.Util;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;

public interface FactoryListener {

	public void onAddRecipe(Recipe r);
	public void onRemoveRecipe(Recipe r);
	public void onAddProduct(Consumable c);
	public void onRemoveProduct(Consumable c);
	public void onSetCount(Recipe r, int amt);
	public void onSetCount(Generator g, int amt);
	public void onAddSupply(ResourceSupply res);
	public void onRemoveSupply(ResourceSupply res);

}
