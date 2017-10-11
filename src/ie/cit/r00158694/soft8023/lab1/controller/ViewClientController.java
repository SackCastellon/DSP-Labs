/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.model.SharedFile;
import ie.cit.r00158694.soft8023.lab1.model.client.Client;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.ResourceBundle;

public class ViewClientController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TableView<SharedFile> tableFiles;

	@FXML
	private TableColumn<SharedFile, String> colName;

	@FXML
	private TableColumn<SharedFile, String> colStatus;

	@FXML
	private Button btnPlayFile;

	@FXML
	private Button btnStopFile;

	@FXML
	private Button btnAddFile;

	@FXML
	private Button btnRemoveFile;

	@FXML
	private TextArea txtLog;

	private ResourceMonitor resourceMonitor;
	private Client client;

	@FXML
	void initialize() {
		tableFiles.setPlaceholder(new Text(resources.getString("client.fileList.empty")));
		colName.setCellValueFactory(param -> {
			String string = param.getValue().getName();
			if (string.equals(resourceMonitor.getLockedFiles().get(client))) string += " ►";
			return new SimpleStringProperty(string);
		});
		colStatus.setCellValueFactory(param -> {
			SimpleStringProperty property = new SimpleStringProperty();
			if (resourceMonitor.getLockedFiles().containsValue(param.getValue().getName())) {
				String clientsLockingFile =
						resourceMonitor.getLockedFiles().entrySet().stream().filter(entry -> entry.getValue().equals(param.getValue().getName())).map(Entry::getKey).map(Client::getClientName).sorted()
								.reduce((s, s2) -> s + ", " + s2).orElse("");
				property.setValue(String.format(resources.getString("file.status.locked"), clientsLockingFile));
			} else {
				property.setValue(resources.getString("file.status.unlocked"));
			}
			return property;
		});

		BooleanBinding isNull = tableFiles.getSelectionModel().selectedItemProperty().isNull();
		btnRemoveFile.disableProperty().bind(isNull);
		btnPlayFile.disableProperty().bind(isNull);
		btnStopFile.disableProperty().bind(isNull);

		Platform.runLater(() -> btnAddFile.requestFocus());
	}

	@FXML
	void addFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(resources.getString("client.addFile"));
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("MP3 audio file", "mp3"));
		File file = fileChooser.showOpenDialog(((Parent) event.getSource()).getScene().getWindow());
		if (!resourceMonitor.addFile(client, new SharedFile(file, file.getName()))) // FIXME
			showAlert("dialog.notAdded");
	}

	@FXML
	void removeFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.deleteFile(client, file)) showAlert("dialog.notRemoved");
	}

	@FXML
	void playFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.readFile(client, file)) showAlert("dialog.notPlayed");
	}

	@FXML
	void stopFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.releaseFile(client, file)) showAlert("dialog.notStopped");
	}

	public void setResourceMonitor(ResourceMonitor resourceMonitor) {
		this.resourceMonitor = resourceMonitor;
		tableFiles.getItems().setAll(resourceMonitor.getFiles());
	}

	public void setClient(Client client) {
		this.client = client;
		txtLog.setText("     −=≡ This client has subscribed to the Resource Monitor ≡=−");
		client.setOnUpdate(event -> {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			txtLog.appendText('\n' + String.format(resources.getString(event.getAction().getKey()), format.format(event.getDate()), event.getClient().getClientName(), event.getFileName()));

			tableFiles.getItems().setAll(resourceMonitor.getFiles());
		});
	}

	private void showAlert(String key) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(null);
		alert.setContentText(resources.getString(key));
		alert.show();
	}
}
