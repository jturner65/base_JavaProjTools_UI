package base_Utils_Objects.particleDynamics.constraints.base;

import java.util.HashMap;
import java.util.Map;

public enum ConstraintType {
	C_NONE(0), C_Circular(1);
	private int value; 
	private static String[] _typeName = new String[]{"None", "Circular/Bar-type constraint"};
	private static Map<Integer, ConstraintType> map = new HashMap<Integer, ConstraintType>(); 
	static { for (ConstraintType enumV : ConstraintType.values()) { map.put(enumV.value, enumV);}}
	private ConstraintType(int _val){value = _val;} 
	public int getVal(){return value;} 	
	public static ConstraintType getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}
	public String getName() {return _typeName[value];}
	public static String getName(int _val) {return _typeName[_val];}
	@Override
    public String toString() { return Character.toString(_typeName[value].charAt(0)).toUpperCase() + _typeName[value].substring(1).toLowerCase(); }	
};