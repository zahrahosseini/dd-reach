package cn.edu.sjtu.stap.demand.rd;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import cn.edu.sjtu.stap.tool.Debug;

public class RdSystemTool {
	// a global map between a method and its (unique) intra-procedural CFG
	private Map<SootMethod, UnitGraph> m2g;
	private static Set<String> commonImmutableTypes = new HashSet<String>();
	static{
		commonImmutableTypes.add("java.lang.Void");
		commonImmutableTypes.add("java.lang.String");
		commonImmutableTypes.add("java.lang.Boolean");
		commonImmutableTypes.add("java.lang.Number");
		commonImmutableTypes.add("java.lang.Byte");
		commonImmutableTypes.add("java.lang.Character");
		commonImmutableTypes.add("java.lang.Short");
		commonImmutableTypes.add("java.lang.Integer");
		commonImmutableTypes.add("java.lang.Long");
		commonImmutableTypes.add("java.lang.Float");
		commonImmutableTypes.add("java.lang.Double");
	}
	
	// Because there can be multiple callees for a specific call site unit,
	// we have to use a Pair to represent a call site here
	private Map<Pair<Unit, SootMethod>, Map<RdValue, Set<Integer>>> callSite2binding;
	
	public void addParamBinding(Unit csUnit, SootMethod callee, SootMethod caller){
		Pair<Unit, SootMethod> csKey = new Pair<Unit, SootMethod>(csUnit, callee);
		Map<RdValue, Set<Integer>> binding = callSite2binding.get(csKey);
		
		if(binding == null){
			if(csUnit instanceof InvokeStmt){
				binding = new HashMap<RdValue, Set<Integer>>();
				callSite2binding.put(csKey, binding);
				
				InvokeStmt invStmt = (InvokeStmt) csUnit;
				InvokeExpr invExpr = invStmt.getInvokeExpr();
				
				List/*ValueBox*/ argList = invExpr.getArgs();
				for(int i = 0; i < argList.size(); i++){
					Value argValue = (Value)argList.get(i);
					// FIXME: argValue must be MutableRef ? by longwen 2012/2/22
					if(true||isMutableRefParam(argValue)){
						Set<Integer> paramIndices = binding.get(argValue);
						if(paramIndices == null){
							paramIndices = new HashSet<Integer>();
						}
						
						// for parameter bindings at call sites, we just
						// store the parameter indices. Using these indices,
						// we can get the formal parameters in the method body
						// when necessary (i.e., the analysis goes into the method)
						paramIndices.add(new Integer(i));
						
						// FIXME:  by longwen 2012/2/22
						binding.put(new RdValue(argValue,caller), paramIndices);
					}
					
				}
			}else{
				throw new RuntimeException("The call site unit must be an instance of InvokeStmt.");
			}
		}else{
			/*
			 * there should be at most one binding for a call graph edge.
			 */
			Debug.P("WARNING: more than one binding exists for the call edge: " 
					+ csUnit + " --> " + callee.getSignature());
		}
	}
	
	/**
	 * Return the parameter binding for a given call site and callee.
	 * 
	 * @param csUnit
	 * @param callee
	 * @return
	 */
	public Map<RdValue, Set<Integer>> getParamBinding(Unit csUnit, SootMethod callee){
		Pair<Unit, SootMethod> csKey = new Pair<Unit, SootMethod>(csUnit, callee);
		Map<RdValue, Set<Integer>> binding = callSite2binding.get(csKey);
		
		return binding;
	}
	
	/**
	 * Because the parameter passing in Java is generally pass-by-value,
	 * the traditional meaning of reference parameter can hardly be used to
	 * judge whether a parameter should be considered in binding in Java.
	 * 
	 * Here we choose use the type of a parameter to determined whether
	 * it is mutable. Specifically, we just exclude some common immutable 
	 * types, such as primitives, their wrappers, and java.lang.String.
	 * 
	 * @param v
	 * @return
	 */
	private boolean isMutableRefParam(Value v){
		Type type = v.getType();
		
		// this condition excludes the primitive types, but not their wrappers
		if(type instanceof RefType){ 
			// TODO: test the cases of Java generics
			String typeName = ((RefType) type).getClassName();
			if(commonImmutableTypes.contains(typeName)){
				return false;
			}else{
				// this is really a conservative design, as there may
				// be a lot of customized immutable types.
				return true; 
			}
		}else{
			// if the type is a primitive type or
			// an other special type, e.g., NullType
			return false;
		}
	}
	
	
	/**
	 * Build the intra-procedural control flow graph for
	 * the given method and index it by storing it into the
	 * global map, m2g.
	 * 
	 * @param m
	 * @return
	 */
	private UnitGraph buildAndIndexCFG(SootMethod m){
		/*
		 * We use EnhancedUnitGraph here to ensure the uniqueness 
		 * of the entry and exit of the CFG
		 */
		// TODO: check the timing of getting active body for method m.
		UnitGraph g = new EnhancedUnitGraph(m.getActiveBody());
		
		m2g.put(m, g);
		return g;
	}
	
	private CallGraph cg;
	
	public void setCallGraph(CallGraph cg){
		this.cg = cg;
	}
	
	public CallGraph getCallGraph(){
		return this.cg;
	}
	
	// the main entry program point of the current analysis
	private RdProgramPoint mainEntryPoint;
	
	/**
	 * Set the main entry method as well as the entry
	 * program point of this method.
	 * 
	 * @param main, the main entry method that must be specified by the client (e.g., GUI)
	 */
	public void setMainEntry(SootMethod main){
		mainEntryPoint = getEntry(main);
	}
	
	private static RdSystemTool _instance = null;
	
	private RdSystemTool() {
		m2g = new HashMap<SootMethod, UnitGraph>();
		callSite2binding = new HashMap<Pair<Unit, SootMethod>, Map<RdValue, Set<Integer>>>();
	}
	
	public static RdSystemTool v() {
		if( _instance == null ) {
			_instance = new RdSystemTool();
		}
		return _instance;
	}
	
	public boolean isMainEntry( RdProgramPoint pp ) {
		boolean isMain = false;

		if(mainEntryPoint == null){
			throw new RuntimeException("The main entry point is not set!");
		}else{
			isMain = mainEntryPoint.equals(pp);
		}
		
		return isMain;
	}	
	
	/**
	 * Get all call sites of method p
	 * 
	 * @param p, the callee method
	 * @return
	 */
	public List<RdProgramPoint> getCallSites( SootMethod p ) {
		List<RdProgramPoint> csList = new ArrayList<RdProgramPoint>();

		Iterator<Edge> edges = cg.edgesInto(p);
		while(edges.hasNext()){
			Edge e = edges.next();
			RdProgramPoint cs = new RdProgramPoint(e.srcUnit(), (SootMethod)e.getSrc());
			csList.add(cs);
		}
		
		return csList;
	}
	
	/**
	 * Return the exit node of method p
	 * 
	 * @param p, the method of which the unique exit is returned
	 * @return
	 */
	public RdProgramPoint getExit( SootMethod p ) {
		UnitGraph graph = m2g.get(p);
		// if the method has not yet been indexed
		if(graph == null){
			graph = buildAndIndexCFG(p);
		}
		
		List<Unit> tails = graph.getTails();
		
		RdProgramPoint exitPoint = null;
		if(tails.size() == 1){
			exitPoint = new RdProgramPoint(tails.get(0), p, false);
		}else{
			Debug.P("WARNING: the number of exit nodes of method: " + p + " is not 1!");
		}
		
		return exitPoint;
	}
	
	/**
	 * Return the entry node of method p
	 * 
	 * @param p, the method of which the unique entry is returned
	 * @return
	 */
	public RdProgramPoint getEntry( SootMethod p ) {
		UnitGraph graph = m2g.get(p);
		// if the method has not yet been indexed
		if(graph == null){ 
			graph = buildAndIndexCFG(p);
		}
		
		List<Unit> heads = graph.getHeads();
		
		RdProgramPoint entryPoint = null;
		if(heads.size() == 1){
			entryPoint = new RdProgramPoint(heads.get(0), p, true);
		}else{
			Debug.P("WARNING: the number of entry nodes of method: " + p + " is not 1!");
		}
		
		return entryPoint;
	}
	/**
	 * 
	 * @param lineNum line number in source code
	 * @param sm soot method contains the specified line
	 * @return
	 */
	public RdProgramPoint getProgramPoint( String lineNum, SootMethod sm ) {
		Body body = sm.getActiveBody();
		RdProgramPoint result = null;
		int line = 0;
		for( Unit u : body.getUnits() ) {
			int lineNumInt = Integer.parseInt(lineNum);
			LineNumberTag tag = (LineNumberTag)u.getTag("LineNumberTag");
			if( tag != null ) {
				line = tag.getLineNumber();
			}
			
			if( line == lineNumInt ) { // the interested line
				result = new RdProgramPoint(u, sm, false);
			}
		}
		
		return result;
	}
	
	/**
	 * Return the CFG for the given method
	 * 
	 * @param m, the method of which the CFG is returned
	 * @return
	 */
	public UnitGraph getCFGForMethod(SootMethod m){
		UnitGraph graph = m2g.get(m);
		
		// if the method has not yet been indexed
		if(graph == null){ 
			graph = buildAndIndexCFG(m);
		}
		
		return graph;
	}
}
