package cn.edu.sjtu.stap.tool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CallGraphDumper {
	
	private CallGraph cg = null;
	private PrintStream ps = null;
	private PrintStream mapFile = null;
	private Set<Edge> edgeSet = null;
	private Map<SootMethod,Integer> method2Index;
	
	
	private static CallGraphDumper instance = null;
	
	public static CallGraphDumper v() {
		if( instance == null ) {
			instance = new CallGraphDumper();
			counter = 0;
		}
		return instance;
	}
	
	private CallGraphDumper() {
		
	}
	
	public void init( CallGraph cg, String fileName ) {
		this.cg = cg;
		this.edgeSet = new HashSet<Edge>();
		this.method2Index = new HashMap<SootMethod, Integer>();
		try {
			ps = new PrintStream(new FileOutputStream(fileName));
			mapFile = new PrintStream(new FileOutputStream(fileName+"_index"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static int edgeCounter = 0;
	private static int counter = 0;
	private Integer addMethod( SootMethod smethod ) {
		this.method2Index.put(smethod, counter);
		mapFile.println(counter+" : "+smethod.getSignature());
		counter++;
		return counter-1;
	}
	
	public void exploreEdge( Edge e ) {
		if( edgeCounter >= 1000 ) {
			//throw new RuntimeException("1000 edges recorded");
		}
		
		SootMethod src = e.src();
		SootMethod tgt = e.tgt();
		Integer srcIndex = this.method2Index.get(src);
		Integer tgtIndex = this.method2Index.get(tgt);
		if( srcIndex == null ) {
			srcIndex = this.addMethod(src);
		}
		
		if( tgtIndex == null ) {
			tgtIndex = this.addMethod(tgt);
		}
		
		ps.println(srcIndex + " -> " + tgtIndex);
		edgeCounter++;
		
//		if( !edgeSet.contains(e) ) {
//			ps.println(srcIndex + " -> " + tgtIndex);
//			edgeSet.add(e);
//		}
	}
	
}
