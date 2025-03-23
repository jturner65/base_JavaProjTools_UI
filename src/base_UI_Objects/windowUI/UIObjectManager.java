package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.uiObjs.base.*;
import base_UI_Objects.windowUI.uiObjs.base.base.*;
import base_UI_Objects.windowUI.uiObjs.menuObjs.*;
import base_UI_Objects.windowUI.uiObjs.renderer.MultiLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.SingleLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;
import base_Utils_Objects.io.messaging.MessageObject;

/**
 * This class will manage all aspects of UI object creation, placement, rendering and interaction.
 * TODO Build an interface with necessary methods for this construct for its owner to interact with.
 * @author John Turner
 *
 */
public class UIObjectManager {
	/**
	 * Used to render objects
	 */
	public static IRenderInterface ri;
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	/**
	* array lists of idxs for float-based UI objects
	*/
	private ArrayList<Integer> guiFloatValIDXs;
	/**
	* array lists of idxs for integer/list-based objects
	*/
	private ArrayList<Integer> guiIntValIDXs;	
	
	//UI objects in this window
	//GUI Objects
	private Base_NumericGUIObj[] guiObjs_Numeric;	
	
	public UIObjectManager(GUI_AppManager _AppMgr, IRenderInterface _ri, MessageObject _msgObj) {
		ri = _ri;
		AppMgr = _AppMgr;
		msgObj = _msgObj;
	}

	// UI object creation
	
	/**
	 * build ui objects from maps, keyed by ui object idx, with value being data
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is object type (GUIObj_Type enum)
	 *           the 5th element is boolean array of : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *           the 6th element is a boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 *           }    
	 * @param tmpListObjVals : map of list object data
	 * @param uiClkCoords : 4-element array of upper corner x,y lower corner x,y coordinates for ui rectangular region
	 * @return y coordinate for end of ui region
	 * 
	 */
	public float _buildGUIObjsForMenu(
			TreeMap<Integer, Object[]> tmpUIObjArray, 
			TreeMap<Integer, String[]> tmpListObjVals, 
			float[] uiClkCoords) {
		int numGUIObjs = tmpUIObjArray.size();
		
		double[][] guiMinMaxModVals = new double[numGUIObjs][3];			//min max mod values
		double[] guiStVals = new double[numGUIObjs];						//starting values
		String[] guiObjNames = new String[numGUIObjs];						//display labels for UI components	
		//TODO Get guiColors from user input 
		int[][][] guiColors = new int[numGUIObjs][2][4];		
		
		//idx 0 is value is sent to owning window, 
		//idx 1 is value is sent on any modifications, 
		//idx 2 is if true, then changes to value are not sent to UIDataUpdater structure automatically
		boolean[][] guiBoolVals = new boolean[numGUIObjs][];				//array of UI flags for UI objects
		// idx 0: whether multi-line(stacked) or not
		// idx 1: if true, build prefix ornament
		// idx 2: if true and prefix ornament is built, make it the same color as the text fill color. 
		boolean[][] guiFormatBoolVals = new boolean[numGUIObjs][];		
				
		GUIObj_Type[] guiObjTypes = new GUIObj_Type[numGUIObjs];
		myPointf[][] corners = new myPointf[numGUIObjs][2];
		float textHeightOffset = AppMgr.getTextHeightOffset();
		// first object's start and end point
		myPointf stPt = new myPointf(uiClkCoords[0], uiClkCoords[1], 0);
		myPointf endPt = new myPointf(uiClkCoords[2], uiClkCoords[1]+textHeightOffset, 0);		
		
		for (int i = 0; i < numGUIObjs; ++i) {
			Object[] obj = tmpUIObjArray.get(i);
			boolean[] formatAra;
			if (obj.length == 6) {
				// object has been built with extended format array specified				
				formatAra = (boolean[])obj[5];
			} else {
				// Not specified, use default values - {false (single line), true (use prefix), false (don't use label color for prefix)}
				formatAra = new boolean[] {false, true,false};
			}
			guiMinMaxModVals[i] = (double[]) obj[0];
			guiStVals[i] = (Double)(obj[1]);
			guiObjNames[i] = (String)obj[2];
			guiObjTypes[i] = (GUIObj_Type)obj[3];
			//TODO Get guiColors from user input/configuration
			guiColors[i] = new int[][] {
						{0,0,0,255}, //stroke
						{0,0,0,255}, // fill
					};

			boolean[] tmpAra = (boolean[])obj[4];
			guiBoolVals[i] = new boolean[(tmpAra.length < 5 ? 5 : tmpAra.length)];
			int idx = 0;
			for (boolean val : tmpAra) {
				guiBoolVals[i][idx++] = val;
			}
			guiFormatBoolVals[i] = new boolean[(formatAra.length < 3 ? 3 : formatAra.length)];
			idx = 0;
			for (boolean val : formatAra) {
				guiFormatBoolVals[i][idx++] = val;
			}
			
			corners[i] = new myPointf[] {new myPointf(stPt), new myPointf(endPt)};
			//move box corners by appropriate amount
			stPt._add(0, textHeightOffset, 0);
			endPt._add(0, textHeightOffset, 0);
		}
		//Make a smaller padding amount for final row
		uiClkCoords[3] =  stPt.y - .5f*textHeightOffset;
		
		//build all objects using these values 
		_buildAllObjects(
				guiObjNames, corners, guiMinMaxModVals, 
				guiStVals, guiBoolVals, guiFormatBoolVals, 
				guiObjTypes, guiColors, tmpListObjVals, AppMgr.getUIOffset(), uiClkCoords[2]);

		// return final y coordinate
		return uiClkCoords[3];
	}//_buildGUIObjsForMenu
	
	
	/**
	 * Build the renderer for a UI object 
	 * @param _owner
	 * @param _corners upper left (idx0) and lower right (idx1) corners of clickable hotspot
	 * @param _start
	 * @param _end
	 * @param _off
	 * @param _menuWidth max width of menu
	 * @param _strkClr
	 * @param _fillClr
	 * @param guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 				idx 0 : Should be multiline
	 * 				idx 1 : Should have ornament
	 * 				idx 2 : Ornament color should match label color 
	 * @return
	 */
	private Base_GUIObjRenderer buildRenderer(
			Base_GUIObj _owner, 
			myPointf[] _corners,
			double[] _off,
			float _menuWidth,
			int[] _strkClr, 
			int[] _fillClr, 
			boolean[] guiFormatBoolVals) {	
		if (guiFormatBoolVals[0]) {
			return new MultiLineGUIObjRenderer(ri, _owner, _corners[0], _corners[1], _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);
		} else {
			return new SingleLineGUIObjRenderer(ri, _owner, _corners[0], _corners[1], _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);			
		}
	}
	
	/**
	 * This will build objects sequentially using the values provided
	 * @param guiObjNames name of each object
	 * @param corners 2-element point array of upper left and lower right corners for object
	 * @param guiMinMaxModVals array of 3 element arrays of min and max value and base modifier
	 * @param guiStVals array of per-object initial values
	 * @param guiBoolVals array of boolean flags describing each object's behavior
	 * @param guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 				idx 0 : Should be multiline
	 * 				idx 1 : Should have ornament
	 * 				idx 2 : Ornament color should match label color 
	 * @param guiObjTypes array of per-object types
	 * @param guiColors 2-element array of int colors, idx0 == stroke, idx1 == fill
	 * @param tmpListObjVals map keyed by object idx where the value is a string array of elements to put in a list object
	 * @param UI_Off Either the ui offset to use for a prefixing ornament before the object's label, or null
	 * @param menuWidth Width of left side menu bar 
	 */
	private void _buildAllObjects(
			String[] guiObjNames, 
			myPointf[][] corners, 
			double[][] guiMinMaxModVals, 
			double[] guiStVals, 
			boolean[][] guiBoolVals,
			boolean[][] guiFormatBoolVals,
			GUIObj_Type[] guiObjTypes, 
			int[][][] guiColors,
			TreeMap<Integer, String[]> tmpListObjVals, 
			double[] UI_off,
			float menuWidth) {
		int numListObjs = 0;
		for(int i =0; i< guiObjNames.length; ++i){
			switch(guiObjTypes[i]) {
				case IntVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Int(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], UI_off, guiColors[i][0], guiColors[i][1]);
					var renderer = buildRenderer(guiObjs_Numeric[i],corners[i], UI_off, menuWidth, guiColors[i][0], guiColors[i][1], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case ListVal : {
					++numListObjs;
					guiObjs_Numeric[i] = new MenuGUIObj_List(i, guiObjNames[i], guiMinMaxModVals[i], 
							guiStVals[i], guiBoolVals[i], UI_off, tmpListObjVals.get(i), guiColors[i][0], guiColors[i][1]);
					var renderer = buildRenderer(guiObjs_Numeric[i],corners[i], UI_off, menuWidth, guiColors[i][0], guiColors[i][1], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case FloatVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Float(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], UI_off, guiColors[i][0], guiColors[i][1]);
					var renderer = buildRenderer(guiObjs_Numeric[i],corners[i], UI_off, menuWidth, guiColors[i][0], guiColors[i][1], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiFloatValIDXs.add(i);
					break;}
				case Button  :{
					//TODO
					msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object ID for a " + guiObjTypes[i].toStrBrf());
					break;
				}
				default : {
					msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object for a " + guiObjTypes[i].toStrBrf());
					break;				
				}
			}
		}
		if(numListObjs != tmpListObjVals.size()) {
			msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+tmpListObjVals.size()+") - some or all of specified list objects will not display properly.");
		}	
	}//_buildAllObjects	
	
	///////////////////////////////
	// UI object interaction
	
	/**
	 * 
	 * @param idx
	 * @param len
	 * @param calFunc
	 * @param desc
	 * @return
	 */
	private boolean _validateUIObjectIdx(int idx, int len, String calFunc, String desc) {
		if (!MyMathUtils.inRange(idx, 0, len)){
			msgObj.dispErrorMessage("Base_uiObjectManager", calFunc, 
				"Attempting to access illegal Numeric UI object to "+desc+" (idx :"+idx+" is out of range). Aborting.");
		return false;
		}		
		return true;
	}
	
	/**
	 * 
	 * @param obj
	 * @param calFunc
	 * @return
	 */
	private boolean _validateIdxIsListObj(Base_GUIObj obj, String calFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.ListVal) {
			msgObj.dispErrorMessage("Base_uiObjectManager", calFunc, 
					"Attempting to access illegal List UI object to "+desc+" (object :"+obj.getName()+" is not a list object). Aborting.");
			return false;
		}
		return true;
	}
	
	/**
	 * Sets the passed UI object's new max value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes
	 * @param maxVal
	 */
	public void setNewUIMaxVal(int idx, double maxVal) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIMaxVal", "set its max value")) {guiObjs_Numeric[idx].setNewMax(maxVal);}	
	}	
	
	/**
	 * Sets the passed UI object's new min value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes
	 * @param minVal
	 */
	public void setNewUIMinVal(int idx, double minVal) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIMinVal", "set its min value")) {guiObjs_Numeric[idx].setNewMin(minVal);}
	}
	
	/**
	 * Force a value to be set in the numeric UI object at the passed IDX
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes and returns 0
	 * @param val
	 * @return the new value that was set, after having been bounded
	 */
	public double setNewUIValue(int idx, double val) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIValue", "set its value")) {guiObjs_Numeric[idx].setVal(val);}
			return 0;
		}		
	
	/**
	 * Set the display text of the passed UI Object, either numeric or boolean
	 * @param idx
	 * @param isNumeric
	 * @param str
	 */
	public void setNewUIDispText(int idx, boolean isNumeric, String str) {
		if (isNumeric) {
			if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIDispText", "set its display text")) {guiObjs_Numeric[idx].setLabel(str);}
			return;
		} else {
			//TODO support boolean UI objects
			if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIDispText", "set its display text")) {guiObjs_Numeric[idx].setLabel(str);}
			return;
		}
	}
	/**
	 * Specify a string to display in the idx'th List UI Object, if it exists, and is a list object
	 * @param idx
	 * @param val
	 * @return
	 */
	public int[] setDispUIListVal(int idx, String val) {		
		if ((!_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setDispUIListVal", "display passed value")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[idx], "setDispUIListVal", "display passed value"))){return new int[0];}
		return ((MenuGUIObj_List) guiObjs_Numeric[idx]).setValInList(val);
	}
	
	/**
	 * Set all the values in the idx'th List UI Object, if it exists, and is a list object
	 * @param idx
	 * @param values
	 * @return
	 */
	public int setAllUIListValues(int idx, String[] values) {		
		if ((!_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setAllUIListValues", "add all list values")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[idx], "setAllUIListValues", "add all list values"))){return -1;}
		return ((MenuGUIObj_List) guiObjs_Numeric[idx]).setListVals(values);
	}
	
	/**
	 * Retrieve the min value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns max value
	 * @return
	 */
	public double getMinUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getMinUIValue","get its min value")) {return guiObjs_Numeric[idx].getMinVal();}
		return Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the max value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns min value
	 * @return
	 */
	public double getMaxUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getMaxUIValue","get its max value")){return guiObjs_Numeric[idx].getMaxVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the mod step value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns 0
	 * @return
	 */
	public double getModStep(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getModStep", "get its mod value")) {return guiObjs_Numeric[idx].getModStep();}
		return 0;
	}
	
	/**
	 * Retrieve the value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns 0
	 * @return
	 */
	public double getUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getUIValue", "get its value")) {return guiObjs_Numeric[idx].getVal();}
		return 0;
	}
	
	/**
	 * Get the string representation of the passed integer listIdx from the UI Object at UIidx
	 * @param UIidx
	 * @param listIdx
	 * @return
	 */
	public String getListValStr(int UIidx, int listIdx) {		
		if ((!_validateUIObjectIdx(UIidx, guiObjs_Numeric.length, "getListValStr", "get a list value at specified idx")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[UIidx], "getListValStr", "get a list value at specified idx"))){return "";}
		return ((MenuGUIObj_List) guiObjs_Numeric[UIidx]).getListValStr(listIdx);
	}
	
	
	
	// UI object rendering
	
	
}//class uiObjectManager
