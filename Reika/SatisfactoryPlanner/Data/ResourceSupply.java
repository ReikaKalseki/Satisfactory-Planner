package Reika.SatisfactoryPlanner.Data;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public interface ResourceSupply<R extends Consumable> extends ItemConsumerProducer {

	public static final Comparator<ResourceSupply> globalSupplySorter = new Comparator<ResourceSupply>() {

		@Override
		public int compare(ResourceSupply o1, ResourceSupply o2) {
			int idx1 = this.getMainSortIndex(o1);
			int idx2 = this.getMainSortIndex(o2);
			return idx1 == idx2 ? o1.getResource().compareTo(o2.getResource()) : Integer.compare(idx1, idx2);
		}

		private int getMainSortIndex(ResourceSupply rs) {
			return (rs instanceof LogisticSupply ? 1000000 : (rs instanceof BaseResourceNode || rs instanceof WaterExtractor ? -1000000 : 0))+1000*rs.getSubSortIndex();
		}

	};

	public int getYield();
	public R getResource();

	public void save(JSONObject block);

	public Building getBuilding();
	public ResourceSupplyType getType();
	public ResourceSupply<R> duplicate();

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

	public int getSubSortIndex();

}
