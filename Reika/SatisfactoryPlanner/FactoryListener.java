package Reika.SatisfactoryPlanner;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.Future;

import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;

public interface FactoryListener extends Comparable<FactoryListener> {

	public int getSortIndex();
	/** This should also add it as a listener */
	public void setFactory(Factory f);

	public void onAddRecipe(Recipe r);
	public void onAddRecipes(Collection<Recipe> cc);
	public void onRemoveRecipe(Recipe r);
	public void onRemoveRecipes(Collection<Recipe> c);
	public void onSetCount(Recipe r, double count);
	public void onSetCount(Generator g, Fuel fuel, double old, double count);
	public void onAddProduct(Consumable c);
	public void onRemoveProduct(Consumable c);
	public void onRemoveProducts(Collection<Consumable> c);
	public void onToggleProductSink(Consumable c);
	public void onAddSupply(ResourceSupply s);
	public void onAddSupplies(Collection<? extends ResourceSupply> c);
	public void onRemoveSupply(ResourceSupply s);
	public void onRemoveSupplies(Collection<? extends ResourceSupply> c);
	public void onUpdateSupply(ResourceSupply s);
	public void onSetToggle(ToggleableVisiblityGroup grp, boolean active);
	public void onUpdateIO();
	public Future<Void> onLoaded();
	public void onCleared();
	public void onSetFile(File f);

	public default int compareTo(FactoryListener f) {
		return Integer.compare(this.getSortIndex(), f.getSortIndex());
	}

}
