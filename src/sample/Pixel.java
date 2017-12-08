package sample;

import javafx.scene.paint.Color;

/**
 * Created by Nils on 03.12.2017.
 */
public class Pixel {
	private int x,y;
	private Color color;

	public Pixel(int x, int y, Color color){
		this.x = x;
		this.y = y;
		this.color = color;
	}

	public Color getColor(){
		return color;
	}

	public int[] getLocation(){
		int[] location = {x, y};
		return location;
	}
}
