package simulation;

import java.util.List;
import java.util.ArrayList;

public class CenterPlace {
	public final Integer id;
	public List<Integer> agents;
	public List<Integer> future_agents;

	public CenterPlace(int id, int len) {
		this.id = id;
		this.agents = new ArrayList<Integer>();
		this.future_agents = new ArrayList<Integer>();
		for(int i = 0; i < len; ++i) {
			future_agents.add(0);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(obj instanceof CenterPlace)
			return this.id == ((CenterPlace) obj).id;
		return false;
	}

	@Override
	public String toString() {
		return "CenterPlace [id=" + id + "]";
	}
	
	public Boolean onPathOf(Integer agent_id) {
		return agents.contains(agent_id);
	}
	
	public Integer distanceFrom(CenterPlace p) {
		return (p.id - this.id + 4) % 4;
	}
	
	public Integer distanceTo(CenterPlace p) {
		return (this.id - p.id + 4) % 4;
	}
	
	public Integer getId() {
		return id;
	}
	
	public List<Integer> getAgents() {
		return agents;
	}
	
	public List<Integer> getFutureAgents() {
		return future_agents;
	}
}
