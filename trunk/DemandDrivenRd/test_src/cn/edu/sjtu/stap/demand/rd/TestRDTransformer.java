package cn.edu.sjtu.stap.demand.rd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.Value;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;
import soot.util.queue.QueueReader;
import cn.edu.sjtu.stap.AnalysisConfig;
import cn.edu.sjtu.stap.tool.CallGraphDumper;
import cn.edu.sjtu.stap.tool.Debug;
import cn.edu.sjtu.stap.demand.rd.binding.DataflowBinder;

public class TestRDTransformer extends SceneTransformer {
	
	private DefinitionFinder df = null;
	
	
	private ArrayList<String> methodList = null;
	private ArrayList<Integer> lineNumList = null;
	
	private static String PROC1 = "<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc1()>";
	private static String PROC2 = "<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc2(cn.edu.sjtu.stap.dataflow.rd.A)>";
	private static String PROC3 = "<cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias: void proc3(cn.edu.sjtu.stap.dataflow.rd.A)>";
	private static String EVA = "<org.apache.commons.math.stat.descriptive.moment.Variance: double evaluate(double[],double[],double,int,int)>";
	
	/**
	 * initialize the start points
	 */
	private void initInput() {
		methodList = new ArrayList<String>();
		lineNumList = new ArrayList<Integer>();
		
		methodList.add(PROC1);
		lineNumList.add(19);
//		methodList.add(PROC1);
//		lineNumList.add(22);
//		methodList.add(PROC1);
//		lineNumList.add(25);
//		methodList.add(PROC1);
//		lineNumList.add(26);
//		methodList.add(PROC1);
//		lineNumList.add(29);
//		methodList.add(PROC2);
//		lineNumList.add(33);
//		methodList.add(PROC2);
//		lineNumList.add(34);
//		methodList.add(PROC3);
//		lineNumList.add(39);
//		methodList.add(PROC3);
//		lineNumList.add(40);
//		methodList.add(EVA);
//		lineNumList.add(516);
	}
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		// check definition finder initialization
		if( df == null ) {
			throw new RuntimeException("ERROR: definition finder undefined");
		}
		//this.initInput();
		methodList = new ArrayList<String>();
		lineNumList = new ArrayList<Integer>();
		methodList.add(AnalysisConfig.getInstance().getMethodSig());
		lineNumList.add(AnalysisConfig.getInstance().getLineNum());
		
		SootClass sclass = Scene.v().getMainClass();
//		SootClass target = Scene.v().getSootClass("org.apache.commons.math.stat.descriptive.moment.Variance");
//		target.setApplicationClass();
		// Step 0. prepare and create the common entities to be used
	
		CallGraph cg = Scene.v().getCallGraph();
		CallGraphDumper dumper = CallGraphDumper.v();
		dumper.init(cg, "callgraph");
		
		// dump call graph
//		try {
//			PrintStream ps=new PrintStream(new FileOutputStream("callgraph"));
//			ps.println(cg.toString());
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
		RdSystemTool.v().setCallGraph(cg);
		// Step 1. get the necessary binding info
		// Build the bindings
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		Iterator<SootClass> appClassIte = appClasses.iterator();
		while(appClassIte.hasNext()){
			SootClass clazz = appClassIte.next();
			GlobalVarUtil.addGlobalsInSootClass(clazz);
		}
		
	    QueueReader<Edge> edgeReader = cg.listener();
	    while(edgeReader.hasNext()){
	    	Edge e = edgeReader.next();
	    	Unit csUnit = e.srcUnit();
	    	SootMethod callee = e.tgt();
	    	SootMethod caller = e.src();
	    	if(csUnit != null 
	    			&& csUnit instanceof InvokeStmt 
	    			&& callee != null 
	    			&& callee.getDeclaringClass().isApplicationClass()
	    			&& caller.getDeclaringClass().isApplicationClass()){
	    		RdSystemTool.v().addParamBinding(csUnit, callee, e.src());
	    	}
	    }
		
		// end of building bindings
		
		SootMethod mainMethod = Scene.v().getMainMethod();
		RdSystemTool.v().setMainEntry(mainMethod);
		
		// Step 2. generate the query 
		// get the start program point
		
		for( int i = 0; i < this.methodList.size(); i++ ) {
			String methodSig = this.methodList.get(i);
			int line = this.lineNumList.get(i);
			
			SootMethod startMethod = Scene.v().getMethod(methodSig);
			
			RdProgramPoint startPoint = RdSystemTool.v().getProgramPoint(line+"", startMethod);
			
			// find the definitions may reach the start point in program
			RdValue startValue = null;
			for( Local local : startMethod.getActiveBody().getLocals() ) {
				if( local.getName().equals(AnalysisConfig.getInstance().getVarName()) ) {
					startValue = new RdValue(local,startMethod);
					break;
				}
			}
			
			List<RdProgramPoint> definitions = this.df.findDefinitinos(startPoint,startValue);
			//System.out.println(definitions);
			// Step 3. use the algorithms to propagate the query
			Set<RdValue> queryDefs = null;
			System.out.println("Reachability to " + startPoint.getUnit()+": ");
			for( RdProgramPoint def : definitions ) {
				System.out.println(def.getUnit());
				Value srcValue = def.getUnit().getDefBoxes().get(0).getValue();
				queryDefs = DataflowBinder.forwardBindingForQueryValue(def.getMethod(), new RdValue(srcValue,def.getMethod()), startMethod);
				
				boolean queryResult = false;
				for( RdValue v : queryDefs ) {
					System.out.println(v);
					RdDemandDrivenAlgorithm algorithm = new RdDemandDrivenAlgorithm();
					System.out.println("wutingfang");
					queryResult |= algorithm.QueryGenKill(def, startPoint,v);
				}
				System.out.println(": " + queryResult);
			}
			System.out.println("==========================================================================");
		}
		
		// Step 4. store the results
	}
	
	public void setDefinitionFinder( DefinitionFinder df ) {
		this.df = df;
	}

}
