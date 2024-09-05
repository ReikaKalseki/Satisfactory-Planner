package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;

public class FunctionalBuilding extends Building {

	private static final ArrayList<String> crafterOrder = new ArrayList();

	static {
		crafterOrder.add("Build_SmelterMk1_C");
		crafterOrder.add("Build_ConstructorMk1_C");
		crafterOrder.add("Build_AssemblerMk1_C");
		crafterOrder.add("Build_FoundryMk1_C");
		crafterOrder.add("Build_ManufacturerMk1_C");
		crafterOrder.add("Build_OilRefinery_C");
		crafterOrder.add("Build_Packager_C");
		crafterOrder.add("Build_Blender_C");
		crafterOrder.add("Build_HadronCollider_C");
		crafterOrder.add("QConverter"); //TODO
		crafterOrder.add("QEncoder"); //TODO
	}

	public final float basePowerCostMW;

	public FunctionalBuilding(String id, String dis, String icon, float mw) {
		super(id, dis, icon);
		basePowerCostMW = mw;
	}

	@Override
	public int compareTo(Building o) {
		if (o instanceof FunctionalBuilding) {
			int idx = crafterOrder.indexOf(id);
			int idx2 = crafterOrder.indexOf(o.id);
			if (idx == idx2)
				return Float.compare(basePowerCostMW, ((FunctionalBuilding)o).basePowerCostMW); //loosely correlates with tier/advancement
			else
				return idx == -1 ? 1 : Integer.compare(idx, idx2);
		}
		else {
			return super.compareTo(o);
		}
	}

}
