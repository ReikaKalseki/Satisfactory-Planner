package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class WaterExtractor implements ExtractableResource<Fluid> {

	public int numberExtractors = 1;

	private float clockSpeed = 1;

	public WaterExtractor() {

	}

	public WaterExtractor(int n) {
		numberExtractors = n;
	}

	private WaterExtractor(JSONObject obj) {
		numberExtractors = obj.has("count") ? obj.getInt("count") : 1;
		clockSpeed = obj.getFloat("clock");
	}

	@Override
	public int getYield() {
		return (int)(Constants.WATER_PUMP_PRODUCTION*clockSpeed)*numberExtractors;
	}

	@Override
	public void setClockSpeed(float spd) {
		clockSpeed = spd;
	}

	public float getClockSpeed() {
		return clockSpeed;
	}

	@Override
	public Fluid getResource() {
		return (Fluid)Database.lookupItem("Desc_Water_C");
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)Database.lookupBuilding("Build_WaterPump_C");
	}

	@Override
	public void save(JSONObject block) {
		block.put("count", numberExtractors);
		block.put("clock", clockSpeed);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.WATER;
	}

	@Override
	public ResourceSupply<Fluid> duplicate() {
		WaterExtractor w = new WaterExtractor();
		w.numberExtractors = numberExtractors;
		return w;
	}

	@Override
	public String getDisplayName() {
		return this.getBuilding().displayName;
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {

	}

}
