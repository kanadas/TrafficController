package simulation;

import java.util.List;

public class Message {
	
	public static class NextRound {
		
		public final List<AgentState> state;
		
		public NextRound(List<AgentState> state) {
			this.state = state;
		}
	}
	
	public static class FinishedStep {
		
		public final int agent_id; 
		public final int num_messages;
		public final int speed;
		
		public FinishedStep(int agent_id, int num_messages, int speed) {
			this.agent_id = agent_id;
			this.num_messages = num_messages;
			this.speed = speed;
		}
	}
}
