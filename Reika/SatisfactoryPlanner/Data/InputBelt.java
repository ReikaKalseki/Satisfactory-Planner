package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;

public class InputBelt extends LogisticSupply<Item> {

	public final BeltTier tier;

	public InputBelt(Item i, BeltTier b) {
		super(i);
		tier = b;
	}

	@Override
	public int getMaximumIO() {
		return tier.maxThroughput;
	}

}
