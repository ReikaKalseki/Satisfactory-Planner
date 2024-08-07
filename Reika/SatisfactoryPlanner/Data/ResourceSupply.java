package Reika.SatisfactoryPlanner.Data;

import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public interface ResourceSupply<R extends Consumable> extends ItemConsumerProducer {

	public int getYield();
	public R getResource();

	public void save(JSONObject block);

	public ResourceSupplyType getType();
	public ResourceSupply<R> duplicate();

	public void getWarnings(Consumer<Warning> c);

	public default String getDescriptiveName() {
		return this.getDisplayName()+" ["+this.getResource().displayName+"]";
	}

	public default Map<Consumable, Float> getIngredientsPerMinute() {
		return Map.of();
	}

	public default Map<Consumable, Float> getProductsPerMinute() {
		return Map.of(this.getResource(), (float)this.getYield());
	}

}
