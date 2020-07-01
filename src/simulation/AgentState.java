package simulation;

import simulation.Position;

public final class AgentState {
	public final int agent_id;
	public final int position; //negative - before intersection, positive - after intersection
	public final Position place;
	public final Position dest;
	public final int waiting_time;
	public final int max_speed; //it's not changing, but here is more convinient
	
	public AgentState(int agent_id, int position, Position place, Position dest, int waiting_time, int max_speed) {
		this.agent_id = agent_id;
		this.position = position;
		this.place = place;
		this.dest = dest;
		this.waiting_time = waiting_time;
		this.max_speed = max_speed;
	}
}
