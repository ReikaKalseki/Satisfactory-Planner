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
	public Resource getLocationIcon() {
		return Database.lookupVehicle("Desc_DroneTransport_C");// ;
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

	@Override
	public int getSubSortIndex() {
		return 4;
	}

	@Override
	public Building getBuilding() {
		return Database.lookupBuilding("Desc_DroneStation_C");
	}

}
