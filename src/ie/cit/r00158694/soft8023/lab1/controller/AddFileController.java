/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.model.SharedFile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddFileController implements ReturnValue<SharedFile> {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TextField txtFilePath;

	@FXML
	private TextField txtFileName;

	@FXML
	private Button btnSave;

	private SharedFile sharedFile;
	private ResourceMonitor monitor;

	@FXML
	void initialize() {
		txtFilePath.textProperty().addListener((observable, oldValue, newValue) -> btnSave
				.setDisable(newValue.trim().isEmpty() || monitor.getFiles().stream().anyMatch(file -> file.getFile().getAbsolutePath().equals(newValue))));
		txtFileName.textProperty()
				.addListener((observable, oldValue, newValue) -> btnSave.setDisable(newValue.trim().isEmpty() || monitor.getFiles().stream().anyMatch(file -> file.getName().equals(newValue))));

		btnSave.setDisable(true);
	}

	@FXML
	void openFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(resources.getString("client.addFile"));
		chooser.getExtensionFilters().setAll(new ExtensionFilter("MP3 audio file", "mp3"));
		File file = chooser.showOpenDialog(((Parent) event.getSource()).getScene().getWindow());
		if (file != null) txtFilePath.setText(file.getAbsolutePath());
	}

	@FXML
	void saveData(ActionEvent event) {
		//		sharedFile = txtFileName.getText(); TODO
		closeDialog(event);
	}

	@FXML
	void closeDialog(ActionEvent event) {
		Parent source = (Parent) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@Override
	public Optional<SharedFile> getReturnValue() { return Optional.ofNullable(sharedFile); }

	public void setMonitor(ResourceMonitor monitor) { this.monitor = monitor; }
}