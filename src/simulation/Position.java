package simulation;

public enum Position {
	N(0), E(1), S(2), W(3), C(-1);
	
	public final int num;
	
	private Position(int num) {
		this.num = num;
	}
	
	public static Position fromString(String c) {
		switch(c) {
		case "N": return N;
		case "E": return E;
		case "S": return S;
		case "W": return W;
		//case "C": return C;
		default: throw new IllegalArgumentException();
		}
	}
	
	public String toString() {
		switch(num) {
		case 0: return "N"; 
		case 1: return "E"; 
		case 2: return "S"; 
		case 3: return "W"; 
		case -1: return "C";
		default: return "?";
		}
	}
}
