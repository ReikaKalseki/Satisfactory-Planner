package fxexpansions;

import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
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

	public void addEntry(GuiInstance<C> g) {
		this.getChildren().add(g.rootNode);
		nodes.put(g.rootNode, g);
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
		this.setMinHeight(Math.max(minRowHeight, this.computePrefHeight(w0)));
	}

}
