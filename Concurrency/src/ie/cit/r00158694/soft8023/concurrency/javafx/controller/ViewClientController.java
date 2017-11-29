/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.concurrency.javafx.controller;

import ie.cit.r00158694.soft8023.concurrency.client.AbstractClient;
import ie.cit.r00158694.soft8023.concurrency.monitor.ResourceMonitor;
import ie.cit.r00158694.soft8023.concurrency.monitor.SharedFile;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
	private AbstractClient client;

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
						resourceMonitor.getLockedFiles().entrySet().stream().filter(entry -> entry.getValue().equals(param.getValue().getName())).map(Entry::getKey).map(AbstractClient::getClientName).sorted()
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
			loader.setLocation(getClass().getResource("/ie/cit/r00158694/soft8023/concurrency/javafx/view/AddFile.fxml"));
			loader.setResources(resources);

			Parent root = loader.load();

			AddFileController controller = loader.getController();
			controller.setMonitor(resourceMonitor);

			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(((Parent) event.getSource()).getScene().getWindow());
			stage.setTitle(resources.getString("client.addFile"));
			stage.setResizable(false);
			stage.setScene(new Scene(root));
			stage.showAndWait();

			controller.getReturnValue().ifPresent(file -> {
				if (!resourceMonitor.addFile(client, file)) showAlert("dialog.add.error");
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void removeFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.deleteFile(client, file)) showAlert("dialog.remove.error");
	}

	@FXML
	void readFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.readFile(client, file)) showAlert("dialog.read.error");
	}

	@FXML
	void releaseFile(ActionEvent event) {
		String file = tableFiles.getSelectionModel().getSelectedItem().getName();
		if (!resourceMonitor.releaseFile(client, file)) showAlert("dialog.release.error");
	}

	public void setResourceMonitor(ResourceMonitor resourceMonitor) {
		this.resourceMonitor = resourceMonitor;
		tableFiles.getItems().setAll(resourceMonitor.getFiles());
	}

	public void setClient(AbstractClient client) {
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
