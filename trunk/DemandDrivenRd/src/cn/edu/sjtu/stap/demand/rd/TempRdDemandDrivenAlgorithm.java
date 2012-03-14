package cn.edu.sjtu.stap.demand.rd;

import java.util.*;

import cn.edu.sjtu.stap.demand.rd.binding.DataflowBinder;

import soot.SootMethod;
import soot.Unit;

public class TempRdDemandDrivenAlgorithm {
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
	public TempRdDemandDrivenAlgorithm() {
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
						for( RdProgramPoint pred : callsite.getPreds() ) {
							propagate( y,pred,
									getKill(new Pair(y,callsite))||getKill(new Pair(y,pentry)),
									getGen(new Pair(y,callsite))||(getGen(new Pair(y,pentry))&&!getKill(new Pair(y,callsite))));
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
			return true;
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
