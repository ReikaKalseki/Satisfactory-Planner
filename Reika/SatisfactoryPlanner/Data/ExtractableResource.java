package Reika.SatisfactoryPlanner.Data;


public interface ExtractableResource<R extends Consumable> extends OverclockableResource<R>  {

	public float getClockSpeed();
	public void setClockSpeed(float spd);
	public FunctionalBuilding getBuilding();
	public default Resource getLocationIcon() { return this.getBuilding(); }

}
