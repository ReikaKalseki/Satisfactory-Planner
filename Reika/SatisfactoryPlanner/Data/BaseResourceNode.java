package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;

public abstract class BaseResourceNode<R extends Consumable> implements ExtractableResource<R> {

	public final Purity purityLevel;
	public final R resource;

	private float clockSpeed = 1;

	public BaseResourceNode(R c, Purity p) {
		resource = c;
		purityLevel = p;
	}

	public final void setClockSpeed(float spd) {
		clockSpeed = spd;
	}

	public final float getClockSpeed() {
		return clockSpeed;
	}

	@Override
	public final R getResource() {
		return resource;
	}

}
