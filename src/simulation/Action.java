package simulation;

import java.util.ArrayList;

//non-null final_speed means commit speed
//non-null speeds means accepting offer
public class Action {
	private ArrayList<Integer> offers;
	private ArrayList<Integer> speeds;
	private Integer final_speed;
	
	public Action(ArrayList<Integer> offers) {
		this.offers = offers;
		this.speeds = null;
		this.final_speed = null;
	}
	
	public Action(ArrayList<Integer> offers, ArrayList<Integer> speeds) {
		this.offers = offers;
		this.speeds = speeds;
		this.final_speed = null;
	}
	
	public Action(Integer final_speed) {
		this.final_speed = final_speed;
		this.offers = null;
		this.speeds = null;
	}

	public ArrayList<Integer> getOffers() {
		return offers;
	}

	public ArrayList<Integer> getSpeeds() {
		return speeds;
	}

	public Integer getFinalSpeed() {
		return final_speed;
	}
}
