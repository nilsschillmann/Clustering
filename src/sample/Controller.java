package sample;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

	private ObservableList<String> algorithmList = FXCollections.observableArrayList("K-Means", "DB Scan");
	Image originalImage;
	WritableImage clusteredImage;

	final DoubleProperty zoomProperty = new SimpleDoubleProperty(1);

	 @FXML
	private VBox root;


	@FXML
	private ChoiceBox choiceBoxAlgorithm;

	@FXML
	private ImageView imageView;

	@FXML
	ToggleButton imageSwitch;

	@FXML
	private MenuItem menuOpen;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private Spinner<Integer> numberOfClusters;
	private SpinnerValueFactory<Integer> spinnerValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 3);

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		zoomProperty.addListener(arg0 -> {
			imageView.setFitWidth(zoomProperty.get() * originalImage.getWidth());
		});

		scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				if (event.getDeltaY() > 0) {
					zoomProperty.set(zoomProperty.get() * 1.1);
				} else if (event.getDeltaY() < 0) {
					zoomProperty.set(zoomProperty.get() / 1.1);
				}
				event.consume();
			}
		});

		choiceBoxAlgorithm.setValue("K-Means");
		choiceBoxAlgorithm.setItems(algorithmList);

		numberOfClusters.setValueFactory(spinnerValueFactory);
		numberOfClusters.setEditable(true);

		try {
			originalImage = new Image(new FileInputStream("out/production/fxtest/image/image.jpg"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void switchImage(){
		if (imageSwitch.isSelected()){
			switchToClustered();
		} else {
			switchToOriginal();
		}
	}

	private void switchToClustered(){
		imageSwitch.setText("Geklustert");
		imageView.setImage(clusteredImage);
	}

	private void switchToOriginal(){
		imageSwitch.setText("Original");
		imageView.setImage(originalImage);
	}

	@FXML
	private void runAlgorithm(){

		int width = (int)originalImage.getWidth();
		int height = (int)originalImage.getHeight();
		PixelReader pixelReader = originalImage.getPixelReader();
		clusteredImage = new WritableImage(pixelReader, width, height);

		PixelWriter writer = clusteredImage.getPixelWriter();

		List<Pixel> pixels = new ArrayList<>(width * height);

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width ; x++) {
				pixels.add( new Pixel(x, y, pixelReader.getColor(x, y) ) );
			}
		}

		List<KMeans.Cluster> cluster = KMeans.cluster(pixels, numberOfClusters.getValue());
		for (KMeans.Cluster cl: cluster) {
			for (Pixel pixel: cl.vectors) {
				writer.setColor(pixel.getLocation()[0], pixel.getLocation()[1], cl.centroid);
			}
		}

		imageView.setImage(clusteredImage);
		if (!imageSwitch.isSelected()){
			imageSwitch.setSelected(true);
			switchImage();
		}

	}

	@FXML
	private void open(){
		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(
				new File(System.getProperty("user.home") + "/Pictures/Saved Pictures")
		);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.img", "*.png"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"),
				new FileChooser.ExtensionFilter("PNG", "*.png")
		);

		String imagePath = fileChooser.showOpenDialog(root.getScene().getWindow()).getAbsolutePath();

		try {
			originalImage = new Image(new FileInputStream(imagePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		imageView.setImage(originalImage);

		zoomProperty.set(1);
	}



}
