package base_UI_Objects.windowUI.uiObjs.menuObjs.buttons;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * This is a subtype of button object that has only 2 states and is connected to a flags structure.
 */
public class GUIObj_Switch extends GUIObj_Button {
    
    /**
     * Index in boolean flag array that corresponds to this button - used for boolean toggle buttons
     */
    protected final int boolFlagIDX;
    
    /**
     * @param objID the index of the object in the managing container
     * @param objParams GUIObj_Params construct that will provide :
     *         boolFlagIDX : index in underlying boolean flag structure coupled to this object
     */
    public GUIObj_Switch(int _objID, GUIObj_Params objParams) {
        super(_objID, objParams);
        boolFlagIDX = objParams.boolFlagIDX;
    }
    
    /**
     * Make sure val adheres to either 0 or 1 - if it is greater than 1 than it should be 0
     * @param _val
     * @return
     */
    @Override
    protected double forceBounds(double _val) {return (_val + 2)%2;}
    
    /**
     * Return how many states this button supports
     */
    @Override
    public final int getNumStates() {return 2;}
    /**
     * Set value as a boolean value
     * @param _val
     */
    public final boolean setValueFromBoolean(boolean _val) {        setVal(_val ? 1.0 : 0); return getValueAsBoolean();    }
    
    public final boolean setBooleanLabelVals(String[] labels, boolean setAsDefault) {
        if(labels.length != 2) {            
            labels = new String[]{"List for boolean switch not properly constructed! Must have explicitly 2 values"}; 
        }
        int retVal = setStateLabels(labels, setAsDefault);
        return (retVal != this._offStateIDX);
    }
    
    /**
     * Get the index in the underlying flags structure that this UI object interacts with
     * @return
     */
    public final int getBoolFlagIDX() {return boolFlagIDX;}
    
}//class GUIObj_Switch
