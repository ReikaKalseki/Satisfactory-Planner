package Reika.SatisfactoryPlanner.Data;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

import javafx.scene.image.Image;

public interface ResourceSupply<S extends ResourceSupply<S, R>, R extends Consumable> extends ItemConsumerProducer, NamedIcon {

	public static final Comparator<ResourceSupply> globalSupplySorter = new Comparator<ResourceSupply>() {

		@Override
		public int compare(ResourceSupply o1, ResourceSupply o2) {
			int idx1 = this.getMainSortIndex(o1);
			int idx2 = this.getMainSortIndex(o2);
			return idx1 == idx2 ? this.compareSame(o1, o2) : Integer.compare(idx1, idx2);
		}

		private int compareSame(ResourceSupply o1, ResourceSupply o2) {
			Consumable c1 = o1.getResource();
			Consumable c2 = o2.getResource();
			return c1 == c2 ? o1.fineCompare(o2) : c1.compareTo(c2);
		}

		private int getMainSortIndex(ResourceSupply rs) {
			return (rs instanceof LogisticSupply ? 1000000 : (rs instanceof BaseResourceNode || rs instanceof WaterExtractor ? -1000000 : 0))+1000*rs.getSubSortIndex();
		}

	};

	public float getYield();
	public R getResource();

	public void save(JSONObject block);

	public Building getBuilding();
	public ResourceSupplyType getType();
	public S duplicate();

	public void getWarnings(Consumer<Warning> c);

	public default int getBuildingCount() {
		return 1;
	}

	public default String getDescriptiveName() {
		return this.getDisplayName()+" ["+this.getResource().displayName+"]";
	}

	public default Map<Consumable, Float> getIngredientsPerMinute() {
		return Map.of();
	}

	public default Map<Consumable, Float> getProductsPerMinute() {
		return Map.of(this.getResource(), (float)this.getYield());
	}

	@Override
	public default Image createIcon(int size) {
		return this.getLocationIcon().createIcon(size);
	}

	public default int fineCompare(S r) {
		return 0;
	}

	public int getSubSortIndex();

	public default float getPowerCost() {
		Building b = this.getBuilding();
		return b instanceof FunctionalBuilding ? this.getBuildingCount()*((FunctionalBuilding)b).basePowerCostMW : 0;
	}

}
