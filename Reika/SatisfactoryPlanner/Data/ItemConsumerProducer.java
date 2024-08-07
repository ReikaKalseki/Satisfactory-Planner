package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

import Reika.SatisfactoryPlanner.NamedIcon;

public interface ItemConsumerProducer {

	public String getDisplayName();

	public NamedIcon getLocationIcon();

	public Map<Consumable, Float> getIngredientsPerMinute();

	public Map<Consumable, Float> getProductsPerMinute();

	//public boolean isPackaging();

	//public boolean isUnpackaging();

	//public boolean isFindables();

}
