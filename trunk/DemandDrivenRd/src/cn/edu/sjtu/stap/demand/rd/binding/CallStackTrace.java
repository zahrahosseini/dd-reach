package cn.edu.sjtu.stap.demand.rd.binding;

import java.util.List;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * An instance of this class represents a trace of
 * call stacks from the source node to the target node.
 * For example, for the call sequence:
 * main -> A -> B <- B <- A -> C
 * with source, B, and target, C.
 * 
 * We record:
 * backward trace: B <- A <- main
 * and forward trace: main -> C
 * and the common ancester, main
 * 
 * Using the info contained in this instance, we can calculate
 * the parameter binding (of reference parameters) with a certion
 * degree of precison (without considering the positions of callsites
 * in their CFGs).
 * 
 * @author ChengZhang
 *
 */
public class CallStackTrace {
	private List<Edge> backwardTrace;
	private List<Edge> forwardTrace;
	private SootMethod commonAncester;

	public List<Edge> getBackwardTrace() {
		return backwardTrace;
	}
	public void setBackwardTrace(List<Edge> backwardTrace) {
		this.backwardTrace = backwardTrace;
	}
	public List<Edge> getForwardTrace() {
		return forwardTrace;
	}
	public void setForwardTrace(List<Edge> forwardTrace) {
		this.forwardTrace = forwardTrace;
	}
	public SootMethod getCommonAncester() {
		return commonAncester;
	}
	public void setCommonAncester(SootMethod commonAncester) {
		this.commonAncester = commonAncester;
	}
	
	public String toString() {
		return "back: "+this.backwardTrace.toString()+"\nforward: "+this.forwardTrace.toString()+"\n";
		
	}
}
