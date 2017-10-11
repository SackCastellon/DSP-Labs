/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1;

import ie.cit.r00158694.soft8023.lab1.controller.ServerController;
import ie.cit.r00158694.soft8023.lab1.model.ResourceMonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class MainFX extends Application {

	private static final ResourceBundle resources = ResourceBundle.getBundle("ie.cit.r00158694.soft8023.lab1.i18n");

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/lab1/view/Server.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			ServerController controller = loader.getController();
			controller.setMonitor(ResourceMonitor.getInstance());

			primaryStage.setTitle(resources.getString("server.title"));
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) { launch(args); }
}
