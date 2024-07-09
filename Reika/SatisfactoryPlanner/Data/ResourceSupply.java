package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public interface ResourceSupply<R extends Consumable> {

	public int getYield();
	public R getResource();
	public Resource getIcon();

	public void save(JSONObject block);

	public ResourceSupplyType getType();
	public ResourceSupply<R> duplicate();

	public void getWarnings(Consumer<Warning> c);

	public String getDisplayName();

	public default String getDescriptiveName() {
		return this.getDisplayName()+" ["+this.getResource().displayName+"]";
	}

}
