package messages;

public class FinishedStepMsg extends SimulationMsg {

	public final int agent_id; 
	public final int speed;
	
	public FinishedStepMsg(int turn_id, int agent_id, int speed) {
		super(turn_id);
		this.agent_id = agent_id;
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "FinishedStepMsg [agent_id=" + agent_id + ", speed=" + speed + "]";
	}
}
