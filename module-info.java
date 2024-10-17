module satisfactoryplanner {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	exports Reika.SatisfactoryPlanner;
	exports Reika.SatisfactoryPlanner.GUI;
	exports fxexpansions;
	opens Reika.SatisfactoryPlanner to javafx.graphics, javafx.fxml;
	opens Reika.SatisfactoryPlanner.GUI to javafx.graphics, javafx.fxml;
	opens fxexpansions to javafx.graphics, javafx.fxml;
	opens Reika.SatisfactoryPlanner.Util to javafx.graphics, javafx.fxml;

	requires java.desktop;
	requires java.management;
	requires com.google.common;
	requires org.apache.commons.io;
	requires org.apache.commons.cli;
	requires org.apache.commons.lang3;
	requires org.apache.commons.logging;
	requires org.json;
	requires org.controlsfx.controls;
	requires javafx.base;
	requires javafx.swing;
	requires nativejavafx.taskbar;
	//requires org.apache.logging.log4j;
	//requires org.apache.logging.log4j.core;
	//requires transitive bridj;
}
