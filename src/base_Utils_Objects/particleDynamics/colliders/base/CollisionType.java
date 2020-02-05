package base_Utils_Objects.particleDynamics.colliders.base;

import java.util.HashMap;
import java.util.Map;


public enum CollisionType {
	CL_NONE(0), FLAT(1), PARTICLE(2), SPHERE(3), BOX(4);
	private int value; 
	private static String[] _typeName = new String[]{"None", "Flat surface", "Particle to particle", "Spherical", "Cylinder", "Bounding Box"};
	private static Map<Integer, CollisionType> map = new HashMap<Integer, CollisionType>(); 
	static { for (CollisionType enumV : CollisionType.values()) { map.put(enumV.value, enumV);}}
	private CollisionType(int _val){value = _val;} 
	public int getVal(){return value;} 	
	public static CollisionType getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum	
	public String getName() {return _typeName[value];}
	public static String getName(int _val) {return _typeName[_val];}
	@Override
    public String toString() { return Character.toString(_typeName[value].charAt(0)).toUpperCase() + _typeName[value].substring(1).toLowerCase(); }	
};