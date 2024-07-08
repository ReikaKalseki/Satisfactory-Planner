package Reika.SatisfactoryPlanner.Data;


public class Fuel {

	public final Generator generator;
	public final Consumable item;
	public final Consumable secondaryItem;
	public final float primaryBurnRate;
	public final float secondaryBurnRate;

	public final Consumable byproduct;
	public final int byproductAmount;

	public Fuel(Generator g, Consumable in, Consumable in2) {
		this(g, in, in2, null, 0);
	}

	public Fuel(Generator g, Consumable in, Consumable in2, Consumable out, int amt) {
		generator = g;
		item = in;
		primaryBurnRate = this.computePrimaryRate();
		secondaryItem = in2;
		secondaryBurnRate = this.computeSecondaryRate();
		byproduct = out;
		byproductAmount = amt;
	}

	/** Per minute */
	public float computePrimaryRate() {
		return item.energyValue > 0 ? generator.powerGenerationMW/item.energyValue*60 : 0;
	}

	private float computeSecondaryRate() {
		if (secondaryItem == null)
			return 0;
		float ret = 60 * generator.powerGenerationMW * generator.supplementalRatio;//generator.getSecondaryBurnRate(1);
		if (secondaryItem instanceof Fluid)
			ret /= Constants.LIQUID_SCALAR;
		return ret;
	}

}
