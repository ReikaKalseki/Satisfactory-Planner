package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;

public interface ExtractableResource<S extends ExtractableResource<S, R>, R extends Consumable> extends OverclockableResource<S, R>  {

	public float getClockSpeed();
	public void setClockSpeed(float spd);
	public default Resource getLocationIcon() { return this.getBuilding(); }

}
