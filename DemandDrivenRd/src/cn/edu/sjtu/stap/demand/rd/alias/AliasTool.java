package cn.edu.sjtu.stap.demand.rd.alias;

import java.util.Set;

import javax.management.RuntimeErrorException;

import cn.edu.sjtu.stap.demand.rd.RdValue;
import soot.jimple.paddle.*;
public class AliasTool {
	
	public static final int SPARK = 1;
	public static final int PADDLE = 2;
	
	private static AliasResult spark_instance = null;
	private static AliasResult paddle_instance = null;
			
	public static AliasResult v( int type ) {
		switch( type ) {
		case SPARK:
			if( spark_instance == null ) {
				spark_instance = new SparkAliasResult();
			}
			return spark_instance;
		case PADDLE:
			// TODO: add Paddle analysis
			return null;
		}
		throw new RuntimeException("ERROR: not such alias result:"+type);
	}
}
