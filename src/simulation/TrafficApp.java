package simulation;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import com.ibm.able.AbleException;

import logging.Logger;
import simulation.agents.*;

public class TrafficApp {

	public static void main(String[] args) throws AbleException {
		int log_level = Integer.parseInt(System.getProperty("LogLevel", "3"));
		Logger.setLogLevel(log_level);
		try	{
			Scanner scanner = new Scanner(System.in);
			ArrayList<Agent> agents = new ArrayList<Agent>();
			int length = scanner.nextInt();
			int n_agents = scanner.nextInt();
			int n_steps = scanner.nextInt();
			for(int i = 0; i < n_agents; ++i) {
				int t_start = scanner.nextInt();
				String from = scanner.next();
				int t_from = scanner.nextInt();
				String dest = scanner.next();
				int t_dest = scanner.nextInt();
				String s_haste = scanner.next();
				int haste = 0;
				boolean random_haste = s_haste.equals("?");
				if(!random_haste) haste = Integer.parseInt(s_haste); 
				String type = scanner.next(); //TODO
				int max_speed = scanner.nextInt();
				int points = scanner.nextInt();
				Agent agent;
				switch(type) {
				case "_":
					agent = new Agent(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				case "R":
					agent = new Righteous(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				case "H":
					agent = new Hoarder(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				case "N":
					agent = new Nervous(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				case "F":
					agent = new Frustrated(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				case "A":
					agent = new Altruistic(length, i, t_start, Direction.fromString(from), Direction.fromString(dest), 
							t_from, t_dest, haste, random_haste, max_speed, points);
					break;
				default:
					throw new IllegalArgumentException("Unknown agent type: " + type);
				}
				agents.add(agent);
			}
			scanner.close();
			CountDownLatch latch = new CountDownLatch(1);
			Simulation sim = new Simulation(length, n_steps, agents, latch);
			for(int i = 0; i < n_agents; ++i) {
				Agent a1 = agents.get(i);
				sim.addAbleEventListener(a1);
				a1.addAbleEventListener(sim);
				for(int j = 0; j < n_agents; ++j)
					if(j != i) a1.addAbleEventListener(agents.get(j));
			}
			//Start simulation
			sim.nextRound();
			latch.await();
			for(Agent a: agents) {
				a.quitAll();
			}
			sim.quitAll();
		} catch (AbleException ae){
			if (ae.getExceptions() == null)
				System.err.println(ae.getLocalizedMessage());
			else
				for (Object o : ae.getExceptions()){
					Exception e = (Exception) o;
					System.err.println(e.getLocalizedMessage());
				}
		} catch (Exception e){
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

}