package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;

public class MainGuiController extends ControllerBase {

	private boolean hasLoaded;

	@FXML
	private TitledPane gridContainer;
	@FXML
	private TitledPane netGridContainer;

	private final RecipeMatrix matrix = new RecipeMatrix(() -> this.updateGridContainer());
	private final ScaledRecipeMatrix scaleMatrix = new ScaledRecipeMatrix(matrix);

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		this.updateGridContainer();
	}

	private void updateGridContainer() {
		try {
			gridContainer.setContent(matrix.createGrid(this));
			netGridContainer.setContent(scaleMatrix.createGrid(this));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}

