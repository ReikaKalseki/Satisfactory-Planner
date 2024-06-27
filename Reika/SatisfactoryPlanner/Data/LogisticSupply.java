package Reika.SatisfactoryPlanner.Data;


public abstract class LogisticSupply implements ResourceSupply {

	public final Consumable resource;

	private int chosenThroughput;

	protected LogisticSupply(Consumable c) {
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

}
