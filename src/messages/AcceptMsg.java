package messages;

import java.util.List;

public class AcceptMsg extends SimulationMsg {
	public final Integer agent_id;
	public final List<Integer> offers;
	
	public AcceptMsg(Integer turn_id, Integer agent_id, List<Integer> offers) {
		super(turn_id);
		this.agent_id = agent_id;
		this.offers = offers;
	}

	@Override
	public String toString() {
		return "AcceptMsg [agent_id=" + agent_id + ", offers=" + offers + "]";
	}
	
}
