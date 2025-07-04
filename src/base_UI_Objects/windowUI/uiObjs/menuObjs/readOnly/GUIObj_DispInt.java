package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_Int;

/**
 * Wrapper class for an integer display object that is read only and is not changed
 * by user interaction.
 */
public class GUIObj_DispInt extends GUIObj_Int implements IReadOnlyGUIObj {
    /**
     * 
     * @param _objID
     * @param objParams
     */
    public GUIObj_DispInt(int _objID, GUIObj_Params objParams) {        
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

    /**
     * Read-only fields ignore any mod so just return original val
     */
    @Override
    public final double modValAssign(double _notUsed) { return getVal();}
    /**
     * Dragging is disabled on read-only fields
     */
    @Override
    public final double dragModVal(double _notUsed) {return getVal();}
    /**
     * Click modification is disabled on read-only fields
     */
    public final double clickModVal(double _notUsed, double _notUsed2) {return getVal();}
    
    /**
     * Display-only values should never update the consuming updater
     */
    @Override
    public final boolean shouldUpdateConsumer() {return false;}    

}//class GUIObj_DispInt
