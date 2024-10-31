package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Warning.PortThroughputWarning;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;

public abstract class BaseResourceNode<S extends BaseResourceNode<S, R>, R extends Consumable> implements ExtractableResource<S, R> {

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
		double yield = this.getYield();
		int max = this.getMaximumThroughput();
		if (yield > max+0.0001) {
			c.accept(new PortThroughputWarning(this.getDescriptiveName(), yield, resource instanceof Fluid ? PipeTier.TWO : BeltTier.SIX, 1));
		}
	}

	@Override
	public int fineCompare(BaseResourceNode r) {
		return purityLevel.compareTo(r.purityLevel);
	}

}
