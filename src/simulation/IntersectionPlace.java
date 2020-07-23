package simulation;

import java.util.List;
import java.util.ArrayList;

public class IntersectionPlace implements Comparable<IntersectionPlace> {
	public final int id;
	public Integer distance;
	public List<Integer> agents;
	public List<Integer> future_agents;
	public float expect_wait_time;
	public float expect_tot_time;
	public Integer best_offer = null;
	public Integer best_offer_id = null;
	public Integer best_offer_speed = null;
	
	public IntersectionPlace(int id, int len) {
		this.id = id;
		this.agents = new ArrayList<Integer>();
		this.future_agents = new ArrayList<Integer>();
		for(int i = 0; i < len; ++i) {
			future_agents.add(0);
		}
		this.distance = null;
	}

	@Override
	public int compareTo(IntersectionPlace p) {
		//Distance ascending
		return this.distance - p.distance;
	}

	@Override
	public String toString() {
		return "IntersectionPlace [id=" + id + ", distance=" + distance + "]";
	}
	
	public Boolean onPathOf(Integer agent_id) {
		return agents.contains(agent_id);
	}
}
