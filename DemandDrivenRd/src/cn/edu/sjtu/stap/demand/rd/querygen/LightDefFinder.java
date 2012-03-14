package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.Set;

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * LightDefFinder only performs some lightweight (TBD...)
 * analysis, attempt to exclude some definitions that
 * are irrelevant to the start point of the queries.
 * (can be also viewed as the slicing criterion)
 * 
 * @author ChengZhang
 *
 */
public class LightDefFinder implements DefFinder {

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
