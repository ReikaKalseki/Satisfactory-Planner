package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Warning.ThroughputWarning;

public abstract class BaseResourceNode<R extends Consumable> implements ExtractableResource<R> {

	public final Purity purityLevel;
	public final R resource;

	private float clockSpeed = 1;

	public BaseResourceNode(R c, Purity p) {
		resource = c;
		purityLevel = p;
	}

	public abstract int getMaximumThroughput();

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

	@Override
	public void save(JSONObject block) {
		block.put("item", resource.id);
		block.put("purity", purityLevel.name());
		block.put("clock", clockSpeed);
	}

	@Override
	public String getDisplayName() {
		return resource.displayName+" Node";
	}

	@Override
	public String getDescriptiveName() {
		return String.format("%s [%s] (%.2f%s)", this.getDisplayName(), purityLevel.name(), clockSpeed*100, "%");
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {
		int yield = this.getYield();
		int max = this.getMaximumThroughput();
		if (yield > max) {
			c.accept(new ThroughputWarning(this.getDescriptiveName(), yield, max));
		}
	}

}
