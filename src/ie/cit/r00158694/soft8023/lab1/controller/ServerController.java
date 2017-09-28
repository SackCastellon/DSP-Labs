/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.IClient;
import ie.cit.r00158694.soft8023.lab1.model.Monitor;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ServerController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private ListView<IClient> listClients;

	@FXML
	private Button btnAddClient;

	@FXML
	private Button btnRemoveClient;

	@FXML
	private Button btnViewClient;

	private Monitor monitor;

	private final Map<IClient, Stage> clientStageMap = new HashMap<>();

	@FXML
	void initialize() {
		BooleanBinding isNull = listClients.getSelectionModel().selectedItemProperty().isNull();
		btnRemoveClient.disableProperty().bind(isNull);
		btnViewClient.disableProperty().bind(isNull);

		listClients.setPlaceholder(new Text(resources.getString("server.clientList.empty")));
		listClients.setCellFactory(param -> new ListCell<IClient>() {
			@Override
			protected void updateItem(IClient item, boolean empty) {
				super.updateItem(item, empty);
				if (!empty) setText(item.getClientName());
			}
		});

		Platform.runLater(() -> btnAddClient.requestFocus());
	}

	@FXML
	void addClient(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/lab1/view/AddClient.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			AddClientController controller = loader.getController();
			controller.setMonitor(monitor);

			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(((Parent) event.getSource()).getScene().getWindow());
			stage.setTitle(resources.getString("server.addClient"));
			stage.setResizable(false);
			stage.setScene(new Scene(root));
			stage.showAndWait();

			controller.getReturnValue().ifPresent(client -> {
				monitor.addClient(client);
				listClients.getItems().setAll(monitor.getClients());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void removeClient(ActionEvent event) {
		IClient client = listClients.getSelectionModel().getSelectedItem();

		if (clientStageMap.containsKey(client)) clientStageMap.remove(client).close();

		monitor.removeClient(client);
		listClients.getItems().setAll(monitor.getClients());
	}

	@FXML
	void viewClient(ActionEvent event) {
		IClient client = listClients.getSelectionModel().getSelectedItem();

		if (clientStageMap.containsKey(client)) {
			clientStageMap.get(client).requestFocus();
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/lab1/view/ViewClient.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			ViewClientController controller = loader.getController();
			controller.setMonitor(monitor);
			controller.setClient(client);

			Stage stage = new Stage();
			stage.initOwner(((Parent) event.getSource()).getScene().getWindow());
			stage.setTitle(String.format("%s: %s", resources.getString("server.viewClient"), client.toString()));
			stage.setResizable(true);
			stage.setScene(new Scene(root));

			clientStageMap.put(client, stage);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
		listClients.getItems().setAll(monitor.getClients());
	}
}
