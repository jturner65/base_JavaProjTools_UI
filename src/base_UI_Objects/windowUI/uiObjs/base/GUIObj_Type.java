package base_UI_Objects.windowUI.uiObjs.base;

import java.util.HashMap;
import java.util.Map;
/**
 * enum used to specify supported types of UI objects
 * @author john turner 
 */
public enum GUIObj_Type {
    IntVal, 
    FloatVal, 
    ListVal, 
    SpacerObj, 
    LabelVal, 
    DispIntVal, 
    DispFloatVal, 
    DispStr, 
    Button, 
    Switch;
    private static final String[] _typeExplanation = new String[]{
            "UI Object holding an integer value",
            "UI Object holding a float value",
            "UI Object holding a list value",
            "Non-display object used as a spacer",
            "UI Object holding a display label",
            "UI Object holding a read-only integer value",
            "UI Object holding a read-only float value",
            "UI Object holding a read-only string value",
            "UI Object representing a button with 2 or more states",
            "UI Object representing toggle button with 2 states, connected to a privFlags structure"};
    private static final String[] _typeName = new String[]{
            "Integer Value","Float Value","List Value","Spacer","Label","Read-Only Int Val","Read-Only Float Val","Read-Only String Value", "Button Object", "Toggle Switch"
        };
    public static String[] getListOfTypes() {return _typeName;}
    private static Map<Integer, GUIObj_Type> map = new HashMap<Integer, GUIObj_Type>(); 
    static { for (GUIObj_Type enumV : GUIObj_Type.values()) { map.put(enumV.ordinal(), enumV);}}
    public int getVal() {return ordinal();}
    public int getOrdinal() {return ordinal();}     
    public static GUIObj_Type getEnumByIndex(int idx){return map.get(idx);}
    public static GUIObj_Type getEnumFromValue(int idx){return map.get(idx);}
    public static int getNumVals(){return map.size();}                        //get # of values in enum
    public String getName() {return _typeName[ordinal()];}
    @Override
    public String toString() { return ""+this.name()+":"+_typeExplanation[ordinal()]; }    
    public String toStrBrf() { return ""+_typeName[ordinal()]; }    
}
