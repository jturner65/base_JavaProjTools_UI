package base_UI_Objects.windowUI.drawnTrajectories.offsets.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum that describes the 3 types of offset "sidewalks" supported by the trajectory manager.
 */
public enum TrajOffsetType {
    NormalOffset(1),
    BallOffset(2),
    RadialOffset(3);
    
    private int value;
    private static final String[] _typeExplanation = new String[]{
            "Offset of specified distance normal to trajectory",
            "Offset built by placing a ball on the trajectory and then finding the edge that is tangent to subsequent balls",
            "Offset built by centering a circle of a specified radius on the trajectory and then finding the edge that is tangent to the circles"
    };
    private static final String[] _typeName = new String[]{"Normal Offset", "Ball Offset", "Radial Offset"};
    private TrajOffsetType(int _val) {  value = _val;}
    
    
    private static Map<Integer, TrajOffsetType> valmap = new HashMap<Integer, TrajOffsetType>(); 
    private static Map<Integer, TrajOffsetType> map = new HashMap<Integer, TrajOffsetType>(); 
    static { for (TrajOffsetType enumV : TrajOffsetType.values()) { valmap.put(enumV.value, enumV); map.put(enumV.ordinal(), enumV);}}
    public String getName() {return this.name();}
    public int getVal(){return value;}     
    public static TrajOffsetType getEnumByIndex(int idx){return map.get(idx);}
    public static TrajOffsetType getEnumFromValue(int idx){return valmap.get(idx);}
    public static int getNumVals(){return map.size();}                        //get # of values in enum            
    @Override
    public String toString() { return ""+this.name()+":"+_typeExplanation[ordinal()]; }    
    public String toStrBrf() { return ""+_typeName[ordinal()]; }    


}//enum TrajOffsetType
