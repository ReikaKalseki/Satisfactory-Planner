package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class FrackingCluster implements ExtractableResource<FrackingCluster, Fluid> {

	private final ArrayList<FrackingNode> nodes = new ArrayList();

	public final int pureCount;
	public final int normalCount;
	public final int impureCount;

	public final Fluid resource;

	private float clockSpeed = 1;

	public FrackingCluster(Fluid f, int pure, int normal, int impure) {
		resource = f;
		pureCount = pure;
		normalCount = normal;
		impureCount = impure;
		for (int i = 0; i < pure; i++)
			nodes.add(new FrackingNode(f, Purity.PURE));
		for (int i = 0; i < normal; i++)
			nodes.add(new FrackingNode(f, Purity.NORMAL));
		for (int i = 0; i < impure; i++)
			nodes.add(new FrackingNode(f, Purity.IMPURE));
	}

	private FrackingCluster(JSONObject obj) {
		this((Fluid)Database.lookupItem(obj.getString("item")), obj.getInt("pure"), obj.getInt("normal"), obj.getInt("impure"));
		this.setClockSpeed(obj.getFloat("clock"));
	}

	public float getYield() {
		int ret = 0;
		for (FrackingNode n : nodes)
			ret += n.getYield();
		return ret;
	}

	public void setClockSpeed(float spd) {
		clockSpeed = spd;
		for (FrackingNode n : nodes)
			n.setClockSpeed(spd);
	}

	public float getClockSpeed() {
		return clockSpeed;
	}

	@Override
	public Fluid getResource() {
		return resource;
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)Database.lookupBuilding("Build_FrackingSmasher_C");
	}

	@Override
	public void save(JSONObject block) {
		block.put("item", resource.id);
		block.put("pure", pureCount);
		block.put("normal", normalCount);
		block.put("impure", impureCount);
		block.put("clock", clockSpeed);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.FRACKING;
	}

	@Override
	public FrackingCluster duplicate() {
		return new FrackingCluster(resource, pureCount, normalCount, impureCount);
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {

	}

	@Override
	public String getDisplayName() {
		return "Resource Well Cluster";
	}

	@Override
	public int getSubSortIndex() {
		return 3;
	}

	public int getNodeCount() {
		return pureCount+normalCount+impureCount;
	}

	@Override
	public int fineCompare(FrackingCluster r) {
		return Integer.compare(this.getCountSort(), r.getCountSort());
	}

	private int getCountSort() {
		return pureCount*3+normalCount*2+impureCount;
	}

}
