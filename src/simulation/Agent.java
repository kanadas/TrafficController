package simulation;

import messages.*;
import logging.Logger; 
	
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
	private Logger logger;
	
	private final Integer agent_id;
	private int length;
	private int time_start;
	private int n_agents;
	private Direction from;
	private Direction dest;
	private int time_from;
	private int time_dest;
	private int haste;
	private boolean random_haste;
	private int max_speed;
	protected int points; 
	private String rules;
	AbleRuleSet ruleSet;
	
	private BitSet commited;
	private BitSet received;
	private ArrayList<OfferMsg> offers;
	private ArrayList<AcceptMsg> accepts;
	private boolean got_offers;
	private Object mutex = new Object(); 
	private boolean finished;
	private int round_id;
	
	private List<AgentState> cur_state;
	
	public Agent(int length, Integer agent_id, int time_start, Direction from, Direction dest, int time_from, 
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
		this.logger = new Logger(this.agent_id);
		this.round_id = 1;
		this.finished = false;
		reset();
		init();
	}
	
	@Override
	public void reset() throws AbleException {
        setAbleEventProcessingEnabled(Able.ProcessingEnabled_PostingEnabled); 
	}	
	
	@Override
	public void processAbleEvent(AbleEvent evt) throws AbleException {			
		//logger.debug("Received event: %s",  evt.getArgObject().toString());	
		if(!(evt.getArgObject() instanceof SimulationMsg)) {
			return;
		}
		SimulationMsg simMsg = (SimulationMsg) evt.getArgObject();
		if(simMsg instanceof NextRoundMsg) {
			logger.trace("Starting round %d", round_id);
			NextRoundMsg msg = (NextRoundMsg) evt.getArgObject();
			AgentState mystate = msg.state.get(this.agent_id);
			Action res;
			n_agents = msg.state.size();
			cur_state = msg.state;
			if(mystate.waiting_time == 0) {
				ruleSet.parseFromARL(rules);
				ruleSet.init();
				
				logger.debug("will process state");
				try {
					Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, null, null});
					res = (Action) output[0];
				} catch (Exception e) {
					System.err.println(e.getMessage());
					throw new AbleException(e.getMessage());
				}
			} else res = Action.commit(0);
			SimulationMsg send;
			if(res.isCommit()) {
				send = new FinishedStepMsg(round_id, this.agent_id, res.getFinalSpeed());
				resetState();
			} else if(res.isAccept()) {
				send = new AcceptMsg(round_id, this.agent_id, res.getAccepts());
			} else {
				send = new OfferMsg(round_id, this.agent_id, res.getOffer());
			}
			synchronized(mutex) {
				received.set(this.agent_id);
			}
			notifyAbleEventListeners(new AbleEvent(this, send));	
			logger.trace("finished");
			if(received.cardinality() == n_agents && !finished) {
				gotAllOffers();
			}
		} else if(simMsg instanceof OfferMsg && simMsg.round_id == round_id) {
			OfferMsg msg = (OfferMsg) evt.getArgObject();
			synchronized(mutex) {
				offers.add(msg);
				received.set(msg.agent_id);			
			}
			if(received.cardinality() == n_agents) {
				gotAllOffers();
			}
		} else if(evt.getArgObject() instanceof AcceptMsg && simMsg.round_id == round_id) {
			AcceptMsg msg = (AcceptMsg) evt.getArgObject();
			synchronized(mutex) {
				accepts.add(msg);
				if(got_offers) received.set(msg.agent_id);
				else commited.set(msg.agent_id);
			}
			if(got_offers && received.cardinality() == n_agents) {
				gotAllAccepts();
			}
		} else if(evt.getArgObject() instanceof FinishedStepMsg && simMsg.round_id == round_id) {
			FinishedStepMsg msg = (FinishedStepMsg) evt.getArgObject();
			synchronized(mutex) {
				commited.set(msg.agent_id);
				if(!received.get(msg.agent_id)) {
					received.set(msg.agent_id);
				}
			}
			logger.debug("Received %d messages, from: %s", received.cardinality(), received.toString());
			if(received.cardinality() == n_agents) {
				if(got_offers) gotAllAccepts();
				else gotAllOffers();
			}
		}
	}
	
	private void gotAllOffers() throws AbleException {
		logger.debug("will process offers: %s", offers.toString());
		try {
			Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, offers, null});
			Action res = (Action) output[0];
			synchronized(mutex) {
				received.clear();
				received.or(commited);
				got_offers = true;
				for(AcceptMsg acc: accepts) received.set(acc.agent_id);
				received.set(this.agent_id);
			}
			if(res.isAccept()) {
				notifyAbleEventListeners(new AbleEvent(this, new AcceptMsg(round_id, this.agent_id, res.getAccepts())));
			} else { //it cannot be offer
				resetState();
				notifyAbleEventListeners(new AbleEvent(this, new FinishedStepMsg(round_id, this.agent_id, res.getFinalSpeed())));
			}
			if(received.cardinality() == n_agents && !finished) {
				gotAllAccepts();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw new AbleException(e.getMessage());
		}	
		logger.trace("finished");
	}
	
	private void gotAllAccepts() throws AbleException {
		logger.debug("will process accepts");
		try {
			Object[] output = (Object[]) ruleSet.process(new Object[] {this, cur_state, null, accepts});
			Action res = (Action) output[0];
			resetState();
			notifyAbleEventListeners(new AbleEvent(this, new FinishedStepMsg(round_id, this.agent_id, res.getFinalSpeed())));
			logger.trace("finished");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw new AbleException(e.getMessage());
		}
	}
	
	private void resetState() {
		synchronized(mutex) {
			commited = new BitSet(n_agents);
			received = new BitSet(n_agents);
			offers = new ArrayList<OfferMsg>();
			accepts = new ArrayList<AcceptMsg>();
			got_offers = false;
			round_id++;
			finished = true;
		}
	}
	
	public Integer computeOffer(Double expected_wait_time) {
		double max_spend = ((double) haste + 1) / 6.0 * (double) points;
		logger.debug("max_spend = %f, real offer = %f", max_spend, max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
		return (int) Math.round(max_spend * (1.0 - 1.0/(0.5*expected_wait_time + 1.0)));
	}
	
	//Accepting only the offers it must
	public Boolean acceptsOffer(AgentState sender, int offer, int my_offer) {		
		logger.debug("Checking if offer %d from %d is better than my offer %d", offer, sender.agent_id, my_offer);
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
	
	public Direction getFrom() {
		return this.from;
	}
	
	public Direction getDest() {
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
