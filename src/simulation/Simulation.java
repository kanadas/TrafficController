package simulation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Vector;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.ibm.able.Able;
import com.ibm.able.AbleDefaultAgent;
import com.ibm.able.AbleEvent;
import com.ibm.able.AbleException;

public class Simulation extends AbleDefaultAgent {
		
	//4 ends of intersection and center
//	public enum Position {
//		N(0), E(1), S(2), W(3), C(-1);
//		
//		public final int num;
//		
//		private Position(int num) {
//			this.num = num;
//		}
//		
//		public static Position fromString(String c) {
//			switch(c) {
//			case "N": return N;
//			case "E": return E;
//			case "S": return S;
//			case "W": return W;
//			//case "C": return C;
//			default: throw new IllegalArgumentException();
//			}
//		}
//	}
	
	private static Random rand = new Random();
	private int length;
	private int steps;
	private int cur_step;
	private ArrayList<Agent> agents;
	private ArrayList<AgentState> curr_state;
	private ArrayList<Integer> velocity;
	private BitSet finished;
	private Float sum_intersection_time;
	private int num_intersection_cross;
	private Integer max_messages;
	private Integer min_messages;
	private Float sum_messages;
	private ArrayList<Integer> curr_msgs;
	private ArrayList<Integer> curr_time;
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
		this.curr_state = new ArrayList<AgentState>();
		this.velocity = new ArrayList<Integer>();
		this.finished = new BitSet(agents.size());
		this.sum_intersection_time = 0F;
		this.num_intersection_cross = 0;
		this.max_messages = null;
		this.min_messages = null;
		this.sum_messages = 0F;
		for(Agent agent: agents) {
			curr_state.add(new AgentState(agent.getAgentId(), 0, agent.getFrom(), agent.getDest(), agent.getTimeStart(), agent.getMaxSpeed()));
			velocity.add(0);
			curr_msgs.add(0);
			curr_time.add(0);
		}
	}
	
	@Override
	public void reset() throws AbleException {
        setAbleEventProcessingEnabled(Able.ProcessingEnabled_PostingEnabled); 
	}
	
	public void nextRound() throws AbleException {
		if(cur_step == steps) {
			finish();
		} else {
			notifyAbleEventListeners(new AbleEvent(this, new Message.NextRound(Collections.unmodifiableList(this.curr_state))));
			cur_step++;
			finished.clear();
		}
	}
	
	@Override
	public void processAbleEvent(AbleEvent evt) throws AbleException {
		if(evt.getArgObject() instanceof Message.FinishedStep) {
			Message.FinishedStep msg = (Message.FinishedStep) evt.getArgObject();
			sum_messages += msg.num_messages;
			finished.set(msg.agent_id);
			curr_msgs.set(msg.agent_id, curr_msgs.get(msg.agent_id) + msg.num_messages);
			if(finished.cardinality() == agents.size())
				finishRound();
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
			Agent agent1 = agents.get(i);
			int velocity1 = velocity.get(i);
			BitSet center_pos1 = getCenterPositions(agent1, state1, velocity1);
			for(int j = 0; j < agents.size(); ++j) {
				if(i == j) continue;
				AgentState state2 = prev_state.get(j);
				Agent agent2 = agents.get(j);
				int velocity2 = velocity.get(j);
				BitSet center_pos2 = getCenterPositions(agent2, state2, velocity2);
				//Take over or collision in the center
				if((state1.place != Position.C && state1.place == state2.place && state1.position <= state2.position && 
						state1.position + velocity1 >= state2.position + velocity2) || (
								center_pos1.intersects(center_pos2))){
					detectedCollision();
					collisions.add(i);
					collisions.add(j);
				}
			}
		}
		boolean starting_free[] = {true, true, true, true};
		for(int i = 0; i < agents.size(); ++i) {
			AgentState state = prev_state.get(i);
			if(state.place != Position.C && state.position == 0 && velocity.get(i) == 0) 
				starting_free[state.place.num] = false;
		}
		for(int i = 0; i < agents.size(); ++i) {
			AgentState state = prev_state.get(i);
			Agent agent = agents.get(i);
			int position = state.position + velocity.get(i);
			Position place = state.place;
			Position dest = state.dest;
			int waiting_time = state.waiting_time;
			sum_intersection_time++;
			if(collisions.contains(i)) {
				sum_intersection_time--;
				waiting_time = Integer.MAX_VALUE;
				position = -1;
				place = Position.N;
			} else if(waiting_time > 0) {
				sum_intersection_time--;
				waiting_time--;
				if(waiting_time == 0) {
					if(starting_free[state.place.num]) {
						num_intersection_cross++;
						starting_free[state.place.num] = false;
						if(agent.isRandomHaste()) agent.setHaste(rand.nextInt() % 6);
					} else {
						//Wait till starting place frees
						waiting_time++;
					}
				}
			} else if(place == Position.C) {
				position = state.position;
				int len = velocity.get(i);
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
						int nmsgs =  curr_msgs.get(i);
						if(min_messages == null) min_messages = nmsgs;
						else min_messages = Math.min(min_messages, nmsgs);
						if(max_messages == null) max_messages = nmsgs;
						else max_messages = Math.max(max_messages, nmsgs);
					} else {
						dest = agent.getFrom();
						waiting_time = agent.getTimeFrom();
					}
				} else {
					int turn = (dest.num - place.num + 4) % 4;
					switch(turn) {
					case 0: //turn around Error
						System.err.println("Turn around error!");
						assert false;
					case 1: //left
						if(dist < 3) {
							position = turn_tb[place.num][dist];
							place = Position.C;
						} else {
							position = dist - 3;
							place = dest;
						} break;
					case 2: //strait
						if(dist < 2) {
							position = turn_tb[place.num][dist];
							place = Position.C;
						} else {
							position = dist - 2;
							place = dest;
						} break;
					case 3: //right
						if(dist < 1) {
							position = turn_tb[place.num][dist];
							place = Position.C;
						} else {
							position = dist - 1;
							place = dest;
						}
					}
				}
			}
			curr_state.add(new AgentState(i, position, place, dest, waiting_time, state.max_speed));
		}
		nextRound();
	}
	
	private BitSet getCenterPositions(Agent agent, AgentState state, int velocity) {
		BitSet res = new BitSet(4);
		int position;
		int len;
		if(state.place == Position.C) {
			position = state.position;
			len = velocity;
		} else {
			position = state.place.num;
			len = velocity - (position - length - 1);
			if(len >= 0) res.set(position);
		}
		while(len > 0 && position != (state.dest.num + 1) % 4) {
			len--;
			position = (position + 3) % 4;
			res.set(position);
		}
		return res;
	}
	
	protected void detectedCollision() {
		System.err.println("Kolizja!!!");
		//TODO better message
	}
	
	protected void finish() {
		System.out.println(sum_intersection_time / num_intersection_cross);
		System.out.println(max_messages + " " + min_messages + " " + (sum_messages / num_intersection_cross));
		latch.countDown();
	}
}
