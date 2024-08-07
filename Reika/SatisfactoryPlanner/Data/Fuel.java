package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Fuel implements ItemConsumerProducer {

	private static final ArrayList<Fuel> fuels = new ArrayList();

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

		fuels.add(this);
	}

	public static List<Fuel> getFuels() {
		return Collections.unmodifiableList(fuels);
	}

	@Override
	public String getDisplayName() {
		return generator.displayName+" ("+item.displayName+")";
	}

	@Override
	public String toString() {
		return item+" x "+primaryBurnRate+" + "+secondaryItem+" x "+secondaryBurnRate+" > "+byproduct+" x "+this.getByproductRate()+" in "+generator;
	}

	@Override
	public Resource getLocationIcon() {
		return generator;
	}

	/** Per minute */
	private float computePrimaryRate() {
		float ret = item.energyValue > 0 ? generator.powerGenerationMW/item.energyValue*60 : 0;
		if (item instanceof Fluid)
			ret /= Constants.LIQUID_SCALAR;
		return ret;
	}

	private float computeSecondaryRate() {
		if (secondaryItem == null)
			return 0;
		float ret = 60 * generator.powerGenerationMW * generator.supplementalRatio;//generator.getSecondaryBurnRate(1);
		if (secondaryItem instanceof Fluid)
			ret /= Constants.LIQUID_SCALAR;
		return ret;
	}

	public float getByproductRate() {
		return byproductAmount*primaryBurnRate;
	}

	@Override
	public Map<Consumable, Float> getIngredientsPerMinute() {
		return secondaryItem == null ? Map.of(item, primaryBurnRate) : Map.of(item, primaryBurnRate, secondaryItem, secondaryBurnRate);
	}

	@Override
	public Map<Consumable, Float> getProductsPerMinute() {
		return byproduct == null ? Map.of() : Map.of(byproduct, this.getByproductRate());
	}

}
