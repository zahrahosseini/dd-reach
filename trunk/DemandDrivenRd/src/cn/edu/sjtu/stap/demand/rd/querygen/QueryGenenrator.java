package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.Set;

import cn.edu.sjtu.stap.AnalysisConfig;

import soot.Body;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * This class generates all the queries for the 
 * REACH (RD) analysis based on the DefFinder and
 * the AliasFinder.
 * 
 * @author ChengZhang
 *
 */
public class QueryGenenrator {
	public static final int SIMPLE_DEF_FINDER = 0;
	public static final int LIGHT_DEF_FINDER = 1;
	public static final int HEAVY_DEF_FINDER = 2;
	
	public static final int NON_ALIAS_FINDER = 0;
	public static final int LIGHT_ALIAS_FINDER = 1;
	public static final int HEAVY_ALIAS_FINDER = 2;
	
	public static Set<RDQuery> generateQueries(Set<Body> bodies, CallGraph cg){
		AnalysisConfig config = AnalysisConfig.getInstance();
		int dfType = config.getDefFinderType();
		int afType = config.getAliasFinderType();
		DefFinder df = DefFinderFactory.getDefFinder(dfType);
		AliasFinder af = AliasFinderFactory.getAliasFinder(afType);
		
		
		
		return null;
	}
}
