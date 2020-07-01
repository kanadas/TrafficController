package simulation;

import com.ibm.able.Able;
import com.ibm.able.AbleDefaultAgent;
import com.ibm.able.AbleEvent;
import com.ibm.able.AbleException;
import com.ibm.able.rules.AbleRuleSet;
import com.ibm.able.rules.AbleRuleSetImpl;

public class Agent extends AbleDefaultAgent {

	private static final long serialVersionUID = 9143011496195622412L;
	
	private final int agent_id;
	private int length;
	private int time_start;
	private Position from;
	private Position dest;
	private int time_from;
	private int time_dest;
	private int haste;
	private boolean random_haste;
	private int max_speed;
	protected int points; 
	private String rules;
	AbleRuleSet ruleSet;
	
	public int getAgentId() {
		return this.agent_id;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public int getTimeStart() {
		return this.time_start;
	}
	
	public Position getFrom() {
		return this.from;
	}
	
	public Position getDest() {
		return this.dest;
	}
	
	public int getTimeFrom() {
		return this.time_from;
	}
	
	public int getTimeDest() {
		return this.time_dest;
	}
	
	public int getHaste() {
		return this.haste;
	}
	
	public boolean isRandomHaste() {
		return this.random_haste;
	}
	
	public int getMaxSpeed() {
		return this.max_speed;
	}
	
	public void setHaste(int haste) {
		this.haste = haste;
	}
	
	public Agent(int length, int agent_id, int time_start, Position from, Position dest, int time_from, 
				 int time_dest, int haste, boolean random_haste, int max_speed, int points) throws AbleException {
		super("Agent");
		this.length = length;
		this.agent_id = agent_id;
		this.time_start = time_start;
		this.from = from;
		this.dest = dest;
		this.time_from = time_from;
		this.time_dest = time_dest;
		this.haste = haste;
		this.random_haste = random_haste;
		this.max_speed = max_speed;
		this.points = points;
		this.rules = "rules/Car.arl";
		this.ruleSet = new AbleRuleSetImpl();
		reset();
		init();
	}
	
	@Override
	public void reset() throws AbleException {
        setAbleEventProcessingEnabled(Able.ProcessingEnabled_PostingEnabled); 
	}
	
	@Override
	public void processAbleEvent(AbleEvent evt) throws AbleException {
		if(evt.getArgObject() instanceof Message.NextRound) {
			Message.NextRound msg = (Message.NextRound) evt.getArgObject();
			AgentState mystate = msg.state.get(this.agent_id);
			Integer speed = 0;
			Integer messages = 0;
			if(mystate.waiting_time > 0) {
				ruleSet.parseFromARL(rules);
				ruleSet.init();
				Object[] output = (Object[]) ruleSet.process(new Object[] {this, msg.state});
				speed = (Integer) output[0];
				messages = (Integer) output[1];
			}
			
			notifyAbleEventListeners(new AbleEvent(this, new Message.FinishedStep(this.agent_id, messages, speed)));
		}
	}
}
