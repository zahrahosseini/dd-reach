package cn.edu.sjtu.stap;



/**
 * This class contains the configurations
 * which are global to the whole analysis.
 * 
 * @author ChengZhang
 *
 */
public class AnalysisConfig {
	private static AnalysisConfig configInstance;
	
	private AnalysisConfig(){
		// just make the default constructor private
		// to use the singleton pattern.
	}
	
	public static AnalysisConfig getInstance(){
		if(configInstance == null){
			configInstance = new AnalysisConfig();
		}
		
		return configInstance;
	}
	
	/*
	 * Here are the configuration items.
	 */
	// the Soot-style method signature of the slicing criterion
	private String methodSig;
	// the line number of the slicing criterion statement
	private int lineNum;
	// the variable name of the slicing criterion
	private String varName;
	// the type of the variable of the slicing criterion
	private String varType;
	
	// the type of def finder used for query generation
	private int defFinderType;
	// the type of alias finder used for query generation
	private int aliasFinderType;

	public String getMethodSig() {
		return methodSig;
	}

	public void setMethodSig(String methodSig) {
		this.methodSig = methodSig;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public int getDefFinderType() {
		return defFinderType;
	}

	public void setDefFinderType(int defFinderType) {
		this.defFinderType = defFinderType;
	}

	public int getAliasFinderType() {
		return aliasFinderType;
	}
	
	public static final int ALIASTYPE_OFF = 0;
	public static final int ALIASTYPE_LOCAL = 1;
	
	public void setAliasFinderType(int aliasFinderType) {
		this.aliasFinderType = aliasFinderType;
	}
	
	
}
