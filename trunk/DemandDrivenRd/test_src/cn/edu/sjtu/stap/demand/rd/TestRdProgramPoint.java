package cn.edu.sjtu.stap.demand.rd;

import java.util.ArrayList;
import java.util.Map;

import soot.Pack;
import soot.PackManager;
import soot.SceneTransformer;
import soot.Transform;


public class TestRdProgramPoint extends SceneTransformer{

	@Override
	protected void internalTransform(String phaseName, Map options) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args){
		/* The Soot arguments are:
		 * -w -no-bodies-for-excluded 
		 * -pp -p jb use-original-names:true 
		 * -keep-line-number -f J 
		 * -cp G:\62_Log\workspace3.6.1_loganalysis\SootIncrementalSlicing\bin_testee\ 
		 * -process-dir G:\62_Log\workspace3.6.1_loganalysis\SootIncrementalSlicing\bin_testee\ 
		 * -main-class cn.edu.sjtu.stap.autolog.dataflow.rd.CallChainExample
		 */
		ArrayList<String> argList = new ArrayList<String>();
		argList.add("-w");
		argList.add("-no-bodies-for-excluded");
		argList.add("-pp");
		argList.add("-p");
		argList.add("jb");
		argList.add("use-original-names:true");
		argList.add("-keep-line-number");
		argList.add("-f");
		argList.add("J");
		argList.add("-cp");
		argList.add("G:\\62_Log\\workspace3.6.1_loganalysis\\SootIncrementalSlicing\\bin_testee\\");
		argList.add("-process-dir");
		argList.add("G:\\62_Log\\workspace3.6.1_loganalysis\\SootIncrementalSlicing\\bin_testee\\");
		argList.add("-main-class");
		argList.add("cn.edu.sjtu.stap.autolog.dataflow.rd.CallChainExample");
		
		Pack wjtp = PackManager.v().getPack("wjtp");
		wjtp.add(new Transform("wjtp.testrdpoint", new TestRdProgramPoint()));
		
		soot.Main.main(argList.toArray(new String[0]));
	}
	
}
