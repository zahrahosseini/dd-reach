package cn.edu.sjtu.stap.demand.rd;

import java.util.*;
import soot.Unit;

/**
 * This class represents the dataflow set for
 * the demand-driven dataflow analysis. 
 * 
 * @author Longwen Lu
 *
 */
public class RdFlowSet {
	
	public static RdFlowSet EMPTYSET = new RdFlowSet();
	
	private Set<RdProgramPoint> _data = null;
	
	public RdFlowSet() {
		this._data = new HashSet<RdProgramPoint>();
	}
	
	public boolean contains( RdProgramPoint d ) { 
		return this._data.contains(d);
	}
	
	public void add( RdProgramPoint d ) {
		this._data.add(d);
	}
	
	public boolean include( RdFlowSet other ) {
		return this._data.containsAll(other._data);
	}
	
	public Set<RdProgramPoint> getAllData() {
		return this._data;
	}
	
	public void union(RdFlowSet other){
		this._data.addAll(other._data);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_data == null) ? 0 : _data.hashCode());
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
		RdFlowSet other = (RdFlowSet) obj;
		if (_data == null) {
			if (other._data != null)
				return false;
		} else if (!_data.equals(other._data))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this._data.toString();
	}
	
}
