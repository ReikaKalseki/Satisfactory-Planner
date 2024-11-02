package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Warning.DroneThroughputWarning;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;

public class DroneStation extends LogisticSupply<DroneStation, Item> {

	public DroneStation(Item c) {
		super(c);
	}

	private DroneStation(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")));
		this.setAmount(obj.getDouble("amount"));
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
	public DroneStation duplicate() {
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

	@Override
	public void getWarnings(Consumer<Warning> c) {
		super.getWarnings(c);
		int droneCapacity = Constants.DRONE_CAPACITY*resource.stackSize;
		int maxThru = droneCapacity/4; //assume avg 4 min round trip
		if (this.getYield() > maxThru)
			c.accept(new DroneThroughputWarning(this));
	}

}
