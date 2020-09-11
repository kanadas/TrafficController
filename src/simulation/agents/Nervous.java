package simulation.agents;

import com.ibm.able.AbleException;
import simulation.Direction;

public class Nervous extends Agent {

	private static final long serialVersionUID = -2215445617344238966L;

	public Nervous(int length, Integer agent_id, int time_start, Direction from, Direction dest, int time_from, 
		 int time_dest, int haste, boolean random_haste, int max_speed, int points) throws AbleException {
	
		super(length, agent_id, time_start, from, dest, time_from, time_dest, haste, random_haste, max_speed, points);
	}
	
	@Override
	public Integer computeOffer(Double expected_wait_time) {
		//Nervous will have big max_spend even with low haste
		double max_spend = ((double) getHaste() + 5) / 10.0 * (double) points;
		logger.debug("Nervous: max_spend = %f, real offer = %f", max_spend, max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
		return (int) Math.round(max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
	}

}
