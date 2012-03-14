package cn.edu.sjtu.stap.demand.rd;

import java.util.*;

public interface DefinitionFinder {
	
	/**
	 * return the definitions in the program which MAY reach startValue at startPoint
	 * Ignore the constraints of value when startValue is null  
	 */
	public List<RdProgramPoint> findDefinitinos( RdProgramPoint startPoint, RdValue startValue );
}
