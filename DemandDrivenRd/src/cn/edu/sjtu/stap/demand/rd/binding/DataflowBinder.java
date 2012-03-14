package cn.edu.sjtu.stap.demand.rd.binding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import cn.edu.sjtu.stap.demand.rd.GlobalVarUtil;
import cn.edu.sjtu.stap.demand.rd.RdFlowSet;
import cn.edu.sjtu.stap.demand.rd.RdProgramPoint;
import cn.edu.sjtu.stap.demand.rd.RdSystemTool;
import cn.edu.sjtu.stap.demand.rd.RdValue;

/**
 * This class is designed to do the binding between lattice elements/dataflow
 * sets at call sites for inter-procedural dataflow analysis.
 * 
 * @author ChengZhang
 * 
 */
public class DataflowBinder {
	/**
	 * Currently this method is not used.
	 * 
	 * @param elem
	 * @param callee
	 * @return
	 */	
	public static RdFlowSet flowElemBinding(RdProgramPoint elem,
			SootMethod callee) {
		RdFlowSet boundToSet = new RdFlowSet();
		Set<Value> boundToValues = new HashSet<Value>();

		Unit elemUnit = elem.getUnit();
		Value elemValue = findLHSValueInUnit(elemUnit);

		RdSystemTool sysTool = RdSystemTool.v();
		Map<RdValue, Set<Integer>> value2indices = sysTool.getParamBinding(
				elemUnit, callee);
		Set<Integer> formalIndices = value2indices.get(elemValue);

		Body calleeBody = callee.getActiveBody();
		if (formalIndices != null) {
			for (Integer index : formalIndices) {
				Value fv = calleeBody.getParameterLocal(index.intValue());
				boundToValues.add(fv);
			}
		}

		// TODO: check how the accesses to class fields are represented!
		if (elemValue instanceof FieldRef) {
			FieldRef fr = (FieldRef) elemValue;
			SootField f = fr.getField();

			if (GlobalVarUtil.isSootFieldGlobal(f)) {
				List<ValueBox> valueBoxes = calleeBody.getUseAndDefBoxes();
				for (ValueBox vb : valueBoxes) {
					Value v = vb.getValue();
					if (v instanceof FieldRef) {
						if (((FieldRef) v).getField().equals(f)) {
							boundToValues.add(v);
						}
					}
				}
			}
		}

		for (Value v : boundToValues) {
			boolean isEntry = elem.isEntry();
			RdProgramPoint rpp = new RdProgramPoint(elem.getUnit(),
					elem.getMethod(), isEntry);
			//rpp.set_variable(v);

			boundToSet.add(rpp);
		}

		return boundToSet;
	}

	public static Set<RdValue> flowElemReverseBinding(RdValue elem,
			SootMethod callee, Unit callSite, SootMethod caller) {
		Set<RdValue> boundToValues = new HashSet<RdValue>();
		
		// process global value
		if( elem.getValue() instanceof StaticFieldRef ) {
			boundToValues.add(elem);
		}
		
		Body calleeBody = callee.getActiveBody();
		int paramIndex = findParamIndex(calleeBody,elem);
		
		RdSystemTool sysTool = RdSystemTool.v();
		Map<RdValue,Set<Integer>> value2indices = sysTool.getParamBinding(
				callSite, callee);
		
		for(Entry<RdValue,Set<Integer>> entry : value2indices.entrySet()) {
			RdValue key = entry.getKey();
			Set<Integer> indicesValue = entry.getValue();
			if( indicesValue.contains(new Integer(paramIndex))) {
				boundToValues.add(key);
				break;
			}
		}
		
		if(elem.getValue() instanceof FieldRef) { 
			FieldRef fr = (FieldRef) elem.getValue();
			SootField f = fr.getField();
			
			if(GlobalVarUtil.isSootFieldGlobal(f)) {
				Body callerBody = caller.getActiveBody();
				List<ValueBox> valueBoxes = callerBody.getUseAndDefBoxes();
				for (ValueBox vb : valueBoxes) {
					Value v = vb.getValue();
					if (v instanceof FieldRef) {
						if (((FieldRef) v).getField().equals(f)) {
							boundToValues.add(new RdValue(v,caller));
						}
					}
				}
			}
		}
		
		return boundToValues;
		// TODO: to be continued 
		
////		Unit elemUnit = elem.getUnit();
////		Value tempValue = findLHSValueInUnit(elemUnit);
//		Value tempValue = elem.get_variable();
//		RdValue elemValue = new RdValue(tempValue,callee);
//		Body calleeBody = callee.getActiveBody();
//		// TODO: check the feasibility of finding the index for a value/variable
//		int paramIndex = findParamIndex(calleeBody, elemValue);
//
//		RdSystemTool sysTool = RdSystemTool.v();
//		Map<RdValue, Set<Integer>> value2indices = sysTool.getParamBinding(
//				callSite, callee);
//
//		for (Entry<RdValue, Set<Integer>> entry : value2indices.entrySet()) {
//			RdValue key = entry.getKey();
//			Set<Integer> indicesValue = entry.getValue();
//			if (indicesValue.contains(new Integer(paramIndex))) {
//				boundToValues.add(key);
//				break;
//			}
//		}
//
//		// TODO: check how the accesses to class fields are represented!
//		if (elemValue.getValue() instanceof FieldRef) {
//			FieldRef fr = (FieldRef) elemValue.getValue();
//			SootField f = fr.getField();
//
//			if (GlobalVarUtil.isSootFieldGlobal(f)) {
//				Body callerBody = caller.getActiveBody();
//				List<ValueBox> valueBoxes = callerBody.getUseAndDefBoxes();
//				for (ValueBox vb : valueBoxes) {
//					Value v = vb.getValue();
//					if (v instanceof FieldRef) {
//						if (((FieldRef) v).getField().equals(f)) {
//							boundToValues.add(new RdValue(v,caller));
//						}
//					}
//				}
//			}
//		}
//
//		for (RdValue v : boundToValues) {
//			boolean isEntry = elem.isEntry();
//			RdProgramPoint rpp = new RdProgramPoint(elem.getUnit(),
//					elem.getMethod(), isEntry);
//			rpp.set_variable(v.getValue());
//
//			boundToSet.add(rpp);
//		}
//	
//		return boundToSet;
	}

	// TODO: test this method carefully
	private static int findParamIndex(Body calleeBody, RdValue elemValue) {
		int paramCount = calleeBody.getMethod().getParameterCount();
		for (int i = 0; i < paramCount; i++) {
			Local paramLocal = calleeBody.getParameterLocal(i);
			RdValue RdLocal = new RdValue(paramLocal,calleeBody.getMethod());
			if (RdLocal.equals(elemValue)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Extract the variable/Value which is defined/modified in the given Unit.
	 * 
	 * @param u
	 * @return
	 */
	private static Value findLHSValueInUnit(Unit u) {
		List<ValueBox> defBoxes = u.getDefBoxes();

		if (defBoxes.size() == 1) {
			return defBoxes.get(0).getValue();
		} else {
			throw new RuntimeException(
					"Current there must be exactly one def box in the unit!");
		}
	}

	/**
	 * FIXME: add the treatment for instance fields.
	 * 
	 * @param srcMethod
	 * @param srcValue
	 * @param startMethod
	 * @return
	 */
	public static Set<RdValue> forwardBindingForQueryValue(SootMethod srcMethod,
			RdValue srcValue, SootMethod startMethod) {
		Set<RdValue> startValues = new HashSet<RdValue>();
		
		// if the source method is the same one as the query-starting method,
		// no binding is necessary, just use the original variable name.
		if( srcMethod.equals(startMethod) ) {
			startValues.add(srcValue);
			return startValues;
		}
		
		// do the binding for global variables (public static fields) 
		// TODO: test it...
		Value sootSrcValue = srcValue.getValue();
		if(sootSrcValue instanceof StaticFieldRef){
			startValues.add(new RdValue(sootSrcValue,startMethod));
//			StaticFieldRef srcFieldRef = (StaticFieldRef)sootSrcValue;
//			
//			List<ValueBox> vbList = startMethod.getActiveBody().getUseAndDefBoxes();
//			for(ValueBox vb : vbList){
//				Value v = vb.getValue();
//				if(v instanceof StaticFieldRef){
//					StaticFieldRef sfr = (StaticFieldRef)v;
//					// if the two instances of FieldRef refer to exactly the same field
//					if(srcFieldRef.getField().equals(sfr.getField())){
//						startValues.add(new RdValue(v, startMethod));
//					}
//				}
//			}
		}

		// do the parameter binding
		RdSystemTool sysTool = RdSystemTool.v();
		CallGraph cg = sysTool.getCallGraph();

		List<CallStackTrace> allTraces = CallStackTraceFinder
				.getCallStackTraces(cg, srcMethod, startMethod);

		outer: for (CallStackTrace trace : allTraces) {
			RdValue value2bind = srcValue;

			// the chain of reverse binding
			List<Edge> backwardTrace = trace.getBackwardTrace();
			for (Edge e : backwardTrace) {
				value2bind = reverseValueBinding(e, value2bind);
				if (value2bind == null) {
					continue outer;
				}
			}

			// the chain of forward binding
			Set<RdValue> values2bind = new HashSet<RdValue>();
			values2bind.add(value2bind);
			List<Edge> forwardTrace = trace.getForwardTrace();
			for (Edge e : forwardTrace) {
				values2bind = forwardValueBinding(e, values2bind);
				if (values2bind.size() == 0) {
					continue outer;
				}
			}

			startValues = values2bind;
		}

		return startValues;
	}

	private static Set<RdValue> forwardValueBinding(Edge e, Set<RdValue> values2bind) {
		RdSystemTool sysTool = RdSystemTool.v();
		Set<RdValue> boundToValues = new HashSet<RdValue>();

		Map<RdValue, Set<Integer>> value2indices = sysTool.getParamBinding(
				e.srcUnit(), e.tgt());
		for (RdValue v : values2bind) {
			// FIXME: compare by name? by longwen 2012/2/22
			Set<Integer> formalIndices = value2indices.get(v);
			Body calleeBody = e.tgt().getActiveBody();
			if (formalIndices != null) {
				for (Integer index : formalIndices) {
					Value fv = calleeBody.getParameterLocal(index.intValue());
					boundToValues.add(new RdValue(fv,e.tgt()));
				}
			}
		}

		return boundToValues;
	}

	private static RdValue reverseValueBinding(Edge e, RdValue value2bind) {
		RdSystemTool sysTool = RdSystemTool.v();
		SootMethod callee = e.tgt();

		Body calleeBody = callee.getActiveBody();
		int paramIndex = findParamIndex(calleeBody, value2bind);
		Map<RdValue, Set<Integer>> value2indices = sysTool.getParamBinding(
				e.srcUnit(), callee);

		RdValue boundValue = null;
		for (Entry<RdValue, Set<Integer>> entry : value2indices.entrySet()) {
			RdValue key = entry.getKey();
			Set<Integer> indicesValue = entry.getValue();
			if (indicesValue.contains(new Integer(paramIndex))) {
				boundValue = key;
				break;
			}
		}

		return boundValue;
	}
}