package sample;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


abstract class KMeans {

	/**
	 * Findet alle Cluster einer Pixelliste nach dem k-Means Verfahren bezüglich des RGB Farbraums.
	 * @param data Liste von Pixeln die geclustert werden sollen.
	 * @param k Anzahl der zu findenden Cluster
	 * @return Liste von Clustern.
	 */
	static List<Cluster> cluster(List<Pixel> data, int k){
		// Anlegen der leeren Cluster.
		List<Cluster> clusters = new ArrayList<>(k);
		List<Cluster> oldClusters;

		// Die grundmenge wird durchmischt damit zufällige startwerte gewählt werden.
		Collections.shuffle(data);

		// zufällige cluster anlegen
		for (int i=1; i<=k; i++){
			clusters.add(new Cluster( data.get(i).getColor(), new LinkedList<>()));
		}

		int runs = 0; // zählt die Durchläufe
		boolean done; // Abbruchvariable
		long before = System.currentTimeMillis();  // zum messen der Zeit pro Durchlauf.

		do {

			// Lege eine Kopie der Bissher erstellten Cluster an und leere die bissherigen. Die Zentruide bleiben dabei erhalten.
			oldClusters = new ArrayList<>();
			for (Cluster cl: clusters) {
				oldClusters.add(cl.copy());
				cl.vectors = new LinkedList<>();
			}

			// pixel zuordnen
			for (Pixel pixel : data) {
				int whichCluster = 0;
				for (int i = 1; i < k; i++) {
					// prüge für jedes Pixel zu welchem CLuster es passt.
					if (isCloser(pixel.getColor(), clusters.get(i).centroid, clusters.get(whichCluster).centroid)) {
						whichCluster = i;
					}
				}
				// zuordnung
				clusters.get(whichCluster).vectors.add(pixel);
			}


			// andere variante zum zuordnen der Cluster
//			int whichCluster;
//			double newDist;
//			for (Pixel pixel : data) {
//				whichCluster = 0;
//				double dist = dist2(pixel.getColor(), cluster.get(whichCluster).centroid);
//				for (int i = 1; i < k; i++) {
//					newDist = isCloser2(dist, pixel.getColor(), cluster.get(i).centroid);
//					if (newDist >= 0) {
//						whichCluster = i;
//						dist = newDist;
//					}
//				}
//				cluster.get(whichCluster).vectors.add(pixel);
//			}

			done = true;
			// Zentruide anpassen
			// wenn sich die Zentruide nicht geändert haben wir abgebrochen
			for (int i =0; i<clusters.size(); i++) {
				clusters.get(i).calibrateCentroids();
				if (!clusters.get(i).centroid.equals(oldClusters.get(i).centroid)){
					done = false;
				}
			}
			runs++;

		}while(!done);

		long time = System.currentTimeMillis() - before;
		System.out.println("Dauer: " + time + " ms");
		System.out.println(runs + " Durchläufe");
		System.out.println(time/runs + " ms pro Durchlauf");
		System.out.println("--------------------------");

		return clusters;
	}

	/**
	 * Prüft ob a näher an x liegt als b.
	 * @param x Bezugspunkt für die Abstandsmessung
	 * @param a
	 * @param b
	 * @return true wenn a näher an x als b
	 */
	private static boolean isCloser(Color x, Color a, Color b){
		double first = (Math.pow(x.getRed()   - a.getRed() , 2) +
						Math.pow(x.getGreen() - a.getGreen() , 2) +
						Math.pow(x.getBlue()  - a.getBlue() , 2));

		double second = (Math.pow(x.getRed()   - b.getRed() , 2) +
						Math.pow(x.getGreen() - b.getGreen() , 2) +
						Math.pow(x.getBlue()  - b.getBlue() , 2));

		return first < second;
	}

	/**
	 * Gibt die euklidische Distanz zwischen a und b zurück.
	 * @param a
	 * @param b
	 * @return
	 */
	private static double dist(double[] a, double[] b){
		if (a.length != b.length){
			throw new IllegalArgumentException("Vector a und Vector b muessen die gleiche Dimension haben.");
		}

		double dist = 0;
		for (int i = 0; i<=a.length; i++){
			dist = dist + Math.pow(a[i]- b[i], 2);
		}
		dist = Math.sqrt(dist);
		return dist;
	}

	/**
	 * Prüft ob a näher an x liegt als b.
	 * @param x Bezugspunkt für die Abstandsmessung
	 * @param a
	 * @param b
	 * @return true wenn a näher an x als b
	 */
	private static double isCloser2(double dist, Color a, Color b){
		double newDist = 0;
		newDist += Math.abs(a.getRed() - b.getRed());
		if (dist < newDist){
			return -1;
		}
		newDist += Math.abs(a.getGreen() - b.getGreen());
		if (dist < newDist){
			return -1;
		}
		newDist += Math.abs(a.getBlue() - b.getBlue());
		if (dist < newDist){
			return -1;
		}
		return newDist;
	}


	private static double dist2(Color a, Color b){
		return (Math.abs(a.getRed()   - b.getRed()) +
				Math.abs(a.getGreen() - b.getGreen()) +
				Math.abs(a.getBlue()  - b.getBlue()));
	}

	/**
	 * Cluster die beim k-Means Algorithmus entstehen.
	 * Jedes Cluster besteht aus einer Farbe als Zentruid und einer Liste von Pixeln die den Inhalt des Clusters darstellen.
	 */
	static class Cluster{
		Color centroid;
		List<Pixel> vectors;

		Cluster(Color centroid, List<Pixel> vectors){
			this.vectors = vectors;
			this.centroid = centroid;
		}

		Cluster copy(){
			return new Cluster(this.centroid, this.vectors);
		}

		/**
		 * Anpassen der Zentruide.
		 * Rückt den Zentruiden in den "Mittelpunkt" des Clusters.
		 */
		void calibrateCentroids(){
			double red = 0;
			double green = 0;
			double blue = 0;
			for (Pixel pixel : vectors) {
				red += pixel.getColor().getRed();
				green += pixel.getColor().getGreen();
				blue += pixel.getColor().getBlue();
			}
			int size = vectors.size();
			red /= size;
			green /= size;
			blue /= size;

			centroid = new Color(red, green, blue, 1);
		}
	}


}
