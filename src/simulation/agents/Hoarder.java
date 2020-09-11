package simulation.agents;

import com.ibm.able.AbleException;

import simulation.AgentState;
import simulation.Direction;

public class Hoarder extends Agent {

	private static final long serialVersionUID = -851700308531879550L;

	public Hoarder(int length, Integer agent_id, int time_start, Direction from, Direction dest, int time_from, 
			int time_dest, int haste, boolean random_haste, int max_speed, int points) throws AbleException {
		
		super(length, agent_id, time_start, from, dest, time_from, time_dest, haste, random_haste, max_speed, points);
	}
	
	@Override
	public Integer computeOffer(Double expected_wait_time) {
		//Hoarder will offer offer maximally 2/3 of his points
		double max_spend = ((double) getHaste() + 1) / 9.0 * (double) points;
		logger.debug("Hoarder: max_spend = %f, real offer = %f", max_spend, max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
		return (int) Math.round(max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
	}
	
	@Override
	public Boolean acceptsOffer(AgentState sender, int offer, int my_offer) {		
		logger.debug("Hoarder: Checking if offer %d from %d is better than my offer %d", offer, sender.agent_id, my_offer);
		//Will accept agents with a bit lesser offers;
		return offer > my_offer * 0.75;
	}

}
