package simulation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Intersection {
	private ArrayList<CenterPlace> places;
	private int len;
	
	public Intersection(int len) {
		this.len = len;
		places = new ArrayList<>();
		for(int i = 0; i < 4; ++i)
			places.add(new CenterPlace(i, len));
	}
	
	public List<CenterPlace> getPlaces() {
		return places;
	}
	
	//-infty < dist <= 4
	public CenterPlace getNext(CenterPlace p, int dist) {
		if(p == null)
			return null;
		//numbers grow in opposite direction that cars drive
		return places.get((p.id - dist + 4) % 4);
	}
	
	public CenterPlace getNext(CenterPlace p) {
		return getNext(p, 1);
	}
	
	public CenterPlace getPrev(CenterPlace p) {
		return getNext(p, -1);
	}
	
	//Returns list of places from begin to end inclusive (in order of car path)
	public List<CenterPlace> getPath(CenterPlace begin, CenterPlace end) {
		if(begin == null || end == null)
			return Collections.emptyList();
		ArrayList<CenterPlace> res = new ArrayList<>();
		for(int i = begin.id; i != end.id; i = (i + 3) % 4)
			res.add(places.get(i));
		res.add(end);
		return res;
	}
	
	public List<CenterPlace> getPath(CenterPlace begin, int length) {
		if(length <= 0) 
			return Collections.emptyList();
		return getPath(begin, getNext(begin, length - 1));
	}
	
	public List<CenterPlace> getPath(AgentState a) {
		return getPath(getFirstPlace(a), getLastPlace(a));
	}
	
	//first center place after leaving direction d
	public CenterPlace getFirstPlace(Direction d) {
		return places.get(d.num);
	}
	
	//last center place before entering direction d
	public CenterPlace getLastPlace(Direction d) {
		return places.get((d.num + 1) % 4);
	}
	
	public CenterPlace getLastPlace(AgentState a) {
		if(a.place == a.dest)
			return null;
		return getLastPlace(a.dest);
	}
	
	public CenterPlace getFirstPlace(AgentState a) {
		if(a.place == a.dest) 
			return null;
		if(a.place != Direction.C)
			return getFirstPlace(a.place);
		return getAgentPlace(a);
	}
	
	public CenterPlace getAgentPlace(AgentState a) {
		if(a.place != Direction.C)
			throw new IllegalArgumentException();
		return places.get(a.position);
	}
	
	public Integer distance(CenterPlace from, AgentState to) {
		if(from == null)
			return Integer.MAX_VALUE;
		if(to.place != Direction.C) return to.position + 1 + from.distanceTo(getLastPlace(to.place));
		return from.distanceTo(getAgentPlace(to));
	}
	
	public Integer distance(AgentState from, CenterPlace to) {
		if(to == null)
			return Integer.MAX_VALUE;
		if(from.place != Direction.C) return len - from.position + to.distanceFrom(getFirstPlace(from.place));
		return to.distanceFrom(getAgentPlace(from));
	}
	
	//Assumes to lies on the path of from
	public Integer distance(AgentState from, AgentState to) {
		if(to.place == Direction.C)
			return distance(from, getAgentPlace(to));
		if(from.place == Direction.C)
			return distance(getAgentPlace(from), to);
		if(to.place == from.place) {
			if(to.position - from.position < 0) return Integer.MAX_VALUE;
			return to.position - from.position;
		}
		if(getFirstPlace(from) == null) 
			return Integer.MAX_VALUE;
		return distance(from, getFirstPlace(from)) + distance(getFirstPlace(from), to);
	}
	
	public Boolean onTrajectory(CenterPlace p, AgentState car) {
		return car.place != car.dest && distance(car, p) <= distance(car, getLastPlace(car));
	}
	
	public Boolean trajectoriesIntersect(AgentState car1, AgentState car2) {
		//Parallel trajectories don't intersect
		if(isInFront(car1, car2) || isInFront(car2, car1))
			return false;
		//If trajectories intersect, then they intersect on first center position of one of the agents 
		//because agents drive in center in the same cyclic direction
		return onTrajectory(getFirstPlace(car1), car2) || onTrajectory(getFirstPlace(car2), car1);
	}
	
	public Boolean isStraitBehind(AgentState me, AgentState car) {
		return car.place != Direction.C && car.place == me.place && car.position > me.position;
	}
		
	public Boolean isInFront(AgentState me, AgentState car) {
		if(car.place == Direction.C) 
			return getPath(me).contains(places.get(car.position));
		if(me.place != car.place)
			return me.dest == car.place && car.place == car.dest;
		return ((me.place == me.dest) == (car.place == car.dest)) && me.position < car.position;
	}
}
