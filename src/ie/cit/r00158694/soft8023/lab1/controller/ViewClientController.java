/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.Client;
import ie.cit.r00158694.soft8023.lab1.model.Monitor;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.ResourceBundle;

public class ViewClientController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TableView<String> tableFiles;

	@FXML
	private TableColumn<String, String> colName;

	@FXML
	private TableColumn<String, String> colStatus;

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

	private Monitor monitor;
	private Client client;

	@FXML
	void initialize() {
		tableFiles.setPlaceholder(new Text(resources.getString("client.fileList.empty")));
		colName.setCellValueFactory(param -> {
			String string = param.getValue();
			if (param.getValue().equals(monitor.getLockedFiles().get(client))) string += " ►";
			return new SimpleStringProperty(string);
		});
		colStatus.setCellValueFactory(param -> {
			SimpleStringProperty property = new SimpleStringProperty();
			if (monitor.getLockedFiles().containsValue(param.getValue())) {
				String clientsLockingFile = monitor.getLockedFiles().entrySet().stream().filter(entry -> entry.getValue().equals(param.getValue())).map(Entry::getKey).map(Client::toString).sorted()
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
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/lab1/view/AddFile.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			AddFileController controller = loader.getController();
			controller.setMonitor(monitor);

			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(((Parent) event.getSource()).getScene().getWindow());
			stage.setTitle(resources.getString("client.addFile"));
			stage.setResizable(false);
			stage.setScene(new Scene(root));
			stage.showAndWait();

			controller.getReturnValue().ifPresent(file -> monitor.addFile(client, file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void removeFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem();
		monitor.removeFile(client, file);
	}

	@FXML
	void playFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem();
		monitor.playFile(client, file);
	}

	@FXML
	void stopFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem();
		monitor.stopPlayingFile(client, file);
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
		tableFiles.getItems().setAll(monitor.getFiles());
	}

	public void setClient(Client client) {
		this.client = client;
		txtLog.setText("     −=≡ This client has subscribed to the folder monitor ≡=−");
		client.setOnUpdate(event -> {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			txtLog.appendText('\n' + String.format(resources.getString(event.getAction().getKey()), format.format(event.getDate()), event.getClient(), event.getFile()));

			tableFiles.getItems().setAll(monitor.getFiles());
		});
	}
}
