package sample;

import java.util.*;

abstract class DBScan {


	/**
	 * Erzeugt eine Liste von geclusterten Pixeln. Die liste besteht aus DBScanObject instanzen welche ein Pixel und
	 * eine ClusterID beinhallten.
	 * @param data Liste zu clusternder Pixel.
	 * @param epsilon Maximaler Abstand der Pixel zu einem Nachbarn.
	 * @param minPts Minimale Anzahl an Pixeln die für ein Cluster benötigt werden.
	 * @param distance Abstandsfunktion: Euklidisch oder Max.
	 * @return Liste von DBScanObject instanzen.
	 */
	static DBScanResult cluster(List<Pixel> data, double epsilon, int minPts, String distance){

		// Liste der Clusterobjekte.
		List<DBScanObject> clusterObjects = new LinkedList<>();

		// Erzeugen der Objektmenge
		// Jedes Pixel wird in ein DBSanObject aufgenommen um eine ClusterID speichern zu können.
		// Jedes Objekt beginnt mit der ClusterID -1 (unklassifiziert)
		for (Pixel pixel : data){
			clusterObjects.add(new DBScanObject(pixel, -1));
		}

		int clusterID = 1; // das erste Cluster erhällt die ID 1 (0 wird für das Rauschen freigehalten)
		for (DBScanObject object: clusterObjects){
			if (object.clusterID == -1){
				// ist ein objekt noch nicht zugeordnet wird für dieses nach einm Cluster gesucht
				if (expandCluster(clusterObjects, object, clusterID, epsilon, minPts, distance)){
					clusterID++; // wurde ein Cluster gefunden wird die ID für das nächste Cluster erhöht.
				}
			}
		}

		return new DBScanResult(clusterObjects, clusterID);
	}

	/**
	 * Sucht ein neues Cluster für ein gegebenes DBScanObject.
	 * @param data Liste verfügbarer potentieller nachbarn.
	 * @param start Objekt von dem aus die Suche beginnt.
	 * @param ClusterID ID für das neue Cluster
	 * @param epsilon Umgebungsabstand
	 * @param minPts Anzahl ab der ein neues Cluster gebildet wird.
	 * @param distance Distanzmaß für die entfernung. Möglich: Euklidisch oder Max.
	 * @return true wenn ein neues Cluster gebildet werden konnte.
	 */
	private static boolean expandCluster(
			List<DBScanObject> data, DBScanObject start, int ClusterID, double epsilon, int minPts, String distance){

		// Startwerte für die Clusterausbreitung.
		List<DBScanObject> seeds = findNeighbors(data, epsilon, start, distance);

		// werden nicht genug Nachbarn gefunden wird start zum Rauschen zugeordnet.
		if (seeds.size() < minPts) {
			start.clusterID = 0;
			return false;
		}

		// gib jedem Objekt aus seeds die neue ClusterID
		for (DBScanObject clusterObject: seeds){
			clusterObject.clusterID = ClusterID;
		}


		seeds.remove(start); // da alle nachbarn von start bereits gefunden wurden kann es aus seeds entfernt werden.
		while (seeds.size() != 0){
			DBScanObject o = seeds.get(0);
			List<DBScanObject> neighbourhood = findNeighbors(data, epsilon, o, distance);
			if (neighbourhood.size() >= minPts){
				// o ist Kernobjekt
				for (DBScanObject clusterObject: neighbourhood){
					if (clusterObject.clusterID == 0 || clusterObject.clusterID == -1){
						if (clusterObject.clusterID == -1){
							// füge alle nicht klassifizierten objekte zu seeds hinzu
							// objekte die als Rauschen klassifiziert haben besitzen ohnehinn nicht genug nachbarn
							// um das Cluster noch zu erweitern.
							seeds.add(clusterObject);
						}
						clusterObject.clusterID = ClusterID;
					}
				}
			}
			seeds.remove(o);
		}
		return true;
	}

	/**
	 * Erzeugt eine Liste aller erreichbarer Nachbarn eines Objekts.
	 * @param data Grundmenge auf der gesucht wird.
	 * @param epsilon Mindestabstand
	 * @param start Objekt von dem aus gesucht werden soll.
	 * @param distance Abstandsfunktion (Euklidisch oder Max)
	 * @return Lister der Nachbarschaft.
	 */
	static private List<DBScanObject> findNeighbors(List<DBScanObject> data, double epsilon, DBScanObject start, String distance){
		List<DBScanObject> neighbours = new ArrayList<>();

		if (distance == "Euklidisch"){
			for (DBScanObject pixel: data){
				if (eukl(pixel.pixel, start.pixel) <= epsilon){
					neighbours.add(pixel);
				}
			}
		}
		if (distance == "Max"){
			for (DBScanObject pixel: data){
				if (max(pixel.pixel, start.pixel) <= epsilon){
					neighbours.add(pixel);
				}
			}
		}
		return neighbours;
	}

	/**
	 * Berechnet den Euklidischen Abstand zweier Pixel
	 */
	private static double eukl(Pixel pixel1, Pixel pixel2) {
		return 256 * Math.sqrt(
				Math.pow(pixel1.getColor().getRed() - pixel2.getColor().getRed(), 2 ) +
				Math.pow(pixel1.getColor().getGreen() - pixel2.getColor().getGreen(), 2 ) +
				Math.pow(pixel1.getColor().getBlue() - pixel2.getColor().getBlue(), 2 )
		);
	}

	/**
	 * Berechnet die Maximus Distanz zweier Pixel
	 */
	private static double max(Pixel pixel1, Pixel pixel2) {
		return 256 * Collections.max(Arrays.asList(
						Math.abs( pixel1.getColor().getRed() - pixel2.getColor().getRed() ),
						Math.abs(pixel1.getColor().getGreen() - pixel2.getColor().getGreen()),
						Math.abs(pixel1.getColor().getBlue() - pixel2.getColor().getBlue()) )
		);
	}

	/**
	 * erweitert ein Pixel um eine ClusterID
	 */
	static class DBScanObject {
		Pixel pixel;
		int clusterID;

		public DBScanObject(Pixel pixel, int clusterID) {
			this.pixel = pixel;
			this.clusterID = clusterID;
		}
	}

	/**
	 * Ergebnis einer geclusterten Menge an Pixeln.
	 */
	static class DBScanResult{
		List<DBScanObject> objects;
		int numberOfClusters;

		public DBScanResult(List<DBScanObject> objects, int numberOfClusters) {
			this.objects = objects;
			this.numberOfClusters = numberOfClusters;
		}
	}


}
