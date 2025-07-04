package base_UI_Objects.windowUI.uiObjs.menuObjs.numeric;

import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public class GUIObj_Int extends Base_GUIObj {

    /**
     * Build an int-based UI object
     * @param _objID the index of the object in the managing container
     * @param _name the name/display label of the object
     * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
     * @param _initVal the initial value of this object
     * @param _flags any preset behavior flags
     */
    public GUIObj_Int(int _objID, GUIObj_Params objParams) {super(_objID, objParams);}
    
    /**
     * Called after UI modification but before range checking - this class only supports integer values.
     */
    @Override
    protected double modValAssign(double _val) {return Math.round(_val);}
    
    /**
     * Set a new modifier value to use for this object : Mod values for integer-based objects will always be integral values
     * @param newMod
     */
    @Override
    public void setNewMod(double newMod){    
        modMult = Math.round(newMod);
        formatStr = "%.0f";
    }
    
    @Override
    protected boolean checkUIObjectStatus_Indiv() {return true;}
    
}//class myGUIObj_Int
