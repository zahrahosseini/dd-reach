package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.HashSet;
import java.util.Set;

import soot.Value;

/**
 * This is a trivial alias finder which 
 * will always return an empty set for any value.
 * We use this alias finder in the case that
 * we just ignore all the aliases.
 * 
 * @author ChengZhang
 *
 */
public class NonAliasFinder implements AliasFinder {

	@Override
	public Set<Value> getAliases(Value v) {
		return new HashSet<Value>();
	}

}
