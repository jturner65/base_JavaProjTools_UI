package base_UI_Objects.windowUI.base;

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
	
}//interface IUIManagerOwner
