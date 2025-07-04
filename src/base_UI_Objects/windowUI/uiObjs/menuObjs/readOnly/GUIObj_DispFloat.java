package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_Float;
/**
 * Wrapper class for a float display object that is read only and is not changed
 * by user interaction.
 */
public class GUIObj_DispFloat extends GUIObj_Float implements IReadOnlyGUIObj {

    public GUIObj_DispFloat(int _objID, GUIObj_Params objParams) {super(_objID, objParams);}
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
}//class GUIObj_DispFloat
