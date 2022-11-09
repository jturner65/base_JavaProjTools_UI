package base_UI_Objects.windowUI.uiObjs.base;

import java.util.HashMap;
import java.util.Map;
/**
 * enum used to specify supported types of UI objects
 * @author john turner 
 */
public enum GUIObj_Type {
	IntVal(0), FloatVal(1), ListVal(2);
	private int value; 
	private String[] _typeExplanation = new String[] {
			"UI Object holding an integer value",
			"UI Object holding a float value",
			"UI Object holding a list value"};
	private static String[] _typeName = new String[] {"Integer Value","Float Value","List Value"};
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, GUIObj_Type> map = new HashMap<Integer, GUIObj_Type>(); 
	static { for (GUIObj_Type enumV : GUIObj_Type.values()) { map.put(enumV.value, enumV);}}
	private GUIObj_Type(int _val){value = _val;} 
	public int getVal(){return value;}
	public static GUIObj_Type getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[value];}
	@Override
    public String toString() { return ""+value + ":"+_typeExplanation[value]; }	
}
