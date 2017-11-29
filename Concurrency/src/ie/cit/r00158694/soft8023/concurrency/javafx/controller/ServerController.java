/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.concurrency.javafx.controller;

import ie.cit.r00158694.soft8023.concurrency.client.AbstractClient;
import ie.cit.r00158694.soft8023.concurrency.monitor.ResourceMonitor;
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
	private ListView<AbstractClient> listClients;

	@FXML
	private Button btnAddClient;

	@FXML
	private Button btnRemoveClient;

	@FXML
	private Button btnViewClient;

	private ResourceMonitor monitor;

	private final Map<AbstractClient, Stage> clientStageMap = new HashMap<>();

	@FXML
	void initialize() {
		BooleanBinding isNull = listClients.getSelectionModel().selectedItemProperty().isNull();
		btnRemoveClient.disableProperty().bind(isNull);
		btnViewClient.disableProperty().bind(isNull);

		listClients.setPlaceholder(new Text(resources.getString("server.clientList.empty")));
		listClients.setCellFactory(param -> new ClientListCell());

		Platform.runLater(() -> btnAddClient.requestFocus());
	}

	@FXML
	void addClient(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/concurrency/javafx/view/AddClient.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			AddClientController controller = loader.getController();
			controller.setResourceMonitor(monitor);

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
		AbstractClient client = listClients.getSelectionModel().getSelectedItem();

		if (clientStageMap.containsKey(client)) clientStageMap.remove(client).close();

		monitor.removeClient(client);
		listClients.getItems().setAll(monitor.getClients());
	}

	@FXML
	void viewClient(ActionEvent event) {
		AbstractClient client = listClients.getSelectionModel().getSelectedItem();

		if (clientStageMap.containsKey(client)) {
			clientStageMap.get(client).requestFocus();
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/concurrency/javafx/view/ViewClient.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			ViewClientController controller = loader.getController();
			controller.setResourceMonitor(monitor);
			controller.setClient(client);

			Stage stage = new Stage();
			stage.initOwner(((Parent) event.getSource()).getScene().getWindow());
			stage.setTitle(String.format("%s: %s", resources.getString("server.viewClient"), client.toString()));
			stage.setResizable(true);
			stage.setScene(new Scene(root));
			stage.setOnCloseRequest(event1 -> clientStageMap.remove(client));

			clientStageMap.putIfAbsent(client, stage);

			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMonitor(ResourceMonitor resourceMonitor) {
		this.monitor = resourceMonitor;
		listClients.getItems().setAll(resourceMonitor.getClients());
	}

	private static class ClientListCell extends ListCell<AbstractClient> {
		@Override
		protected void updateItem(AbstractClient item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) setText(item.getClientName());
			else setText("");
		}
	}
}
