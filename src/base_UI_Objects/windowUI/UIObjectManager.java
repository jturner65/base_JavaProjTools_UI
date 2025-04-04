package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.IUIManagerOwner;
import base_UI_Objects.windowUI.base.WinAppPrivStateFlags;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
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
	
	private final IUIManagerOwner owner;
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	/**
	 * subregion of window where UI objects may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	public float[] uiClkCoords;
	
	/**
	* array lists of idxs for float-based UI objects
	*/
	private ArrayList<Integer> guiFloatValIDXs;
	/**
	* array lists of idxs for integer/list-based objects
	*/
	private ArrayList<Integer> guiIntValIDXs;	
	/**
	* array lists of idxs for integer/list-based objects
	*/
	private ArrayList<Integer> guiLabelValIDXs;		
	/**
	 * Numeric Gui Objects
	 */
	private Base_NumericGUIObj[] guiObjs_Numeric;	

	/**
	 * structure to facilitate communicating UI changes with functional code
	 */
	protected UIDataUpdater uiUpdateData;
	
	/**
	 * Boolean array of default behavior boolean values, if formatting is not otherwise specified
	 *  idx 0: value is sent to owning window,  
	 *  idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *  idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 */
	protected final boolean[] dfltUIBehaviorVals = new boolean[]{true, false, false};	
	/**
	 * Boolean array of default UI format values, if formatting is not otherwise specified : 
	 *  idx 0: whether multi-line(stacked) or not                                                  
	 *  idx 1: if true, build prefix ornament                                                      
	 *  idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	public final boolean[] dfltUIFmtVals =  new boolean[] {false, true, false};	
	
	////////////////////////
	/// owner's private state/functionality flags, (displayed in grid of 2-per-column buttons)
	
	/**
	 * UI Application-specific flags and UI components (buttons)
	 */	
	protected WinAppPrivStateFlags privFlags;		
	/**
	 * Button labels for true value buttons
	 */
	private String[] truePrivFlagLabels; //needs to be in order of flags
	/**
	 * Button labels for false value buttons
	 */
	private String[] falsePrivFlagLabels;//needs to be in order of flags	
	/**
	 * Colors for boolean buttons set to True based on child-class window specific values
	 */
	private int[][] privFlagTrueColors;
	/**
	 * Colors for boolean buttons set to False based on child-class window specific values
	 */
	private int[][] privFlagFalseColors;
	/**
	 * Non random true button color
	 */
	private final int[] trueBtnClr = new int[]{220,255,220,255};
	/**
	 * Non-random false button color
	 */
	private final int[] falseBtnClr = new int[]{255,215,215,255};
	
	/**
	 * False button color to use if button labels are the same and using random colors
	 */
	private static final int[] baseBtnFalseClr = new int[]{180,180,180, 255};
	
	/**
	 * only modifiable idx's will be shown as buttons - this needs to be in order of flag names
	 */
	private int[] privModFlgIdxs;
	/**
	 * Click dimensions for each button
	 */
	private float[][] privFlagBtns;	
	/**
	 * array of priv buttons to be cleared next frame - 
	 * should always be empty except when buttons need to be cleared
	 */
	private ArrayList<Integer> privBtnsToClear;
	
	public UIObjectManager(IRenderInterface _ri, IUIManagerOwner _owner, GUI_AppManager _AppMgr, MessageObject _msgObj) {
		ri = _ri;
		owner = _owner;
		AppMgr = _AppMgr;
		msgObj = _msgObj;
	}
		
	// UI object creation	
	public void initAllGUIObjs(boolean isMenu, float[] uiClkRect) {
		//initialize arrays to hold idxs of int and float items being created.
		guiFloatValIDXs = new ArrayList<Integer>();
		guiIntValIDXs = new ArrayList<Integer>();
		guiLabelValIDXs = new ArrayList<Integer>();
		if (!isMenu) {
			// list box values - keyed by list obj IDX, value is string array of list obj values
			TreeMap<Integer, String[]> tmpListObjVals = new TreeMap<Integer, String[]>();
			// ui object values - keyed by object idx, value is object array of describing values
			TreeMap<Integer, Object[]> tmpUIObjArray = new TreeMap<Integer, Object[]>();
			//  set up all gui objects for this window
			//setup all ui objects and record final y value in sidebar menu for UI Objects in this window
			owner.setupOwnerGUIObjsAras(tmpUIObjArray,tmpListObjVals);					
			//initialized for sidebar menu as well as for display windows
			guiObjs_Numeric = new Base_NumericGUIObj[tmpUIObjArray.size()]; // list of modifiable gui objects
			//build ui objects
			uiClkRect[3] = _buildGUIObjsForMenu(tmpUIObjArray, tmpListObjVals, uiClkRect);	
		} else {
			//no guiObjs for menu
			guiObjs_Numeric = new Base_NumericGUIObj[0];
		}
		//build UI boolean buttons
		ArrayList<Object[]> tmpBtnNamesArray = new ArrayList<Object[]>();
		//  set up all window-specific boolean buttons for this window
		// this must return -all- priv buttons, not just those that are interactive (some may be hidden to manage functional booleans)
		int _numPrivFlags = owner.initAllOwnerUIButtons(tmpBtnNamesArray);
		//initialize all private buttons based on values put in arraylist
		uiClkRect[3] = _buildAllPrivButtons(tmpBtnNamesArray, uiClkRect);
		// init specific sim flags
		privFlags = new WinAppPrivStateFlags(owner,_numPrivFlags);
		// set instance-specific initial flags
		int[] trueFlagIDXs = owner.getOwnerFlagIDXsToInitToTrue();
		//set local value for flags that should be initialized to true (without passing to instancing class handler yet)		
		if(null!=trueFlagIDXs) {_initPassedPrivFlagsToTrue(trueFlagIDXs);}		
		// build instance-specific UI update communication object if exists
		_buildUIUpdateStruct();
		
	}//_initAllGUIObjs
	
	/**
	 * Build the object array that describes a label object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
	 * @return
	 */
	public final Object[] uiObjInitAra_Label(double initVal, String name) {
		return uiObjInitAra_Label(initVal, name, dfltUIFmtVals);
	}		
	
	/**
	 * Build the object array that describes a integer object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @return
	 */
	public final Object[] uiObjInitAra_Label(double initVal, String name, boolean[] boolFmtVals) {
		return new Object[] {new double[0], initVal, name, GUIObj_Type.LabelVal, new boolean[] {false,false,false}, boolFmtVals};	
	}
	
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}	
		
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @return
	 */
	public final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.IntVal,boolVals, boolFmtVals};	
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.FloatVal,boolVals, boolFmtVals};	
	}

	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_List(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_List(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.ListVal,boolVals, boolFmtVals};	
	}	
	
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
	 * @param uiClkRect : 4-element array of upper corner x,y lower corner x,y coordinates for ui rectangular region
	 * @return y coordinate for end of ui region
	 */
	private float _buildGUIObjsForMenu(
			TreeMap<Integer, Object[]> tmpUIObjArray, 
			TreeMap<Integer, String[]> tmpListObjVals, 
			float[] uiClkRect) {
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
		float textHeightOffset = AppMgr.getTextHeightOffset();
	
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
		}		
		//build all objects using these values 
		_buildAllObjects(guiObjNames, guiMinMaxModVals, 
				guiStVals, guiBoolVals, guiFormatBoolVals, 
				guiObjTypes, guiColors, tmpListObjVals, AppMgr.getUIOffset(), uiClkRect[2]);
		//Objects are created by here and assigned renderers
		// Assign hotspots
		myPointf newStPt = new myPointf(uiClkRect[0], uiClkRect[1], 0);
		for (int i = 0; i < guiObjs_Numeric.length; ++i) {
			// Get next newStPt as we calculate the hotspot region for every UI object
			newStPt = guiObjs_Numeric[i].reCalcHotSpot(newStPt, textHeightOffset, uiClkRect[0], uiClkRect[2]);			
		}
		//Make a smaller padding amount for final row
		uiClkRect[3] =  newStPt.y - .5f*textHeightOffset;
		// return final y coordinate
		return uiClkRect[3];
	}//_buildGUIObjsForMenu
		
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
	private Base_GUIObjRenderer buildRenderer(
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
					guiObjs_Numeric[i] = new MenuGUIObj_Int(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case ListVal : {
					++numListObjs;
					guiObjs_Numeric[i] = new MenuGUIObj_List(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], tmpListObjVals.get(i));
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case FloatVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Float(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiFloatValIDXs.add(i);
					break;}
				case LabelVal :{
					guiObjs_Numeric[i] = new MenuGUIObj_DispValue(i, guiObjNames[i], guiStVals[i]);					
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiLabelValIDXs.add(i);
					break;}
				case Button  :{
					//TODO
					msgObj.dispWarningMessage("UIObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object ID for a " + guiObjTypes[i].toStrBrf());
					break;
				}
				default : {
					msgObj.dispWarningMessage("UIObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object for a " + guiObjTypes[i].toStrBrf());
					break;				
				}
			}
		}
		if(numListObjs != tmpListObjVals.size()) {
			msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+tmpListObjVals.size()+") - some or all of specified list objects will not display properly.");
		}	
	}//_buildAllObjects	
	

	/**
	 * calculate button length
	 */
	private float _calcBtnLength(String tStr, String fStr){return MyMathUtils.max(ri.textWidth(tStr), ri.textWidth(fStr));}
	
	private void _setBtnDims(int idx, float xStart, float yEnd, float oldBtnLen, float btnLen) {privFlagBtns[idx]= new float[] {xStart+oldBtnLen, yEnd, btnLen, AppMgr.getTextHeightOffset() };}
	
	/**
	 * Take populated arraylist of object arrays describing private buttons and use these to initialize actual button arrays
	 * @param tmpBtnNamesArray arraylist of object arrays, each entry in object array holding a true string, a false string and an integer idx for the button
	 */	
	private float _buildAllPrivButtons(ArrayList<Object[]> tmpBtnNamesArray, float[] uiClkRect) {
		// finalize setup for UI toggle buttons - convert to arrays
		truePrivFlagLabels = new String[tmpBtnNamesArray.size()];
		falsePrivFlagLabels = new String[truePrivFlagLabels.length];
		privModFlgIdxs = new int[truePrivFlagLabels.length];
		for (int i = 0; i < truePrivFlagLabels.length; ++i) {
			Object[] tmpAra = tmpBtnNamesArray.get(i);
			truePrivFlagLabels[i] = (String) tmpAra[0];
			falsePrivFlagLabels[i] = (String) tmpAra[1];
			privModFlgIdxs[i] = (int) tmpAra[2];
		}
		return _buildPrivBtnRects(0, truePrivFlagLabels.length, uiClkRect);
	}//_buildAllPrivButtons
	
	/**
	 * set up boolean button rectangles using initialized truePrivFlagLabels and falsePrivFlagLabels
	 * @param yDisp displacement for button to be drawn
	 * @param numBtns number of buttons to make
	 */
	private float _buildPrivBtnRects(float yDisp, int numBtns, float[] uiClkRect){
		privFlagBtns = new float[numBtns][];
		float yOffset = AppMgr.getTextHeightOffset();
		if (numBtns == 0) {	return uiClkRect[3];	}
		float maxBtnLen = 0.95f * AppMgr.getMenuWidth(), halfBtnLen = .5f*maxBtnLen;
		uiClkRect[3] += yOffset;
		float oldBtnLen = 0;
		boolean lastBtnHalfStLine = false, startNewLine = true;
		for(int i=0; i<numBtns; ++i){						//clickable button regions - as rect,so x,y,w,h - need to be in terms of sidebar menu 
			float btnLen = _calcBtnLength(truePrivFlagLabels[i].trim(),falsePrivFlagLabels[i].trim());
			//either button of half length or full length.  if half length, might be changed to full length in next iteration.
			//pa.pr("_buildPrivBtnRects: i "+i+" len : " +btnLen+" cap 1: " + truePrivFlagLabels[i].trim()+"|"+falsePrivFlagLabels[i].trim());
			if(btnLen > halfBtnLen){//this button is bigger than halfsize - it needs to be made full size, and if last button was half size and start of line, make it full size as well
				btnLen = maxBtnLen;
				if(lastBtnHalfStLine){//make last button full size, and make button this button on another line
					privFlagBtns[i-1][2] = maxBtnLen;
					uiClkRect[3] += yOffset;
				}
				_setBtnDims(i, uiClkRect[0], uiClkRect[3], 0, btnLen);
				//privFlagBtns[i]= new float[] {(float)(uiClkRect[0]-winInitVals.getXOffset()), (float) uiClkRect[3], btnLen, yOff };				
				uiClkRect[3] += yOffset;
				startNewLine = true;
				lastBtnHalfStLine = false;
			} else {//button len should be half width unless this button started a new line
				btnLen = halfBtnLen;
				if(startNewLine){//button is starting new line
					lastBtnHalfStLine = true;
					_setBtnDims(i, uiClkRect[0], uiClkRect[3], 0, btnLen);
					startNewLine = false;
				} else {//should only get here if 2nd of two <1/2 width buttons in a row
					lastBtnHalfStLine = false;
					_setBtnDims(i, uiClkRect[0], uiClkRect[3], oldBtnLen, btnLen);
					uiClkRect[3] += yOffset;
					startNewLine = true;					
				}
			}			
			oldBtnLen = btnLen;
		}
		if(lastBtnHalfStLine){//set last button full length if starting new line
			privFlagBtns[numBtns-1][2] = maxBtnLen;
			uiClkRect[3] += yOffset;
		}
		uiClkRect[3] += AppMgr.getRowStYOffset();
		initPrivFlagColors();
		return uiClkRect[3];
	}//_buildPrivBtnRects
	
	/**
	 * set up initial colors for sim specific flags for display
	 */
	protected void initPrivFlagColors(){
		privFlagTrueColors = new int[truePrivFlagLabels.length][4];
		privFlagFalseColors = new int[privFlagTrueColors.length][4];
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		for (int i = 0; i < privFlagTrueColors.length; ++i) { 
			privFlagTrueColors[i] = new int[]{tr.nextInt(150),tr.nextInt(100),tr.nextInt(150), 255};
			if(truePrivFlagLabels[i].equals(falsePrivFlagLabels[i])) {
				privFlagFalseColors[i] = baseBtnFalseClr;
			} else {
				privFlagFalseColors[i] = new int[]{0,255-privFlagTrueColors[i][1],255-privFlagTrueColors[i][2], 255};
			}
		}			
	}	
	
	/**
	 * set labels of boolean buttons for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new 
	 * @param fLbl
	 */
	public void setButtonLabels(int idx, String tLbl, String fLbl) {truePrivFlagLabels[idx] = tLbl;falsePrivFlagLabels[idx] = fLbl;}
	
	
	///////////////////////////////
	// UI object interaction
	
	/**
	 * This has to be called after UI structs are built and set - this creates and populates the 
	 * structure that serves to communicate UI data to consumer from UI Window.
	 */
	private void _buildUIUpdateStruct() {
		//set up UI->to->Consumer class communication object - only make instance of object here, 
		//initialize it after private flags are built and initialized
		uiUpdateData = owner.buildOwnerUIDataUpdateObject();
		if (uiUpdateData == null) {return;}
		TreeMap<Integer, Integer> intValues = new TreeMap<Integer, Integer>();    
		for (Integer idx : guiIntValIDXs) {				intValues.put(idx, guiObjs_Numeric[idx].getValueAsInt());}		
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		for (Integer idx : guiFloatValIDXs) {			floatValues.put(idx, guiObjs_Numeric[idx].getValueAsFloat());}
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
		for(Integer i=0; i < privFlags.numFlags;++i) {		boolValues.put(i, privFlags.getFlag(i));}	
		uiUpdateData.setAllVals(intValues, floatValues, boolValues); 
	}//_buildUIUpdateStruct
	
	
	/**
	 * call after single draw - will clear window-based priv buttons that are momentary
	 */
	public final void clearAllPrivBtns() {
		if(privBtnsToClear.size() == 0) {return;}
		//only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : privBtnsToClear) {if (privFlags.getFlag(idx)) {privFlags.setFlag(idx, false);}}
		privBtnsToClear.clear();
	}//clearPrivBtns()

	
	/**
	 * Called by privFlags bool struct, to update uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	public final void checkSetBoolAndUpdate(int idx, boolean val) {
		if((uiUpdateData != null) && uiUpdateData.checkAndSetBoolValue(idx, val)) {
			owner.updateOwnerCalcObjUIVals();
		}
	}

	/**
	 * This will check if boolean value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetBoolValue(int idx, boolean value) {return uiUpdateData.checkAndSetBoolValue(idx, value);}
	/**
	 * This will check if Integer value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetIntVal(int idx, int value) {return uiUpdateData.checkAndSetIntVal(idx, value);}
	/**
	 * This will check if float value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetFloatVal(int idx, float value) {return uiUpdateData.checkAndSetFloatVal(idx, value);}
	
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateBoolValFromExecCode(int idx, boolean value) {privFlags.setFlag(idx, value);uiUpdateData.setBoolValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateIntValFromExecCode(int idx, int value) {guiObjs_Numeric[idx].setVal(value);uiUpdateData.setIntValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateFloatValFromExecCode(int idx, float value) {guiObjs_Numeric[idx].setVal(value);uiUpdateData.setFloatValue(idx, value);}
	
	
	/**
	 * Set UI values by object type, sending value to 
	 * @param UIidx
	 */
	public final void setUIWinVals(int UIidx) {
		//Determine whether int (int or list) or float
		GUIObj_Type objType = guiObjs_Numeric[UIidx].getObjType();
		switch (objType) {
			case IntVal : {
				int ival = guiObjs_Numeric[UIidx].getValueAsInt();
				int origVal = uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj int handling, if pertinent
					owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case ListVal : {
				int ival = guiObjs_Numeric[UIidx].getValueAsInt();
				int origVal = uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj int (list idx)-related handling, if pertinent
					owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case FloatVal : {
				float val = guiObjs_Numeric[UIidx].getValueAsFloat();
				float origVal = uiUpdateData.getFloatValue(UIidx);
				if(checkAndSetFloatVal(UIidx, val)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj float handling, if pertinent
					owner.setUI_OwnerFloatValsCustom(UIidx, val, origVal);
				}
				break;}
			case Button : {
				msgObj.dispWarningMessage(owner.getClassName(), "setUIWinVals", "Attempting to set a value for an unsupported Button UI object : " + objType.toStrBrf());
				break;}
			default : {
				msgObj.dispWarningMessage(owner.getClassName(), "setUIWinVals", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());
				break;}
			
		}//switch on obj type
	}//setUIWinVals	
	
	/**
	 * Reset all values to be initial values. 
	 * @param forceVals If true, this will bypass setUIWinVals, if false, will call set vals, to propagate changes to window vars 
	 */
	public final void resetUIVals(boolean forceVals){
		for(int i=0; i<guiObjs_Numeric.length;++i){				guiObjs_Numeric[i].resetToInit();		}
		if (!forceVals) {
			setAllUIWinVals();
		}
	}//resetUIVals
	/**
	 * set all window values for UI objects
	 */
	protected final void setAllUIWinVals() {for(int i=0;i<guiObjs_Numeric.length;++i){if(guiObjs_Numeric[i].shouldUpdateWin(true)){setUIWinVals(i);}}}
	
		
	/**
	 * clear button next frame - to act like momentary switch.  will also clear UI object
	 * @param idx
	 */
	public final void clearBtnNextFrame(int idx) {_addPrivBtnToClear(idx);		checkAndSetBoolValue(idx, false);}
		
	/**
	 * add a button to clear after next draw
	 * @param idx index of button to clear
	 */
	private final void _addPrivBtnToClear(int idx) {
		privBtnsToClear.add(idx);
	}

	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	private void _initPassedPrivFlagsToTrue(int[] idxs) { 
		privFlags.setAllFlagsToTrue(idxs);
	}	
	
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
	 * Set all the values in the uiObjIdx List UI Object, if it exists, and is a list object
	 * @param uiObjIdx the list obj's index
	 * @param values the list of values to set
	 * @param setAsDefault whether or not these new values should be set as the default values
	 * @return
	 */
	public int setAllUIListValues(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if ((!_validateUIObjectIdx(uiObjIdx, guiObjs_Numeric.length, "setAllUIListValues", "set/replace all list values")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[uiObjIdx], "setAllUIListValues", "set/replace all list values"))){return -1;}
		return ((MenuGUIObj_List) guiObjs_Numeric[uiObjIdx]).setListVals(values, setAsDefault);
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
	
	/////////////////////////////////
	// UI object rendering	
	
	/**
	 * Draw this window's gui objects in sidebar menu
	 * @param animTimeMod
	 */
	public final void drawWindowGuiObjs(boolean isDebug, boolean usRndBtnClrs, float animTimeMod) {
		//draw UI Objs
		drawGUIObjs(isDebug, animTimeMod);
		//draw all boolean-based buttons for this window
		drawAppFlagButtons(usRndBtnClrs);
	}//drawWindowGuiObjs	
	
	
	/**
	 * Draw UI Objs
	 * @param animTimeMod for potential future animated UI Objects
	 */
	public final void drawGUIObjs(boolean isDebug, float animTimeMod) {
		ri.pushMatState();	
		if (isDebug) { 	for(int i =0; i<guiObjs_Numeric.length; ++i){guiObjs_Numeric[i].drawDebug();}}
		else {			for(int i =0; i<guiObjs_Numeric.length; ++i){guiObjs_Numeric[i].draw();}}
		ri.popMatState();	
	}
	
	//draw a series of strings in a row
	public final void dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
		ri.setFill(clrAra, clrAra[3]);
		ri.setColorValStroke(IRenderInterface.gui_Black,255);
		ri.drawRect(loc);		
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		//pa.translate(-xOff*.5f,-yOff*.5f);
		ri.showText(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
		//pa.translate(width, 0);
	}
	
	/**
	 * Draw application-specific flag buttons
	 * @param useRandBtnClrs
	 */
	private final void drawAppFlagButtons(boolean useRandBtnClrs) {
		ri.pushMatState();	
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		String label;
		int[] clr;
		if(useRandBtnClrs){
			for(int i =0; i<privModFlgIdxs.length; ++i){
				if(privFlags.getFlag(privModFlgIdxs[i])){
					label = truePrivFlagLabels[i];
					clr = privFlagTrueColors[i];		
				} else {
					label = falsePrivFlagLabels[i];
					clr = privFlagFalseColors[i];
				}	
				dispBttnAtLoc(label,privFlagBtns[i],clr);	
			}
		} else {
			for(int i =0; i<privModFlgIdxs.length; ++i){
				if(privFlags.getFlag(privModFlgIdxs[i])){
					label = truePrivFlagLabels[i];
					clr = trueBtnClr;
				} else {																
					label = falsePrivFlagLabels[i];
					clr = falseBtnClr;
				}
				dispBttnAtLoc(label,privFlagBtns[i],clr);	
			}	
		}		
		ri.popMatState();	
	}//drawAppFlagButtons
	
	
	
	
	
}//class uiObjectManager
