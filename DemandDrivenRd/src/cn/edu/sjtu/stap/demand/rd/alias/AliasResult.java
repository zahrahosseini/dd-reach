package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.Set;
import soot.Context;

import cn.edu.sjtu.stap.demand.rd.RdValue;

public interface AliasResult {
	
	public boolean mayAlias( Context c, RdValue a, RdValue b );
	public boolean mustAlias( Context c, RdValue a, RdValue b );
}
