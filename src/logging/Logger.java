package logging;

public class Logger {
	public static final int DEBUG = 0;
	public static final int TRACE = 1;
	public static final int INFO = 2;
	public static final int ERROR = 3;
	public static final int BUG = 4;
	
	private static int logLevel = 3; 
	
	public static void setLogLevel(int logLevel) {
		Logger.logLevel = logLevel;
	}
	
	private String prefix;
	
	public Logger(Integer agentId) {
		prefix = "[Agent " + agentId + "] ";
	}
	
	public Logger(Class<?> clazz) {
		prefix = "[" + clazz.getSimpleName() + "] ";
	}
	
	public void _debug(String msg, Object ...objects) {
		if(logLevel <= DEBUG) {
			System.out.printf(prefix + "DEBUG: " + msg + "\n", objects);
		}
	}
	
	public void _trace(String msg, Object ...objects) {		
		if(logLevel <= TRACE) {
			System.out.printf(prefix + "TRACE: " + msg + "\n", objects);
		}
	}
	
	public void _info(String msg, Object ...objects) {
		if(logLevel <= INFO) {
			System.out.printf(prefix + "INFO: " + msg + "\n", objects);
		}
	}
	
	public void _error(String msg, Object ...objects) {
		if(logLevel <= ERROR) {
			System.err.printf(prefix + "ERROR: " + msg + "\n", objects);
			System.err.flush();
		}
	}
	
	public void _bug(String msg, Object ...objects) {
		if(logLevel <= BUG) {
			System.err.printf(prefix + "BUG: " + msg + "\n", objects);
			System.err.flush();
		}
	}
	
	public void debug(String msg) {
		_debug(msg);
	}
	
	public void trace(String msg) {
		_trace(msg);
	}
	
	public void info(String msg) {
		_info(msg);
	}
	
	public void error(String msg) {
		_error(msg);
	}
	
	public void bug(String msg) {
		_bug(msg);
	}
	
	public void debug(String msg, Object a1) {
		_debug(msg, a1);
	}
	
	public void trace(String msg, Object a1) {		
		_trace(msg, a1);
	}
	
	public void info(String msg, Object a1) {
		_info(msg, a1);
	}
	
	public void error(String msg, Object a1) {
		_error(msg, a1);
	}
	
	public void bug(String msg, Object a1) {
		_bug(msg, a1);
	}
	
	public void debug(String msg, Object a1, Object a2) {
		_debug(msg, a1, a2);
	}
	
	public void trace(String msg, Object a1, Object a2) {
		_trace(msg, a1, a2);
	}
	
	public void info(String msg, Object a1, Object a2) {
		_info(msg, a1, a2);
	}
	
	public void error(String msg, Object a1, Object a2) {
		_error(msg, a1, a2);
	}
	
	public void bug(String msg, Object a1, Object a2) {
		_bug(msg, a1, a2);
	}
	
	public void debug(String msg, Object a1, Object a2, Object a3) {
		_debug(msg, a1, a2, a3);
	}
	
	public void trace(String msg, Object a1, Object a2, Object a3) {
		_trace(msg, a1, a2, a3);
	}
	
	public void info(String msg, Object a1, Object a2, Object a3) {
		_info(msg, a1, a2, a3);
	}
	
	public void error(String msg, Object a1, Object a2, Object a3) {
		_error(msg, a1, a2, a3);
	}
	
	public void bug(String msg, Object a1, Object a2, Object a3) {
		_bug(msg, a1, a2, a3);
	}
}
