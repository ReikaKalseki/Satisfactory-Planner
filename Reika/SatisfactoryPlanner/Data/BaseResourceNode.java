package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;

public abstract class BaseResourceNode implements ExtractableResource {

	public final Purity purityLevel;
	public final Consumable resource;

	private float clockSpeed = 1;

	public BaseResourceNode(Consumable c, Purity p) {
		resource = c;
		purityLevel = p;
	}

	public final void setClockSpeed(float spd) {
		clockSpeed = spd;
	}

	public final float getClockSpeed() {
		return clockSpeed;
	}

}
