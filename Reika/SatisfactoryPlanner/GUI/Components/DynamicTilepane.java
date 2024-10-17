package Reika.SatisfactoryPlanner.GUI.Components;

import javafx.scene.layout.TilePane;

@Deprecated
public class DynamicTilepane {

	public final TilePane pane;

	public DynamicTilepane(TilePane p) {
		pane = p;
		/*
		p.widthProperty().addListener((val, old, nnew) -> {
			int rows = 1;
			double w = 0;
			for (Item i : cost.keySet()) {
				GuiInstance<ItemCountController> added = GuiUtil.addIconCount(i, cost.get(i), buildCostBar);
				w += added.controller.getWidth()+buildCostBar.getHgap()*2;
				Logging.instance.log("Added "+i+" x "+cost.get(i)+" w="+added.controller.getWidth()+"+"+buildCostBar.getHgap()+", sum="+w+"/"+buildCostBar.getWidth());
				if (w >= buildCostBar.getWidth()) {
					rows++;
					w = 0;
				}
			}

			Logging.instance.log("rows="+rows);
			//double cols = buildCostBar.getWidth()/(buildCostBar.getTileWidth()+buildCostBar.getHgap());
			//int rows = (int)Math.ceil(cost.size()/cols);
			buildCostBar.setMinHeight(rows*buildCostBar.getTileHeight()+(rows-1)*buildCostBar.getVgap());
			statisticsGrid.getRowConstraints().get(1).setMinHeight(buildCostBar.getMinHeight());
		});*/
	}

}
