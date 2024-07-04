package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

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

}
