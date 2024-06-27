package Reika.SatisfactoryPlanner.Data;


public class WaterExtractor implements ExtractableResource {

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

}
