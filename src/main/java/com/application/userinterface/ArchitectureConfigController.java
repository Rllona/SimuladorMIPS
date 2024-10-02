package com.application.userinterface;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ArchitectureConfigController {
	private MainController mainController;
	
	//Congiguración de arquitectura
	@FXML
	private Slider fadderSlider;
	@FXML
	private Slider multiplierSlider;
	@FXML
	private Slider dividerSlider;
	@FXML
	public Label faddLabel;
	@FXML
	public Label mulLabel;
	@FXML
	public Label divLabel;
	@FXML
	public Label warningLabel;
	
	public void onArchitectureOKButtonDown(ActionEvent e) {
		if(fadderSlider.getValue() <= multiplierSlider.getValue() && multiplierSlider.getValue() <= dividerSlider.getValue()) {
			mainController.setFaddValue((int) fadderSlider.getValue());
			mainController.setMulValue((int) multiplierSlider.getValue());
			mainController.setDivValue((int) dividerSlider.getValue());
			
			Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
	        stage.close();
		}else {
			warningLabel.setVisible(true);
		}
	}
	
	public void setMainController(MainController mainController) {
        this.mainController = mainController;
        StringConverter<Number> converter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.format("%.0f", object.doubleValue());
            }

            @Override
            public Number fromString(String string) {
                return Double.parseDouble(string);
            }
        };
        faddLabel.textProperty().bindBidirectional(fadderSlider.valueProperty(), converter); 
        mulLabel.textProperty().bindBidirectional(multiplierSlider.valueProperty(), converter); 
        divLabel.textProperty().bindBidirectional(dividerSlider.valueProperty(), converter);
    }
	
	public void setFaddValue(int value) {
		fadderSlider.setValue(value);
	}
	
	public void setMulValue(int value) {
		multiplierSlider.setValue(value);
	}
	
	public void setDivValue(int value) {
		dividerSlider.setValue(value);
	}
}
