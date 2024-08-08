package Reika.SatisfactoryPlanner;

import java.io.File;

import Reika.SatisfactoryPlanner.Data.Constants.ToggleableVisiblityGroup;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Fuel;
import Reika.SatisfactoryPlanner.Data.Generator;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.Data.ResourceSupply;

public interface FactoryListener extends Comparable<FactoryListener> {

	public int getSortIndex();
	public void setFactory(Factory f);

	public void onAddRecipe(Recipe r);
	public void onRemoveRecipe(Recipe r);
	public void onSetCount(Recipe r, float count);
	public void onSetCount(Generator g, Fuel fuel, int old, int count);
	public void onAddProduct(Consumable c);
	public void onRemoveProduct(Consumable c);
	public void onAddSupply(ResourceSupply s);
	public void onRemoveSupply(ResourceSupply s);
	public void onSetToggle(ToggleableVisiblityGroup grp, boolean active);
	public void onUpdateIO();
	public void onLoaded();
	public void onCleared();
	public void onSetFile(File f);

	public default int compareTo(FactoryListener f) {
		return Integer.compare(this.getSortIndex(), f.getSortIndex());
	}

}