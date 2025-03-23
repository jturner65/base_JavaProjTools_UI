package base_UI_Objects.windowUI.base;

import java.util.ArrayList;
import java.util.TreeMap;

import base_UI_Objects.windowUI.uiData.UIDataUpdater;

/**
 *	Describes the methods that must be implemented in a construct that will own a UIObjectManager
 */
public interface IUIManagerOwner {
	
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
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is object type (GUIObj_Type enum)
	 *           the 5th element is boolean array of : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *           	idx 3: if true, do not build prefix ornament
	 *              idx 4: if true and prefix ornament is built, make it the same color as the text fill color. 
	 * @param tmpListObjVals
	 */
	public void setupOwnerGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
	
	/**
	 * Build button descriptive arrays : each object array holds true label, false label, and idx of button in owning child class
	 * this must return count of -all- booleans managed by privFlags, not just those that are interactive buttons (some may be 
	 * hidden to manage booleans that manage or record state)
	 * @param tmpBtnNamesArray ArrayList of Object arrays to be built containing all button definitions. 
	 * @return count of -all- booleans to be managed by privFlags
	 */
	public int initAllOwnerUIButtons(ArrayList<Object[]> tmpBtnNamesArray);
	
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
	 * Switch structure only that handles priv flags being set or cleared. Called from WinAppPrivStateFlags structure
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	public void handleOwnerPrivFlags(int idx, boolean val, boolean oldVal);
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	public void handlePrivFlagsDebugMode(boolean val);

}//interface IUIManagerOwner
