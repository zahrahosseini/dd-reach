package cn.edu.sjtu.stap.dataflow.rd;
/**
 * This class contains three methods which correspond to
 * the three procedures illustrated in Figure 5.5 in the 
 * dissertation of Evelyn Duesterwald.
 * 
 * For the convenience of finding the correspondence between
 * this code and the example in the dissertation, we maintain
 * a mapping of line numbers in the end of this source file.
 * 
 * @author ChengZhang
 *
 */
public class ExampleChpFive5withAlias {
	public static A x;  /* global variable x */
	static void  proc1(){
		A y; /* local variable y */
		x = new B(); 
		y = new A(); /* these assignments simulate read(x, y) */

		if(x == null){
			proc3(x);
		}
		
		y = x;
		proc2(y);
		
		/* the printing statement simulates write(x, y) System.out.printf("x: %d, y: %d", x, y);*/
		write(x, y);
	}
	
	static void proc2(A f){
		if(f == x){
			proc3(f);
		}
	}
	
	static void proc3(A g){
		if(g == x){
			x = g;
		}
	}
	
	public static void main( String args[] ) {
		proc1();
	}
	
	static void write(A x, A y){
		System.out.printf(x.toString()+y.toString());
	}
	
	/*
	 * 
	 * mapping between line numbers: 
	 *  dissertation  source code
	 *       1           --
	 *       2           19
	 *       3           21
	 *       4           22
	 *       5           25
	 *       6           26
	 *       7           29
	 *       8           --
	 *       9           --
	 *      10           33
	 *      11           34
	 *      12           --
	 *      13           --
	 *      14           39
	 *      15           40
	 *      16           --
	 *      
	 */
} 


class A{}
class B extends A{};