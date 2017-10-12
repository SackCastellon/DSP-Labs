/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.client.Client;
import ie.cit.r00158694.soft8023.lab1.model.client.FullClient;
import ie.cit.r00158694.soft8023.lab1.model.monitor.ResourceMonitor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class AddClientController implements ReturnValue<Client> {

	@FXML
	private TextField txtClientName;

	@FXML
	private Button btnSave;

	private Client client;
	private ResourceMonitor ResourceMonitor;

	@FXML
	void initialize() {
		txtClientName.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean disable = txtClientName.getText().trim().isEmpty() || ResourceMonitor.getClients().stream().anyMatch(client -> client.getClientName().equals(txtClientName.getText()));
			btnSave.setDisable(disable);
		});
		btnSave.setDisable(true);
	}

	@FXML
	void saveData(ActionEvent event) {
		client = new FullClient(txtClientName.getText(), ResourceMonitor);
		closeDialog(event);
	}

	@FXML
	void closeDialog(ActionEvent event) {
		Parent source = (Parent) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@Override
	public Optional<Client> getReturnValue() { return Optional.ofNullable(client); }

	public void setResourceMonitor(ResourceMonitor ResourceMonitor) { this.ResourceMonitor = ResourceMonitor; }
}
