package base_UI_Objects.windowUI.uiObjs.base.base;

import java.util.HashMap;
import java.util.Map;
/**
 * enum used to specify supported types of UI objects
 * @author john turner 
 */
public enum GUIObj_Type {
	IntVal, FloatVal, ListVal, LabelVal, Button;
	private static final String[] _typeExplanation = new String[] {
			"UI Object holding an integer value",
			"UI Object holding a float value",
			"UI Object holding a list value",
			"UI Object holding a read-only value",
			"UI Object representing a button reflecting 2 or more states"};
	private static final String[] _typeName = new String[] {"Integer Value","Float Value","List Value","Read-Only Value", "Button Object"};
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, GUIObj_Type> map = new HashMap<Integer, GUIObj_Type>(); 
	static { for (GUIObj_Type enumV : GUIObj_Type.values()) { map.put(enumV.ordinal(), enumV);}}
	public int getVal(){return ordinal();} 	
	public static GUIObj_Type getEnumByIndex(int idx){return map.get(idx);}
	public static GUIObj_Type getEnumFromValue(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[ordinal()];}
	@Override
    public String toString() { return ""+this.name()+":"+_typeExplanation[ordinal()]; }	
    public String toStrBrf() { return ""+_typeName[ordinal()]; }	
}
