package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class SimpleProductionSupply implements ResourceSupply<SimpleProductionSupply, Consumable> {

	public final SimpleProductionBuilding producer;

	public int count = 1;

	public SimpleProductionSupply(SimpleProductionBuilding b) {
		producer = b;
	}

	private SimpleProductionSupply(JSONObject obj) {
		this((SimpleProductionBuilding)Database.lookupBuilding(obj.getString("building")));
		count = obj.getInt("amount");
	}

	@Override
	public final float getYield() {
		return producer.getProductionRate()*count;
	}

	public final Consumable getResource() {
		return producer.getItem();
	}

	@Override
	public void save(JSONObject block) {
		block.put("building", producer.id);
		block.put("amount", count);
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {

	}

	@Override
	public int getBuildingCount() {
		return count;
	}

	@Override
	public int fineCompare(SimpleProductionSupply r) {
		return producer.compareTo(r.producer);
	}

	@Override
	public String getDisplayName() {
		return producer.displayName;
	}

	@Override
	public NamedIcon getLocationIcon() {
		return producer;
	}

	@Override
	public Building getBuilding() {
		return producer;
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.SIMPLEPROD;
	}

	@Override
	public SimpleProductionSupply duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSubSortIndex() {
		return -5;
	}

}
