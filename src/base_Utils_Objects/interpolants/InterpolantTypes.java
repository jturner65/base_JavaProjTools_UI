package base_Utils_Objects.interpolants;

import java.util.HashMap;
import java.util.Map;


/**
 * describes the type of interpolant used
 * @author john
 *
 */
public enum InterpolantTypes {	
	linear(0), smoothVelocity(1), smoothAccel(2), sine(3);
	private int value; 
	private static final String[] 
			_typeExplanation = new String[] {"Linear","Cubic (Continuous Velocity)","Quintic (Continuous Accel)","Sine"};
	private static final String[] 
			_typeName = new String[] {"Linear","Cubic","Quintic","Sine"};
	//used for file names
	private static final String[] 
			_typeBrfName = new String[] {"linear","cubic","quintic","sine"};
	
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, InterpolantTypes> map = new HashMap<Integer, InterpolantTypes>(); 
		static { for (InterpolantTypes enumV : InterpolantTypes.values()) { map.put(enumV.value, enumV);}}
	private InterpolantTypes(int _val){value = _val;} 
	public int getVal(){return value;}
	public static InterpolantTypes getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[value];}
	public String getBrfName() {return _typeBrfName[value];}
	public String getExplanation() {return _typeExplanation[value];}
	public static String getNameByVal(int _val) {return _typeName[_val];}
	public static String getBrfNameByVal(int _val) {return _typeBrfName[_val];}
	public static String getExplanationByVal(int _val) {return _typeExplanation[_val];}
	@Override
    public String toString() { return ""+value + ":"+_typeExplanation[value]; }	

}


