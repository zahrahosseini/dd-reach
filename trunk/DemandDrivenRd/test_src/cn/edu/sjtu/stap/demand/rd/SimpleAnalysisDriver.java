package cn.edu.sjtu.stap.demand.rd;

import soot.Pack;
import soot.PackManager;
import soot.Transform;
import cn.edu.sjtu.stap.AnalysisConfig;
import cn.edu.sjtu.stap.tool.Debug;

/**
 * This class is used as the main entry to the whole analysis.
 * It is simple, because we just use it during testing phase.
 * Thus more complicated entry class may be designed and replace
 * this one. 
 * 
 * @author ChengZhang
 *
 */
public class SimpleAnalysisDriver {
	
	
	public static void main(String[] args){
		/*
		 * the arguments to Soot are supposed to be specified in 
		 * the command line (i.e., args), so we do not set them here.
		 */
		// In this simple driver, we just manually specify the config here.
		AnalysisConfig config = AnalysisConfig.getInstance();
		config.setMethodSig("<cn.edu.sjtu.stap.autolog.dataflow.rd.ExampleChpFive5: void proc2(int)>");
		config.setLineNum(33);
		config.setVarName("x");
		config.setVarType("int");
		Debug.DEBUG = false;
		Pack wjtp = PackManager.v().getPack("wjtp");
		TestRDTransformer trans =  new TestRDTransformer();
		trans.setDefinitionFinder(new HardCodedFinder());
		//trans.setDefinitionFinder(new TypeDefinitionFinder());
		wjtp.add(new Transform("wjtp.testdr", trans));
		
		soot.Main.main(args); // invoke the analysis
		
		// TODO: gather and format the results and check them.
	}
}
