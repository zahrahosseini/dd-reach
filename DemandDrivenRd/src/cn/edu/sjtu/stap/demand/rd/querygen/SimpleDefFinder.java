package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Body;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;

/**
 * SimpleDefFinder just find out all the definitions
 * in the program, no matter what variables are defined
 * in the statements.
 * 
 * @author ChengZhang
 *
 */
public class SimpleDefFinder implements DefFinder {
	protected Set<Body> bodies;
	protected CallGraph cg;
	
	@Override
	public Set<Unit> getDefs() {
		Set<Unit> defs = new HashSet<Unit>();
		
		if(bodies != null){
			for(Body b : bodies){
				Chain<Unit> unitChain = b.getUnits();
				Iterator<Unit> uIte = unitChain.iterator();
				while(uIte.hasNext()){
					Unit u = uIte.next();
					if(u instanceof AssignStmt){
						defs.add(u);
					}
				}
			}
		}
		
		return defs;
	}

	@Override
	public void setAliasFinder(AliasFinder af) {
		// As no alias info is needed in this SimpleDefFinder, 
		// this method actually does nothing.
	}

	@Override
	public void setBodies(Set<Body> bodies) {
		this.bodies = bodies;
	}

	@Override
	public void setCallGraph(CallGraph cg) {
		this.cg = cg;
	}

}
