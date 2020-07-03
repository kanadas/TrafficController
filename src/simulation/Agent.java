package simulation;

import java.util.BitSet;
import java.util.ArrayList;

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
	
	//TODO this structures aren't thread-safe
	private BitSet commited;
	private BitSet receaved;
	private ArrayList<Message.Offer> offers;
	private ArrayList<Message.Accept> accepts;
	private boolean finished;
	private boolean got_offers;

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
			Action res;
			commited = new BitSet(msg.state.size());
			receaved = new BitSet(msg.state.size());
			commited.set(this.agent_id); //not waiting for myself
			receaved.set(this.agent_id); //not waiting for myself
			offers = new ArrayList<Message.Offer>();
			accepts = new ArrayList<Message.Accept>();
			finished = false;
			got_offers = false;
			if(mystate.waiting_time > 0) {
				ruleSet.parseFromARL(rules);
				ruleSet.init();
				Object[] output = (Object[]) ruleSet.process(new Object[] {this, msg.state, null, null});
				res = (Action) output[0];
			} else res = new Action(0);
			Object send;
			if(res.getFinalSpeed() != null) {
				send = new Message.FinishedStep(this.agent_id, res.getFinalSpeed());
				finished = true;
			} else if(res.getSpeeds() != null)
				send = new Message.Accept(this.agent_id, res.getOffers(), res.getSpeeds());
			else send = new Message.Offer(this.agent_id, res.getOffers());
			notifyAbleEventListeners(new AbleEvent(this, send));
		} else if(evt.getArgObject() instanceof Message.Offer && !finished) {
			Message.Offer msg = (Message.Offer) evt.getArgObject();
			offers.add(msg);
			receaved.set(msg.agent_id);
			if(receaved.cardinality() == receaved.length()) {
				gotAllOffers();
			}
		} else if(evt.getArgObject() instanceof Message.Accept && !finished) {
			Message.Accept msg = (Message.Accept) evt.getArgObject();
			accepts.add(msg);
			if(got_offers) receaved.set(msg.agent_id);
			if(got_offers && receaved.cardinality() == receaved.length()) {
				gotAllAccepts();
			}
		} else if(evt.getArgObject() instanceof Message.FinishedStep) {
			Message.FinishedStep msg = (Message.FinishedStep) evt.getArgObject();
			commited.set(msg.agent_id);
			receaved.set(msg.agent_id);
			if(receaved.cardinality() == receaved.length()) {
				if(got_offers) gotAllAccepts();
				else gotAllOffers();
			}
		}
	}
	
	private void gotAllOffers() throws AbleException {
		Object[] output = (Object[]) ruleSet.process(new Object[] {this, null, offers, null});
		Action res = (Action) output[0];
		receaved.clear();
		receaved.or(commited);
		got_offers = true;
		for(Message.Accept acc: accepts) receaved.set(acc.agent_id);
		if(res.getSpeeds() != null)
			notifyAbleEventListeners(new AbleEvent(this, new Message.Accept(this.agent_id, res.getOffers(), res.getSpeeds())));
		else {//it cannot be offer
			notifyAbleEventListeners(new AbleEvent(this, new Message.FinishedStep(this.agent_id, res.getFinalSpeed())));
			finished = true;
		}
	}
	
	private void gotAllAccepts() throws AbleException {
		Object[] output = (Object[]) ruleSet.process(new Object[] {this, null, null, accepts});
		Action res = (Action) output[0];
		notifyAbleEventListeners(new AbleEvent(this, new Message.FinishedStep(this.agent_id, res.getFinalSpeed())));
		finished = true;
	}
	
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
	
	public int getPoints() {
		return this.points;
	}
}
