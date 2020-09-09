package simulation;

import simulation.Direction;

public final class AgentState {
	public final Integer agent_id;
	public final int position; //negative - before intersection, positive - after intersection
	public final Direction place;
	public final Direction dest;
	public final int waiting_time;
	public final int max_speed; //it's not changing, but here is more convinient
	public final int haste; //a bit of cheating
	
	public AgentState(Integer agent_id, int position, Direction place, Direction dest, int waiting_time, int max_speed, int haste) {
		this.agent_id = agent_id;
		this.position = position;
		this.place = place;
		this.dest = dest;
		this.waiting_time = waiting_time;
		this.max_speed = max_speed;
		this.haste = haste;
	}

	@Override
	public String toString() {
		return "AgentState [agent_id=" + agent_id + ", position=" + position + ", place=" + place + ", dest=" + dest + ", wait = " + waiting_time + "]\n";
	}
	
	public Integer getAgentId() {
		return agent_id;
	}

	public int getPosition() {
		return position;
	}

	public Direction getPlace() {
		return place;
	}

	public Direction getDest() {
		return dest;
	}

	public int getWaitingTime() {
		return waiting_time;
	}

	public int getMaxSpeed() {
		return max_speed;
	}

	public int getHaste() {
		return haste;
	}
}
