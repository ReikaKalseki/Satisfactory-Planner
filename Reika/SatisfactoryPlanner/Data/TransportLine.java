package Reika.SatisfactoryPlanner.Data;

public class TransportLine extends Building {

	public TransportLine(String id, String dis, String icon) {
		super(id, dis, icon);
	}

	@Override
	public BuildingCategory getCategory() {
		return BuildingCategory.LINE;
	}

}
