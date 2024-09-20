package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;

public class CraftingBuilding extends FunctionalBuilding {

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
		crafterOrder.add("Build_Converter_C");
		crafterOrder.add("Build_QuantumEncoder_C");
	}

	public CraftingBuilding(String id, String dis, String icon, float mw) {
		super(id, dis, icon, mw);
	}

	@Override
	public int compareTo(Building o) {
		if (o instanceof CraftingBuilding) {
			int idx = crafterOrder.indexOf(id);
			int idx2 = crafterOrder.indexOf(o.id);
			if (idx == -1)
				idx = 9999;
			if (idx2 == -1)
				idx2 = 9999;
			return idx == idx2 ? super.compareTo(o) : Integer.compare(idx, idx2);
		}
		else {
			return 1;
		}
	}

}
