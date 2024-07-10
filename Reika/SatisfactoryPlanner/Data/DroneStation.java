package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class DroneStation extends LogisticSupply<Item> {

	public DroneStation(Item c) {
		super(c);
	}

	private DroneStation(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")));
		this.setAmount(obj.getInt("amount"));
	}

	@Override
	public int getPortCount() {
		return Constants.DRONE_STOP_PORTS;
	}

	@Override
	public Resource getIcon() {
		return Database.lookupVehicle("Desc_DroneTransport_C");// Database.lookupBuilding("Desc_TrainDockingStation_C");
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.DRONE;
	}

	@Override
	public ResourceSupply<Item> duplicate() {
		return new DroneStation(resource);
	}

	@Override
	public String getDisplayName() {
		return "Drone Port";
	}

}
