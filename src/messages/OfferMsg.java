package messages;

public class OfferMsg extends SimulationMsg {
	public final Integer agent_id;
	public final Integer offer;
	
	public OfferMsg(int turn_id, Integer agent_id, Integer offer) {
		super(turn_id);
		this.agent_id = agent_id;
		this.offer = offer;
	}

	@Override
	public String toString() {
		return "OfferMsg [agent_id=" + agent_id + ", offer=" + offer + "]";
	}

	public Integer getAgentId() {
		return agent_id;
	}

	public Integer getOffer() {
		return offer;
	}
	
	//minimal rules
	public boolean betterThan(int my_haste, OfferMsg msg, int his_haste) {
		return offer > msg.offer || 
			(offer == msg.offer && (my_haste > his_haste|| 
			(my_haste == his_haste && agent_id < msg.agent_id)));
	}
	
}
