package messages;

import java.util.List;

public class OfferMsg {
	public final Integer agent_id;
	public final List<Integer> offers;
	
	public OfferMsg(Integer agent_id, List<Integer> offers) {
		this.agent_id = agent_id;
		this.offers = offers;
	}

	@Override
	public String toString() {
		return "OfferMsg [agent_id=" + agent_id + ", offers=" + offers + "]";
	}
}
