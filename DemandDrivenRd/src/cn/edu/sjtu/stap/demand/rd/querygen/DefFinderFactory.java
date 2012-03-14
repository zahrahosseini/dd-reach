package cn.edu.sjtu.stap.demand.rd.querygen;

/**
 * The factory class to generate various instances of implementing classes of
 * interface DefFinder on demand.
 * 
 * @author ChengZhang
 * 
 */
public class DefFinderFactory {
	public static DefFinder getDefFinder(int dfType) {
		DefFinder dfInstance = null;

		switch (dfType) {
			case QueryGenenrator.LIGHT_DEF_FINDER:
				dfInstance = new LightDefFinder();
				break;
			case QueryGenenrator.HEAVY_DEF_FINDER:
				dfInstance = new HeavyDefFinder();
				break;
			default:
				dfInstance = new SimpleDefFinder();
		}
		
		return dfInstance;
	}
}
