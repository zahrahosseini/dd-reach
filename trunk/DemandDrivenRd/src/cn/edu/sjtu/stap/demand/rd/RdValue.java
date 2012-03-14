package cn.edu.sjtu.stap.demand.rd;

import soot.Value;
import soot.SootMethod;

public class RdValue {
	
	private Value value = null;
	private SootMethod sootMethod = null;
	
	public RdValue( Value v, SootMethod sm ) {
		this.value = v;
		this.sootMethod = sm;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public SootMethod getSootMethod() {
		return sootMethod;
	}

	public void setSootMethod(SootMethod sootMethod) {
		this.sootMethod = sootMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sootMethod == null) ? 0 : sootMethod.hashCode());
		result = prime * result + ((value == null || value.toString() == null) ? 0 : value.toString().hashCode());
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
		RdValue other = (RdValue) obj;
		if (sootMethod == null) {
			if (other.sootMethod != null)
				return false;
		} else if (!sootMethod.equals(other.sootMethod))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (other.value == null) {
			return false;
		} else if (!this.value.toString().equals(other.value.toString()))
			return false;
		return true;
	}
	
	public String toString() {
		return this.value.toString();
	}
	
	
}
