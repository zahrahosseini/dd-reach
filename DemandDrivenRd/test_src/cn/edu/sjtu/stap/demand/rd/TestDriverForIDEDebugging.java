package cn.edu.sjtu.stap.demand.rd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plume.Option;
import plume.Options;
import plume.Options.ArgException;
import soot.Pack;
import soot.PackManager;
import soot.Transform;
import cn.edu.sjtu.stap.AnalysisConfig;

/**
 * Almost identical to {@link CommandLineAnalysisDriver}
 * 
 * @author ChengZhang
 *
 */
public class TestDriverForIDEDebugging {
	static final String ALIASLOCAL = "LOCAL";
	static final String ALIASOFF = "OFF";
	 
	@Option ("-a alias option")
	public static String aliasOption = ALIASOFF;
	
	@Option ("-m main class") 
	public static String mainClass;
	
	@Option ("-s start method")
	public static String startMethod;
	
	@Option ("-l start line number")
	public static int lineNum;
	
	@Option ("-v value")
	public static String value;
	
	/**
	 * @param args
	 * @throws ArgException 
	 */
	public static void main(String[] args) throws ArgException {
		String[] depJars = new String[]{
			"ant.jar",
			"ant-launcher.jar",
			"antlr-2.7.5.jar",
			"avalon-logkit-2.1.jar",
			"commons-collections-3.2.1.jar",
			"commons-lang-2.4.jar",
			"commons-logging-1.1.jar",
			"hsqldb-1.7.1.jar",
			"jdom-1.0.jar",
			"junit-3.8.1.jar",
			"log4j-1.2.12.jar",
			"maven-ant-tasks-2.0.9.jar",
			"oro-2.0.8.jar",
			"servletapi-2.3.jar",
			"velocity-1.7.jar",
			"velocity-1.7-dep.jar",
			"werken-xpath-0.9.4.jar"
		};
		
		StringBuilder sootClassPath = new StringBuilder(".");
		String jarDir = "G:/62_Log/workspace3.6.1_loganalysis/DemandDrivenRd/benchmarks/velocity/"; // it changes with the local directory
		for(String jar : depJars){
			sootClassPath.append(File.pathSeparator);
			sootClassPath.append(jarDir);
			sootClassPath.append(jar);
		}
		sootClassPath.append(File.pathSeparator);
		sootClassPath.append("stub_bin");
		
	    Options options = new Options("Demand driven command line", TestDriverForIDEDebugging.class);
	    String[] remaining_args = options.parse(args);
	    System.out.println(mainClass);
	    
	    // soot arguments
	    List<String> argsList = new ArrayList<String>(Arrays.asList(remaining_args));
	    argsList.addAll(Arrays.asList(new String[]{
	    		"-main-class",
	    		mainClass,
	    		"-pp",
	    		"-w",
	    		"-app",
	    		"-p",
	    		"jb",
	    		"use-original-names:false",
	    		"-keep-line-number",
	    		"-no-bodies-for-excluded",
	    		"-cp",
	    		sootClassPath.toString(),
	    		"-include",
	    		"org.apache.velocity.",
	    		"-process-dir",
	    		(jarDir + File.separator + "velocity-1.7.jar"),
	    		"-process-dir",
	    		"stub_bin"
	    }));
	    
	    AnalysisConfig config = AnalysisConfig.getInstance();
	    config.setMethodSig(startMethod);
		config.setLineNum(lineNum);
		config.setVarName(value);
	    if( aliasOption.equals(ALIASLOCAL) ) {
	    	config.setAliasFinderType(AnalysisConfig.ALIASTYPE_LOCAL);
	    } else {
	    	config.setAliasFinderType(AnalysisConfig.ALIASTYPE_OFF);
	    }
	    
	    Pack wjtp = PackManager.v().getPack("wjtp");
		TestRDTransformer trans =  new TestRDTransformer();
		trans.setDefinitionFinder(new TypeDefinitionFinder());
		
		wjtp.add(new Transform("wjtp.testdr", trans));
	    args = argsList.toArray(new String[0]);
	    soot.Main.main(args);
		
	}

}
