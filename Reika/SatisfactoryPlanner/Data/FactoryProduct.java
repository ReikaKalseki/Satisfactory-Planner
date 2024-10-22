package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class FactoryProduct {

	public final Consumable item;

	public boolean isSinking;

	public FactoryProduct(Consumable c) {
		item = c;
	}

}
