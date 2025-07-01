package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.base.Base_ReadOnlyGUIObj;

/**
 * Label that displays a numeric value that may change but cannot be interacted with.
 */
public class GUIObj_DispNumber extends Base_ReadOnlyGUIObj {
    /**
     * 
     * @param _objID
     * @param objParams
     */
    public GUIObj_DispNumber(int _objID, GUIObj_Params objParams) {        
        super(_objID, objParams);      
        modMult = 0.0;
        formatStr = "%.0f";    
     }
    
    /**
     * Val can be set to whatever number desired - read only numeric objects have no bounds
     * @param _val
     * @return
     */
    @Override
    protected double forceBounds(double _val) {return _val;}
    
    
    @Override
    protected final String getValueAsString(double _val) {    return String.format(formatStr,_val);}


}//class GUIObj_DispNumber
