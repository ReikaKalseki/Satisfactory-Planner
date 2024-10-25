package Reika.SatisfactoryPlanner.Data.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.Fraction;

import Reika.SatisfactoryPlanner.Data.Constants;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;

public class Fuel implements ItemConsumerProducer, Comparable<Fuel> {

	private static final ArrayList<Fuel> fuels = new ArrayList();

	public final Generator generator;
	public final Consumable item;
	public final Consumable secondaryItem;
	public final Fraction primaryBurnRate;
	public final Fraction secondaryBurnRate;

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
	private Fraction computePrimaryRate() {
		Fraction ret = item.energyValue > 0 ? Fraction.getFraction(generator.powerGenerationMW*60, item.energyValue) : Fraction.ZERO;
		if (item instanceof Fluid)
			ret = ret.divideBy(Fraction.getFraction(Constants.LIQUID_SCALAR));
		return ret;
	}

	private Fraction computeSecondaryRate() {
		if (secondaryItem == null)
			return Fraction.ZERO;
		Fraction ret = Fraction.getFraction(60 * generator.powerGenerationMW * generator.supplementalRatio);//generator.getSecondaryBurnRate(1);
		if (secondaryItem instanceof Fluid)
			ret = ret.divideBy(Fraction.getFraction(Constants.LIQUID_SCALAR));
		return ret;
	}

	public Fraction getByproductRate() {
		return primaryBurnRate.multiplyBy(byproductAmount);
	}

	@Override
	public Map<Consumable, Fraction> getIngredientsPerMinute() {
		return secondaryItem == null ? Map.of(item, primaryBurnRate) : Map.of(item, primaryBurnRate, secondaryItem, secondaryBurnRate);
	}

	@Override
	public Map<Consumable, Fraction> getProductsPerMinute() {
		return byproduct == null ? Map.of() : Map.of(byproduct, this.getByproductRate());
	}

	@Override
	public int compareTo(Fuel o) {
		return item.compareTo(o.item);
	}

}
