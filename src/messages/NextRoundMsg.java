package messages;

import java.util.List;

import simulation.AgentState;

public class NextRoundMsg extends SimulationMsg {
	public final List<AgentState> state;
	
	public NextRoundMsg(int turn_id, List<AgentState> state) {
		super(turn_id);
		this.state = state;
	}

	@Override
	public String toString() {
		return "NextRoundMsg [state=" + state + "]";
	}
}
