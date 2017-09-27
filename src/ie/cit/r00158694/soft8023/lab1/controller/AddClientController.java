/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.Client;

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

	@FXML
	void initialize() { btnSave.disableProperty().bind(txtClientName.textProperty().isEmpty()); }

	@FXML
	void saveData(ActionEvent event) {
		client = new Client(txtClientName.getText());
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
}
