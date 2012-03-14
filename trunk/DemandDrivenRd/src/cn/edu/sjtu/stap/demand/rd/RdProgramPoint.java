package cn.edu.sjtu.stap.demand.rd;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sjtu.stap.demand.rd.alias.AliasTool;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.internal.JIdentityStmt;
import soot.toolkits.graph.UnitGraph;

/**
 * This class represents the program point in the dataflow analysis.
 * More exactly, a program point is usually used as a dataflow item
 * in the dataflow analysis.
 * 
 * @author Longwen Lu
 *
 */
public class RdProgramPoint {
	
	private Unit _unit = null;
	private SootMethod _method = null;
	private boolean _isEntry = false;

	
	public RdProgramPoint( Unit unit, SootMethod method ) {
		this( unit,method,false );
	}
	
	public RdProgramPoint( Unit unit, SootMethod method, boolean isEntry ) {
		this._unit = unit;
		this._method = method;
		this._isEntry = isEntry;
	}
	
	public Unit getUnit() {
		return this._unit;
	}
	
	public SootMethod getMethod() {
		return this._method;
	}
	
	public boolean isEntry() {
		return this._isEntry;
	}
	
	/**
	 * Return the predecessors of the program point/unit in the CFG
	 * If there are no predecessors for the unit, return an empty list.
	 * 
	 */
	public List<RdProgramPoint> getPreds() {
		RdSystemTool sysTool = RdSystemTool.v();
		UnitGraph cfg = sysTool.getCFGForMethod(_method);
        List<Unit> predUnits = cfg.getPredsOf(_unit);
        
        List<RdProgramPoint> predPoints = new ArrayList<RdProgramPoint>();
        if(predUnits != null){
        	for(Unit pu : predUnits){
        		List<Unit> heads = cfg.getHeads();
        		boolean isEntry = heads.contains(pu);
        		predPoints.add(new RdProgramPoint(pu, _method, isEntry));
        	}
        }
        
		return predPoints;
	}
	
	/**
	 * return the method called by this program point/unit.
	 * Note if this is not a call site, return null.
	 * 
	 * @return
	 */
	public SootMethod getCallMethod() {
		if(_unit instanceof InvokeStmt){
			InvokeStmt is = (InvokeStmt) _unit;
			InvokeExpr iExp = is.getInvokeExpr();
			// TODO: check whether we should use getMethod() or getMethodRef()
			SootMethod callee = iExp.getMethod();
			
			return callee;
		}
		
		return null;
	}
	
	
	/**
	 * check whether the program point kills the def that
	 * reaches the program point.
	 * 
	 * @param value 
	 * @param def
	 * @return
	 */
	public boolean kill( RdValue value, RdProgramPoint def ) {
		// exclude the parameter & this reference passing statement
		if( _unit instanceof JIdentityStmt ) {
			JIdentityStmt junit = (JIdentityStmt) _unit;
			Value rightValue = junit.rightBox.getValue();
			if( junit.rightBox != null && (rightValue instanceof ParameterRef || rightValue instanceof ThisRef) ) {
				return false;
			}
		}
		
		List<ValueBox> defBoxes = _unit.getDefBoxes();
		
		boolean killed = false;
		if( defBoxes != null && defBoxes.size() != 0 ) {
			// TODO: we will take into account aliases here!
			ValueBox vb = defBoxes.get(0);
			Value v1 = vb.getValue();
			Value v2 = value.getValue();
			// TODO: find better solution to check equality of values/variables in Jimple!
			if(v1.toString().equals(v2.toString())
					|| AliasTool.v().mustAlias(this, new RdValue(v1,this._method), value) ){ // check aliases 
				
				if( this.equals(def) ) { // the same value, the same statement
					killed = false;
				} else {
					killed = true;
				}
			}
		}
		
		return killed;
		
//		if( def.getUnit().equals(this.getUnit()) ) {
//			return false;
//		}
//		
//		List<ValueBox> defBoxes = _unit.getDefBoxes();
//		
//		boolean killed = false;
//		if(defBoxes != null && defBoxes.size() != 0){
//			// here we assume there is exactly one def per dUnit
//			Unit dUnit = def.getUnit();
//			ValueBox defVb = dUnit.getDefBoxes().get(0);
//			
//			// TODO: we will take into account aliases here!
//			ValueBox vb = defBoxes.get(0);
//			Value v1 = vb.getValue();
//			Value v2 = defVb.getValue();
//			if(v1.equals(v2)){
//				killed = true;
//			}else{
//				// TODO: find better solution to check equality of values/variables in Jimple!
//				if(v1.toString().equals(v2.toString()) /*&& v1.getType().equals(v2.getType())*/){
//					killed = true;
//				}
//			}
//		}
//		
//		return killed;
//		
	}
	
	/**
	 * check whether the program point generates the def.
	 * (It is a bit curious here. so I am wondering whether
	 * the design intention should be really like this...)
	 * 
	 * @param value
	 * @param def
	 * @return
	 */
	public boolean gen( RdValue value, RdProgramPoint def ) {
		return this.equals(def);
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_method == null) ? 0 : _method.hashCode());
		result = prime * result + ((_unit == null) ? 0 : _unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RdProgramPoint other = (RdProgramPoint) obj;
		if (_method == null) {
			if (other._method != null)
				return false;
		} else if (!_method.equals(other._method))
			return false;
		if (_unit == null) {
			if (other._unit != null)
				return false;
		} else if (!_unit.equals(other._unit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this._unit.toString();
	}
	
	
}
