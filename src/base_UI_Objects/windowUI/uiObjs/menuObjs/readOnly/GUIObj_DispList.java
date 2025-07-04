package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_List;

/**
 * Wrapper class to manage a read only version of a GUIObj_List, 
 * which can display multiple string values but not be interactively modified
 */
public class GUIObj_DispList extends GUIObj_List implements IReadOnlyGUIObj {
      
    public GUIObj_DispList(int _objID, GUIObj_Params objParams) { super(_objID, objParams);    }
    @Override
    protected final boolean checkUIObjectStatus_Indiv() {return true;   }
    
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
    
    
}//class GUIObj_DispList
