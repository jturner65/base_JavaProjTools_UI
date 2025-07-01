package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.base.Base_ReadOnlyGUIObj;

/**
 * This class only displays a Label with no data
 */
public class GUIObj_Label extends Base_ReadOnlyGUIObj {

    public GUIObj_Label(int _objID, GUIObj_Params objParams) {    super(_objID, objParams);}

    /**
     * set new display text for this label. Does not add ':'
     * @param _str
     */
    @Override
    public final void setLabel(String _str) {    label = _str;    }
    
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
        protected final String[] getStrDataForVal() {  return new String[0];}
}//GUIObj_Label
