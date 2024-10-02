package com.application.userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.application.interpreter.*;

public class MainController {
	//Menú:
	private ArchitectureConfigController archController;
	private int faddValue = 2;
	private int mulValue = 6;
	private int divValue = 10;
	
	@FXML
	private Menu configurationMenu;
	@FXML
	private RadioMenuItem branchPredictorDisable;
	@FXML
	private RadioMenuItem branchPredictorNotTaken;
	@FXML
	private RadioMenuItem branchPredictor1bit;
	@FXML
	private RadioMenuItem branchPredictor2bit;
	@FXML
	private RadioMenuItem forwarding;
	@FXML
	private RadioMenuItem segmentation;
	
	//Editor de código:
	@FXML
	public Button executeButton;
	@FXML
	public Button cyclesExecutionButton;
	@FXML
	public Button cancelButton;
	@FXML
	private TextArea inputCodeArea;
	@FXML
	public Button saveImageButton;
	
	//Tabla de registros:
	@FXML
    private TableView<Register> registersTable;
	@FXML
    private TableColumn<Register, String> registersIdCol;
    @FXML
    private TableColumn<Register, Integer> registersValueCol;
    
    @FXML
    private TableView<FloatRegister> fregistersTable;
    @FXML
    private TableColumn<FloatRegister, String> fregistersIdCol;
    @FXML
    private TableColumn<FloatRegister, Float> fregistersValueCol;
    
    //Tabla de memoria:
  	@FXML
  	private TableView<MemoryData> memoryTable;
  	@FXML
  	private TableColumn<MemoryData, String> memoryAddressCol;
  	@FXML
  	private TableColumn<MemoryData, String> memoryDataCol;
  	@FXML
  	private TableColumn<MemoryData, String> memoryVariablesCol;
    
    //Diagrama de ciclos:
    @FXML
    private GridPane grid;
    @FXML
    private VBox fixedColumn;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane fixedScrollPane;
    @FXML
    private ScrollPane forwardingsPane;
    @FXML
    private Pane forwardingsBox;
    
    //Estadísticas:
    @FXML
    private TextArea statsArea;
    
    @FXML
    private TextArea infoArea;
    
    //Constantes:
    final double BOX_WIDTH = 70;
    final double BOX_HEIGHT = 30;
    final double FIRST_BOX_WIDTH = 140;
    
    MIPSInterpreter interpreter;
    public boolean isFirstCycle = true;
	
	public void onExecuteButtonDown(ActionEvent e) {
		if(inputCodeArea.getText().isEmpty()) {
			return;
		}
		
		initializeDiagram();
		
		interpreter = new MIPSInterpreter(this);
		interpreter.instructionsParser(inputCodeArea.getText());
		interpreter.runCompleteCode();
		
		loadRegistersTables(interpreter.getRegisters(), interpreter.getFregisters());
		loadMemoryTable(interpreter.getMemory(), interpreter.getMemoryVariables());
		loadStatistics(interpreter.getCycles(), interpreter.getTotalInsCounter() - interpreter.getCancelledInsCounter(),
        		interpreter.getDataStallsCounter(), interpreter.getRAWStallsCounter(), interpreter.getWAWStallsCounter(), 
        		interpreter.getStructuralStallsCounter(), interpreter.getBranchResolveStallsCounter(), interpreter.getBranchMispredictStallsCounter(), 
        		interpreter.getBranchBuffer(), interpreter.getInstructions());
		cancelButton.setDisable(false);
		saveImageButton.setDisable(false);
		restoreEnvironment();
	}
	
	public void onNextCycleButtonDown(ActionEvent e) {		
		if(inputCodeArea.getText().isEmpty()) {
			return;
		}
		
		if(isFirstCycle) {
			initializeDiagram();
			interpreter = new MIPSInterpreter(this);
			interpreter.instructionsParser(inputCodeArea.getText());
			configurationMenu.setDisable(true);
			inputCodeArea.setEditable(false);
			cancelButton.setDisable(false);
			saveImageButton.setDisable(false);
			changeToNextCycleImage();
			isFirstCycle = false;
			if(branchPredictor1bit.isSelected() || branchPredictor2bit.isSelected()) {
				infoArea.setVisible(true);
			}
		}
		
		boolean codeEnded = interpreter.runCycle();
		
		if(codeEnded) {
			System.out.println("Número de ciclos ejecutados: " + interpreter.getCycles());
			restoreEnvironment();
			infoArea.setVisible(false);
		}

        loadRegistersTables(interpreter.getRegisters(), interpreter.getFregisters());
        loadMemoryTable(interpreter.getMemory(), interpreter.getMemoryVariables());
        loadStatistics(interpreter.getCycles(), interpreter.getTotalInsCounter() - interpreter.getCancelledInsCounter(),
        		interpreter.getDataStallsCounter(), interpreter.getRAWStallsCounter(), interpreter.getWAWStallsCounter(), 
        		interpreter.getStructuralStallsCounter(), interpreter.getBranchResolveStallsCounter(), interpreter.getBranchMispredictStallsCounter(),
        		interpreter.getBranchBuffer(), interpreter.getInstructions());
	}
	
	public void onSaveDiagramImageButtonDown(ActionEvent e) {
		WritableImage diagramImage = snapshot(scrollPane, null);
		WritableImage instructionsImage = snapshot2(fixedScrollPane, null);
		WritableImage arrowsImage = snapshot(forwardingsPane, null);
		arrowsImage = makeWhitePixelsTransparent(arrowsImage);
		diagramImage = blendImages(diagramImage, arrowsImage);
		
		WritableImage combinedImage = combineImages(instructionsImage, diagramImage);

		Stage stage = (Stage) saveImageButton.getScene().getWindow();
        saveAsPNG(combinedImage, stage);
	}
	
	public void onCancelButtonDown(ActionEvent e) {
		interpreter = null;
		clearDiagram();
		registersTable.getItems().clear();
		registersTable.refresh();
		fregistersTable.getItems().clear();
		fregistersTable.refresh();
		memoryTable.getItems().clear();
		memoryTable.refresh();
		statsArea.setText("");
		cancelButton.setDisable(true);
		saveImageButton.setDisable(true);
		infoArea.setVisible(false);
		restoreEnvironment();
	}
	
	public void restoreEnvironment() {
		configurationMenu.setDisable(false);
		inputCodeArea.setEditable(true);
		changeToCyclesExecutionImage();
		isFirstCycle = true;
	}
	
	public void changeToNextCycleImage() {
		Image nextCycleImage = new Image(getClass().getResource("/arrowIcon.png").toString());
		ImageView imageView = new ImageView(nextCycleImage);
		imageView.setFitHeight(12);
		imageView.setFitWidth(25);
		cyclesExecutionButton.setGraphic(imageView);
	}
	
	public void changeToCyclesExecutionImage() {
		Image cyclesExecutionImage = new Image(getClass().getResource("/playCicleIcon.png").toString());
		ImageView imageView = new ImageView(cyclesExecutionImage);
		imageView.setFitHeight(25);
		imageView.setFitWidth(25);
		cyclesExecutionButton.setGraphic(imageView);
	}
	
	//Métodos para el Menú
	
	public void onArchitectureButtonDown(ActionEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ArchitectureConfig.fxml"));
	        Parent root = loader.load();
			
			archController = loader.getController();
			archController.setMainController(this);
			archController.setFaddValue(faddValue);
			archController.setMulValue(mulValue);
			archController.setDivValue(divValue);
			
	        Stage popupStage = new Stage();
	        popupStage.initModality(Modality.APPLICATION_MODAL);
	        popupStage.initStyle(StageStyle.DECORATED);
	        popupStage.setResizable(false);
	        popupStage.setTitle("Configurar Arquitectura");
	        popupStage.setScene(new Scene(root));
	        popupStage.show();
	        
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void onInfoButtonDown(ActionEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("HelpInfo.fxml"));
	        Parent root = loader.load();
			
	        Stage popupStage = new Stage();
	        popupStage.initModality(Modality.APPLICATION_MODAL);
	        popupStage.initStyle(StageStyle.DECORATED);
	        popupStage.setResizable(false);
	        popupStage.setTitle("Información y Funcionamiento");
	        popupStage.setScene(new Scene(root));
	        popupStage.show();
	        
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public int getFaddValue() {
		return faddValue;
	}
	
	public int getMulValue() {
		return mulValue;
	}
	
	public int getDivValue() {
		return divValue;
	}
	
	public void setFaddValue(int value) {
		faddValue = value;
	}
	
	public void setMulValue(int value) {
		mulValue = value;
	}
	
	public void setDivValue(int value) {
		divValue = value;
	}
	
	public int getBranchPredictionConfig() {
		int bp = 0;
		if(branchPredictorDisable.isSelected()) {
			bp = 0;
		}else if(branchPredictorNotTaken.isSelected()) {
			bp = 1;
		}else if(branchPredictor1bit.isSelected()) {
			bp = 2;
		}else if(branchPredictor2bit.isSelected()) {
			bp = 3;
		}
		return bp;
	}
	
	public boolean getForwardingConfig() {
		return forwarding.isSelected();
	}
	
	public boolean getSegmentationConfig() {
		return segmentation.isSelected();
	}
	
	//Métodos de la Tabla de Registros
	public void loadRegistersTables(List<Register> regs, List<FloatRegister> fregs) {	
		ObservableList<Register> regsList = FXCollections.observableArrayList(regs);
		ObservableList<FloatRegister> fregsList = FXCollections.observableArrayList(fregs);
		
		registersIdCol.setCellValueFactory(new PropertyValueFactory<Register, String>("id"));
		registersValueCol.setCellValueFactory(new PropertyValueFactory<Register, Integer>("value"));
		fregistersIdCol.setCellValueFactory(new PropertyValueFactory<FloatRegister, String>("id"));
		fregistersValueCol.setCellValueFactory(new PropertyValueFactory<FloatRegister, Float>("value"));
		
		registersTable.setItems(regsList);
		fregistersTable.setItems(fregsList);
		registersTable.refresh();
		fregistersTable.refresh();
	}
	
	//Métodos de la Tabla de Memoria
	public void loadMemoryTable(String[] memory, HashMap<String, Integer> memoryVariables) {	
		ObservableList<MemoryData> memoryList = FXCollections.observableArrayList();
		Map<Integer, String> reversedVariables = new HashMap<>();
        for (Map.Entry<String, Integer> entry : memoryVariables.entrySet()) {
        	reversedVariables.put(entry.getValue(), entry.getKey());
        }
		
        for (int i = 0; i < memory.length; i++) {
        	String variable = "";
        	if (memoryVariables.containsValue(i*4)) {
        		variable = reversedVariables.get(i*4);
        	}
        	memoryList.add(new MemoryData(memory[i], Integer.toString(i*4), variable));
        }
		
        memoryAddressCol.setCellValueFactory(new PropertyValueFactory<>("memoryAddress"));
		memoryDataCol.setCellValueFactory(new PropertyValueFactory<>("memoryValue"));
		memoryVariablesCol.setCellValueFactory(new PropertyValueFactory<>("memoryVariable"));
		
		memoryTable.setItems(memoryList);
		memoryTable.refresh();
	}
	
	//Métodos del Diagrama de Ciclos
	private void initializeDiagram() {
		clearDiagram();
        scrollPane.vvalueProperty().bindBidirectional(fixedScrollPane.vvalueProperty());
        scrollPane.vvalueProperty().bindBidirectional(forwardingsPane.vvalueProperty());
        scrollPane.hvalueProperty().bindBidirectional(forwardingsPane.hvalueProperty());

        fixedColumn.setMinWidth(FIRST_BOX_WIDTH);
        fixedColumn.setPrefWidth(FIRST_BOX_WIDTH);
        fixedColumn.setMaxWidth(FIRST_BOX_WIDTH);
	}
	
	public void addDiagramRow(int nInstruction, String instruction) {
		grid.setMinHeight(BOX_HEIGHT * nInstruction);
		grid.setPrefHeight(BOX_HEIGHT * nInstruction);
		grid.setMaxHeight(BOX_HEIGHT * nInstruction);
		forwardingsBox.setMinHeight(BOX_HEIGHT * nInstruction);
		forwardingsBox.setPrefHeight(BOX_HEIGHT * nInstruction);
		forwardingsBox.setMaxHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setMinHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setPrefHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setMaxHeight(BOX_HEIGHT * nInstruction);
		addBox(0, nInstruction, instruction, "aliceblue", true);
	}
	
	public void addDiagramColumn(int nCycles, int instructionF, int instructionD, int instructionE, int instructionM, int instructionW, int[] instructionsFadd, int[] instructionsMul, int[] instructionsDiv) {
		grid.setMinWidth(BOX_WIDTH * nCycles);
		grid.setPrefWidth(BOX_WIDTH * nCycles);
		grid.setMaxWidth(BOX_WIDTH * nCycles);
		forwardingsBox.setMinWidth(BOX_WIDTH * nCycles);
		forwardingsBox.setPrefWidth(BOX_WIDTH * nCycles);
		forwardingsBox.setMaxWidth(BOX_WIDTH * nCycles);
		if(instructionF != -1) {
			if(!interpreter.getDataRAWStall() && !interpreter.getDataWAWStall() && !interpreter.getStructuralFaddStall() && !interpreter.getStructuralMulStall() && !interpreter.getStructuralDivStall()) {
				addBox(nCycles, instructionF, "IF", "gold", false);
			}else if(interpreter.getDataRAWStall()){
				addBox(nCycles, instructionF, "RAW", "powderblue", false);
			}else if(interpreter.getDataWAWStall()){
				addBox(nCycles, instructionF, "WAW", "powderblue", false);
			}else if(interpreter.getStructuralFaddStall() || interpreter.getStructuralMulStall() || interpreter.getStructuralDivStall()) {
				addBox(nCycles, instructionF, "STR", "powderblue", false);
			}
		}
		if(instructionD != -1) {
			addBox(nCycles, instructionD, "ID", "powderblue", false);
		}
		if(instructionE != -1) {
			if(!interpreter.getStructuralMemStall()) {
				addBox(nCycles, instructionE, "EX", "indianred", false);
			}else {
				addBox(nCycles, instructionE, "STR", "indianred", false);
			}
		}
		if(instructionM != -1) {
			addBox(nCycles, instructionM, "MEM", "lightgreen", false);
		}
		if(instructionW != -1) {
			addBox(nCycles, instructionW, "WB", "plum", false);
		}
		for(int i = 0; i < instructionsFadd.length; i++) {
			if(instructionsFadd[i] != -1) {
				if(!interpreter.getStructuralFaddMemStall()) {
					if(interpreter.getSegmentationEnabled()) {
						addBox(nCycles, instructionsFadd[i], "A " + (i+1), "indianred", false);
					}else {
						addBox(nCycles, instructionsFadd[i], "A", "indianred", false);
					}
				}else {
					addBox(nCycles, instructionsFadd[i], "STR", "indianred", false);
				}
			}
		}
		for(int i = 0; i < instructionsMul.length; i++) {
			if(instructionsMul[i] != -1) {
				if(!interpreter.getStructuralMulMemStall()) {
					if(interpreter.getSegmentationEnabled()) {
						addBox(nCycles, instructionsMul[i], "MUL " + (i+1), "indianred", false);
					}else {
						addBox(nCycles, instructionsMul[i], "MUL", "indianred", false);
					}
				}else {
					addBox(nCycles, instructionsMul[i], "STR", "indianred", false);
				}
			}
		}
		for(int i = 0; i < instructionsDiv.length; i++) {
			if(instructionsDiv[i] != -1) {
				if(interpreter.getSegmentationEnabled()) {
					addBox(nCycles, instructionsDiv[i], "DIV " + (i+1), "indianred", false);
				}else {
					addBox(nCycles, instructionsDiv[i], "DIV", "indianred", false);
				}
			}
		}
	}
	
	private void addBox(int col, int row, String text, String color, boolean isInstruction) {
        Pane box = new Pane();
        box.setStyle("-fx-border-color: darkgray; -fx-background-color: " + color + ";");        
        box.setMinWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);
        box.setMaxWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);  
        box.setMinHeight(BOX_HEIGHT);
        box.setMaxHeight(BOX_HEIGHT);
        
        Label label = new Label(text);
        label.setStyle("-fx-alignment: center;");
        label.setMinWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);
        label.setMinHeight(BOX_HEIGHT);
        
        box.getChildren().add(label);
        
        if(isInstruction) {
        	fixedColumn.getChildren().add(box);
        }else {
        	grid.add(box, col, row);
        }
    }
	
	public void addForwardingArrow(int startBoxX, int startBoxY, int endBoxX, int endBoxY, boolean mToE) {
		Line arrow = new Line();
		arrow.setStartX(startBoxX * BOX_WIDTH + 60);
		arrow.setStartY(startBoxY * BOX_HEIGHT + 20);
		if(!mToE) {
			arrow.setEndX(endBoxX * BOX_WIDTH + 10);
		}else {
			arrow.setEndX(endBoxX * BOX_WIDTH + 20);
		}
		
		arrow.setEndY(endBoxY * BOX_HEIGHT + 10);
		
		Line arrowhead1 = new Line();			
		Line arrowhead2 = new Line();
		
		double x = arrow.getEndX() - arrow.getStartX();
		double y = arrow.getEndY() - arrow.getStartY();
		
		double sqrt2Over2 = Math.sqrt(2) / 2;
        
        double newX = sqrt2Over2 * (x - y);
        double newY = sqrt2Over2 * (x + y);
        
        double magnitude = Math.sqrt(newX * newX + newY * newY);

        double normalizedX = newX / magnitude;
        double normalizedY = newY / magnitude;
        
        arrowhead1.setStartX(arrow.getEndX());
		arrowhead1.setStartY(arrow.getEndY());
		arrowhead1.setEndX(arrow.getEndX() - normalizedX * 5);
		arrowhead1.setEndY(arrow.getEndY() - normalizedY * 5);
		
		newX = sqrt2Over2 * (x + y);
        newY = sqrt2Over2 * (-x + y);
        
        magnitude = Math.sqrt(newX * newX + newY * newY);

        normalizedX = newX / magnitude;
        normalizedY = newY / magnitude;
        
        arrowhead2.setStartX(arrow.getEndX());
		arrowhead2.setStartY(arrow.getEndY());
		arrowhead2.setEndX(arrow.getEndX() - normalizedX * 5);
		arrowhead2.setEndY(arrow.getEndY() - normalizedY * 5);
		
		forwardingsBox.getChildren().add(arrow);
		forwardingsBox.getChildren().add(arrowhead1);
		forwardingsBox.getChildren().add(arrowhead2);
	}
	
	public void removeLastForwardingArrows(int n) {
		for(int i = 0; i < n; i++) {
			forwardingsBox.getChildren().remove(forwardingsBox.getChildren().size()-1);
			forwardingsBox.getChildren().remove(forwardingsBox.getChildren().size()-1);
			forwardingsBox.getChildren().remove(forwardingsBox.getChildren().size()-1);
		}
	}
	
	public void clearDiagram() {
		grid.getChildren().clear();
		grid.setMinHeight(0);
		grid.setPrefHeight(0);
		grid.setMaxHeight(0);
		grid.setMinWidth(0);
		grid.setPrefWidth(0);
		grid.setMaxWidth(0);
		forwardingsBox.getChildren().clear();
		forwardingsBox.setMinHeight(0);
		forwardingsBox.setPrefHeight(0);
		forwardingsBox.setMaxHeight(0);
		forwardingsBox.setMinWidth(0);
		forwardingsBox.setPrefWidth(0);
		forwardingsBox.setMaxWidth(0);
		fixedColumn.getChildren().clear();
		fixedColumn.setMinHeight(0);
		fixedColumn.setPrefHeight(0);
		fixedColumn.setMaxHeight(0);
	}
	
	//Métodos del Área de Estadísticas:
	private void loadStatistics(int cycles, int instructions, int dataStalls, int dataRAWStalls, int dataWAWStalls, int structuralStalls, int branchResolveStalls, int branchMispredictStalls, HashMap<Integer, BranchTargetBuffer> branchBuffer, String[] ins) {
		float cpi = (float) cycles / instructions;
		String statsText = "Ejecución:\n"
				+ "  " + cycles + " Ciclos\n"
				+ "  " + instructions + " Instrucciones\n"
				+ "  " + cpi + " CPI\n"
				+ "\n"
				+ "Paradas:\n"
				+ "  " + dataStalls + " Paradas por riesgos de datos:\n"
				+ "      " + dataRAWStalls + " Paradas por riesgos RAW\n"
				+ "      " + dataWAWStalls + " Paradas por riesgos WAW\n"
				+ "  " + structuralStalls + " Paradas por riesgos estructurales\n"
				+ "  " + branchResolveStalls + " Paradas por resolución de salto\n"
				+ "  " + branchMispredictStalls + " Paradas por fallo de predicción de salto";
		statsArea.setText(statsText);
		
		String infoText = "Estado de predictores de salto:\n\n";
		for (int i = 0; i < ins.length; i++) {
			if(branchBuffer.containsKey(i)) {
				infoText = infoText.concat("-Instrucción " + (i+1) + " (" + ins[i] + "): " + branchBuffer.get(i).getPredictionState());
				if(branchBuffer.get(i).getPredictionState().equals("0") | branchBuffer.get(i).getPredictionState().equals("00") | branchBuffer.get(i).getPredictionState().equals("01")) {
					infoText = infoText.concat(" (Salto No Tomado)\n");
				}else {
					infoText = infoText.concat(" (Salto Tomado)\n");
				}
			}
		}
		infoArea.setText(infoText);
	}
	
	//Métodos para la generación de imagen
	private WritableImage snapshot(ScrollPane scrollPane, SnapshotParameters params) {
        double width = BOX_WIDTH * interpreter.getCycles();
        double height = BOX_HEIGHT * interpreter.getTotalInsCounter();
        WritableImage image = new WritableImage((int) Math.round(width), (int) Math.round(height));
        return scrollPane.getContent().snapshot(params, image);
    }
	
	private WritableImage snapshot2(ScrollPane scrollPane, SnapshotParameters params) {
        double width = FIRST_BOX_WIDTH;
        double height = BOX_HEIGHT * interpreter.getTotalInsCounter();
        WritableImage image = new WritableImage((int) Math.round(width), (int) Math.round(height));
        return scrollPane.getContent().snapshot(params, image);
    }
	
	private WritableImage makeWhitePixelsTransparent(WritableImage image) {
	    PixelReader pixelReader = image.getPixelReader();
	    int width = (int) image.getWidth();
	    int height = (int) image.getHeight();
	    WritableImage transparentImage = new WritableImage(width, height);
	    PixelWriter pixelWriter = transparentImage.getPixelWriter();

	    // Define the color to be considered as white (adjust as needed)
	    Color white = Color.WHITE;

	    // Iterate through each pixel of the image
	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            Color color = pixelReader.getColor(x, y);
	            if (color.equals(white)) {
	                // If the pixel is white, make it fully transparent
	                pixelWriter.setColor(x, y, Color.TRANSPARENT);
	            } else {
	                // Otherwise, leave the pixel unchanged
	                pixelWriter.setColor(x, y, Color.rgb(50, 40, 50));
	            }
	        }
	    }
	    return transparentImage;
	}
	
	private WritableImage blendImages(WritableImage image2, WritableImage image3) {
	    PixelReader pixelReader2 = image2.getPixelReader();
	    PixelReader pixelReader3 = image3.getPixelReader();
	    int width = (int) image2.getWidth();
	    int height = (int) image2.getHeight();
	    WritableImage blendedImage = new WritableImage(width, height);
	    PixelWriter pixelWriter = blendedImage.getPixelWriter();

	    // Iterate through each pixel of the images
	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            // Get the color and alpha values of the pixels from the images
	            Color color2 = pixelReader2.getColor(x, y);
	            Color color3 = pixelReader3.getColor(x, y);
	            double alpha3 = color3.getOpacity();

	            // Blend the colors based on alpha values
	            Color blendedColor = color2.interpolate(color3, alpha3);

	            // Set the blended color to the corresponding pixel of the combined image
	            pixelWriter.setColor(x, y, blendedColor);
	        }
	    }
	    return blendedImage;
	}
	
	private WritableImage combineImages(WritableImage image1, WritableImage image2) {
		int width = (int) (FIRST_BOX_WIDTH + BOX_WIDTH * interpreter.getCycles());
	    int height = (int) (BOX_HEIGHT * interpreter.getTotalInsCounter());
	    WritableImage combinedImage = new WritableImage(width, height);
	    combinedImage.getPixelWriter().setPixels(0, 0, (int) image1.getWidth(), (int) image1.getHeight(), image1.getPixelReader(), 0, 0);
	    combinedImage.getPixelWriter().setPixels((int) image1.getWidth(), 0, (int) image2.getWidth(), (int) image2.getHeight(), image2.getPixelReader(), 0, 0);
	    return combinedImage;
	}

    private void saveAsPNG(WritableImage image, Stage stage) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Imagen");

        // Filtro para solo mostrar archivos PNG
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        // Mostrar el diálogo de guardar archivo
        File file = fileChooser.showSaveDialog(stage);

        // Si el usuario selecciona un archivo, guardarlo
        if (file != null) {
            try {
                // Convertir WritableImage a BufferedImage
                java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                // Guardar la imagen como archivo PNG
                ImageIO.write(bufferedImage, "png", file);
                System.out.println("Imagen guardada en: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}