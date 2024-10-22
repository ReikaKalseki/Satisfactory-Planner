package fxexpansions;

import static javafx.geometry.Orientation.HORIZONTAL;

import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

public class ExpandingTilePane<C extends SizedControllerBase> extends TilePane {

	private final HashMap<Node, GuiInstance<C>> nodes = new HashMap();

	private boolean needsResize;

	public double minRowHeight;

	public ExpandingTilePane() {
		super();
		this.widthProperty().addListener((val, old, nnew) -> {
			needsResize = true;
			Platform.runLater(() -> this.recomputeSize(nnew.doubleValue()));
		});
		this.getChildren().addListener((ListChangeListener)(c) -> {
			while (c.next()) {
				for (Node n : ((List<Node>)c.getRemoved()))
					nodes.remove(c);
			}
			needsResize = true;
			Platform.runLater(() -> this.recomputeSize(this.getWidth()));
		});
	}

	public void clear() {
		this.getChildren().clear();
		nodes.clear();
	}

	public void addEntry(GuiInstance<C> g) {
		this.getChildren().add(g.rootNode);
		nodes.put(g.rootNode, g);
	}

	public void removeEntry(GuiInstance<C> g) {
		this.getChildren().remove(g.rootNode);
		nodes.remove(g.rootNode, g);
	}

	private void recomputeSize(double w0) {
		if (!needsResize)
			return;
		needsResize = false;/*
		double height = 0;
		double maxRowH = -1;
		double w = 0;
		for (Node n : this.getChildren()) {
			GuiInstance<C> g = nodes.get(n);
			w += g.controller.getWidth()+this.getHgap()*2;
			maxRowH = Math.max(maxRowH, g.controller.getHeight());
			//System.out.println("Added "+g+" w="+g.controller.getWidth()+"+"+this.getHgap()+", sum="+w+"/"+this.getWidth());
			if (w >= w0+this.getHgap()) {
				System.out.println("Adding row @ w="+w+"/"+w0+", h="+maxRowH+" after "+g.controller);
				w = 0;
				height += maxRowH+this.getVgap();
				maxRowH = -1;
			}
			else {
				//System.out.println(g.controller+" still on same row, w="+w+"/"+w0);
			}
		}
		height += maxRowH;
		System.out.println("Net H="+height);
		this.setMinHeight(height);*/
		double min = minRowHeight;
		if (nodes.size() > 0) {
			min = Math.max(min, nodes.values().iterator().next().controller.getHeight());
		}
		this.setPrefHeight(Math.max(min, this.computePrefHeight(w0)));
		this.setMinHeight(Region.USE_PREF_SIZE);
		this.setMaxHeight(Region.USE_PREF_SIZE);
	}

	@Override
	protected double computePrefHeight(double forWidth) {
		final Insets insets = this.getInsets();
		int prefRows = 0;
		if (forWidth != -1) {
			int prefCols = this.computeColumns(forWidth - this.snapSpaceX(insets.getLeft()) - this.snapSpaceX(insets.getRight()), this.getTileWidth());
			prefRows = this.computeOther(this.getChildren().size(), prefCols);
		}
		else {
			prefRows = this.getOrientation() == HORIZONTAL? this.computeOther(this.getChildren().size(), this.getPrefColumns()) : this.getPrefRows();
		}
		return this.snapSpaceY(insets.getTop()) + this.computeContentHeight(prefRows, this.getRealTileHeight()) + this.snapSpaceY(insets.getBottom());
	}

	private double getRealTileHeight() {
		double max = 0;
		for (GuiInstance<C> gui : nodes.values()) {
			max = Math.max(max, gui.controller.getHeight());
		}
		for (Node n : this.getChildren()) {
			if (n instanceof Region)
				max = Math.max(max, ((Region)n).getHeight());
		}
		return max;
	}

	private double computeContentHeight(int rows, double tileheight) {
		if (rows == 0) return 0;
		return rows * tileheight + (rows - 1) * this.snapSpaceY(this.getVgap());
	}

	private int computeOther(int numNodes, int numCells) {
		double other = (double)numNodes/(double)Math.max(1, numCells);
		return (int)Math.ceil(other);
	}

	private int computeColumns(double width, double tilewidth) {
		double snappedHgap = this.snapSpaceX(this.getHgap());
		return Math.max(1,(int)((width + snappedHgap) / (tilewidth + snappedHgap)));
	}

	private int computeRows(double height, double tileheight) {
		double snappedVgap = this.snapSpaceY(this.getVgap());
		return Math.max(1, (int)((height + snappedVgap) / (tileheight + snappedVgap)));
	}

}
