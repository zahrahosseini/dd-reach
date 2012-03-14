package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Context;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.spark.sets.EqualsSupportingPointsToSet;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;

import cn.edu.sjtu.stap.demand.rd.RdProgramPoint;
import cn.edu.sjtu.stap.demand.rd.RdValue;

public class LocalAliasResult implements AliasResult{
	
	private Map<SootMethod,LocalMustAliasAnalysis> cache = null;
	
	public LocalAliasResult() {
		cache = new HashMap<SootMethod, LocalMustAliasAnalysis>();
	}


	@Override
	public boolean mustAlias(RdProgramPoint pp, RdValue a, RdValue b) {
		SootMethod smethod = pp.getMethod();
		if( a.getSootMethod() != smethod || b.getSootMethod() != smethod || // not in the same method 
				!(a.getValue() instanceof Local) || !(b.getValue() instanceof Local) || // a and b are not local
				!(pp.getUnit() instanceof Stmt)) { // pp is not a statement
			return false;
		}
		Local locala = (Local)a.getValue();
		Local localb = (Local)b.getValue();
		LocalMustAliasAnalysis lmaAnalysis = cache.get(smethod);
		if( lmaAnalysis == null) {
			lmaAnalysis = new LocalMustAliasAnalysis(new EnhancedUnitGraph(smethod.getActiveBody()));
			cache.put(smethod, lmaAnalysis);
		}
		return lmaAnalysis.mustAlias(locala, (Stmt)pp.getUnit(), localb, (Stmt)pp.getUnit());
		
	}
	
	
}
