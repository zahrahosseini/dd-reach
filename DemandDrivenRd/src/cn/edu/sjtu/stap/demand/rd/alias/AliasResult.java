package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.Set;
import soot.Context;

import cn.edu.sjtu.stap.demand.rd.RdProgramPoint;
import cn.edu.sjtu.stap.demand.rd.RdValue;

public interface AliasResult {
	
	/**
	 * whether value a and value b are must-alias at
	 * program point pp 
	 * @param pp program point 
	 * @param a program value a
	 * @param b program value b
	 * @return
	 */
	public boolean mustAlias( RdProgramPoint pp, RdValue a, RdValue b );
}
