package messages;

public class FinishedStepMsg {

	public final int agent_id; 
	public final int speed;
	
	public FinishedStepMsg(int agent_id, int speed) {
		this.agent_id = agent_id;
		this.speed = speed;
	}
}
