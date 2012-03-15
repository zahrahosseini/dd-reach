package cn.edu.sjtu.stap.demand.rd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.edu.sjtu.stap.AnalysisConfig;

import plume.*;
import soot.Pack;
import soot.PackManager;
import soot.Transform;



public class CommandLineAnalysisDriver {
	 
	@Option ("-m main class") 
	public static String mainClass;
	
	@Option ("-s start method")
	public static String startMethod;
	
	@Option ("-l start line number")
	public static int lineNum;
	
	@Option ("-v value")
	public static String value;
	
	public static void main( String args[] ) {
	    Options options = new Options("Demand driven command line",CommandLineAnalysisDriver.class);
	    String[] remaining_args = options.parse_or_usage(args);
	    System.out.println(mainClass);
	    
	    // soot arguments
	    List<String> argsList = new ArrayList<String>();
	    argsList.addAll(Arrays.asList(new String[]{
	    		"-main-class",
	    		mainClass,
	    		//"cn.edu.sjtu.stap.dataflow.rd.ExampleChpFive5withAlias",
	    		"-w",
	    		"-no-bodies-for-excluded",
	    		"-pp",
	    		"jb",
	    		"use-original-names:true",
	    		"-keep-line-number",
	    		"-f",
	    		"J",
	    		"-cp",
	    		"F:\\workspace4eclipse\\DemandDrivenRd\\bin_testee\\",
	    		"-process-dir",
	    		"F:\\workspace4eclipse\\DemandDrivenRd\\bin_testee\\"
	    }));
	    
	    AnalysisConfig config = AnalysisConfig.getInstance();
	    config.setMethodSig(startMethod);
		config.setLineNum(lineNum);
		config.setVarName(value);
	    
	    
	    Pack wjtp = PackManager.v().getPack("wjtp");
		TestRDTransformer trans =  new TestRDTransformer();
		trans.setDefinitionFinder(new TypeDefinitionFinder());
		
		wjtp.add(new Transform("wjtp.testdr", trans));
	    args = argsList.toArray(new String[0]);
	    soot.Main.main(args);
	}

}