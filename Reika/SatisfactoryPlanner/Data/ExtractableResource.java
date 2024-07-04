package Reika.SatisfactoryPlanner.Data;


public interface ExtractableResource<R extends Consumable> extends ResourceSupply<R> {

	public float getClockSpeed();
	public void setClockSpeed(float spd);
	public FunctionalBuilding getBuilding();
	public default Resource getIcon() { return this.getBuilding(); }

}
