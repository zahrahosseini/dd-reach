package cn.edu.sjtu.stap.demand.rd.binding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * This class contains the main algorithms to:
 * 1) Find the common ancestors of two call graph nodes (the source and target).
 * 2) Enumerate all the paths connecting a common ancestor and the source/target.
 * 3) For the CallStackTrace by splicing each pair of backward and forward call chains.
 * 
 * TODO: I think the number of CallStackTrace may be reduced by checking the parameter
 * passing mechanism (e.g., must by pass-by-ref) and some other criteria.
 * 
 * @author ChengZhang
 *
 */
public class CallStackTraceFinder {
	public static List<CallStackTrace> getCallStackTraces(CallGraph cg, SootMethod src, SootMethod tgt){
		List<CallStackTrace> traceList = new ArrayList<CallStackTrace>();
		
		Set<SootMethod> commonAncestors = findCommonAncestors(cg, src, tgt); 
		
		for(SootMethod commAnc : commonAncestors){
			Set<List<Edge>> backwardTraces = new HashSet<List<Edge>>();
			// FIXME: backwardTraces contains a null edge
			findAllChainsBetweenNodes(cg, commAnc, src, null, new ArrayList<Edge>(), backwardTraces);
			Set<List<Edge>> forwardTraces = new HashSet<List<Edge>>();
			findAllChainsBetweenNodes(cg, commAnc, tgt, null, new ArrayList<Edge>(), forwardTraces);
			
			for(List<Edge> bt : backwardTraces){
				for(List<Edge> ft :forwardTraces){
					// splice the backward and forward call chains
					CallStackTrace cst = new CallStackTrace();
					cst.setBackwardTrace(bt);
					cst.setForwardTrace(ft);
					cst.setCommonAncester(commAnc);
					
					traceList.add(cst);
				}
			}
		}
		
		return traceList;
	}

	public static Set<SootMethod> findCommonAncestors(CallGraph cg,
			SootMethod src, SootMethod tgt) {
		// find the common ancestors of the source and target
		Set<SootMethod> ancestorsOfSrc = getCallingAncestors(cg, src);
		Set<SootMethod> ancestorsOfTgt = getCallingAncestors(cg, tgt);
		// the ancestorOfSrc set is modified and will be used as the intersection
		ancestorsOfSrc.retainAll(ancestorsOfTgt);
		
		return ancestorsOfSrc;
	}
	
	public static  void findAllChainsBetweenNodes(CallGraph cg,
			SootMethod src, SootMethod tgt, Edge e, List<Edge> tmpPath, Set<List<Edge>> allCallChains) {
		   if( src == null || tgt == null ){
			   System.err.println("Error: end nodes should never be null.");
			   return;
		   }
		   
		   // check if we've reached our destination
		   if (src.equals(tgt)) {
		      // bookkeeping: add the last node and make a copy of path for later
		      // path.add(fromNode);
		      // allPaths.add(new ArrayList(path));
		      List<Edge> pathCopy = new ArrayList<Edge>(tmpPath);
		      // FIXME: BY longwen 2012/2/22
		      if( e != null ) {
		    	  pathCopy.add(e);
		      }
		      allCallChains.add(pathCopy);
		      return;
		   }

		   // check if we've been here before (no cycles in path)
		   if (isContainedInEdgeList(tmpPath, e))
		      return;

		   // bookkeeping: mark the fromNode as visited
//		   tempPath.add(startNodeIndex);
		   if(e != null){
			   tmpPath.add(e);
		   }
		   
		   //
		   // walk over all edges departing from the fromNode
		   Iterator<Edge> currentOutEdges = cg.edgesOutOf(src);
		   while(currentOutEdges.hasNext()){
			   Edge outEdge = currentOutEdges.next();
			   findAllChainsBetweenNodes(cg, outEdge.tgt(), tgt, outEdge, tmpPath, allCallChains);
		   }
		   
		   // unmark this node again
		   if(tmpPath.size() > 0){
			   tmpPath.remove(tmpPath.size()-1);
		   }
	}
	
	private static boolean isContainedInEdgeList(List<Edge> eList, Edge edge){
		boolean isContained = false;
		for(Edge e : eList){
			if(edge.equals(e)){
				isContained = true;
				break;
			}
		}
		return isContained;
	}

	public static Set<SootMethod> getCallingAncestors(CallGraph cg,
			SootMethod src) {
		Set<SootMethod> ancestors = new HashSet<SootMethod>();
		List<SootMethod> workList = new ArrayList<SootMethod>();
		ancestors.add(src); // this is the corner case, a node is the ancestor of itself.
		workList.add(src);
		
		while(!workList.isEmpty()){
			SootMethod tmpSrc = workList.remove(0);
			Iterator<Edge> eIte = cg.edgesInto(tmpSrc);
			
			while(eIte.hasNext()){
				Edge e = eIte.next();
				// TODO: for now we only consider applications classes (to scale up at the cost of soundness)
				SootMethod srcMethod = e.src();
				if(srcMethod.getDeclaringClass().isApplicationClass()){
					boolean isAddedIn = ancestors.add(srcMethod);
					// if the current src method is already contained in the ancestor set,
					// we do not add it into the work list.
					if(isAddedIn){
						workList.add(srcMethod);
					}
				}
			}
			
		}
		
		return ancestors;
	}
	
	
}
