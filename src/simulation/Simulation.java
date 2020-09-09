package simulation;

import messages.*;
import logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Vector;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.ibm.able.Able;
import com.ibm.able.AbleDefaultAgent;
import com.ibm.able.AbleEvent;
import com.ibm.able.AbleException;

public class Simulation extends AbleDefaultAgent {
			
	private static final long serialVersionUID = -7429691757214576852L;
	private static Logger logger = new Logger(Simulation.class);
	private static Random rand = new Random();
	private static int HASTE_LEVELS = 6;
	private int length;
	private int steps;
	private int cur_step;
	private ArrayList<Agent> agents;
	private ArrayList<AgentState> curr_state;
	private Integer[] velocity;
	private Intersection intersection;
	private BitSet finished;
	private Double sum_intersection_time;
	private int num_intersection_cross;
	private Integer max_messages;
	private Integer min_messages;
	private Float sum_messages;
	private Integer[] curr_msgs;
	private Integer[] curr_time;
	private Double[] haste_sum_time;
	private Integer[] haste_cross;
	private Object mutex = new Object();
	private CountDownLatch latch;
	
	//Assuming that index in agents list is equal to agent_id
	public Simulation(int length, int steps, ArrayList<Agent> agents, CountDownLatch latch) throws AbleException {
		super("Simulation");
		reset();
		init();
		this.length = length;
		this.steps = steps;
		this.cur_step = 0;
		this.agents = agents;
		this.latch = latch;
		this.intersection = new Intersection(length);
		this.curr_state = new ArrayList<AgentState>();
		this.finished = new BitSet(agents.size());
		this.sum_intersection_time = 0.0;
		this.num_intersection_cross = 0;
		this.max_messages = null;
		this.min_messages = null;
		this.sum_messages = 0F;
		this.velocity = new Integer[agents.size()];
		Arrays.fill(this.velocity, 0);
		this.curr_msgs = new Integer[agents.size()];
		Arrays.fill(this.curr_msgs, 0);
		this.curr_time = new Integer[agents.size()];
		Arrays.fill(this.curr_time, 0);
		this.haste_sum_time = new Double[HASTE_LEVELS];
		Arrays.fill(this.haste_sum_time, 0.0);
		this.haste_cross = new Integer[HASTE_LEVELS];
		Arrays.fill(this.haste_cross, 0);
		for(Agent agent: agents) {
			curr_state.add(new AgentState(agent.getAgentId(), 0, agent.getFrom(), agent.getDest(), agent.getTimeStart(), agent.getMaxSpeed(), agent.getHaste()));
		}
	}
	
	@Override
	public void reset() throws AbleException {
        setAbleEventProcessingEnabled(Able.ProcessingEnabled_PostingEnabled); 
	}
	
	public void nextRound() throws AbleException {
		logger.trace("NextRound");
		logger.trace("\n" + curr_state.toString());
		logger.trace("Agent points: %s", agents.stream().map(Agent::getPoints).collect(Collectors.toList()).toString());
		finished.clear();
		if(cur_step == steps) {
			finish();
		} else {
			cur_step++;	
			notifyAbleEventListeners(new AbleEvent(this, new NextRoundMsg(cur_step, Collections.unmodifiableList(this.curr_state))));
		}
	}
	
	@Override
	public void processAbleEvent(AbleEvent evt) throws AbleException {
		logger.trace("Event: %s", evt.getArgObject().toString());
		if(evt.getArgObject() instanceof FinishedStepMsg) {
			FinishedStepMsg msg = (FinishedStepMsg) evt.getArgObject();
			agentSendMessage(msg.agent_id);
			synchronized(mutex) {
				finished.set(msg.agent_id);
				velocity[msg.agent_id] = msg.speed;
			}
			logger.debug("%d Agents finished", finished.cardinality());
			if(finished.cardinality() == agents.size()) {
				finishRound();
			}

		} else if(evt.getArgObject() instanceof AcceptMsg) {
			AcceptMsg msg = (AcceptMsg) evt.getArgObject();
			agentSendMessage(msg.agent_id);	
		} else if(evt.getArgObject() instanceof OfferMsg) {
			OfferMsg msg = (OfferMsg) evt.getArgObject();
			agentSendMessage(msg.agent_id);			
		}
	}
	
	private void agentSendMessage(int agent_id) {
		if(curr_state.get(agent_id).waiting_time == 0) {
			synchronized(mutex) {
				sum_messages++;
				curr_msgs[agent_id]++;
			}
		}
	}
	
	//Intersection: (l := length - 1)
	//			   N
	//			   0 l
	//			   1 .
	//			   . .
	//			   . .
	//			   . 1
	//			   l 0
	//W  l ... 1 0 0 1 l ... 1 0  E
	// 	 0 1 ... l 3 2 0 1 ... l
	//			   0 l
	//			   1 .
	// 			   . .
	//			   . .
	//			   . 1
	//			   l 0
	//			   S
	protected void finishRound() throws AbleException {
		
		logger.trace("Finished Round %d", cur_step);
		
		//Position in the center [starting position][distance]
		int turn_tb[][] = new int[4][3]; 
		for(int i = 0; i < 4; ++i) {
			turn_tb[i][0] = i;
			turn_tb[i][1] = (i + 3) % 4;
			turn_tb[i][2] = (i + 2) % 4;
		}
		ArrayList<AgentState> prev_state = curr_state;
		curr_state = new ArrayList<AgentState>();
		Vector<Integer> collisions = new Vector<Integer>(); 
		//Checking for collisions
		for(int i = 0; i < agents.size(); ++i) {
			AgentState state1 = prev_state.get(i);
			if(state1.waiting_time > 0) continue;
			for(int j = 0; j < i; ++j) {
				AgentState state2 = prev_state.get(j);
				if(state2.waiting_time > 0) continue;
				if(willDriveInto(state1, velocity[i], state2, velocity[j]) || willDriveInto(state2, velocity[j], state1, velocity[i])) {
					detectedCollision(state1, velocity[i], state2, velocity[j]);
					collisions.add(i);
					collisions.add(j);
				}
			}
		}
		boolean starting_free[] = {true, true, true, true};
		for(int i = 0; i < agents.size(); ++i) {
			AgentState state = prev_state.get(i);
			if(state.place != Direction.C && state.waiting_time == 0 && state.position == 0 && velocity[i] == 0) 
				starting_free[state.place.num] = false;
		}
		for(int i = 0; i < agents.size(); ++i) {	
			AgentState state = prev_state.get(i);
			Agent agent = agents.get(i);
			int position = state.position + velocity[i];
			Direction place = state.place;
			Direction dest = state.dest;
			int waiting_time = state.waiting_time;			
			sum_intersection_time++;
			haste_sum_time[agent.getHaste()]++;
			if(collisions.contains(i)) {
				sum_intersection_time--;
				haste_sum_time[agent.getHaste()]--;
				waiting_time = Integer.MAX_VALUE;
				position = -1;
				place = Direction.N;
			} else if(waiting_time > 0) {
				sum_intersection_time--;
				haste_sum_time[agent.getHaste()]--;
				waiting_time--;
				if(waiting_time == 0) {
					if(starting_free[state.place.num]) {
						num_intersection_cross++;
						starting_free[state.place.num] = false;
						if(agent.isRandomHaste()) {
							agent.setHaste(rand.nextInt(6));
						}
						haste_cross[agent.getHaste()]++;
					} else {
						//Wait till starting place frees
						waiting_time++;
					}
				}
			} else if(place == Direction.C) {
				position = state.position;
				int len = velocity[i];
				while(len > 0 && position != (dest.num + 1) % 4) {
					len --;
					position = (position + 3) % 4;
				}
				if(len > 0) {
					place = dest;
					position = len - 1;
				}
			} else if(position >= length) {
				int dist = position - length;
				if(place == dest) {
					//Agent arrived in destination
					position = 0;
					if(agent.getFrom() == dest) {
						dest = agent.getDest();
						waiting_time = agent.getTimeDest();
						int nmsgs =  curr_msgs[i];
						logger.debug("Agent %d send %d messages", agent.getAgentId(), nmsgs);
						if(min_messages == null) min_messages = nmsgs;
						else min_messages = Math.min(min_messages, nmsgs);
						if(max_messages == null) max_messages = nmsgs;
						else max_messages = Math.max(max_messages, nmsgs);
						curr_msgs[i] = 0;
					} else {
						dest = agent.getFrom();
						waiting_time = agent.getTimeFrom();
					}
				} else {
					int turn = (dest.num - place.num + 4) % 4;
					switch(turn) {
					case 0: //turn around Error
						logger.error("Turn around error!");
						assert false;
					case 1: //left
						if(dist < 3) {
							position = turn_tb[place.num][dist];
							place = Direction.C;
						} else {
							position = dist - 3;
							place = dest;
						} break;
					case 2: //strait
						if(dist < 2) {
							position = turn_tb[place.num][dist];
							place = Direction.C;
						} else {
							position = dist - 2;
							place = dest;
						} break;
					case 3: //right
						if(dist < 1) {
							position = turn_tb[place.num][dist];
							place = Direction.C;
						} else {
							position = dist - 1;
							place = dest;
						}
					}
				}
			}
			curr_state.add(new AgentState(i, position, place, dest, waiting_time, state.max_speed, agent.getHaste()));
		}
		nextRound();
	}

	private boolean willDriveInto(AgentState state1, int velocity1, AgentState state2, int velocity2) {
		if(intersection.isInFront(state1, state2) && intersection.distance(state1, state2) <= velocity1) {			
			return true;
		}
		CenterPlace p2 = intersection.getFirstPlace(state2);
		return intersection.onTrajectory(p2, state1) && intersection.distance(state1, p2) <= velocity1 && intersection.distance(state2, p2) <= velocity2;
	}
	
	private void detectedCollision(AgentState state1, int velocity1, AgentState state2, int velocity2) {
		if(!willDriveInto(state1, velocity1, state2, velocity2)) {
			detectedCollision(state2, velocity2, state1, velocity1);
			return;
		}
		String collPlaceMsg;
		if(state2.place != Direction.C && (state2.place == state1.place || state2.place == state1.dest) && intersection.distance(state1, state2) <= velocity1) {			
			collPlaceMsg = "Kolizja w " + state2.place + ", pozycja " + state2.position;
		} else {
			CenterPlace p2 = intersection.getFirstPlace(state2);
			collPlaceMsg = "Kolizja na środku, pozycja " + p2.id;
		}
		logger._error("Kolizja: \nAgent %d rusza z %s pozycja %d, w kierunku %s z prędkością %d\nAgent %d rusza z %s pozycja %d, w kierunku %s z prędkością %d\n%s", 
				state1.agent_id, state1.place.toString(), state1.position, state1.dest.toString(), velocity1, 
				state2.agent_id, state2.place.toString(), state2.position, state2.dest.toString(), velocity2, collPlaceMsg);	
	}
	
	private void finish() {
		System.out.printf("%.4f", sum_intersection_time / num_intersection_cross);
		for(int i = 0; i < HASTE_LEVELS; ++i) {
			if(haste_cross[i] == 0) {
				System.out.print(" 0");
			} else {
				System.out.printf(" %.4f", (haste_sum_time[i] / haste_cross[i]));
			}
		}
		System.out.printf("\n%d %d %.4f\n", max_messages, min_messages, (sum_messages / num_intersection_cross));
		latch.countDown();
	}
}
