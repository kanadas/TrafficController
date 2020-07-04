package messages;

import java.util.List;

public class AcceptMsg {
	public final Integer agent_id;
	public final List<Integer> offers;
	//Puts maximal accepted speed, lower speeds are accepted by default
	public final List<Integer> speeds;
	
	public AcceptMsg(Integer agent_id, List<Integer> offers, List<Integer> speeds) {
		this.agent_id = agent_id;
		this.offers = offers;
		this.speeds = speeds;
	}
}
