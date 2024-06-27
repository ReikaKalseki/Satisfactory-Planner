package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;

public class TrainStation extends LogisticSupply {

	public final int numberBuildings;

	public TrainStation(Consumable c, int buildings) {
		super(c);
		numberBuildings = buildings;
	}

	@Override
	public int getMaximumIO() {
		return numberBuildings*Constants.TRUCK_STOP_PORTS*(resource instanceof Fluid ? Constants.PIPE2_LIMIT : BeltTier.FIVE.maxThroughput);
	}

}
