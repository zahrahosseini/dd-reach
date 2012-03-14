package cn.edu.sjtu.stap.demand.rd.querygen;

/**
 * This factory generates instances of implementing classes of interface
 * AliasFinder on demand.
 * 
 * @author ChengZhang
 * 
 */
public class AliasFinderFactory {
	public static AliasFinder getAliasFinder(int afType) {
		AliasFinder afInstance = null;

		switch (afType) {
			case QueryGenenrator.LIGHT_ALIAS_FINDER:
				afInstance = new LightAliasFinder();
				break;
			case QueryGenenrator.HEAVY_ALIAS_FINDER:
				afInstance = new HeavyAliasFinder();
				break;
			default:
				afInstance = new NonAliasFinder();
		}

		return afInstance;
	}
}
