package cn.edu.sjtu.stap.tool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CallGraphDumper {
	
	private CallGraph cg = null;
	private PrintStream ps = null;
	private Set<Edge> edgeSet = null;
	
	private static CallGraphDumper instance = null;
	
	public static CallGraphDumper v() {
		if( instance == null ) {
			instance = new CallGraphDumper();
		}
		return instance;
	}
	
	private CallGraphDumper() {
		
	}
	
	public void init( CallGraph cg, String fileName ) {
		this.cg = cg;
		this.edgeSet = new HashSet<Edge>();
		try {
			ps=new PrintStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void exploreEdge( Edge e ) {
		if( !edgeSet.contains(e) ) {
			ps.println(e);
			edgeSet.add(e);
		}
	}
	
}
