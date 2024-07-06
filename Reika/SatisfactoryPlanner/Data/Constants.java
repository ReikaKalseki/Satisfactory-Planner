package Reika.SatisfactoryPlanner.Data;

import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.function.Predicate;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Main;

import javafx.scene.image.Image;

public class Constants {

	public static final int WATER_PUMP_PRODUCTION = 120;

	public static final int TRUCK_STOP_PORTS = 2;
	public static final int TRAIN_STATION_PORTS = 2;
	public static final int DRONE_STOP_PORTS = 1;

	public static final int BASE_SOLID_YIELD = 60;
	public static final int BASE_OIL_YIELD = 120;
	public static final int BASE_FRACKING_YIELD = 60;

	public static enum Purity {
		IMPURE(0.5F),
		NORMAL(1F),
		PURE(2F);

		public final float yieldMultiplier;

		public final Image image;

		private Purity(float y) {
			yieldMultiplier = y;
			image = new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.name().toLowerCase(Locale.ENGLISH)+".png"));
		}

		public int getSolidYield() {
			return (int)(BASE_SOLID_YIELD*yieldMultiplier);
		}

		public int getOilYield() {
			return (int)(BASE_OIL_YIELD*yieldMultiplier);
		}

		public int getFrackingYield() {
			return (int)(BASE_FRACKING_YIELD*yieldMultiplier);
		}
	}

	public static interface BuildingTier {

		public Building getBuilding();

	}

	public static enum BeltTier implements BuildingTier {
		ONE(60),
		TWO(120),
		THREE(270),
		FOUR(480),
		FIVE(780);

		public final int maxThroughput;

		private BeltTier(int s) {
			maxThroughput = s;
		}

		public Building getBuilding() {
			return Database.lookupBuilding("Build_ConveyorBeltMk"+(this.ordinal()+1)+"_C");

		}
	}

	public static enum PipeTier implements BuildingTier {
		ONE(300, "Build_Pipeline_C"),
		TWO(600, "Build_PipelineMK2_C");

		public final int maxThroughput;
		public final String buildingID;

		private PipeTier(int s, String id) {
			maxThroughput = s;
			buildingID = id;
		}

		public Building getBuilding() {
			return Database.lookupBuilding(buildingID);

		}
	}

	public static enum MinerTier implements BuildingTier {
		ONE(1),
		TWO(2),
		THREE(4);

		public final float speedMultiplier;

		private MinerTier(float s) {
			speedMultiplier = s;
		}

		public Building getBuilding() {
			return Database.lookupBuilding("Build_MinerMk"+(this.ordinal()+1)+"_C");
		}
	}

	public static enum ResourceSupplyType {
		MINER(SolidResourceNode.class),
		WATER(WaterExtractor.class),
		OIL(OilNode.class),
		FRACKING(FrackingCluster.class),
		BELT(InputBelt.class),
		PIPE(InputPipe.class),
		TRUCK(TruckStation.class),
		TRAIN(TrainStation.class),
		DRONE(DroneStation.class),
		;

		private final Class<? extends ResourceSupply> objectClass;

		private ResourceSupplyType(Class<? extends ResourceSupply> c) {
			objectClass = c;
		}

		public ResourceSupply construct(JSONObject obj) throws Exception {
			Constructor<ResourceSupply> c = (Constructor<ResourceSupply>)objectClass.getDeclaredConstructor(JSONObject.class);
			c.setAccessible(true);
			return c.newInstance(obj);
		}
	}

	public static enum ToggleableVisiblityGroup {
		EQUIPMENT(i -> i.isEquipment, r -> r.isSoleProduct(i -> i.isEquipment)),
		BIOMASS(i -> i.isBiomass, r -> r.isSoleProduct(i -> i.isBiomass)),
		FICSMAS(i -> i.isFicsmas, r -> r.isFicsmas),
		ALTERNATE(i -> false, r -> r.isAlternate),
		POST10(i -> false, r -> false), //TODO POST10
		FINDABLE(i -> i.isFindables(), r -> r.isFindables()),
		PACKAGING(i -> false, r -> r.isPackaging()),
		UNPACKAGING(i -> false, r -> r.isUnpackaging()),
		;

		public final int bitflag;

		public final Predicate<Consumable> isItemInGroup;
		public final Predicate<Recipe> isRecipeInGroup;

		private ToggleableVisiblityGroup(Predicate<Consumable> pi, Predicate<Recipe> pr) {
			isItemInGroup = pi;
			isRecipeInGroup = pr;

			bitflag = 1 << this.ordinal();
		}
	}
}
