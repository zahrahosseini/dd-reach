package cn.edu.sjtu.stap.demand.rd;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sjtu.stap.tool.Debug;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.tagkit.LineNumberTag;

public class HardCodedFinder implements DefinitionFinder {
	
	private ArrayList<String> methodList  = null;
	private ArrayList<Integer> lineNumList = null;
	
	private void initDefinition() {
		methodList = new ArrayList<String>();
		lineNumList = new ArrayList<Integer>();
		
		methodList.add("<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc1()>");
		lineNumList.add(18);
//		methodList.add("<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc1()>");
//		lineNumList.add(19);
//		methodList.add("<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc1()>");
//		lineNumList.add(25);
//		methodList.add("<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc3(cn.edu.sjtu.stap.dataflow.rd.A)>");
//		lineNumList.add(40);
//		methodList.add("<cn.edu.sjtu.stap.dataflow.rd.ApacheMathTest: void main(java.lang.String[])>");
//		lineNumList.add(8);
	}
	@Override
	public List<RdProgramPoint> findDefinitinos(RdProgramPoint startPoint, RdValue startValue) {
		RdProgramPoint definition;
		int line;
		ArrayList<RdProgramPoint> result = new ArrayList<RdProgramPoint>();
		initDefinition();
		
		for( int i = 0; i < this.methodList.size(); i++ ) {
			String defMethodSig = methodList.get(i);
			int defLineNum = lineNumList.get(i);
			
			SootMethod defMethod = Scene.v().getMethod(defMethodSig);
			
			line = 0;
			for( Unit unit : defMethod.getActiveBody().getUnits() ) {
				LineNumberTag tag = (LineNumberTag)unit.getTag("LineNumberTag");
				if( tag != null ) {
					line = tag.getLineNumber();
				}

				if( line == defLineNum && unit.getDefBoxes().size() > 0 ) {
					
					String leftValueName = unit.getDefBoxes().get(0).getValue().toString();
					if(leftValueName.contains("y")||leftValueName.contains("x")) {
						Debug.P(leftValueName);
						definition = new RdProgramPoint(unit, defMethod, false);
						result.add(definition);
					}
				}
			}
		}
		return result;
	}

}
