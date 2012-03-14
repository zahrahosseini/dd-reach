package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.Set;

import soot.Context;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootField;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.spark.sets.EqualsSupportingPointsToSet;

import cn.edu.sjtu.stap.demand.rd.RdValue;

public class SparkAliasResult implements AliasResult{
	
	private PointsToAnalysis pta = null;
	
	public SparkAliasResult() {
		pta = Scene.v().getPointsToAnalysis();
	}

	@Override
	public boolean mayAlias(Context c, RdValue a, RdValue b) {
		PointsToSet ptsa = this.getPts(c, a);
		PointsToSet ptsb = this.getPts(c, b);
		if( ptsa.hasNonEmptyIntersection(ptsb) ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean mustAlias(Context c, RdValue a, RdValue b) {
		PointsToSet ptsa = this.getPts(c, a);
		PointsToSet ptsb = this.getPts(c, b);
		
		if( !(ptsa instanceof EqualsSupportingPointsToSet)
				|| !(ptsb instanceof EqualsSupportingPointsToSet) ) {
			throw new RuntimeException("ERROR: no equals method in points to set");
		}
		
		EqualsSupportingPointsToSet epoint2sa = (EqualsSupportingPointsToSet)ptsa; 
		EqualsSupportingPointsToSet epoint2sb = (EqualsSupportingPointsToSet)ptsb;
		
		if( ptsa.isEmpty() ) {
			return false;
		} else if( epoint2sa.pointsToSetEquals(epoint2sb) ) {
			return true;
		}
		return false;
	}
	
	private PointsToSet getPts( Context c, RdValue v ) {
		Value value = v.getValue();
		if( value instanceof Local ) { // local value
			Local l = (Local)value;
			return pta.reachingObjects(l);
		} else if( value instanceof FieldRef ){ // static value
			FieldRef fr = (FieldRef)value;
			SootField sf = fr.getField();
			return pta.reachingObjects(sf);
		}
		throw new RuntimeException("Error: unrecegnized value type");
	}
	
}
