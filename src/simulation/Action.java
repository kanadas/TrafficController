package simulation;

import java.util.List;

public class Action {
	private Integer offer;
	private List<Integer> accepts;
	private Integer final_speed;
	
	private Action(Integer offer, List<Integer> accepts, Integer final_speed) {
		this.offer = offer;
		this.accepts = accepts;
		this.final_speed = final_speed;
	}
	
	public static Action offer(Integer offer) {
		return new Action(offer, null, null);
	}
	
	public static Action accept(List<Integer> accepts) {
		return new Action(null, accepts, null);
	}
	
	public static Action commit(Integer final_speed) {
		return new Action(null, null, final_speed);
	}

	public Integer getOffer() {
		return offer;
	}

	public List<Integer> getAccepts() {
		return accepts;
	}

	public Integer getFinalSpeed() {
		return final_speed;
	}
	
	public boolean isOffer() {
		return offer != null;
	}
	
	public boolean isAccept() {
		return accepts != null;
	}
	
	public boolean isCommit() {
		return final_speed != null;
	}
}
