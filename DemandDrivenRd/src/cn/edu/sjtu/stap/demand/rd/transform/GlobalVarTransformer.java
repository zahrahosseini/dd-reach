package cn.edu.sjtu.stap.demand.rd.transform;

import java.util.Iterator;
import java.util.Map;

import cn.edu.sjtu.stap.demand.rd.GlobalVarUtil;
import cn.edu.sjtu.stap.demand.rd.SootGlobalVariable;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.util.Chain;

/**
 * This transformer class is designed to identify all the global
 * variables (currently, public fields) in the whole program.
 * 
 * @author ChengZhang
 * 
 */
public class GlobalVarTransformer extends SceneTransformer {

	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		
		Iterator<SootClass> classIte = appClasses.iterator();
		while(classIte.hasNext()){
			SootClass clazz = classIte.next();
			Chain<SootField> fields = clazz.getFields();
			Iterator<SootField> fIte = fields.iterator();
			
			while(fIte.hasNext()){
				SootField f = fIte.next();
				
				/*
				 * Currently, we just consider the public fields.
				 * Protected or package fields are not taken into account
				 * and may be treated in the future if necessary.
				 */
				if(f.isPublic()){
					SootGlobalVariable sgv = new SootGlobalVariable(f);
					sgv.setAbsoluteGlobal(true);
					GlobalVarUtil.addGlobal(sgv);
				}
			}
		}
	}
}
