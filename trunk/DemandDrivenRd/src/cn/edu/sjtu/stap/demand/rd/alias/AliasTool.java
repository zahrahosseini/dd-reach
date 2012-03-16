package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.Set;

import javax.management.RuntimeErrorException;

import cn.edu.sjtu.stap.demand.rd.RdValue;
import soot.jimple.paddle.*;
public class AliasTool {
	
	private static AliasResult local_instance = null;
	
	
	public static AliasResult v() {
		if( local_instance == null ) {
			local_instance = new LocalAliasResult();
		}
		return local_instance;
	}
}
