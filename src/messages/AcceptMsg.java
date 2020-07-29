package messages;

import java.util.List;

public class AcceptMsg {
	public final Integer agent_id;
	public final List<Integer> offers;
	
	public AcceptMsg(Integer agent_id, List<Integer> offers) {
		this.agent_id = agent_id;
		this.offers = offers;
	}

	@Override
	public String toString() {
		return "AcceptMsg [agent_id=" + agent_id + ", offers=" + offers + "]";
	}
	
	public 
}
