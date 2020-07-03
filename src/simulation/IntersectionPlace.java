package simulation;

import java.util.ArrayList;

public class IntersectionPlace implements Comparable<IntersectionPlace> {
	public final int id;
	public Integer distance;
	public ArrayList<Integer> agents;
	public ArrayList<Integer> future_agents;
	public float expect_wait_time;
	public float expect_tot_time;
	
	public IntersectionPlace(int id, int len) {
		this.id = id;
		this.agents = new ArrayList<Integer>();
		for(int i = 0; i < len; ++i) future_agents.add(0);
		this.distance = null;
	}

	@Override
	public int compareTo(IntersectionPlace p) {
		//Distance ascending
		return this.distance - p.distance;
	}
}
