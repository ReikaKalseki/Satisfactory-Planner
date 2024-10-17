package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Warning.PortThroughputWarning;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;

public abstract class LogisticSupply<S extends LogisticSupply<S, R>, R extends Consumable> implements ResourceSupply<S, R> {

	public final R resource;

	private int chosenThroughput;

	protected LogisticSupply(R c) {
		resource = c;
	}

	public final void setAmount(int amt) {
		chosenThroughput = amt;
	}

	@Override
	public final float getYield() {
		return chosenThroughput;
	}

	public abstract int getPortCount();

	public RateLimitedSupplyLine getMaximumPortFlow() {
		return resource instanceof Fluid ? PipeTier.TWO : BeltTier.SIX;
	}

	public final boolean isBottlenecked() {
		return chosenThroughput > this.getPortCount()*this.getMaximumPortFlow().getMaxThroughput();
	}

	public final R getResource() {
		return resource;
	}

	@Override
	public void save(JSONObject block) {
		block.put("item", resource.id);
		block.put("amount", chosenThroughput);
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {
		if (this.isBottlenecked()) {
			c.accept(new PortThroughputWarning(this.getDescriptiveName(), chosenThroughput, this.getMaximumPortFlow(), this.getPortCount()));
		}
	}

	@Override
	public int fineCompare(LogisticSupply r) {
		return Integer.compare(chosenThroughput, r.chosenThroughput);
	}

}
