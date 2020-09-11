package simulation.agents;

import com.ibm.able.AbleException;

import simulation.AgentState;
import simulation.Direction;

public class Altruistic extends Agent {
	
	private static final long serialVersionUID = 1L;

	public Altruistic(int length, Integer agent_id, int time_start, Direction from, Direction dest, int time_from, 
			int time_dest, int haste, boolean random_haste, int max_speed, int points) throws AbleException {
	
		super(length, agent_id, time_start, from, dest, time_from, time_dest, haste, random_haste, max_speed, points);
	}
	
	//Allows to go agents with smaller offer if have higher haste, based on difference between haste levels.
	@Override
	public Boolean acceptsOffer(AgentState sender, int offer, int my_offer) {		
		logger.debug("Altruistic: Checking if offer %d from %d is better than my offer %d", offer, sender.agent_id, my_offer);
		//Let's go agents with higher haste
		return offer > (((double) this.getHaste() + 1.0) / 6.0)*my_offer;
	}

}
