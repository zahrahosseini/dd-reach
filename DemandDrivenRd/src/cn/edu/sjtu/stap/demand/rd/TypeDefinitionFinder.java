package cn.edu.sjtu.stap.demand.rd;

import java.util.ArrayList;
import java.util.List;


import soot.ArrayType;
import soot.Hierarchy;
import soot.Local;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.util.Chain;

public class TypeDefinitionFinder implements DefinitionFinder {
	
	Hierarchy hierarchy = null;
	
	@Override
	public List<RdProgramPoint> findDefinitinos(RdProgramPoint startPoint, RdValue startValue) {
		Chain<SootClass> allClasses = Scene.v().getClasses();
		List<RdProgramPoint> result = new ArrayList<RdProgramPoint>();
		
		hierarchy = new Hierarchy();
		// get compatible locals
		for( SootClass sclass : allClasses ) {
			if( sclass.isApplicationClass() ) {
				List<SootMethod> allMethods = sclass.getMethods();
				for( SootMethod smethod : allMethods ) {
					if( smethod.hasActiveBody() ) {
						for(Unit unit : smethod.getActiveBody().getUnits()) {
							List<ValueBox> defs = unit.getDefBoxes();
							if( defs.size() > 0 ) {
								Value value = defs.get(0).getValue();
								if( startValue == null || this.isCompatible(startValue.getValue().getType(),value.getType()) ) {
									RdProgramPoint pp = new RdProgramPoint(unit, smethod);
									result.add(pp);
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * If type a is compatible with b, eg. type b value can be assigned 
	 * to type a reference, return true; else return false. PrimTypes are
	 * considered compatible with each other.
	 * @param a type a
	 * @param b type b
	 * @return
	 */
	private boolean isCompatible( Type a, Type b ) {
		Type tempa = a;
		Type tempb = b;
		
		// get element type
		while( tempa instanceof ArrayType ) {
			tempa = ((ArrayType)tempa).getElementType();
		}
		
		while( tempb instanceof ArrayType ) {
			tempb = ((ArrayType)tempb).getElementType();
		}
		
		
		if( (tempa instanceof PrimType) && (tempb instanceof PrimType) ) {
			return true;
		} else if( tempa instanceof PrimType )  { // b is not a PrimType while a is.
			return false;
		} else {
			if( (tempa instanceof RefType) && (tempb instanceof RefType) ) {
				RefType refa = (RefType)tempa;
				RefType refb = (RefType)tempb;
				SootClass sclassa = refa.getSootClass();
				SootClass sclassb = refb.getSootClass();
				if( hierarchy.isClassSubclassOfIncluding(sclassb, sclassa) ) {
					return true;
				} else {
					return false;
				}
				
			} else {
				throw new RuntimeException("ERROR: wrong type!");
			}
		}
	}

}
