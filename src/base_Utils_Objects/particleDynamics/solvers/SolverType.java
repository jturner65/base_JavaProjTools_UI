package base_Utils_Objects.particleDynamics.solvers;

import java.util.HashMap;
import java.util.Map;


public enum SolverType {
	GROUND(0), EXP_E(1), MIDPOINT(2), RK3(3), RK4(4), IMP_E(5), TRAP(6), VERLET(7), RK4_G(8);
	private int value; 
	private static String[] _typeName = new String[]{"Ground_Truth", "Explicit_Euler", "Midpoint", "RK3", "RK4", "Implicit_Euler", "Trapezoidal", "Verlet", "Gen_RK4"};
	private static Map<Integer, SolverType> map = new HashMap<Integer, SolverType>(); 
	static { for (SolverType enumV : SolverType.values()) { map.put(enumV.value, enumV);}}
	private SolverType(int _val){value = _val;} 
	public int getVal(){return value;} 	
	public static SolverType getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum			
	public String getName() {return _typeName[value];}
	public static String getName(int _val) {return _typeName[_val];}
	@Override
    public String toString() { return Character.toString(_typeName[value].charAt(0)).toUpperCase() + _typeName[value].substring(1).toLowerCase(); }	
};
