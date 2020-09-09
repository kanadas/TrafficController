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
	
}
