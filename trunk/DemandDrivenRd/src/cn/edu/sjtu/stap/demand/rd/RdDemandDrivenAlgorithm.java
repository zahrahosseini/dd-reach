package cn.edu.sjtu.stap.demand.rd;

import java.util.*;

import cn.edu.sjtu.stap.demand.rd.binding.DataflowBinder;

import soot.SootMethod;
import soot.Unit;

public class RdDemandDrivenAlgorithm {
	private static Set<RdValue> EMPTYSET = new HashSet<RdValue>();
	
	private Set<Pair<RdValue,RdProgramPoint>> worklist = null;
	private Map<RdProgramPoint,Set<RdValue>> query = null;
	private Map<Pair<RdValue,RdProgramPoint>,Boolean> KILL = null;
	private Map<Pair<RdValue,RdProgramPoint>,Boolean> GEN = null;
	private Set<Pair<RdValue,RdProgramPoint>> visited = null;
	private RdProgramPoint targetDef = null;
	Set<Pair<RdValue,RdProgramPoint>> summaryWorklist = null;
	
	
	/**
	 * This constructor just allocates empty containers objects
	 */
	public RdDemandDrivenAlgorithm() {
		this.visited = new HashSet<Pair<RdValue,RdProgramPoint>>();
		this.KILL = new HashMap<Pair<RdValue,RdProgramPoint>,Boolean>();
		this.GEN = new HashMap<Pair<RdValue,RdProgramPoint>,Boolean>();
		this.summaryWorklist = new HashSet<Pair<RdValue,RdProgramPoint>>();
	}
	
	public boolean QueryGenKill( RdProgramPoint definition, RdProgramPoint startPoint, RdValue startValue ) {
		this.worklist = new HashSet<Pair<RdValue,RdProgramPoint>>();
		this.query = new HashMap<RdProgramPoint,Set<RdValue>>();
		this.targetDef = definition;
		
		addQuery(startPoint,startValue);
		worklist.add(new Pair<RdValue,RdProgramPoint>(startValue,startPoint));
		
		while( !worklist.isEmpty() ) {
			// get an element from work list
			Pair<RdValue,RdProgramPoint> pair = worklist.iterator().next();
			worklist.remove(pair);
			RdValue y = pair.data1; // value
			RdProgramPoint m = pair.data2; // program point
			
			if( RdSystemTool.v().isMainEntry(m) ) {
				Set<RdValue> vs = getQuery(m);
				if( !vs.isEmpty() ) {
					continue;
				}
			} else if( m.isEntry() ) { // m is entry of procedure p
				SootMethod p = m.getMethod();
				Set<RdValue> tempSet =  new HashSet<RdValue>();
				tempSet.add(y);
				
				for( RdProgramPoint callsite : RdSystemTool.v().getCallSites(p) ) {
					Set<RdValue> reverseSet = reverseFunction(callsite, tempSet, p);
					if( !reverseSet.isEmpty() ) {
						if( !getQuery(callsite).containsAll(reverseSet) ) {
							addQuery(callsite,reverseSet);
							
							for( RdValue value : reverseSet ) {
								worklist.add(new Pair<RdValue,RdProgramPoint>(value,callsite));
							}
						}
					}
				}
			} else { // otherwise
				for( RdProgramPoint pred : m.getPreds() ) {
					SootMethod q = pred.getCallMethod();
					boolean killy,geny;
					
					if( q != null ) { // pred is a call site
						// kill and gen of call site are described by the value of method entry  
						computeGenAndKill(y,q);
						killy = getKill(new Pair<RdValue,RdProgramPoint>(y,RdSystemTool.v().getEntry(q)));
						geny = getGen(new Pair<RdValue,RdProgramPoint>(y,RdSystemTool.v().getEntry(q)));
					} else {
						killy = pred.kill(y, targetDef);
						geny = pred.gen(y, targetDef);
					}
					
					if( geny ) {
						return true;
					}
					if( !killy ) {
						if( !getQuery(pred).contains(y) ) {
							addQuery(pred,y);
							worklist.add(new Pair<RdValue,RdProgramPoint>(y,pred));
						}
					}
				}
			}
			
		}
		
		return false;
	}
	
	public void computeGenAndKill( RdValue value, SootMethod p ) {
		RdProgramPoint pexit = RdSystemTool.v().getExit(p);
		RdProgramPoint pentry = RdSystemTool.v().getEntry(p);
		
		if( visited(value,pentry) ) {
			return; // result previously computed
		}
		
		Pair<RdValue,RdProgramPoint> key = new Pair<RdValue,RdProgramPoint>(value,pexit);
		summaryWorklist.add(key);
		this.GEN.put(key, false);
		this.KILL.put(key, false);
		
		while( !summaryWorklist.isEmpty() ) {
			// remove an element from list
			key = summaryWorklist.iterator().next();
			summaryWorklist.remove(key);
			RdValue y = key.data1;
			RdProgramPoint n = key.data2;
			visited.add(new Pair<RdValue,RdProgramPoint>(y,n));
			SootMethod r = n.getCallMethod();
			
			if( r != null ) { // r is a call site
				RdProgramPoint rentry = RdSystemTool.v().getEntry(r);
				
				if( visited(y,rentry) ) {
					for( RdProgramPoint pred : n.getPreds() ) {
						propagate(value,pred,
								getKill(new Pair(y,n)) || getKill(new Pair(y,rentry)),
								getGen(new Pair(y,n)) || (getGen(new Pair(y,rentry)) && !getKill(new Pair(y,n))));
					}
				} else { // trigger computation of summaries for r
					RdProgramPoint rexit = RdSystemTool.v().getExit(r);
					Pair tempkey = new Pair(y,rexit);
					GEN.put(tempkey, false);
					KILL.put(tempkey, false);
					summaryWorklist.add(tempkey);
				}
			} else if( n.isEntry() ) { // propagate to call site if needed
				SootMethod q = n.getMethod();
				
				for( RdProgramPoint callsite : RdSystemTool.v().getCallSites(q) ) {
					if( visited( y,callsite ) ) { 
						RdProgramPoint qentry = RdSystemTool.v().getEntry(q);
						for( RdProgramPoint pred : callsite.getPreds() ) {
							// TODO: may be an error in paper, it should be qentry instead of pentry
							propagate( y,pred,
									getKill(new Pair(y,callsite))||getKill(new Pair(y,qentry)),
									getGen(new Pair(y,callsite))||(getGen(new Pair(y,qentry))&&!getKill(new Pair(y,callsite))));
						}
					}
				}
			} else {
				for( RdProgramPoint pred : n.getPreds() ) {
					propagate(y,pred,
							getKill(new Pair(y,n)) || n.kill(y,targetDef),
							getGen(new Pair(y,n)) || (n.gen(y,targetDef) && !getKill(new Pair(y,n)) ) );
				}
			}
		}
		
		return;
	}
	
	private void propagate( RdValue value, RdProgramPoint pp, boolean newkill, boolean newgen  ) {
		Pair<RdValue,RdProgramPoint> tempkey = new Pair<RdValue,RdProgramPoint>(value,pp);
		
		Boolean oldkill = getKill(tempkey);
		Boolean oldgen = getGen(tempkey);
		
		boolean tempkill = oldkill.booleanValue() && newkill;
		boolean tempgen = oldgen.booleanValue() || newgen;
		
		this.KILL.put(tempkey, tempkill);
		this.GEN.put(tempkey, tempgen);
		
		if( (oldkill.booleanValue() != tempkill ) || (oldgen.booleanValue() != tempgen) ) {
			summaryWorklist.add(new Pair(value,pp));
		}
	}
	
	public Set<RdValue> reverseFunction( RdProgramPoint cs, Set<RdValue> valueSet, SootMethod callee ) {
		Set<RdValue> result = new HashSet<RdValue>();
		Unit callSite = cs.getUnit();
		SootMethod caller = cs.getMethod();
		
		for( RdValue value : valueSet ) {
			Set<RdValue> tempRes = DataflowBinder.flowElemReverseBinding(value, callee, callSite, caller);
			if( tempRes != null ) {
				result.addAll(tempRes); // union the result
			}
		}
		
		return result;
	}
	
	private boolean visited( RdValue value, RdProgramPoint pp ) {
		return visited.contains(new Pair<RdValue,RdProgramPoint>(value,pp));
	}
	
	private boolean getKill( Pair<RdValue,RdProgramPoint> pair ) {
		Boolean result = this.KILL.get(pair);
		if( result == null ) {
			return true;
		}
		return result.booleanValue();
	}
	
	private boolean getGen( Pair<RdValue,RdProgramPoint> pair ) {
		Boolean result = this.GEN.get(pair);
		if( result == null ) {
			return false;
		}
		return result.booleanValue();
	}
	
	private void addQuery( RdProgramPoint pp, Set<RdValue> vs ) {
		Set<RdValue> result = this.query.get(pp);
		if( result == null ) {
			result = new HashSet<RdValue>();
		}
		result.addAll(vs);
		this.query.put(pp, result);
	}
	
	private void addQuery( RdProgramPoint pp, RdValue value ) {
		Set<RdValue> result = this.query.get(pp);
		if( result == null ) {
			result = new HashSet<RdValue>();
		}
		result.add(value);
		this.query.put(pp, result);
	}
	
	private Set<RdValue> getQuery( RdProgramPoint pp ) {
		Set<RdValue> result = this.query.get(pp);
		if( result == null ) {
			result = EMPTYSET;
		}
		return result;
	}
	
}


//package cn.edu.sjtu.stap.autolog.demand.rd;
//
//import soot.Unit;
//import java.util.*;
//
//import cn.edu.sjtu.stap.autolog.demand.rd.binding.DataflowBinder;
//import cn.edu.sjtu.stap.autolog.incrementalslicing.visual.Debug;
//import soot.SootMethod;
//
//
///* TODO: list
// * 1) parameter binding by value 
// * 2) propagate value instead of definition
// * 3) kill and gen function in RdProgramPoint
// */
//public class RdDemandDrivenAlgorithm {
//	private Set<Pair<RdProgramPoint,RdProgramPoint>> worklist = null; // pair <definition, program point>
//	private Map<RdProgramPoint,RdFlowSet> query = null;
//	private Map<Pair<RdProgramPoint,RdProgramPoint>,Boolean> KILL = null; // map <definition, program point> to boolean
//	private Map<Pair<RdProgramPoint,RdProgramPoint>,Boolean> GEN = null;
//	private Set<Pair<RdProgramPoint,RdProgramPoint>> visited = null; // <definition,program point>
//	Set<Pair<RdProgramPoint,RdProgramPoint>> summaryWorklist = null;
//	
//	/**
//	 * This constructor just allocates empty containers objects
//	 */
//	public RdDemandDrivenAlgorithm() {
//		this.KILL = new HashMap<Pair<RdProgramPoint,RdProgramPoint>,Boolean>();
//		this.GEN = new HashMap<Pair<RdProgramPoint,RdProgramPoint>,Boolean>();
//		this.visited = new HashSet<Pair<RdProgramPoint,RdProgramPoint>>();
//		this.summaryWorklist = new HashSet<Pair<RdProgramPoint,RdProgramPoint>>();
//	}
//	
//	public boolean QueryGenKill( RdProgramPoint d, RdProgramPoint n ) {
//		worklist = new HashSet<Pair<RdProgramPoint,RdProgramPoint>>();
//		query = new HashMap<RdProgramPoint,RdFlowSet>();
//		
//		addQuery(n,d);
//		worklist.add(new Pair<RdProgramPoint,RdProgramPoint>(d,n));
//		
//		while( !worklist.isEmpty() ) {
//			// get an element from worklist
//			Pair<RdProgramPoint,RdProgramPoint> pair = worklist.iterator().next();
//			worklist.remove(pair);
//			Debug.P("DEBUG: about to process query: <" + pair.data1.getUnit() + ", " + pair.data2.getUnit()+ ">");
//			RdProgramPoint y = pair.data1; // definition
//			RdProgramPoint m = pair.data2; // program point
//			
//			if( RdSystemTool.v().isMainEntry(m) ) {
//				RdFlowSet fs = getQuery(m);
//				if( !fs.equals(RdFlowSet.EMPTYSET) ) {
//					// FIXME: union problem, different from the algorithm in paper
//					// return false;
//					continue;
//				}
//			} else if( m.isEntry() ) { // m is entry of procedure p
//				SootMethod p = m.getMethod();
//				RdFlowSet tempSet = new RdFlowSet();
//				tempSet.add(y);
//				
//				for( RdProgramPoint callsite : RdSystemTool.v().getCallSites(p)  ) {
//					// FIXME: this is about parameter binding, not reverse function.
//					// FIXME: we may have to add the treatment to virtual calls here!
//					RdFlowSet reverseSet = reverseFunction(callsite, tempSet, p); 
//					if( !reverseSet.equals(RdFlowSet.EMPTYSET) ){ 
//						if( !getQuery(callsite).include(reverseSet) ) {
//							addQuery(callsite,reverseSet);
//							
//							for( RdProgramPoint element : reverseSet.getAllData() ) {
//								worklist.add(new Pair<RdProgramPoint,RdProgramPoint>(element,callsite));
//							}
//						}
//					}
//				}
//			} else { // otherwise
//				for( RdProgramPoint pred : m.getPreds() ) {
//					SootMethod q = pred.getCallMethod();
//					boolean killy,geny;
//					
//					if( q != null ) { // pred is a call site
//						computeGenAndKill(y,q);
//						// FIXME: kill and gen of call site are described by the value of method entry  
//						killy = getKill(new Pair<RdProgramPoint,RdProgramPoint>(y,RdSystemTool.v().getEntry(q)));
//						geny = getGen(new Pair<RdProgramPoint,RdProgramPoint>(y,RdSystemTool.v().getEntry(q)));
//					} else {
//						killy = pred.kill(y);
//						geny = pred.gen(y);
//					}
//					
//					if( geny ) {
//						return true;
//					}
//					if( !killy ) {
//						if( !getQuery(pred).contains(y) ) {
//							addQuery(pred,y);
//							worklist.add(new Pair<RdProgramPoint,RdProgramPoint>(y,pred));
//						}
//					}
//					
//					// FIXME: union problem, different from the algorithm in paper
//					/*
//					if( killy ) {
//						return false;
//					} 
//					if( !geny ) {
//						if( !getQuery(pred).contains(y) ) {
//							addQuery(pred,y);
//							worklist.add(new Pair<RdProgramPoint,RdProgramPoint>(y,pred));
//						}
//					}*/
//				}
//			}
//			
//		}
//		// FIXME: union problem, different from the algorithm in paper
//		return false;
//		//return true;
//	}
//	
//	public void computeGenAndKill( RdProgramPoint def, SootMethod p ) {
//		RdProgramPoint pexit = RdSystemTool.v().getExit(p);
//		RdProgramPoint pentry = RdSystemTool.v().getEntry(p);
//		
//		if( visited(def,pentry) ) {
//			return; // result previously computed
//		}
//		Pair<RdProgramPoint,RdProgramPoint> key = new Pair<RdProgramPoint,RdProgramPoint>(def,pexit);
//		summaryWorklist.add(key);
//		this.GEN.put(key, false);
//		this.KILL.put(key, false);
//		
//		while( !summaryWorklist.isEmpty() ) {
//			// remove an element from list
//			key = summaryWorklist.iterator().next();
//			summaryWorklist.remove(key);
//			RdProgramPoint y = key.data1;
//			RdProgramPoint n = key.data2;
//			visited.add(new Pair<RdProgramPoint,RdProgramPoint>(y,n));
//			Debug.P("DEBUG: compute summary: " + key );
//			SootMethod r = n.getCallMethod();
//			
//			if( r != null ) { // r is a call site
//				RdProgramPoint rentry = RdSystemTool.v().getEntry(r);
//				
//				if( visited( y,rentry ) ) {
//					for( RdProgramPoint pred : n.getPreds() ) {
//						propagate(def,pred,
//								getKill(new Pair(y,n)) || getKill(new Pair(y,rentry)),
//								getGen(new Pair(y,n)) || (getGen(new Pair(y,rentry)) && !getKill(new Pair(y,n))));
//					}
//				} else { // trigger computation of summaries for r
//					RdProgramPoint rexit = RdSystemTool.v().getExit(r);
//					Pair tempkey = new Pair(y,rexit);
//					GEN.put(tempkey, false);
//					KILL.put(tempkey, false);
//					summaryWorklist.add(tempkey);
//				}
//			} else if( n.isEntry() ) { // propagate to call sites if needed
//				SootMethod q = n.getMethod();
//				
//				for( RdProgramPoint callsite : RdSystemTool.v().getCallSites(q) ) {
//					if( visited( y,callsite ) ) { // FIXME: why should y, call site be visited
//						for( RdProgramPoint pred : callsite.getPreds() ) {
//							propagate( y,pred,
//									getKill(new Pair(y,callsite))||getKill(new Pair(y,pentry)),
//									getGen(new Pair(y,callsite))||(getGen(new Pair(y,pentry))&&!getKill(new Pair(y,callsite))));
//						}
//					}
//				}
//			} else {
//				for( RdProgramPoint pred : n.getPreds() ) {
//					propagate(y,pred,
//							getKill(new Pair(y,n)) || n.kill(y),
//							getGen(new Pair(y,n)) || (n.gen(y) && !getKill(new Pair(y,n)) ) );
//				}
//			}
//		}
//		
//		
//		return;
//	}
//	
//	private boolean getKill( Pair<RdProgramPoint,RdProgramPoint> pair ) {
//		Boolean result = this.KILL.get(pair);
//		if( result == null ) {
//			return true;
//		}
//		return result.booleanValue();
//	}
//	
//	private boolean getGen( Pair<RdProgramPoint,RdProgramPoint> pair ) {
//		Boolean result = this.GEN.get(pair);
//		if( result == null ) {
//			return false;
//		}
//		return result.booleanValue();
//	}
//	
//	private void propagate( RdProgramPoint def, RdProgramPoint pp, boolean newkill, boolean newgen ) {
//		Pair tempkey = new Pair(def,pp);
//		
//		Boolean oldkill = getKill(tempkey);
//		Boolean oldgen = getGen(tempkey);
//		
//		//TODO:union problem 
//		boolean tempkill = oldkill.booleanValue() && newkill;
//		boolean tempgen = oldgen.booleanValue() || newgen;
//		
//		this.KILL.put(tempkey, tempkill);
//		this.GEN.put(tempkey, tempgen);
//		
//		if( (oldkill.booleanValue() != tempkill) || (oldgen.booleanValue() != tempgen) ) {
//			summaryWorklist.add(new Pair(def,pp));
//		}
//	}
//	
//	private boolean visited( RdProgramPoint def, RdProgramPoint pp ) {
//		return visited.contains(new Pair<RdProgramPoint,RdProgramPoint>(def,pp));
//	}
//	
//	public RdFlowSet reverseFunction(RdProgramPoint pp, RdFlowSet flowSet, SootMethod callee) {
//		RdFlowSet results = new RdFlowSet();
//		Unit callSite = pp.getUnit();
//		SootMethod caller = pp.getMethod();
//		
//		for(RdProgramPoint tmpPP : flowSet.getAllData()){
//			RdFlowSet tmpRes = DataflowBinder.flowElemReverseBinding(tmpPP, callee, callSite, caller);
//			if(tmpRes != null){
//				results.union(tmpRes);
//			}
//		}
//		
//		return results;
//	}
//	
//	private RdFlowSet getQuery( RdProgramPoint pp ) {
//		RdFlowSet result = this.query.get(pp);
//		if( result == null ) {
//			result = RdFlowSet.EMPTYSET;
//		}
//		return result;
//	}
//	
//	private void addQuery( RdProgramPoint pp, RdFlowSet fs ) {
//		RdFlowSet result = this.query.get(pp);
//		if( result == null ) {
//			result = new RdFlowSet();
//		}
//		Set<RdProgramPoint> toAdd = fs.getAllData();
//		for( RdProgramPoint element : toAdd ) {
//			result.add(element);
//		}
//		this.query.put(pp, result);
//	}
//	
//	private void addQuery( RdProgramPoint pp, RdProgramPoint definition ) {
//		RdFlowSet result = this.query.get(pp);
//		if( result == null ) {
//			result = new RdFlowSet();
//		}
//		result.add(definition);
//		this.query.put(pp, result);
//	}
//	
//}


class Pair<T1,T2> {
	T1 data1 = null;
	T2 data2 = null;
	
	public Pair( T1 data1, T2 data2 ) {
		this.data1 = data1;
		this.data2 = data2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data1 == null) ? 0 : data1.hashCode());
		result = prime * result + ((data2 == null) ? 0 : data2.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return "Pair<" + data1 +", " + data2 +">";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (data1 == null) {
			if (other.data1 != null)
				return false;
		} else if (!data1.equals(other.data1))
			return false;
		if (data2 == null) {
			if (other.data2 != null)
				return false;
		} else if (!data2.equals(other.data2))
			return false;
		return true;
	}
	
	
}