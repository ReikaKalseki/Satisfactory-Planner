module satisfactoryplanner {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	exports Reika.SatisfactoryPlanner;
	exports Reika.SatisfactoryPlanner.GUI;
	opens Reika.SatisfactoryPlanner to javafx.graphics, javafx.fxml;
	opens Reika.SatisfactoryPlanner.GUI to javafx.graphics, javafx.fxml;
	opens Reika.SatisfactoryPlanner.Util to javafx.graphics, javafx.fxml;

	requires java.desktop;
	requires com.google.common;
	requires org.apache.commons.io;
	requires org.apache.commons.cli;
	requires org.apache.commons.lang3;
	requires org.apache.commons.logging;
	requires org.json;
}
