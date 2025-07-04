package base_UI_Objects.windowUI.uiObjs.menuObjs.numeric;

import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public class GUIObj_Float extends Base_GUIObj {
    /**
     * Build a float-based UI object
     * @param _objID the index of the object in the managing container
     * @param _name the name/display label of the object
     * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
     * @param _initVal the initial value of this object
     * @param _flags any preset behavior flags
     * @param strkClr stroke color of text
     * @param fillClr fill color around text
     */
    public GUIObj_Float(int _objID, GUIObj_Params objParams) {
        super(_objID, objParams);
    }
    
    /**
     * Float object has no manipulations during modification
     */
    @Override
    protected double modValAssign(double _val) {return _val;}
    
    /**
     * Set a new modifier value to use for this object
     * @param _newval
     */
    @Override
    public final void setNewMod(double _newval){
        double minMaxDiff = getMinMaxDiff();
        if (_newval > minMaxDiff) {    _newval = minMaxDiff;}
        modMult = _newval;
        // update format string based on new mod
        setFormatString(modMult); 
    }

    @Override
    protected boolean checkUIObjectStatus_Indiv() {return true;    }    
        
}//class myGUIObj_Float
