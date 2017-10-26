/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.javafx.controller;

import ie.cit.r00158694.soft8023.lab1.monitor.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.monitor.SharedFile;
import javafx.beans.binding.Bindings;
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
		btnSave.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			String path = txtFilePath.textProperty().getValueSafe();
			String name = txtFileName.textProperty().getValueSafe();
			return path.trim().isEmpty() || name.trim().isEmpty() || !new File(path).isFile() || monitor.getFiles().stream()
					.anyMatch(file -> file.getFile().getAbsolutePath().equals(path) || file.getName().equals(name));
		}, txtFilePath.textProperty(), txtFileName.textProperty()));
	}

	@FXML
	void chooseFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(resources.getString("client.addFile"));
		chooser.getExtensionFilters().setAll(new ExtensionFilter(resources.getString("file.mp3.description"), "*.mp3"));
		File file = chooser.showOpenDialog(((Parent) event.getSource()).getScene().getWindow());
		if (file != null) txtFilePath.setText(file.getAbsolutePath());
	}

	@FXML
	void saveData(ActionEvent event) {
		sharedFile = new SharedFile(new File(txtFilePath.getText()), txtFileName.getText());
		closeDialog(event);
	}

	@FXML
	void closeDialog(ActionEvent event) {
		Parent source = (Parent) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@Override
	public Optional<SharedFile> getReturnValue() {
		return Optional.ofNullable(sharedFile);
	}

	public void setMonitor(ResourceMonitor monitor) {
		this.monitor = monitor;
	}
}