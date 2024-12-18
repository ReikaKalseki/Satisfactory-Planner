package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Util.JSONUtil;

public class WaterExtractor implements ExtractableResource<WaterExtractor, Fluid> {

	public int numberExtractors = 1;

	private float clockSpeed = 1;

	public WaterExtractor() {

	}

	public WaterExtractor(int n) {
		numberExtractors = n;
	}

	private WaterExtractor(JSONObject obj) {
		numberExtractors = JSONUtil.getInt(obj, "count", 1);
		clockSpeed = obj.getFloat("clock");
	}

	@Override
	public double getYield() {
		return Constants.WATER_PUMP_PRODUCTION*clockSpeed*numberExtractors;
	}

	@Override
	public void setClockSpeed(float spd) {
		clockSpeed = spd;
	}

	public float getClockSpeed() {
		return clockSpeed;
	}

	@Override
	public Fluid getResource() {
		return (Fluid)Database.lookupItem("Desc_Water_C");
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)Database.lookupBuilding("Build_WaterPump_C");
	}

	@Override
	public void save(JSONObject block) {
		block.put("count", numberExtractors);
		block.put("clock", clockSpeed);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.WATER;
	}

	@Override
	public WaterExtractor duplicate() {
		WaterExtractor w = new WaterExtractor();
		w.numberExtractors = numberExtractors;
		return w;
	}

	@Override
	public String getDisplayName() {
		return this.getBuilding().displayName;
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {

	}

	@Override
	public int getSubSortIndex() {
		return 1;
	}

	@Override
	public int getBuildingCount() {
		return numberExtractors;
	}

	@Override
	public int fineCompare(WaterExtractor r) {
		return Integer.compare(numberExtractors, r.numberExtractors);
	}

}
