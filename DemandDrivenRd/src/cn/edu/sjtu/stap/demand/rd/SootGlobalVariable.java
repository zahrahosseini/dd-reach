package cn.edu.sjtu.stap.demand.rd;

import java.util.HashSet;
import java.util.Set;

import soot.SootField;
import cn.edu.sjtu.stap.tool.Debug;

/**
 * TODO: currently we just consider the "absolute globals" for the reasons below:
 * 1) They are the most common global variables in Java
 * 2) Other kinds of global variables, such as package visible or type hierarchy visible 
 *    variables, are more expensive to identify. But there may not be so many of them.
 * 
 * @author ChengZhang
 *
 */
public class SootGlobalVariable implements GlobalVariable {
	private SootField value;
	
	// the field indicates whether the variable is a public field of some class/instance
	private boolean isAbsoluteGlobal;
	
	// the field represents the packages where the current "global" variable is visible.
	// Note that when the field isAbsoluteGlobal is true, this field does not necessarily
	// contain all the packages (as the visible scope is indicated by isAbsoluteGlobal).
	private Set<String> visiblePackages; 
	
	public SootGlobalVariable(SootField v){
		this.value = v;
//		visiblePackages = new HashSet<String>();
	}
	
	public boolean isAbsoluteGlobal() {
		return isAbsoluteGlobal;
	}

	public void setAbsoluteGlobal(boolean isAbsoluteGlobal) {
		this.isAbsoluteGlobal = isAbsoluteGlobal;
	}
	
	public Set<String> getVisiblePackages() {
		return visiblePackages;
	}

	public void setVisiblePackages(Set<String> visiblePackages) {
		this.visiblePackages = visiblePackages;
	}

	@Override
	public void setValue(Object v) {
		if(v instanceof SootField){
			value = (SootField)v;
		}else{
			Debug.P("WARNING: The global value is not a SootField.");
		}
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	public SootField getSootGlobalVariable(){
		return value;
	}
	
	public boolean equals(Object gv2){
		if(gv2 instanceof SootGlobalVariable){
			SootGlobalVariable sgv2 = (SootGlobalVariable) gv2;
			return value.equals(gv2);
		}else{
			return false;
		}
	}
	
	public int hashCode(){
		return value.hashCode();
	}
}
