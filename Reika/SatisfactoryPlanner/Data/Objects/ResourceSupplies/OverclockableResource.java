package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;

public interface OverclockableResource<S extends OverclockableResource<S, R>, R extends Consumable> extends ResourceSupply<S, R> {

	/** This is the raw multiplier, not the % */
	public float getClockSpeed();
	public void setClockSpeed(float spd);

	@Override
	public default float getPowerCost() {
		Building b = this.getBuilding();
		return b instanceof FunctionalBuilding ? this.getBuildingCount()*((FunctionalBuilding)b).getPowerCost(this.getClockSpeed()*100, 0) : 0;
	}

}
