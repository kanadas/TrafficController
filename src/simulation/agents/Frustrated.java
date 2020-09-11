package simulation.agents;

import com.ibm.able.AbleEvent;
import com.ibm.able.AbleException;
import java.util.List;
import simulation.Direction;
import simulation.AgentState;
import messages.SimulationMsg;
import messages.NextRoundMsg;

public class Frustrated extends Agent {
	
	private static final long serialVersionUID = 1L;
	
	private int agents_crossed = 0;
	private List<AgentState> prev_state = null;

	public Frustrated(int length, Integer agent_id, int time_start, Direction from, Direction dest, int time_from, 
			int time_dest, int haste, boolean random_haste, int max_speed, int points) throws AbleException {
	
		super(length, agent_id, time_start, from, dest, time_from, time_dest, haste, random_haste, max_speed, points);
	}

	@Override
	public void processAbleEvent(AbleEvent evt) throws AbleException {
		super.processAbleEvent(evt);
		if(!(evt.getArgObject() instanceof SimulationMsg)) {
			return;
		}
		SimulationMsg simMsg = (SimulationMsg) evt.getArgObject();
		if(simMsg instanceof NextRoundMsg) {
			NextRoundMsg msg = (NextRoundMsg)simMsg;
			AgentState my_state = msg.state.get(getAgentId());
			if(my_state.waiting_time > 0) {
				agents_crossed = 0;
				prev_state = null;
				return;
			}
			if(prev_state != null) {
				for(int i = 0; i < prev_state.size(); i++) {
					if(i == getAgentId()) continue;
					if(msg.state.get(i).waiting_time > 0 && prev_state.get(i).waiting_time == 0) {
						agents_crossed++;
					}
				}
			}
			prev_state = msg.state;
		}
	}
	
	@Override
	public Integer computeOffer(Double expected_wait_time) {
		//Max spend factor is average of factors of agents_crossed and haste
		double max_spend = (((double) getHaste() + 1) / 6.0 + 1.0 - 1.0 / (0.5*agents_crossed + 1.0)) / 2 * (double) points;
		//agents_crossed adds to expected time
		double offer = Math.round(max_spend * (1.0 - 1.0/(0.5*(expected_wait_time + agents_crossed) + 1.0)));
		logger.debug("Frustrated: max_spend = %f, real offer = %f agents_crossed = %d", max_spend, offer, agents_crossed);
		return (int)offer;
	}
	
}
