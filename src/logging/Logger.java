package logging;

public class Logger {
	public static final int DEBUG = 0;
	public static final int TRACE = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	public static final int BUG = 4;
	//DEBUGGING
	private static int logLevel = 0; 
	
	public static void setLogLevel(int logLevel) {
		Logger.logLevel = logLevel;
	}
	
	private static Object printMutex = new Object();
	private String prefix;
	
	public Logger(Integer agentId) {
		prefix = "[Agent " + agentId + "] ";
	}
	
	public Logger(Class<?> clazz) {
		prefix = "[" + clazz.getSimpleName() + "] ";
	}
	
	public void _debug(String msg, Object ...objects) {
		if(logLevel <= DEBUG) {
			synchronized (printMutex) { 
				System.out.printf(prefix + "DEBUG: " + msg + "\n", objects);
				System.out.flush();
			}
		}
	}
	
	public void _trace(String msg, Object ...objects) {		
		if(logLevel <= TRACE) {
			synchronized (printMutex) { 
				System.out.printf(prefix + "TRACE: " + msg + "\n", objects);
				System.out.flush();
			}
		}
	}
	
	public void _warning(String msg, Object ...objects) {
		if(logLevel <= WARNING) {
			synchronized (printMutex) { 
				System.out.printf(prefix + "WARNING: " + msg + "\n", objects);
				System.out.flush();
			}
		}
	}
	
	public void _error(String msg, Object ...objects) {
		if(logLevel <= ERROR) {
			synchronized (printMutex) { 
				System.err.printf(prefix + "ERROR: " + msg + "\n", objects);
				System.err.flush();
				//TODO: needed for stdout and stderr synchronization, later think about sth better
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void _bug(String msg, Object ...objects) {
		if(logLevel <= BUG) {
			synchronized (printMutex) { 
				System.err.printf(prefix + "BUG: " + msg + "\n", objects);
				System.err.flush();
				//TODO: needed for stdout and stderr synchronization, later think about sth better
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void debug(String msg) {
		_debug(msg);
	}
	
	public void trace(String msg) {
		_trace(msg);
	}
	
	public void warning(String msg) {
		_warning(msg);
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
	
	public void warning(String msg, Object a1) {
		_warning(msg, a1);
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
	
	public void warning(String msg, Object a1, Object a2) {
		_warning(msg, a1, a2);
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
	
	public void warning(String msg, Object a1, Object a2, Object a3) {
		_warning(msg, a1, a2, a3);
	}
	
	public void error(String msg, Object a1, Object a2, Object a3) {
		_error(msg, a1, a2, a3);
	}
	
	public void bug(String msg, Object a1, Object a2, Object a3) {
		_bug(msg, a1, a2, a3);
	}
}
