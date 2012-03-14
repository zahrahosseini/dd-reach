package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.Set;

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;

import cn.edu.sjtu.stap.demand.rd.RdProgramPoint;
/**
 * HeavyDefFinder tries to use some heavy weight 
 * program analysis, which is probably based on
 * precise call stack/path enumeration, to precisely
 * exclude the definitions that are irrelevant to
 * the start point of queries (aka slicing criterion) 
 * 
 * @author ChengZhang
 *
 */
public class HeavyDefFinder implements DefFinder {

	@Override
	public Set<Unit> getDefs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAliasFinder(AliasFinder af) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBodies(Set<Body> bodies) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCallGraph(CallGraph cg) {
		// TODO Auto-generated method stub
		
	}

}
