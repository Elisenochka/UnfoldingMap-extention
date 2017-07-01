package module6;

import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Button {

	/**
	 *
	 */
	protected boolean clicked = false;
	protected boolean selected = false;
	protected boolean mouseIns = false;
	private String title;
	float buttonX;
	float buttonY;
	PApplet parent;



	public boolean getClicked() {
		return clicked;
	}

	public void setClicked(boolean state) {
		clicked = state;
	}
	public boolean getSelected() {
		return selected;
	}

	public void setSelected(boolean state) {
		selected = state;
	}




	public Button(PApplet p, String title, float x, float y, boolean selected){
		this.title = title;
		this.buttonX=x;
		this.buttonY=y;
		this.selected = selected;
		this.parent = p;

	}
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}



}
