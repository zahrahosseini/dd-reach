package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.Set;

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * In order to generate queries, we first have to find
 * the (relevant) definitions in the program. 
 * 
 * @author ChengZhang
 *
 */
public interface DefFinder {
	public Set<Unit> getDefs();
	public void setAliasFinder(AliasFinder af);
	public void setBodies(Set<Body> bodies);
	public void setCallGraph(CallGraph cg);
}	
