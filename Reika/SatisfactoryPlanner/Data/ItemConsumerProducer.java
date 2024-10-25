package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import org.apache.commons.lang3.math.Fraction;

import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public interface ItemConsumerProducer {

	public String getDisplayName();

	public NamedIcon getLocationIcon();

	public Map<Consumable, Fraction> getIngredientsPerMinute();

	public Map<Consumable, Fraction> getProductsPerMinute();

	//public boolean isPackaging();

	//public boolean isUnpackaging();

	//public boolean isFindables();

}
