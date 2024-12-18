package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Warning.WarningSeverity;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;

public class TrainStation<R extends Consumable> extends LogisticSupply<TrainStation<R>, R> {

	public final int numberBuildings;

	public final boolean isFluid;

	public TrainStation(R c, int buildings) {
		super(c);
		numberBuildings = buildings;
		isFluid = c instanceof Fluid;
	}

	private TrainStation(JSONObject obj) {
		this((R)Database.lookupItem(obj.getString("item")), obj.getInt("stations"));
		this.setAmount(obj.getDouble("amount"));
	}

	@Override
	public int getPortCount() {
		return numberBuildings*Constants.TRAIN_STATION_PORTS;
	}

	@Override
	public Resource getLocationIcon() {
		return Database.lookupBuilding(isFluid ? "Build_TrainDockingStationLiquid_C" : "Build_TrainDockingStation_C");
	}

	@Override
	public void save(JSONObject block) {
		super.save(block);
		block.put("stations", numberBuildings);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.TRAIN;
	}

	@Override
	public TrainStation duplicate() {
		return new TrainStation(resource, numberBuildings);
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {
		super.getWarnings(c);
		if (numberBuildings > 4) {
			c.accept(new Warning(WarningSeverity.INFO, "Long trains may bottleneck intersections and often require multiple locomotives"));
		}
	}

	@Override
	public String getDisplayName() {
		return "Train Station";
	}

	@Override
	public int getSubSortIndex() {
		return 3;
	}

	@Override
	public int getBuildingCount() {
		return numberBuildings;
	}

	@Override
	public Building getBuilding() {
		return Database.lookupBuilding(isFluid ? "Desc_TrainDockingStationLiquid_C" : "Desc_TrainDockingStation_C");
	}

}
