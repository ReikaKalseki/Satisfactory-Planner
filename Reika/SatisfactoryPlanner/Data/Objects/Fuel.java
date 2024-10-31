package Reika.SatisfactoryPlanner.Data.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Reika.SatisfactoryPlanner.Data.Constants;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;

public class Fuel implements ItemConsumerProducer, Comparable<Fuel> {

	private static final ArrayList<Fuel> fuels = new ArrayList();

	public final Generator generator;
	public final Consumable item;
	public final Consumable secondaryItem;
	public final double primaryBurnRate;
	public final double secondaryBurnRate;

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
	private double computePrimaryRate() {
		float ret = item.energyValue > 0 ? generator.powerGenerationMW/item.energyValue*60 : 0;
		if (item instanceof Fluid)
			ret /= Constants.LIQUID_SCALAR;
		return ret;
	}

	private double computeSecondaryRate() {
		if (secondaryItem == null)
			return 0;
		float ret = 60 * generator.powerGenerationMW * generator.supplementalRatio;//generator.getSecondaryBurnRate(1);
		if (secondaryItem instanceof Fluid)
			ret /= Constants.LIQUID_SCALAR;
		return ret;
	}

	public double getByproductRate() {
		return byproductAmount*primaryBurnRate;
	}

	@Override
	public Map<Consumable, Double> getIngredientsPerMinute() {
		return secondaryItem == null ? Map.of(item, primaryBurnRate) : Map.of(item, primaryBurnRate, secondaryItem, secondaryBurnRate);
	}

	@Override
	public Map<Consumable, Double> getProductsPerMinute() {
		return byproduct == null ? Map.of() : Map.of(byproduct, this.getByproductRate());
	}

	@Override
	public int compareTo(Fuel o) {
		return item.compareTo(o.item);
	}

}
