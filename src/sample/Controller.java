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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

	Image originalImage;
	WritableImage clusteredImage;
	final DoubleProperty zoomProperty = new SimpleDoubleProperty(1);

	 @FXML
	private VBox root;

	@FXML
	private ChoiceBox<String> choiceBoxAlgorithm;
	private ObservableList<String> algorithmList = FXCollections.observableArrayList("K-Means", "DB Scan");

	@FXML
	private ChoiceBox<String> CBdistance;
	private ObservableList<String> distances = FXCollections.observableArrayList("Euklidisch", "Max");

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
	private SpinnerValueFactory<Integer> NOCspinnerValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 3);

	@FXML
	private Spinner<Integer> minPts;
	private SpinnerValueFactory<Integer> minPtsSpinnerValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 3);

	@FXML
	private Spinner<Integer> epsilon;
	private SpinnerValueFactory<Integer> epsilonSpinnerValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 3);

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

		choiceBoxAlgorithm.setValue("DB Scan");
		choiceBoxAlgorithm.setItems(algorithmList);

		CBdistance.setValue("Euklidisch");
		CBdistance.setItems(distances);

		numberOfClusters.setValueFactory(NOCspinnerValueFactory);
		numberOfClusters.setEditable(true);

		epsilon.setValueFactory(epsilonSpinnerValueFactory);
		epsilon.setEditable(true);

		minPts.setValueFactory(minPtsSpinnerValueFactory);
		minPts.setEditable(true);

		try {
			originalImage = new Image(new FileInputStream("C:\\Users\\Nils\\IdeaProjects\\Clustering\\out\\production\\Clustering\\image\\image.jpg"));
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

		if (choiceBoxAlgorithm.getValue().toString() == "K-Means"){
			clusterWithKMeans(pixels, numberOfClusters.getValue(), writer);
		}

		if (choiceBoxAlgorithm.getValue().toString() == "DB Scan"){
			clusterWithDBScan(pixels, epsilon.getValue(), minPts.getValue(), writer);
		}



		imageView.setImage(clusteredImage);
		if (!imageSwitch.isSelected()){
			imageSwitch.setSelected(true);
			switchImage();
		}

	}

	private void clusterWithKMeans(List<Pixel> pixels, int k, PixelWriter writer){
		List<KMeans.Cluster> cluster = KMeans.cluster(pixels, k);
		for (KMeans.Cluster cl: cluster) {
			for (Pixel pixel: cl.vectors) {
				writer.setColor(pixel.getLocation()[0], pixel.getLocation()[1], cl.centroid);
			}
		}
	}

	private void clusterWithDBScan(List<Pixel> pixels, double epsilon, int minPts, PixelWriter writer){

		DBScan.DBScanResult result = DBScan.cluster(pixels, epsilon, minPts, CBdistance.getValue());
		List<DBScan.DBScanObject> objects = result.objects;
		List<Color> colors = new ArrayList<>();

		colors.add(new Color(1, 1, 1, 1));
		for (int i = 1; i < result.numberOfClusters; i++) {
			colors.add(generateRandomColor());
		}
		for (DBScan.DBScanObject object: objects){
			writer.setColor(object.pixel.getLocation()[0], object.pixel.getLocation()[1], colors.get(object.clusterID));
		}
	}

	private Color generateRandomColor(){
		return new Color(Math.random(), Math.random(), Math.random(), 1);
	}

	@FXML
	private void open(){
		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(
				//new File("out/production/Clustering/image")
				new File(System.getProperty("user.home"))
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
