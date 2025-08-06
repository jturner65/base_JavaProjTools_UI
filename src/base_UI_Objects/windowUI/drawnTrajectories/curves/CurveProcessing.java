package base_UI_Objects.windowUI.drawnTrajectories.curves;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum describes the various kinds of general point-array-based curve processing supported
 */
public enum CurveProcessing {
    subdivide,
    tuck,
    equalDist,
    resample;
    private static final String[] 
            _typeExplanation = new String[]{
                    "Subdivide the curve", 
                    "Smooth the curve by moving each point toward the average of its two neighbors by some amount",
                    "Rebuild the curve so that all points are equidistant from each other",
                    "Resample the curve so that it is made up of some new number of equidistant points"};
    private static final String[] 
            _typeName = new String[]{"Subdivide", "Tuck", "Equal Distance", "Resample"};
    
    public static String[] getListOfTypes() {return _typeName;}
    private static Map<Integer, CurveProcessing> map = new HashMap<Integer, CurveProcessing>(); 
        static { for (CurveProcessing enumV : CurveProcessing.values()) { map.put(enumV.ordinal(), enumV);}}
    public int getVal() {return ordinal();}     
    public int getOrdinal() {return ordinal();}
    public static CurveProcessing getEnumByIndex(int idx){return map.get(idx);}
    public static CurveProcessing getEnumFromValue(int idx){return map.get(idx);}
    public static int getNumVals(){return map.size();}                        //get # of values in enum
    public String getName() {return _typeName[ordinal()];}
    public String getBrfName() {return _typeName[ordinal()];}
    public String getExplanation() {return _typeExplanation[ordinal()];}
    public static String getNameByVal(int _val) {return _typeName[_val];}
    public static String getExplanationByVal(int _val) {return _typeExplanation[_val];}
    @Override
    public String toString() { return ""+this.name()+":"+_typeExplanation[ordinal()]; }    
    public String toStrBrf() { return ""+_typeExplanation[ordinal()]; } 

}//enum CurveProcessing
