package sample;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

abstract class KMeans {

	static List<Cluster> cluster(List<Pixel> data, int k){
		// Anlegen der leeren Kluster.
		List<Cluster> clusters;
		List<Cluster> oldClusters;
		clusters = new ArrayList<>(k);

		Collections.shuffle(data);

		// zufällige cluster anlegen
		for (int i=1; i<=k; i++){
			clusters.add(new Cluster( data.get(i).getColor(), new LinkedList<>()));
		}

		int runs = 0;
		boolean done;
		long before = System.currentTimeMillis();

		do {
			oldClusters = new ArrayList<>();

			for (Cluster cl: clusters) {
				oldClusters.add(cl.copy());
				cl.vectors = new LinkedList<>();
			}

			// pixel zuordnen
			for (Pixel pixel : data) {
				int whichCluster = 0;
				for (int i = 1; i < k; i++) {
					if (isCloser(pixel.getColor(), clusters.get(i).centroid, clusters.get(whichCluster).centroid)) {
						whichCluster = i;
					}
				}
				clusters.get(whichCluster).vectors.add(pixel);
			}

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

	private static boolean isCloser(Color x, Color a, Color b){
		double first = (Math.pow(x.getRed()   - a.getRed() , 2) +
						Math.pow(x.getGreen() - a.getGreen() , 2) +
						Math.pow(x.getBlue()  - a.getBlue() , 2));

		double second = (Math.pow(x.getRed()   - b.getRed() , 2) +
						Math.pow(x.getGreen() - b.getGreen() , 2) +
						Math.pow(x.getBlue()  - b.getBlue() , 2));

		return first < second;
	}

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
