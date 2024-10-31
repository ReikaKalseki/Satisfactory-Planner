package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public interface ItemConsumerProducer {

	public String getDisplayName();

	public NamedIcon getLocationIcon();

	public Map<Consumable, Double> getIngredientsPerMinute();

	public Map<Consumable, Double> getProductsPerMinute();

	//public boolean isPackaging();

	//public boolean isUnpackaging();

	//public boolean isFindables();

}
