package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class WaterExtractor implements ExtractableResource<Fluid> {

	private float clockSpeed = 1;

	public WaterExtractor() {

	}

	private WaterExtractor(JSONObject obj) {
		clockSpeed = obj.getFloat("clock");
	}

	@Override
	public int getYield() {
		return (int)(Constants.WATER_PUMP_PRODUCTION*clockSpeed);
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
		block.put("clock", clockSpeed);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.WATER;
	}

	@Override
	public ResourceSupply<Fluid> duplicate() {
		return new WaterExtractor();
	}

}
