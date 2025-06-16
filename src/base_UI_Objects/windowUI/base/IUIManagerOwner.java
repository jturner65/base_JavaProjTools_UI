package base_UI_Objects.windowUI.base;

import java.util.TreeMap;

import base_Math_Objects.vectorObjs.doubles.myVector;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 *	Describes the methods that must be implemented in a construct that will own a UIObjectManager
 */
public interface IUIManagerOwner {
	/**
	 * Retrieve the Owner's class ID
	 * @return
	 */
	public int getID();
	
	/**
	 * Retrieve the Owner's class name, for debug messages
	 * @return
	 */
	public String getClassName();
	
	/**
	 * This function is called on ui value update, to pass new ui values on to window-owned consumers
	 */
	public void updateOwnerCalcObjUIVals();
	
	/**
	 * Initialization specifics for the owning manager
	 */
	public void initOwnerStateDispFlags();
	
	/**
	 * Access to retrieve appropriate initial uiClkCoords based on parent to manager owner.
	 * @return
	 */
	public float[] getOwnerParentWindowUIClkCoords(); 
	
	/**
	 * Called if int-handling guiObjs[UIidx] (int or list) has new data which updated UI adapter. 
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param ival integer value of new data
	 * @param oldVal integer value of old data in UIUpdater
	 */
	public void setUI_OwnerIntValsCustom(int UIidx, int ival, int oldVal);
	
	/**
	 * Called if float-handling guiObjs[UIidx] has new data which updated UI adapter.  
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param val float value of new data
	 * @param oldVal float value of old data in UIUpdater
	 */
	public abstract void setUI_OwnerFloatValsCustom(int UIidx, float val, float oldVal);

	/**
	 * Build appropriate UIDataUpdater instance for application
	 * @return
	 */	
	public UIDataUpdater buildOwnerUIDataUpdateObject();
	
	/**
	 * Retrieve the Owner's UIDataUpdater
	 * @return
	 */
	public UIDataUpdater getUIDataUpdater();
	
	/**
	 * Build all UI objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
	 * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
	 * 			- The object IDX                   
	 *          - A double array of min/max/mod values                                                   
	 *          - The starting value                                                                      
	 *          - The label for object                                                                       
	 *          - The object type (GUIObj_Type enum)
	 *          - A boolean array of behavior configuration values : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *          - A boolean array of renderer format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
	 * 				the first element is the object index
	 * 				the second element is true label
	 * 				the third element is false label
	 * 				the final element is integer flag idx 
	 */
	public void setupOwnerGUIObjsAras(TreeMap<String, GUIObj_Params> tmpUIObjMap, TreeMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap);
	
	/**
	 * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
	 */
	public int getTotalNumOfPrivBools();
	
	/**
	 * set initial values for private flags for instancing window - set before initMe is called
	 */	
	public int[] getOwnerFlagIDXsToInitToTrue();
	
	/**
	 * Called by privFlags bool struct, to update uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	public void checkSetBoolAndUpdate(int idx, boolean val);

	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public void updateBoolValFromExecCode(int idx, boolean value);
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public void updateIntValFromExecCode(int idx, int value);
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public void updateFloatValFromExecCode(int idx, float value);
	
	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from WinAppPrivStateFlags structure
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	public void handleOwnerPrivFlags(int idx, boolean val, boolean oldVal);
	
	/**
	 * Application-specific Debug mode functionality. Called only from privflags structure
	 * @param val
	 */
	public void handleOwnerPrivFlagsDebugMode(boolean val);
	
	////////////////////////
	/// Start Mouse interaction - these should provide adapter-like access to the uiManager's mouse handling routines
	/**
	 * Handle mouse interaction via a mouse click
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param mseBtn which button is pressed : 0 is left, 1 is right
	 * @return whether a UI object was clicked in
	 */
	public boolean handleMouseClick(int mouseX, int mouseY, int mseBtn);
	
	/**
	 * Handle mouse interaction via the mouse moving over a UI object
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @return whether a UI object has the mouse pointer moved over it
	 */	
	public boolean handleMouseMove(int mouseX, int mouseY);
	
	/**
	 * Handle mouse interaction via the mouse wheel
	 * @param ticks
	 * @param mult amount to modify view based on sensitivity and whether shift is pressed or not
	 * @return whether a UI object has been modified via the mouse wheel
	 */
	public boolean handleMouseWheel(int ticks, float mult);
	
	/**
	 * Handle mouse interaction via the clicked mouse drag
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param pmouseX previous mouse x on screen
	 * @param pmouseY previous mouse y on screen
	 * @param mseDragInWorld vector of mouse drag in the world, for interacting with trajectories
	 * @param mseBtn what mouse btn is pressed
	 * @return whether a UI object has been modified via a drag action
	 */
	public boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn);

	/**
	 * Handle mouse interactive when the mouse button is released - in general consider this the end of a mouse-driven interaction
	 */	
	public void handleMouseRelease();
	////////////////////////
	/// End Mouse interaction
	
}//interface IUIManagerOwner
