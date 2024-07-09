package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class TruckStation extends LogisticSupply<Item> {

	public TruckStation(Item c) {
		super(c);
	}

	private TruckStation(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")));
		this.setAmount(obj.getInt("amount"));
	}

	@Override
	public int getMaximumIO() {
		return Constants.TRUCK_STOP_PORTS*BeltTier.FIVE.maxThroughput;
	}

	@Override
	public Resource getIcon() {
		return Database.lookupVehicle("Desc_Truck_C");// Database.lookupBuilding("Desc_TrainDockingStation_C");
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.TRUCK;
	}

	@Override
	public ResourceSupply<Item> duplicate() {
		return new TruckStation(resource);
	}

	@Override
	public String getDisplayName() {
		return "Truck Route";
	}

}
