package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.BuildingTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;

public abstract class TieredLogisticSupply<S extends TieredLogisticSupply<S, T, R>, T extends BuildingTier & RateLimitedSupplyLine, R extends Consumable> extends LogisticSupply<S, R> {

	public T tier = this.getTierValues()[0];

	public TieredLogisticSupply(R i, T b) {
		super(i);
		tier = b;
	}

	@Override
	public final int getPortCount() {
		return 1;
	}

	@Override
	public final RateLimitedSupplyLine getMaximumPortFlow() {
		return tier == null ? null : tier;
	}

	@Override
	public final Resource getLocationIcon() {
		return tier == null ? null : tier.getBuilding();
	}

	@Override
	public final String getDisplayName() {
		return tier == null ? "None" : this.getLocationIcon().displayName;
	}

	@Override
	public final Building getBuilding() {
		return tier == null ? null : tier.getBuilding();
	}

	public abstract T[] getTierValues();

}
