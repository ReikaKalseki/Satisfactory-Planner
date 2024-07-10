package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Warning.ResourceIconName;

public class RecipeProductLoop {

	public final Recipe recipe1;
	public final Recipe recipe2;
	public final Consumable item1;
	public final Consumable item2;

	public RecipeProductLoop(Consumable c1, Consumable c2, Recipe r1, Recipe r2) {
		item1 = c1;
		item2 = c2;
		recipe1 = r1;
		recipe2 = r2;
	}

	@Override
	public String toString() {
		return "Recipe production loop: "+item1.displayName+" from "+recipe1.displayName+" <--> "+item2.displayName+" from "+recipe2.displayName;
	}

	public Warning getDeadlockWarning() {
		if (item1 instanceof Fluid || item2 instanceof Fluid) {
			String msg = "A production loop exists between "+recipe1.displayName+" and "+recipe2.displayName+", but is also supplied externally. This risks a deadlock if consumption of that fluid drops or supply exceeds expectations. Consider either splitting these into two isolated fluid networks, or adding another recipe to consume excess "+p.item.displayName, new ResourceIconName(p.item);
		}
		else {

		}
	}

}
