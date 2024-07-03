package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;

public class FrackingCluster implements ExtractableResource<Fluid> {

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

	public int getYield() {
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
	public Building getBuilding() {
		return Database.lookupBuilding("Desc_FrackingSmasher_C");
	}

}
