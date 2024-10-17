package Reika.SatisfactoryPlanner.Data.Objects.Buildables;

import java.util.HashMap;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

public class SimpleProductionBuilding extends FunctionalBuilding {

	private static final HashMap<String, String> producerItems = new HashMap();

	static {
		producerItems.put("Build_TreeGiftProducer_C", "Desc_Gift_C");
	}

	private float productionRate;

	public SimpleProductionBuilding(String id, String dis, String icon, float mw, int sslots) {
		super(id, dis, icon, mw, sslots);
	}

	public SimpleProductionBuilding(String id, String dis, String icon, float mw, float exp, float sexp, int sslots) {
		super(id, dis, icon, mw, exp, sexp, sslots);
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.SIMPLEPROD;
	}

	public void setDelay(float time) {
		productionRate = 60F/time;
	}

	public float getProductionRate() {
		return productionRate;
	}

	public Consumable getItem() {
		return Database.lookupItem(producerItems.get(id));
	}

}
