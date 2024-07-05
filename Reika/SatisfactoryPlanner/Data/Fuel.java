package Reika.SatisfactoryPlanner.Data;


public class Fuel {

	public final Consumable item;
	public final Consumable secondaryItem;
	public final float secondaryItemRatio;

	public final Consumable byproduct;
	public final int byproductAmount;

	public Fuel(Consumable in, Consumable in2, float in2Ratio) {
		this(in, in2, in2Ratio, null, 0);
	}

	public Fuel(Consumable in, Consumable in2, float in2Ratio, Consumable out, int amt) {
		item = in;
		secondaryItem = in2;
		secondaryItemRatio = in2Ratio;
		byproduct = out;
		byproductAmount = amt;
	}

}
