package Reika.SatisfactoryPlanner.GUI;

import javafx.scene.layout.GridPane;

public interface RecipeMatrixContainer {

	public void setMatrix(MatrixType mt, GridPane g);
	public GridPane getMatrix(MatrixType mt);

	public static enum MatrixType {
		IN,
		OUT,
		SUM;
	}

}
