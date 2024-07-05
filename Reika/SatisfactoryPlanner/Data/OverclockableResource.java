package Reika.SatisfactoryPlanner.Data;


public interface OverclockableResource<R extends Consumable> extends ResourceSupply<R> {

	public float getClockSpeed();
	public void setClockSpeed(float spd);

}
