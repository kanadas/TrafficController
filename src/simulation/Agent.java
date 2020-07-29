package simulation;

import messages.*;
	
import java.util.BitSet;
import java.util.ArrayList;
import java.util.List;

import com.ibm.able.Able;
import com.ibm.able.AbleDefaultAgent;
import com.ibm.able.AbleEvent;
import com.ibm.able.AbleException;
import com.ibm.able.rules.AbleRuleSet;
import com.ibm.able.rules.AbleRuleSetImpl;

public class Agent extends AbleDefaultAgent {

	private static final long serialVersionUID = 9143011496195622412L;
	
	private final Integer agent_id;
	private int length;
	private int time_start;
	private int n_agents;
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
	private ArrayList<OfferMsg> offers;
	private ArrayList<AcceptMsg> accepts;
	private boolean finished;
	private boolean got_offers;
	
	private List<AgentState> cur_state;
	
	public Agent(int length, Integer agent_id, int time_start, Position from, Position dest, int time_from, 
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
		if(evt.getArgObject() instanceof NextRoundMsg) {
			NextRoundMsg msg = (NextRoundMsg) evt.getArgObject();
			AgentState mystate = msg.state.get(this.agent_id);
			Action res;
			this.n_agents = msg.state.size();
			this.cur_state = msg.state;
			commited = new BitSet(n_agents);
			receaved = new BitSet(n_agents);
			offers = new ArrayList<OfferMsg>();
			accepts = new ArrayList<AcceptMsg>();
			finished = false;
			got_offers = false;
			if(mystate.waiting_time == 0) {
				ruleSet.parseFromARL(rules);
				ruleSet.init();
				
				System.out.printf("[Agent %d] gonna process state\n", agent_id);
				
				Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, null, null});
				res = (Action) output[0];
			} else res = Action.commit(0);
			Object send;
			if(res.isCommit()) {
				send = new FinishedStepMsg(this.agent_id, res.getFinalSpeed());
				finished = true;
			} else if(res.isAccept())
				send = new AcceptMsg(this.agent_id, res.getAccepts());
			else send = new OfferMsg(this.agent_id, res.getOffer());
			receaved.set(this.agent_id);
			notifyAbleEventListeners(new AbleEvent(this, send));
			
			System.out.println("[Agent " + String.valueOf(this.getAgentId()) + "] finished");

		} else if(evt.getArgObject() instanceof OfferMsg && !finished) {
			OfferMsg msg = (OfferMsg) evt.getArgObject();
			offers.add(msg);
			receaved.set(msg.agent_id);
			
			//System.out.printf("[Agent %d] got offers: %s card = %d size = %d\n", agent_id, receaved, receaved.cardinality(), receaved.size());
			
			if(receaved.cardinality() == n_agents) {
				gotAllOffers();
			}
		} else if(evt.getArgObject() instanceof AcceptMsg && !finished) {
			AcceptMsg msg = (AcceptMsg) evt.getArgObject();
			accepts.add(msg);
			if(got_offers) receaved.set(msg.agent_id);
			else commited.nextSetBit(msg.agent_id);
			if(got_offers && receaved.cardinality() == n_agents) {
				gotAllAccepts();
			}
		} else if(evt.getArgObject() instanceof FinishedStepMsg && !finished) {
			FinishedStepMsg msg = (FinishedStepMsg) evt.getArgObject();
			commited.set(msg.agent_id);
			receaved.set(msg.agent_id);			
			if(receaved.cardinality() == n_agents) {
				if(got_offers) gotAllAccepts();
				else gotAllOffers();
			}
			
		}		
	}
	
	private void gotAllOffers() throws AbleException {
		System.out.printf("[Agent %d] gonna process offers: %s\n", agent_id, offers.toString());
		
		Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, offers, null});
		Action res = (Action) output[0];
		receaved.clear();
		receaved.or(commited);
		got_offers = true;
		for(AcceptMsg acc: accepts) receaved.set(acc.agent_id);
		receaved.set(this.agent_id);
		if(res.isAccept())
			notifyAbleEventListeners(new AbleEvent(this, new AcceptMsg(this.agent_id, res.getAccepts())));
		else { //it cannot be offer
			notifyAbleEventListeners(new AbleEvent(this, new FinishedStepMsg(this.agent_id, res.getFinalSpeed())));
			finished = true;
		}
		
		System.out.println("[Agent " + String.valueOf(this.getAgentId()) + "] finished");
	}
	
	private void gotAllAccepts() throws AbleException {
		System.out.printf("[Agent %d] gonna process accepts\n", agent_id);
		
		Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, null, accepts});
		Action res = (Action) output[0];
		notifyAbleEventListeners(new AbleEvent(this, new FinishedStepMsg(this.agent_id, res.getFinalSpeed())));
		finished = true;
		
		System.out.println("[Agent " + String.valueOf(this.getAgentId()) + "] finished");

	}
	
	public Integer computeOffer(Double expected_wait_time) {
		double max_spend = ((double) haste) / 5 * (double) points; 
		return (int) Math.round(max_spend * (1 - 1/expected_wait_time));
	}
	
	//Accepting only the offers it must
	//TODO For now doesn't check if offer intersects
	public Boolean acceptsOffer(AgentState sender, Integer offer, Integer my_offer) {
		if(offer != my_offer)
			return offer > my_offer;
		if(sender.haste != haste)
			return sender.haste > haste;
		return sender.agent_id < agent_id;
	}
	
	public Integer getAgentId() {
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
	
	public void setPoints(int points) {
		this.points = points;
	}
}
