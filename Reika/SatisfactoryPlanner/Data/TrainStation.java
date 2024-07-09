package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Warning.WarningSeverity;

public class TrainStation<R extends Consumable> extends LogisticSupply<R> {

	public final int numberBuildings;

	public final boolean isFluid;

	public TrainStation(R c, int buildings) {
		super(c);
		numberBuildings = buildings;
		isFluid = c instanceof Fluid;
	}

	private TrainStation(JSONObject obj) {
		this((R)Database.lookupItem(obj.getString("item")), obj.getInt("stations"));
		this.setAmount(obj.getInt("amount"));
	}

	@Override
	public int getMaximumIO() {
		return numberBuildings*Constants.TRUCK_STOP_PORTS*(isFluid ? PipeTier.TWO.maxThroughput : BeltTier.FIVE.maxThroughput);
	}

	@Override
	public Resource getIcon() {
		return Database.lookupBuilding(isFluid ? "Desc_TrainDockingStationLiquid_C" : "Desc_TrainDockingStation_C");
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
	public ResourceSupply<R> duplicate() {
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

}
