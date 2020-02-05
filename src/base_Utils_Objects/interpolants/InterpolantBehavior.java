package base_Utils_Objects.interpolants;

import java.util.HashMap;
import java.util.Map;

/**
 * describes the behavior of interpolant when used as an animator
 * @author john
 */
public enum InterpolantBehavior {
	pingPong(0), 
	pingPongStop(1), 
	oneWayFwdLoop(2), 
	oneWayBkwdLoop(3),
	oneWayFwdStopLoop(4), 
	oneWayBkwdStopLoop(5);
	private int value; 
	private static final String[] 
			_typeExplanation = new String[] {"Ping-pong", "Ping-pong w/pause", "1-way fwd loop", "1-way bckwd loop", "1-way fwd pause loop", "1-way bckwd pause loop"};
	private static final String[] 
			_typeName = new String[] {"Ping-pong", "Ping-pong w/pause", "1-way fwd loop", "1-way bckwd loop",  "1-way fwd pause loop", "1-way bckwd pause loop"};
	//used for file names
	private static final String[] 
			_typeBrfName = new String[] {"pingPong", "pingPongStop", "oneWayFwdLoop", "oneWayBkwdLoop", "oneWayFwdStopLoop", "oneWayBkwdStopLoop"};
	
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, InterpolantBehavior> map = new HashMap<Integer, InterpolantBehavior>(); 
		static { for (InterpolantBehavior enumV : InterpolantBehavior.values()) { map.put(enumV.value, enumV);}}
	private InterpolantBehavior(int _val){value = _val;} 
	public int getVal(){return value;}
	public static InterpolantBehavior getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[value];}
	public String getBrfName() {return _typeBrfName[value];}
	public String getExplanation() {return _typeExplanation[value];}
	public static String getNameByVal(int _val) {return _typeName[_val];}
	public static String getBrfNameByVal(int _val) {return _typeBrfName[_val];}
	public static String getExplanationByVal(int _val) {return _typeExplanation[_val];}
	@Override
    public String toString() { return ""+value + ":"+_typeExplanation[value]; }	

}// enum InterpolantBehavior 


