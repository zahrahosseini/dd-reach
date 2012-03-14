package cn.edu.sjtu.stap.demand.rd.querygen;

import java.util.Set;

import soot.Value;

public interface AliasFinder {
	public Set<Value> getAliases(Value v);
}
