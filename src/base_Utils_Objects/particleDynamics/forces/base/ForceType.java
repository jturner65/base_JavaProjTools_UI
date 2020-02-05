package base_Utils_Objects.particleDynamics.forces.base;

import java.util.HashMap;
import java.util.Map;

public enum ForceType {
	F_NONE(0), S_SCALAR(1), S_VECTOR(2), ATTR(3), REPL(4), DAMPSPRING(5), DSPR_THETABAR(6);		
	private int value; 
	private static String[] _typeName = new String[]{"None", "Gravity-type force (scalar particle quantity)", "Air Drag-type force (vector particle quantity)", "Attractor", "Repulsor", "Damped Spring", "Force back to ThetaBar"};
	private static Map<Integer, ForceType> map = new HashMap<Integer, ForceType>(); 
	static { for (ForceType enumV : ForceType.values()) { map.put(enumV.value, enumV);}}
	private ForceType(int _val){value = _val;} 
	public int getVal(){return value;} 	
	public static ForceType getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum	
	public String getName() {return _typeName[value];}
	public static String getName(int _val) {return _typeName[_val];}
	@Override
    public String toString() { return Character.toString(_typeName[value].charAt(0)).toUpperCase() + _typeName[value].substring(1).toLowerCase(); }	
};
