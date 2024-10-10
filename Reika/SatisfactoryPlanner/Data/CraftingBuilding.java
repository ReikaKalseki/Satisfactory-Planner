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

	public CraftingBuilding(String id, String dis, String icon, float mw, int sslots) {
		super(id, dis, icon, mw, sslots);
	}

	public CraftingBuilding(String id, String dis, String icon, float mw, float exp, float sexp, int sslots) {
		super(id, dis, icon, mw, exp, sexp, sslots);
	}

	@Override
	public int compareTo(Building o) {
		int ret = super.compareTo(o);
		if (o instanceof CraftingBuilding) {
			int idx = crafterOrder.indexOf(id);
			int idx2 = crafterOrder.indexOf(o.id);
			if (idx < 0)
				idx = 999;
			if (idx2 < 0)
				idx2 = 999;
			ret += (int)Math.signum(Integer.compare(idx, idx2))*1000;
		}
		return ret;
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.CRAFTER;
	}

}
