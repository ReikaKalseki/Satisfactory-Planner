package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class ItemAmount {

	public final Consumable item;
	public final float amount;

	public ItemAmount(Consumable c, float amt) {
		item = c;
		amount = amt;
	}

}
