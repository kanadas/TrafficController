package messages;

import java.util.List;

import simulation.AgentState;

public class NextRoundMsg {
	public final List<AgentState> state;
	
	public NextRoundMsg(List<AgentState> state) {
		this.state = state;
	}
}
