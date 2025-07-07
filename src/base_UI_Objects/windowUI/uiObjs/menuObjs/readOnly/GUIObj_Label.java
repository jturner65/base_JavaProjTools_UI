package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * This class only displays a Label with no data
 */
public class GUIObj_Label extends Base_GUIObj implements IReadOnlyGUIObj {

    public GUIObj_Label(int _objID, GUIObj_Params objParams) {    super(_objID, objParams);}
    
    /**
     * Don't want to add the colon to the read-only labels
     */
    @Override
    protected void setDispLabel() {_dispLabel = label + (isValueRange() ? " Range" : "");}
    /**
     * Get this UI object's value as a string - overridden by classes that do not use val directly
     * @return
     */
    @Override
    protected final String getValueAsString(double _notUsed) {    return "";}
    
    /**
     * Get string data array representing the value this UI object holds
     * @return
     */
    @Override
    protected final String[] getStrDataForVal() {  return new String[0];}
    
    /**
     * Val can be set to whatever number desired - read only numeric objects have no bounds
     * @param _val
     * @return
     */
    @Override
    protected double forceBounds(double _val) {return _val;}
    /**
     * Set a new modifier value to use for this object : Mod values for read-only fields objects will always be 0
     * @param _unused
     */
    @Override
    public final void setNewMod(double _newval) {}
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

    @Override
    protected final boolean checkUIObjectStatus_Indiv() {return true;   }
  
    
}//GUIObj_Label
