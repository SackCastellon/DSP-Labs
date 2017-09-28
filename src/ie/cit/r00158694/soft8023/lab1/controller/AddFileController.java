package ie.cit.r00158694.soft8023.lab1.controller;

import ie.cit.r00158694.soft8023.lab1.model.Monitor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class AddFileController implements ReturnValue<String> {

	@FXML
	private TextField txtFileName;

	@FXML
	private Button btnSave;

	private String fileName;
	private Monitor monitor;

	@FXML
	void initialize() {
		txtFileName.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean disable = txtFileName.getText().isEmpty() || monitor.getFiles().contains(txtFileName.getText());
			btnSave.setDisable(disable);
		});
	}

	@FXML
	void saveData(ActionEvent event) {
		fileName = txtFileName.getText();
		closeDialog(event);
	}

	@FXML
	void closeDialog(ActionEvent event) {
		Parent source = (Parent) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@Override
	public Optional<String> getReturnValue() { return Optional.ofNullable(fileName); }

	public void setMonitor(Monitor monitor) { this.monitor = monitor; }
}
