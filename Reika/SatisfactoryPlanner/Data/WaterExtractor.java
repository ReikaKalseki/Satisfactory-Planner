package Reika.SatisfactoryPlanner.Data;


public class WaterExtractor implements ExtractableResource<Fluid> {

	private float clockSpeed = 1;

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
	public Building getBuilding() {
		return Database.lookupBuilding("Build_WaterPump_C");
	}

}
