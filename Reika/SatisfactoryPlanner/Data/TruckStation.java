package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;

public class TruckStation extends LogisticSupply {

	public TruckStation(Item c) {
		super(c);
	}

	@Override
	public int getMaximumIO() {
		return Constants.TRUCK_STOP_PORTS*BeltTier.FIVE.maxThroughput;
	}

}
