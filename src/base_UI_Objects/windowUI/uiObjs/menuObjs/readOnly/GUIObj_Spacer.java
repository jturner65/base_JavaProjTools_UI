/**
 * 
 */
package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * 
 */
public class GUIObj_Spacer extends Base_GUIObj implements IReadOnlyGUIObj {
    /**
     * Desired width this spacer should take up
     */
    private final float _width;
    /**
     * Desired height this spacer should take up 
     */
    private final float _height;
    /**
     * @param _objID
     * @param objParams
     */
    public GUIObj_Spacer(int _objID, GUIObj_Params objParams) {
        super(_objID, objParams);
        _width = objParams.spacerWidth;
        _height = objParams.spacerHeight;
    }
    
    /**
     * Return the max width feasible for this UI object's text (based on possible values + label length if any)
     * @return
     */
    @Override
    public final float getMaxTextWidth() {    return _width;}
    
    /**
     * Return the height of the text this object will be displaying - don't bother with argument
     * @return
     */
    @Override
    public final float getMaxTextHeight(float _notUsed) {         return _height;}
    
    /**
     * Do not want any labels for a spacer
     */
    @Override
    protected final void setDispLabel() {_dispLabel = "";}
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
     * Object is ignored so val should also be ignored
     * @param _val
     * @return
     */
    @Override
    protected final double forceBounds(double _val) {return _val;}
    /**
     * Set a new modifier value to use for this object : Mod values for read-only fields objects will always be 0
     * @param _unused
     */
    @Override
    public final void setNewMod(double _newval) {}
    /**
     * Spacer should not return any value
     */
    @Override
    public final double modValAssign(double _notUsed) { return getVal();}
    /**
     * Spacer is un draggable
     */
    @Override
    public final double dragModVal(double _notUsed) {return getVal();}
    /**
     * Spacer is unclickable
     */
    public final double clickModVal(double _notUsed, double _notUsed2) {return getVal();}
    /**
     * Spacer is never used by the owning window
     * @return
     */
    @Override
    public final boolean isUsedByWindow() {               return false;} 
    /**
     * Spacer never updates the owning window
     * @return
     */
    @Override
    protected final boolean isUpdateWindowWhileMod() {    return false;}
    /**
     * Spacer is a read only object
     * @return
     */
    @Override
    public final boolean isReadOnly() {                   return true;}
    /**
     * Spacer never updates the consumer
     */
    @Override
    public final boolean shouldUpdateConsumer() {return false;}

    @Override
    protected final boolean checkUIObjectStatus_Indiv() {return true;   }

}//class GUIObj_Spacer
