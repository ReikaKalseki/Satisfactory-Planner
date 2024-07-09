package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Warning.ThroughputWarning;

public abstract class LogisticSupply<R extends Consumable> implements ResourceSupply<R> {

	public final R resource;

	private int chosenThroughput;

	protected LogisticSupply(R c) {
		resource = c;
	}

	public final void setAmount(int amt) {
		chosenThroughput = amt;
	}

	@Override
	public final int getYield() {
		return chosenThroughput;
	}

	public abstract int getMaximumIO();

	public final boolean isBottlenecked() {
		return chosenThroughput > this.getMaximumIO();
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
		int max = this.getMaximumIO();
		if (chosenThroughput > max) {
			c.accept(new ThroughputWarning(this.getDescriptiveName(), chosenThroughput, max));
		}
	}

}
