package Reika.SatisfactoryPlanner.GUI.Windows;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.BaseResourceNode;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.FrackingCluster;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.FromFactorySupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.OverclockableResource;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.SimpleProductionSupply;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.TrainStation;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.WaterExtractor;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.GUI.Components.FactoryStatisticsContainer;
import Reika.SatisfactoryPlanner.GUI.Components.ItemCountController;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.RecipeListCell;
import Reika.SatisfactoryPlanner.Util.ColorUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.GuiInstance;
import javafx.application.HostServices;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SummaryViewController extends FactoryStatisticsContainer {

	@FXML
	protected ScrollPane root;

	@FXML
	protected VBox mainContent;

	@FXML
	protected GridPane recipeGrid;

	@FXML
	protected GridPane supplyGrid;

	//@FXML
	//protected GridPane generatorGrid;

	@FXML
	protected Button exportButton;

	@FXML
	protected TilePane buttonBar;

	@FXML
	protected TitledPane generatorPanel;

	@FXML
	protected TitledPane supplyPanel;

	@FXML
	protected VBox generatorList;

	@Override
	public void init(HostServices services) throws IOException {
		super.init(services);

		GuiUtil.setButtonEvent(exportButton, () -> {
			String name = factory.name;
			if (Strings.isNullOrEmpty(name))
				name = "Unnamed";
			File png = Main.getRelativeFile("Summaries/"+URLEncoder.encode(name, Charsets.UTF_8)+".png");
			if (png.exists()) {
				if (!GuiUtil.getConfirmation("Exported summary '"+png.getName()+"' already exists\n(modified "+Main.timeStampFormat.format(new Date(png.lastModified()))+").\n\nDo you want to overwrite it?"))
					return;
			}
			buttonBar.setVisible(false);
			buttonBar.setManaged(false);
			this.getWindow().sizeToScene();
			BufferedImage img = SwingFXUtils.fromFXImage(mainContent.snapshot(null, null), null);
			png.getParentFile().mkdirs();
			ImageIO.write(img, "png", png);
			buttonBar.setVisible(true);
			buttonBar.setManaged(true);
			this.setSize();
			Logging.instance.log("Exported summary for factory "+name+" to "+png.getCanonicalPath());
			GuiUtil.raiseDialog(AlertType.INFORMATION, "Summary Saved", "Summary View saved to\n"+GuiUtil.splitToWidth(png.getCanonicalPath(), 400, "(?=[\\s\\\\]+)", GuiSystem.getDefaultFont()), a -> {
				Button b = (Button)a.getDialogPane().lookupButton(ButtonType.NEXT);
				b.setText("Show");
				GuiUtil.setButtonEvent(b, () -> services.showDocument(png.toPath().toString()));
			}, 600, ButtonType.NEXT, ButtonType.OK);
		});

		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		root.setMaxWidth(bounds.getWidth()*0.8);
		root.setMaxHeight(bounds.getHeight()*0.8);
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);
	}

	@Override
	public void setFactory(Factory f) {
		super.setFactory(f);
		this.updateStats();
	}

	private void setPanelVisibility(TitledPane tp, boolean vis) {
		tp.setVisible(vis);
		tp.setManaged(vis);
	}

	@Override
	public void updateStats(EnumSet<StatFlags> flags) {
		super.updateStats(flags);

		generatorList.getChildren().clear();

		boolean gens = factory.getTotalGeneratorCount() > 0;
		this.setPanelVisibility(generatorPanel, gens);
		if (gens) {
			for (Generator g : Database.getAllGenerators()) {
				if (factory.getCount(g) > 0) {
					HBox hb = new HBox();
					hb.setSpacing(24);
					hb.getChildren().add(g.createImageView(32));
					for (Fuel f : g.getFuels()) {
						int amt = factory.getCount(g, f);
						if (amt > 0) {
							ItemCountController cc = GuiUtil.addIconCount(f.item, amt, 4, true, hb).controller;
						}
					}
					generatorList.getChildren().add(hb);
				}
			}
		}

		this.setPanelVisibility(supplyPanel, !factory.getSupplies().isEmpty());
		this.setPanelVisibility(warningPanel, this.areWarningsActive());

		wrapperPanel.setText("Factory \""+factory.name+"\"");

		supplyGrid.getChildren().clear();
		supplyGrid.getColumnConstraints().clear();
		supplyGrid.getRowConstraints().clear();

		GuiUtil.setWidth(GuiUtil.addColumnToGridPane(supplyGrid, 0), 40);
		ColumnConstraints cc = GuiUtil.addColumnToGridPane(supplyGrid, 1);
		cc.setMaxWidth(Double.MAX_VALUE);
		cc.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cc.setMinWidth(Region.USE_COMPUTED_SIZE);
		cc.setHgrow(Priority.ALWAYS);
		cc.setFillWidth(true);
		cc.setHalignment(HPos.CENTER);
		GuiUtil.setWidth(GuiUtil.addColumnToGridPane(supplyGrid, 2), 100);
		supplyGrid.getColumnConstraints().get(supplyGrid.getColumnCount()-1).setHalignment(HPos.CENTER);

		int i = 0;
		ArrayList<ResourceSupply> li = new ArrayList(factory.getSupplies());
		Collections.sort(li, ResourceSupply.globalSupplySorter);
		for (ResourceSupply r : li) {
			GuiUtil.addRowToGridPane(supplyGrid, i);
			Region io = this.buildResourceSummaryRow(r);
			StackPane sp = GuiUtil.createItemDisplay(r.getLocationIcon(), 32, false);
			supplyGrid.add(sp, 0, i);
			supplyGrid.add(io, 1, i);
			GuiInstance<ItemRateController> ct = GuiUtil.createItemView(r.getResource(), r.getYield(), supplyGrid, 2, i);
			io.setMaxWidth(Double.MAX_VALUE);
			io.setMaxHeight(Double.MAX_VALUE);
			GuiUtil.setHeight(supplyGrid.getRowConstraints().get(i), 40);
			if (i%2 == 1) {
				io.getStyleClass().add("table-row-darken");
				sp.getStyleClass().add("table-row-darken");
				ct.rootNode.getStyleClass().add("table-row-darken");
			}
			i++;
		}

		recipeGrid.getChildren().clear();
		recipeGrid.getColumnConstraints().clear();
		recipeGrid.getRowConstraints().clear();

		GuiUtil.setWidth(GuiUtil.addColumnToGridPane(recipeGrid, 0), 240);
		cc = GuiUtil.addColumnToGridPane(recipeGrid, 1);
		cc.setMaxWidth(Double.MAX_VALUE);
		cc.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cc.setMinWidth(Region.USE_COMPUTED_SIZE);
		cc.setHgrow(Priority.ALWAYS);
		cc.setFillWidth(true);
		cc.setHalignment(HPos.CENTER);
		GuiUtil.setWidth(GuiUtil.addColumnToGridPane(recipeGrid, 2), 40);
		GuiUtil.setWidth(GuiUtil.addColumnToGridPane(recipeGrid, 3), 64);
		recipeGrid.getColumnConstraints().get(recipeGrid.getColumnCount()-1).setHalignment(HPos.CENTER);

		i = 0;
		for (Recipe r : factory.getRecipes()) {
			if (factory.getCount(r) <= 0)
				continue;
			GuiUtil.addRowToGridPane(recipeGrid, i);
			Region lb = new Label(r.displayName);
			if (r.isAlternate)
				lb = GuiUtil.createSpacedHBox(lb, Database.lookupItem("Desc_HardDrive_C").createImageView(), null);
			float amt = factory.getCount(r);
			Node io = RecipeListCell.buildIODisplay(r, false, amt);
			StackPane bld = GuiUtil.createItemDisplay(r.productionBuilding, 32, false);
			Label ct = new Label(String.format("x%.2f", amt));
			if (amt-(int)amt > 0.01)
				ct.setStyle("-fx-text-fill: "+ColorUtil.getCSSHex(UIConstants.WARN_COLOR));
			lb.setPadding(new Insets(0, 4, 0, 4));
			recipeGrid.add(lb, 0, i);
			recipeGrid.add(io, 1, i);
			recipeGrid.add(bld, 2, i);
			recipeGrid.add(ct, 3, i);
			lb.setMaxWidth(Double.MAX_VALUE);
			lb.setMaxHeight(Double.MAX_VALUE);
			ct.setMaxWidth(Double.MAX_VALUE);
			ct.setMaxHeight(Double.MAX_VALUE);
			ct.setAlignment(Pos.CENTER);

			GuiUtil.setHeight(recipeGrid.getRowConstraints().get(i), 40);
			if (i%2 == 1) {
				lb.getStyleClass().add("table-row-darken");
				io.getStyleClass().add("table-row-darken");
				bld.getStyleClass().add("table-row-darken");
				ct.getStyleClass().add("table-row-darken");
			}
			i++;
		}
		/*
		for (Generator g : Database.getAllGenerators()) {
			for (Fuel f : g.getFuels()) {
				int amt = factory.getCount(g, f);
				if (amt > 0) {

				}
			}
		}
		 */
		this.setSize();
	}

	private void setSize() {
		this.getWindow().sizeToScene();
		this.getWindow().setHeight(Math.min(this.getWindow().getHeight(), Screen.getPrimary().getVisualBounds().getHeight()*0.8));
	}

	private Region buildResourceSummaryRow(ResourceSupply r) {
		HBox ret = new HBox();
		if (r instanceof BaseResourceNode) {
			ret.getChildren().add(new ImageView(((BaseResourceNode)r).purityLevel.image));
		}
		if (r instanceof WaterExtractor) {
			ret.getChildren().add(new Label("x"+((WaterExtractor)r).numberExtractors));
		}
		if (r instanceof SimpleProductionSupply) {
			ret.getChildren().add(new Label("x"+((SimpleProductionSupply)r).count));
		}
		if (r instanceof TrainStation) {
			ret.getChildren().add(new Label(((TrainStation)r).numberBuildings+" Cars"));
		}
		if (r instanceof FromFactorySupply) {
			ret.getChildren().add(new Label(((FromFactorySupply)r).sourceFactory));
		}
		if (r instanceof FrackingCluster) {
			FrackingCluster fc = (FrackingCluster)r;
			for (int i = 0; i < fc.impureCount; i++)
				ret.getChildren().add(new ImageView(Purity.IMPURE.image));
			for (int i = 0; i < fc.normalCount; i++)
				ret.getChildren().add(new ImageView(Purity.NORMAL.image));
			for (int i = 0; i < fc.pureCount; i++)
				ret.getChildren().add(new ImageView(Purity.PURE.image));
		}
		if (r instanceof OverclockableResource) {
			for (int i = 0; i < Math.ceil((((OverclockableResource)r).getClockSpeed()-1)/0.5D); i++) {
				ret.getChildren().add(Database.lookupItem("Desc_CrystalShard_C").createImageView());
			}
		}
		ret.setAlignment(Pos.CENTER_LEFT);
		ret.setSpacing(12);
		ret.setPadding(new Insets(0, 12, 0, 12));
		return ret;
	}
}

