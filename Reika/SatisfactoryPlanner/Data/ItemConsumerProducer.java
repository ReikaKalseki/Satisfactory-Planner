package Reika.SatisfactoryPlanner.Data;

import java.util.Map;

public interface ItemConsumerProducer {

	public String getDisplayName();

	public FunctionalBuilding getBuilding();

	public Map<Consumable, Float> getIngredientsPerMinute();

	public Map<Consumable, Float> getProductsPerMinute();

	//public boolean isPackaging();

	//public boolean isUnpackaging();

	//public boolean isFindables();

}
