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
		public final int speed;
		
		public FinishedStep(int agent_id, int speed) {
			this.agent_id = agent_id;
			this.speed = speed;
		}
	}
	
	public static class Offer {
		public final int agent_id;
		public final List<Integer> offers;
		
		public Offer(int agent_id, List<Integer> offers) {
			this.agent_id = agent_id;
			this.offers = offers;
		}
	}
	
	public static class Accept {
		public final int agent_id;
		public final List<Integer> offers;
		//Puts maximal accepted speed, lower speeds are accepted by default
		public final List<Integer> speeds;
		
		public Accept(int agent_id, List<Integer> offers, List<Integer> speeds) {
			this.agent_id = agent_id;
			this.offers = offers;
			this.speeds = speeds;
		}
	}
	
}
