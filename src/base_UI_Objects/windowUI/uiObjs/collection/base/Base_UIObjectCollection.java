package base_UI_Objects.windowUI.uiObjs.collection.base;

import java.util.ArrayList;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.renderer.MultiLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.SingleLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * Implementations of this class will own 1 or more UI objects to be displayed
 * together
 */
public abstract class Base_UIObjectCollection {
	/**
	 * Render interface
	 */
	public static IRenderInterface ri;
	/**
	 * subregion of window where UI objects this collection manages may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	protected float[] uiClkCoords;
	/**
	 * UI objects in this collection
	 */
	protected Base_GUIObj[] guiObjsAra;
	
	/**
	* array lists of idxs for label/read-only objects
	*/	
	private ArrayList<Integer> guiButtonIDXs;
	/**
	* array lists of idxs for float-based UI objects.
	*/
	private ArrayList<Integer> guiFloatValIDXs;	
	/**
	* array lists of idxs for integer/list-based objects
	*/
	private ArrayList<Integer> guiIntValIDXs;
	/**
	* array lists of idxs for label/read-only objects
	*/	
	private ArrayList<Integer> guiLabelValIDXs;

	public Base_UIObjectCollection(IRenderInterface _ri, float[] _uiClkCoords) {
		ri=_ri;
		System.arraycopy(_uiClkCoords, 0, uiClkCoords, 0, uiClkCoords.length);
		//initialize arrays to hold idxs of int and float items being created.
		guiButtonIDXs = new ArrayList<Integer>();
		guiFloatValIDXs = new ArrayList<Integer>();
		guiIntValIDXs = new ArrayList<Integer>();
		guiLabelValIDXs = new ArrayList<Integer>();
		guiObjsAra = new Base_GUIObj[0];
	}

	/**
	 * Build the renderer for a UI object 
	 * @param _owner
	 * @param _start
	 * @param _end
	 * @param _off
	 * @param _menuWidth max width of menu
	 * @param _colors : index 0 is stroke, index 1 is fill
	 * @param guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 				idx 0 : Should be multiline
	 * 				idx 1 : Should have ornament
	 * 				idx 2 : Ornament color should match label color 
	 * @return
	 */
	protected Base_GUIObjRenderer buildObjRenderer(
			Base_GUIObj _owner, 
			double[] _off,
			float _menuWidth,
			int[][] _colors, 
			boolean[] guiFormatBoolVals) {
		
		int[] _strkClr = _colors[0];
		int[] _fillClr= _colors[1]; 
		if (guiFormatBoolVals[0]) {
			return new MultiLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);
		} else {
			return new SingleLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);			
		}
	}//buildObjRenderer
	
	
	/**
	 * Retrieve the hotspot for this collection
	 * @return
	 */
	public final float[] getUiClkCoords() {		return uiClkCoords;	}
	
	/**
	 * Set the hotspot for this collection
	 * @param _uiClkCoords
	 */
	public final void setUiClkCoords(float[] _uiClkCoords) {		
		System.arraycopy(_uiClkCoords, 0, uiClkCoords, 0, uiClkCoords.length);	
	}
	
	/**
	 * Draw the UI clickable region rectangle, for debug
	 */
	private final void drawUIRect() {
		ri.setStrokeWt(2.0f);
		ri.setNoFill();
		ri.setColorValStroke(IRenderInterface.gui_DarkCyan, 255);
		ri.drawRect(uiClkCoords[0], uiClkCoords[1], uiClkCoords[2]-uiClkCoords[0], uiClkCoords[3]-uiClkCoords[1]);
	}
	
	protected abstract void drawDbgGUIObjsInternal(float animTimeMod);
	
	protected abstract void drawGUIObjsInternal(int msClkObj, float animTimeMod);
	
	/**
	 * Draw all gui objects, with appropriate highlights for debug and if object is being edited or not
	 * @param isDebug
	 * @param animTimeMod
	 */
	public void drawGUIObjs(boolean isDebug, int msClkObj, float animTimeMod) {
		ri.pushMatState();
		//draw UI Objs
		if(isDebug) {
			drawDbgGUIObjsInternal(animTimeMod);
			drawUIRect();
		} else {			
			drawGUIObjsInternal(msClkObj, animTimeMod);
		}	
		ri.popMatState();	
	}//drawAllGuiObjs
	
	/**
	 * Check if the screen location [x,y] lies within the UI region this object manages
	 * @param x
	 * @param y
	 * @return
	 */
	public final boolean checkIn(int x, int y) {return ((x > uiClkCoords[0])&&(x <= uiClkCoords[2]) && (y > uiClkCoords[1])&&(y <= uiClkCoords[3]));}

}//class Base_UIObjectCollection
