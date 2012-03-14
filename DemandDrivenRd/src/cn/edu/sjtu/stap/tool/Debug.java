package cn.edu.sjtu.stap.tool;

public class Debug {
	public static boolean DEBUG = false;
	
	public static void P( Object o ) {
		if( DEBUG ) {
			System.out.println(o);
		}
	}
	
	/**
	 * Interesting method for logging in breakpoints :)
	 * 
	 * @param o
	 * @return
	 */
	public static boolean BPPrint(Object o){
		if( DEBUG ){
			System.out.println("DEBUG: " + o);
		}
		
		return (o != null && o.toString().contains("goto"));
	}
}
