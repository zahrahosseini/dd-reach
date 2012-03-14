package cn.edu.sjtu.stap.demand.rd;

import java.util.HashSet;
import java.util.Set;

import soot.SootClass;
import soot.SootField;
import soot.util.Chain;

/**
 * This class is designed to store and maintain
 * all the global variables in the whole program.
 * 
 * @author ChengZhang
 *
 */
public class GlobalVarUtil {
	private static Set<GlobalVariable> globals = new HashSet<GlobalVariable>();
	
	public static void clear(){
		globals.clear();
	}
	
	public static void addGlobal(GlobalVariable gv){
		globals.add(gv);
	}	
	
	public static boolean isSootFieldGlobal(SootField f){
		SootGlobalVariable querySootGV = new SootGlobalVariable(f);
		
		return globals.contains(querySootGV);
	}
	
	// FIXME: not a good design, involving info specific to Soot framework...
	public static int addGlobalsInSootClass(SootClass clazz){
		int globalNum = 0;
		Chain<SootField> fields = clazz.getFields();
		for(SootField f : fields){
			if(f.isPublic()){
				SootGlobalVariable gv = new SootGlobalVariable(f);
				gv.setAbsoluteGlobal(true);
				globals.add(gv);
				
				globalNum++;
			}
		}
		
		return globalNum;
	}
}
