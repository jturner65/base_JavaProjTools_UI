package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.IUIManagerOwner;
import base_UI_Objects.windowUI.base.WinAppPrivStateFlags;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_DispValue;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Float;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Int;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_List;
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
	 * Display window/construct owner of this UIObjectManager
	 */
	private final IUIManagerOwner owner;
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	/**
	 * msg object for output to console or log
	 */
	private MessageObject msgObj;
	/**
	 * subregion of window where UI objects may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	private float[] _uiClkCoords;
	/**
	* array lists of idxs for toggle-able multi-state objects
	*/	
	private ArrayList<Integer> _guiButtonIDXs;
	/**
	 * array lists of idxs for float-based UI objects.
	 */
	private ArrayList<Integer> _guiFloatValIDXs;
	
	/**
	 * array lists of idxs for integer/list-based objects
	 */
	private ArrayList<Integer> _guiIntValIDXs;
	/**
	 * array lists of idxs for label/read-only objects
	 */	
	private ArrayList<Integer> _guiLabelValIDXs;
	/**
	 * Numeric Gui Objects
	 */
	private Base_GUIObj[] _guiObjsAra;
	/**
	 * Base_GUIObj that was clicked on for modification
	 */
	private Base_GUIObj _msClickObj;
	
	/**
	 * mouse button clicked - consumed for individual click mod
	 */
	private int _msBtnClicked;
	/**
	 * object mouse moved over
	 */
	private int _msOvrObj;	
	/**
	 * structure to facilitate communicating UI changes with functional code
	 */
	private UIDataUpdater _uiUpdateData;
	
	/**
	 * Boolean array of default behavior boolean values, if formatting is not otherwise specified
	 *  idx 0: value is sent to owning window,  
	 *  idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *  idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 */
	private static final boolean[] dfltUIBehaviorVals = new boolean[]{true, false, false};	
	/**
	 * Boolean array of default UI format values, if formatting is not otherwise specified : 
	 *  idx 0: whether multi-line(stacked) or not                                                  
	 *  idx 1: if true, build prefix ornament                                                      
	 *  idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	private static final boolean[] dfltUIFmtVals =  new boolean[] {false, true, false};	
	/**
	 * Boolean array of default UI format values, if formatting is not otherwise specified : 
	 *  idx 0: whether multi-line(stacked) or not                                                  
	 *  idx 1: if true, build prefix ornament                                                      
	 *  idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	private static final boolean[] dfltMultiLineUIFmtVals =  new boolean[] {true, true, false};	
	////////////////////////
	/// owner's private state/functionality flags, (displayed in grid of 2-per-column buttons)
	
	/**
	 * UI Application-specific flags and UI components (buttons)
	 */	
	private WinAppPrivStateFlags _privFlags;		
	/**
	 * Button labels for true or false value buttons
	 */
	private String[][] _privFlagButtonLabels; //needs to be in order of flags

	/**
	 * Colors for boolean buttons set to True or false based on child-class window specific values
	 */
	private int[][][] _privFlagButtonColors;
	/**
	 * Non random button color for true (idx 1) and false (idx 0);
	 */
	private final int[][] btnColors = new int[][] {new int[]{255,215,215,255}, new int[]{220,255,220,255}};
	
	/**
	 * False button color to use if button labels are the same and using random colors
	 */
	private static final int[] baseBtnFalseClr = new int[]{180,180,180, 255};
	
	/**
	 * only modifiable idx's will be shown as buttons - this needs to be in order of flag names
	 */
	private int[] _privModFlgIdxs;
	/**
	 * Click dimensions for each button
	 */
	private float[][] _privFlagBtns;	
	/**
	 * array of priv buttons to be cleared next frame - 
	 * should always be empty except when buttons need to be cleared
	 */
	private ArrayList<Integer> _privBtnsToClear;
	
	// Class name to use for any debugging messages
	private final String dispMsgClassName;
	
	public UIObjectManager(IRenderInterface _ri, IUIManagerOwner _owner, GUI_AppManager _AppMgr, MessageObject _msgObj) {
		ri = _ri;
		owner = _owner;
		_uiClkCoords = new float[4];
		dispMsgClassName = "UIObjectManager ("+owner.getClassName()+")";
		AppMgr = _AppMgr;
		msgObj = _msgObj;
		_msClickObj = null;
		_msBtnClicked = -1;
	}
		
	/**
	 * UI object creation	
	 */
	public void initAllGUIObjects() {
		_privBtnsToClear = new ArrayList<Integer>();
		//initialize arrays to hold idxs of int and float items being created.
		_guiButtonIDXs = new ArrayList<Integer>();
		_guiFloatValIDXs = new ArrayList<Integer>();
		_guiIntValIDXs = new ArrayList<Integer>();
		_guiLabelValIDXs = new ArrayList<Integer>();
		_guiObjsAra = new Base_GUIObj[0];
		//////////////
		// build all UI objects using specifications from instancing window
		owner.initOwnerStateDispFlags();
		
		// Setup proper ui click coords
		float[] _uiClickCoords = owner.getOwnerParentWindowUIClkCoords();
		System.arraycopy(_uiClickCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);
		
		//////////////////////////////
		//build ui objects
		// list box values - keyed by list obj IDX, value is string array of list obj values
		TreeMap<Integer, String[]> tmpListObjVals = new TreeMap<Integer, String[]>();
		// ui object values - keyed by object idx, value is object array of describing values
		TreeMap<Integer, Object[]> tmpUIObjArray = new TreeMap<Integer, Object[]>();
		//  set up all gui objects for this window
		//setup all ui objects and record final y value in sidebar menu for UI Objects in this window
		owner.setupOwnerGUIObjsAras(tmpUIObjArray,tmpListObjVals);			
		//initialized for sidebar menu as well as for display windows
		_guiObjsAra = new Base_GUIObj[tmpUIObjArray.size()]; // list of modifiable gui objects
		//build ui objects
		_uiClkCoords[3] = _buildGUIObjsForMenu(tmpUIObjArray, tmpListObjVals, _uiClkCoords);		
		
		//////////////////////////////
		//build UI boolean buttons - necessary for menu and not menu
		TreeMap<Integer, Object[]> tmpBtnNamesArray = new TreeMap<Integer, Object[]>();
		//  set up all window-specific boolean buttons for this window
		// this must return -all- priv buttons, not just those that are interactive (some may be hidden to manage functional booleans)
		int _numPrivFlags = owner.initAllOwnerUIButtons(tmpBtnNamesArray);
		//initialize all private buttons based on values put in arraylist
		_uiClkCoords[3] = _buildAllPrivButtons(tmpBtnNamesArray, _uiClkCoords);
		// init specific sim flags
		_privFlags = new WinAppPrivStateFlags(owner,_numPrivFlags);
		// set instance-specific initial flags
		int[] trueFlagIDXs = owner.getOwnerFlagIDXsToInitToTrue();
		//set local value for flags that should be initialized to true (without passing to instancing class handler yet)		
		if(null!=trueFlagIDXs) {_initPassedPrivFlagsToTrue(trueFlagIDXs);}
		
		// build instance-specific UI update communication object if exists
		_buildUIUpdateStruct();
		
	}//_initNumericGUIObjs
	
	/**
	 * Set _uiClkCoords to be passed array
	 * @param cpy
	 */
	public final void initUIClickCoords(float[] cpy){System.arraycopy(cpy, 0, _uiClkCoords, 0, _uiClkCoords.length);}
	
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
	 * Build the object array that describes a label object that is multiLine
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default UI format boolean values for multi-line labels. Label objects' behavior is restricted
	 * @return
	 */
	public final Object[] uiObjInitAra_LabelMultiLine(double initVal, String name) {
		return uiObjInitAra_Label(initVal, name, dfltMultiLineUIFmtVals);
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
	 * Build the object array that describes a integer object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_IntMultiLine(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
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
	 * Build the object array that describes a float object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final Object[] uiObjInitAra_FloatMultiLine(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
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
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
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
	 * Build the object array that describes a list object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values for multi-line list box
	 * @return
	 */
	public final Object[] uiObjInitAra_ListMultiLine(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_List(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
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
	 * Build the object array that describes a button object
	 * @param labels the list of labels this button supports. The size of the list is the number of states the button will handle.
	 * @param btnIdx the index of the button, for UI lookup
	 * @return
	 */
	public final Object[] uiObjInitAra_Btn(String[] labels, int btnIdx) {
		return new Object[] {labels, btnIdx};
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
		if(numGUIObjs > 0) {			
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
			//format for object and renderer
			boolean[] formatAra;
			for (int i = 0; i < numGUIObjs; ++i) {
				Object[] obj = tmpUIObjArray.get(i);
				if (obj.length == 6) {
					// object has been built with extended format array specified				
					formatAra = (boolean[])obj[5];
				} else {
					// Not specified, use default values - {false (single line), true (use prefix), false (don't use label color for prefix)}
					formatAra = dfltUIFmtVals;
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
			// Assign hotspots for UI components
			myPointf newStPt = new myPointf(uiClkRect[0], uiClkRect[1], 0);
			boolean lastObjWasMultiLine = false;
			for (int i = 0; i < _guiObjsAra.length; ++i) {
				if (lastObjWasMultiLine && (!_guiObjsAra[i].isMultiLine())) {
					newStPt.x = uiClkRect[0];
					newStPt.y = _guiObjsAra[i-1].getEnd().y;
				}
				// Get next newStPt as we calculate the hotspot region for every UI object
				newStPt = _guiObjsAra[i].reCalcHotSpot(newStPt, textHeightOffset, uiClkRect[0], uiClkRect[2]);		
				lastObjWasMultiLine = _guiObjsAra[i].isMultiLine();
			}
			//specify the end of this block of UI clickable coordinates based on if last object was multi-line or not
			uiClkRect[3] = lastObjWasMultiLine ?  _guiObjsAra[_guiObjsAra.length-1].getEnd().y : newStPt.y;							
			uiClkRect[3] -= .5f*textHeightOffset;
		}
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
	private Base_GUIObjRenderer _buildObjRenderer(
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
					_guiObjsAra[i] = new MenuGUIObj_Int(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					_guiIntValIDXs.add(i);
					break;}
				case ListVal : {
					++numListObjs;
					_guiObjsAra[i] = new MenuGUIObj_List(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], tmpListObjVals.get(i));
					_guiIntValIDXs.add(i);
					break;}
				case FloatVal : {
					_guiObjsAra[i] = new MenuGUIObj_Float(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					_guiFloatValIDXs.add(i);
					break;}
				case LabelVal :{
					_guiObjsAra[i] = new MenuGUIObj_DispValue(i, guiObjNames[i], guiStVals[i]);					
					_guiLabelValIDXs.add(i);
					break;}
				case Button  :{
					_guiButtonIDXs.add(i);
					//TODO
					_dispWarnMsg("_buildAllObjects", "Instantiating a Button UI object not yet supported for ID : "+i);
					break;
				}
				default : {
					_dispWarnMsg("_buildAllObjects", "Attempting to instantiate unknown UI object for a " + guiObjTypes[i].toStrBrf());
					break;				
				}				
			}//switch
			var renderer = _buildObjRenderer(_guiObjsAra[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
			_guiObjsAra[i].setRenderer(renderer);			
		}
		if(numListObjs != tmpListObjVals.size()) {
			_dispWarnMsg("_buildAllObjects", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+tmpListObjVals.size()+") - some or all of specified list objects will not display properly.");
		}	
	}//_buildAllObjects	
	

	/**
	 * calculate button length
	 */
	private float _calcBtnLength(String tStr, String fStr){return MyMathUtils.max(ri.getTextWidth(tStr), ri.getTextWidth(fStr));}
	
	private void _setBtnDims(int idx, float xStart, float yEnd, float oldBtnLen, float btnLen) {_privFlagBtns[idx]= new float[] {xStart+oldBtnLen, yEnd, btnLen, AppMgr.getTextHeightOffset() };}
	
	/**
	 * Take populated arraylist of object arrays describing private buttons and use these to initialize actual button arrays
	 * @param tmpBtnNamesArray arraylist of object arrays, each entry in object array holding a true string, a false string and an integer idx for the button
	 */	
	private float _buildAllPrivButtons(TreeMap<Integer, Object[]> tmpBtnNamesArray, float[] uiClkRect) {
		// finalize setup for UI toggle buttons - convert to arrays
		_privFlagButtonLabels = new String[2][];
		String[] truePrivFlagLabels = new String[tmpBtnNamesArray.size()];
		String[] falsePrivFlagLabels = new String[truePrivFlagLabels.length];
		_privModFlgIdxs = new int[truePrivFlagLabels.length];
		for (int i = 0; i < truePrivFlagLabels.length; ++i) {
			Object[] tmpAra = tmpBtnNamesArray.get(i);
			String[] labelAra = (String[])tmpAra[0];
			truePrivFlagLabels[i] = labelAra[0];
			falsePrivFlagLabels[i] = labelAra[1];
			_privModFlgIdxs[i] = (int) tmpAra[1];
		}
		_privFlagButtonLabels[0]=falsePrivFlagLabels;
		_privFlagButtonLabels[1]=truePrivFlagLabels;		
		return _buildPrivBtnRects(0, truePrivFlagLabels.length, uiClkRect);
	}//_buildAllPrivButtons
	
	/**
	 * set up boolean button rectangles using initialized truePrivFlagLabels and falsePrivFlagLabels
	 * @param yDisp displacement for button to be drawn
	 * @param numBtns number of buttons to make
	 */
	private float _buildPrivBtnRects(float yDisp, int numBtns, float[] uiClkRect){
		_privFlagBtns = new float[numBtns][];
		if (numBtns == 0) {	return uiClkRect[3];	}
		float yOffset = AppMgr.getTextHeightOffset();
		float maxBtnLen = 0.98f * AppMgr.getMenuWidth(), halfBtnLen = .5f*maxBtnLen;
		uiClkRect[3] += yOffset;
		float oldBtnLen = 0;
		boolean lastBtnHalfStLine = false, startNewLine = true;
		for(int i=0; i<numBtns; ++i){						//clickable button regions - as rect,so x,y,w,h - need to be in terms of sidebar menu 
			float btnLen = _calcBtnLength(_privFlagButtonLabels[1][i].trim(),_privFlagButtonLabels[0][i].trim());
			//either button of half length or full length.  if half length, might be changed to full length in next iteration.
			//_dispDbgMsg("_buildPrivBtnRects","i: "+i+" len : " +btnLen+" cap 1: " + truePrivFlagLabels[i].trim()+"|"+falsePrivFlagLabels[i].trim());
			if(btnLen > halfBtnLen){//this button is bigger than halfsize - it needs to be made full size, and if last button was half size and start of line, make it full size as well
				btnLen = maxBtnLen;
				if(lastBtnHalfStLine){//make last button full size, and make button this button on another line
					_privFlagBtns[i-1][2] = maxBtnLen;
					uiClkRect[3] += yOffset;
				}
				_setBtnDims(i, uiClkRect[0], uiClkRect[3], 0, btnLen);
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
			_privFlagBtns[numBtns-1][2] = maxBtnLen;
			uiClkRect[3] += yOffset;
		}
		uiClkRect[3] += AppMgr.getRowStYOffset();
		_initPrivFlagColors();
		return uiClkRect[3];
	}//_buildPrivBtnRects
	
	/**
	 * Find index in flag name arrays of passed boolean IDX
	 * @param idx
	 * @return
	 */
	public final int getFlagAraIdxOfBool(int idx) {
		for(int i=0;i<_privModFlgIdxs.length;++i) {if(idx == _privModFlgIdxs[i]) {return i;}	}		
		return -1;//not found
	}
	
	/**
	 * set up initial colors for sim specific flags for display
	 */
	private void _initPrivFlagColors(){
		_privFlagButtonColors = new int[2][][];
		int[][] _privFlagTrueColors = new int[_privFlagButtonLabels[0].length][4];
		int[][] _privFlagFalseColors = new int[_privFlagTrueColors.length][4];
		for (int i = 0; i < _privFlagTrueColors.length; ++i) { 
			_privFlagTrueColors[i] = MyMathUtils.randomIntClrAra(150, 100, 150);
			if(_privFlagButtonLabels[0][i].equals(_privFlagButtonLabels[1][i])) {
				_privFlagFalseColors[i] = baseBtnFalseClr;
			} else {
				_privFlagFalseColors[i] = new int[]{0,255-_privFlagTrueColors[i][1],255-_privFlagTrueColors[i][2], 255};
			}
		}
		_privFlagButtonColors[0] = _privFlagFalseColors;
		_privFlagButtonColors[1] = _privFlagTrueColors;
	}// _initPrivFlagColors
	
	/**
	 * Set labels of boolean buttons for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new true label
	 * @param fLbl new false label
	 */
	public void setButtonLabels(int idx, String tLbl, String fLbl) {_privFlagButtonLabels[1][idx] = tLbl;_privFlagButtonLabels[0][idx] = fLbl;}
	
	/**
	 * Pass all flag states to initialized structures in instancing window handler
	 */
	public final void refreshPrivFlags() {		_privFlags.refreshAllFlags();	}
	
	///////////////////////////////
	// UI object interaction
	
	/**
	 * Called by _privFlags bool struct, to update _uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	public final void checkSetBoolAndUpdate(int idx, boolean val) {
		if((_uiUpdateData != null) && _uiUpdateData.checkAndSetBoolValue(idx, val)) {
			owner.updateOwnerCalcObjUIVals();
		}
	}

	/**
	 * This will check if boolean value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetBoolValue(int idx, boolean value) {return _uiUpdateData.checkAndSetBoolValue(idx, value);}
	/**
	 * This will check if Integer value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetIntVal(int idx, int value) {return _uiUpdateData.checkAndSetIntVal(idx, value);}
	/**
	 * This will check if float value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	public final boolean checkAndSetFloatVal(int idx, float value) {return _uiUpdateData.checkAndSetFloatVal(idx, value);}	
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateBoolValFromExecCode(int idx, boolean value) {_privFlags.setFlag(idx, value);_uiUpdateData.setBoolValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateIntValFromExecCode(int idx, int value) {_guiObjsAra[idx].setVal(value);_uiUpdateData.setIntValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateFloatValFromExecCode(int idx, float value) {_guiObjsAra[idx].setVal(value);_uiUpdateData.setFloatValue(idx, value);}
	
	
	/***
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIobj object to set value of
	 * @param UIidx index of object within gui obj ara
	 */
	private final void _setUIWinValsInternal(Base_GUIObj UIobj, int UIidx) {
		//Determine whether int (int or list) or float
		GUIObj_Type objType = UIobj.getObjType();
		switch (objType) {
			case IntVal : {
				int ival = UIobj.getValueAsInt();
				int origVal = _uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj int handling, if pertinent
					owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case ListVal : {
				int ival = UIobj.getValueAsInt();
				int origVal = _uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj int (list idx)-related handling, if pertinent
					owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case FloatVal : {
				float val = UIobj.getValueAsFloat();
				float origVal = _uiUpdateData.getFloatValue(UIidx);
				if(checkAndSetFloatVal(UIidx, val)) {
					if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj float handling, if pertinent
					owner.setUI_OwnerFloatValsCustom(UIidx, val, origVal);
				}
				break;}
			case LabelVal : {
				_dispWarnMsg("setUIWinVals", "Attempting to process the value `" + UIobj.getValueAsString()+"` from the `" + UIobj.getName()+ "` label object.");				
				break;}
			case Button : {
				_dispWarnMsg("setUIWinVals", "Attempting to set a value for an unsupported Button UI object : " + objType.toStrBrf());
				break;}
			default : {
				_dispWarnMsg("setUIWinVals", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());
				break;}
			
		}//switch on obj type
	
	}//_setUIWinValsInternal
	
	/**
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIidx index of object within gui obj ara
	 */
	public final void setUIWinVals(int UIidx) {		_setUIWinValsInternal(_guiObjsAra[UIidx], UIidx);	}//setUIWinVals	
	
	/**
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIidx index of object within gui obj ara
	 */
	public final void setUIWinVals(Base_GUIObj UIobj) {		_setUIWinValsInternal(UIobj, UIobj.getObjID());	}//setUIWinVals	
	
	/**
	 * Reset guiObj given by passed index to starting value
	 * @param uiIdx
	 */
	public final void resetUIObj(int uiIdx) {_guiObjsAra[uiIdx].resetToInit();setUIWinVals(uiIdx);}
	
	/**
	 * Reset all values to be initial values. 
	 * @param forceVals If true, this will bypass setUIWinVals, if false, will call set vals, to propagate changes to window vars 
	 */
	public final void resetUIVals(boolean forceVals){
		for(int i=0; i<_guiObjsAra.length;++i){				_guiObjsAra[i].resetToInit();		}
		if (!forceVals) {			setAllUIWinVals();		}
	}//resetUIVals
	
	
	
	/**
	 * manage loading pre-saved UI component values, if useful for this window's load/save (if so call from child window's implementation
	 * @param vals
	 * @param stIdx
	 */
	public final void hndlFileLoad_GUI(String winName, String[] vals, int[] stIdx) {
		++stIdx[0];
		//set values for ui sliders
		while(!vals[stIdx[0]].contains(winName + "_custUIComps")){
			if(vals[stIdx[0]].trim() != ""){	setValFromFileStr(vals[stIdx[0]]);	}
			++stIdx[0];
		}
		++stIdx[0];				
	}//hndlFileLoad_GUI
	
	/**
	 * manage saving this window's UI component values.  if needed call from child window's implementation
	 * @return
	 */
	public final ArrayList<String> hndlFileSave_GUI(String winName){
		ArrayList<String> res = new ArrayList<String>();
		res.add(winName);
		for(int i=0;i<_guiObjsAra.length;++i){	res.add(_guiObjsAra[i].getStrFromUIObj(i));}		
		//bound for custom components
		res.add(winName + "_custUIComps");
		//add blank space
		res.add("");
		return res;
	}//
	
	
	/**
	 * this sets the value of a gui object from the data held in a string
	 * @param str
	 */
	public final void setValFromFileStr(String str){
		String[] toks = str.trim().split("\\|");
		//window has no data values to load
		if(toks.length==0){return;}
		int uiIdx = Integer.parseInt(toks[0].split("\\s")[1].trim());
		_guiObjsAra[uiIdx].setValFromStrTokens(toks);
		setUIWinVals(uiIdx);//update window's values with UI construct's values
	}//setValFromFileStr
	
	/**
	 * set all window values for UI objects
	 */
	public final void setAllUIWinVals() {for(int i=0;i<_guiObjsAra.length;++i){if(_guiObjsAra[i].shouldUpdateWin(true)){setUIWinVals(i);}}}
		
	/**
	 * call after single draw - will clear window-based priv buttons that are momentary
	 */
	public final void clearAllPrivBtns() {
		if(_privBtnsToClear.size() == 0) {return;}
		// only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : _privBtnsToClear) {if (_privFlags.getFlag(idx)) {_privFlags.setFlag(idx, false);}}
		_privBtnsToClear.clear();
	}//clearPrivBtns
	
	/**
	 * clear button next frame - to act like momentary switch.  will also clear UI object
	 * @param idx
	 */
	public final void clearBtnNextFrame(int idx) {addPrivBtnToClear(idx);		checkAndSetBoolValue(idx, false);}
		
	/**
	 * add a button to clear after next draw
	 * @param idx index of button to clear
	 */
	public final void addPrivBtnToClear(int idx) {		_privBtnsToClear.add(idx);	}

	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	private void _initPassedPrivFlagsToTrue(int[] idxs) { 	_privFlags.setAllFlagsToTrue(idxs);	}	
	
	/**
	 * Access private flag values
	 * @param idx
	 * @return
	 */
	public final boolean getPrivFlag(int idx) {				return _privFlags.getFlag(idx);}
	
	/**
	 * Whether or not the _privFlags structure is in debug mode
	 * @return
	 */
	public final boolean getPrivFlagIsDebug() {				return _privFlags.getIsDebug();}
	
	/**
	 * Retrieve the integer representation of the bitflags - the idx'ithed 32 flag bits.
	 * @param idx
	 * @return
	 */
	public final int getPrivFlagAsInt(int idx) {			return _privFlags.getFlagsAsInt(idx);}
	
	/**
	 * Set private flag values
	 * @param idx
	 * @param val
	 */
	public final void setPrivFlag(int idx, boolean val) {		_privFlags.setFlag(idx, val);}
	
	/**
	 * Validate that the passed idx exists in the list of objects 
	 * @param idx index of potential objects
	 * @param len number of objects stored in object array
	 * @param calFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether or not the passed index corresponds to a valid location in the array of UI objects
	 */
	private boolean _validateUIObjectIdx(int idx, int len, String calFunc, String desc) {
		if (!MyMathUtils.inRange(idx, 0, len)){
			msgObj.dispErrorMessage(dispMsgClassName, calFunc, 
				"Attempting to access illegal Numeric UI object to "+desc+" (idx :"+idx+" is out of range). Aborting.");
			return false;
		}		
		return true;
	}
	
	/**
	 * Validate whether the passed UI object is a listVal object
	 * @param obj the object to check
	 * @param calFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether the passed UI object is a listVal object
	 */
	private boolean _validateIdxIsListObj(Base_GUIObj obj, String calFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.ListVal) {
			msgObj.dispErrorMessage(dispMsgClassName, calFunc, 
					"Attempting to access illegal List UI object to "+desc+" (object :"+obj.getName()+" is not a list object). Aborting.");
			return false;
		}
		return true;
	}
	
	/**
	 * Sets the passed UI object's new max value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes
	 * @param maxVal
	 * @return whether modification was performed or not
	 */
	public boolean setNewUIMaxVal(int idx, double maxVal) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIMaxVal", "set its max value")) {_guiObjsAra[idx].setNewMax(maxVal);return true;}	
		return false;
	}
	
	
	/**
	 * Sets the passed UI object's new min value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes.
	 * @param minVal
	 * @return whether modification was performed or not
	 */
	public boolean setNewUIMinVal(int idx, double minVal) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIMinVal", "set its min value")) {_guiObjsAra[idx].setNewMin(minVal);return true;}
		return false;
	}
	
	/**
	 * Force a value to be set in the numeric UI object at the passed idx
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes and returns -Double.MAX_VALUE
	 * @param val
	 * @return value being set, or -Double.MAX_VALUE if idx is out of range
	 */
	public double setNewUIValue(int idx, double val) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIValue", "set its value")) {return _guiObjsAra[idx].setVal(val);}
		return -Double.MAX_VALUE;
	}		
	
	/**
	 * Set the display text of the passed UI Object, either numeric or boolean
	 * @param idx
	 * @param isNumeric
	 * @param str
	 */
	public void setNewUIDispText(int idx, boolean isNumeric, String str) {
		if (isNumeric) {
			if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIDispText", "set its display text")) {_guiObjsAra[idx].setLabel(str);}
			return;
		} else {
			//TODO support boolean UI objects
			if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIDispText", "set its display text")) {_guiObjsAra[idx].setLabel(str);}
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
		if ((!_validateUIObjectIdx(idx, _guiObjsAra.length, "setDispUIListVal", "display passed value")) || 
				(!_validateIdxIsListObj(_guiObjsAra[idx], "setDispUIListVal", "display passed value"))){return new int[0];}
		return ((MenuGUIObj_List) _guiObjsAra[idx]).setValInList(val);
	}
	
	/**
	 * Set all the values in the uiObjIdx List UI Object, if it exists, and is a list object
	 * @param uiObjIdx the list obj's index
	 * @param values the list of values to set
	 * @param setAsDefault whether or not these new values should be set as the default values
	 * @return
	 */
	public int setAllUIListValues(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if ((!_validateUIObjectIdx(uiObjIdx, _guiObjsAra.length, "setAllUIListValues", "set/replace all list values")) || 
				(!_validateIdxIsListObj(_guiObjsAra[uiObjIdx], "setAllUIListValues", "set/replace all list values"))){return -1;}
		return ((MenuGUIObj_List) _guiObjsAra[uiObjIdx]).setListVals(values, setAsDefault);
	}
	
	/**
	 * Retrieve the min value of a numeric UI object
	 * @param idx index in numeric UI object array to access.
	 * @return min value allowed, or Double.MAX_VALUE if idx out of range
	 */
	public double getMinUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getMinUIValue","get its min value")) {return _guiObjsAra[idx].getMinVal();}
		return Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the max value of a numeric UI object
	 * @param idx index in numeric UI object array to access.
	 * @return max value allowed, or -Double.MAX_VALUE if idx out of range
	 */
	public double getMaxUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getMaxUIValue","get its max value")){return _guiObjsAra[idx].getMaxVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the mod step value of a numeric UI object
	 * @param idx index in numeric UI object array to access.
	 * @return mod value of UI object, or 0 if idx out of range
	 */
	public double getModStep(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getModStep", "get its mod value")) {return _guiObjsAra[idx].getModStep();}
		return 0;
	}
	
	/**
	 * Retrieve the value of a numeric UI object
	 * @param idx index in numeric UI object array to access.
	 * @return the current value of the UI object, or -Double.MAX_VALUE if idx out of range
	 */
	public double getUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getUIValue", "get its value")) {return _guiObjsAra[idx].getVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Get the string representation of the passed integer listIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array to access.
	 * @param listIdx index in list of elements to access
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getListValStr(int UIidx, int listIdx) {		
		if ((!_validateUIObjectIdx(UIidx, _guiObjsAra.length, "getListValStr", "get a list value at specified idx")) || 
				(!_validateIdxIsListObj(_guiObjsAra[UIidx], "getListValStr", "get a list value at specified idx"))){return "";}
		return ((MenuGUIObj_List) _guiObjsAra[UIidx]).getListValStr(listIdx);
	}
	
	/**
	 * This has to be called after UI structs are built and set - this creates and populates the 
	 * structure that serves to communicate UI data to consumer from UI Window.
	 */
	private void _buildUIUpdateStruct() {
		//set up UI->to->Consumer class communication object - only make instance of object here, 
		//initialize it after private flags are built and initialized
		_uiUpdateData = owner.buildOwnerUIDataUpdateObject();
		if (_uiUpdateData == null) {return;}
		TreeMap<Integer, Integer> intValues = new TreeMap<Integer, Integer>();    
		for (Integer idx : _guiIntValIDXs) {				intValues.put(idx, _guiObjsAra[idx].getValueAsInt());}		
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		for (Integer idx : _guiFloatValIDXs) {			floatValues.put(idx, _guiObjsAra[idx].getValueAsFloat());}
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
		//TODO 
		//for (Integer idx : _guiButtonIDXs) {			boolValues.put(idx, _guiObjsAra[idx].getValueAsFloat());}
		
		for(Integer i=0; i < _privFlags.numFlags;++i) {		boolValues.put(i, _privFlags.getFlag(i));}	
		_uiUpdateData.setAllVals(intValues, floatValues, boolValues); 
	}//_buildUIUpdateStruct
		
	/**
	 * Get the _uiUpdateData used by the owner
	 * @return
	 */
	public UIDataUpdater getUIDataUpdater() {return _uiUpdateData;}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Start Mouse and keyboard handling	
	/**
	 * updates values in UI with programatic changes 
	 * @param UIidx
	 * @param val
	 * @return
	 */
	public final boolean setWinToUIVals(int UIidx, double val){return val == _guiObjsAra[UIidx].setVal(val);}
	/**
	 * Check if point x,y is between r[0], r[1] and r[0]+r[2], r[1]+r[3]
	 * @param x
	 * @param y
	 * @param r rectangle - idx 0,1 is upper left corner, idx 2,3 is width, height
	 * @return
	 */
	public final boolean msePtInRect(int x, int y, float[] r){return ((x >= r[0])&&(x <= r[0]+r[2])&&(y >= r[1])&&(y <= r[1]+r[3]));}
	
	public final boolean msePtInUIClckCoords(int x, int y){
		return ((x > _uiClkCoords[0])&&(x <= _uiClkCoords[2])
				&&(y > _uiClkCoords[1])&&(y <= _uiClkCoords[3]));
	}	
	
	/**
	 * check if mouse location is in UI buttons, and handle button click if so
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	private boolean _checkUIButtons(int mouseX, int mouseY){
		if(0==_privFlagBtns.length) {return false;}
		boolean mod = false;
		//keep checking -see if clicked in UI buttons (flag-based buttons)
		for(int i = 0;i<_privFlagBtns.length;++i){
			mod = msePtInRect(mouseX, mouseY, _privFlagBtns[i]); 
			//_dispDbgMsg("_checkUIButtons","Handle mouse click in window : "+ ID + " : (" + mouseX+","+mouseY+") : "+mod + ": btn rect : "+_privFlagBtns[i][0]+","+_privFlagBtns[i][1]+","+_privFlagBtns[i][2]+","+_privFlagBtns[i][3]);
			if (mod){ 
				_privFlags.toggleFlag(_privModFlgIdxs[i]);
				//setPrivFlags(_privModFlgIdxs[i],!getPrivFlags(_privModFlgIdxs[i])); 
				return mod;
			}			
		}
		return mod;
	}//_checkUIButtons	
	
	/**
	 * handle a mouse click
	 * @param mouseX x location on screen
	 * @param mouseY y location on screen
	 * @param mseBtn which button is pressed : 0 is left, 1 is right
	 * @param retVals : idx 0 is if an object has been modified
	 * 					idx 1 is if we should set "setUIObjMod" to true
	 * @return _msClickObj
	 */
	public final boolean handleMouseClick(int mouseX, int mouseY, int mseBtn, boolean[] retVals){
		_msClickObj = null;
		if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			int idx = _checkInAllObjs(mouseX, mouseY);
			if(idx >= 0) {
				//found in list of UI objects
				_msBtnClicked = mseBtn; 
				_msClickObj = _guiObjsAra[idx];
				_msClickObj.setHasFocus();
				if(AppMgr.isClickModUIVal()){//allows for click-mod without dragging
					_setUIObjValFromClickAlone(_msClickObj);
					//Check if modification from click has changed the value of the object
					if(_msClickObj.getIsDirty()) {retVals[1] = true;}
				} 				
				retVals[0] = true;
			}
		}			
		if(!retVals[0]) {			retVals[0] = _checkUIButtons(mouseX, mouseY);	}
		return _msClickObj != null;
	}//handleMouseClick
	
	/**
	 * Check inside all objects to see if passed mouse x,y is within hotspot
	 * @param mouseX
	 * @param mouseY
	 * @return idx of object that mouse resides in, or -1 if none
	 */
	private final int _checkInAllObjs(int mouseX, int mouseY) {
		for(int j=0; j<_guiObjsAra.length; ++j){if(_guiObjsAra[j].checkIn(mouseX, mouseY)){ return j;}}
		return -1;
	}	
	
	/**
	 * Handle mouse move over window - returns the object ID of the object the mouse is over
	 * @param mouseX
	 * @param mouseY
	 * @return Whether or not the mouse has moved over a valid UI object
	 */
	public final boolean handleMouseMove(int mouseX, int mouseY){
		if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			_msOvrObj = _checkInAllObjs(mouseX, mouseY);
		} else {			_msOvrObj = -1;		}
		return _msOvrObj != -1;
	}//handleMouseMov
	
	/**
	 * Handle mouse-driven modification to a UI object, by modAmt
	 * @param modAmt the amount to modify the UI object
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	private final boolean[] _handleMouseModInternal(double modAmt) {
		// idx 0 is if an object has been modified
		// idx 1 is if we should set "setUIObjMod" to true
		boolean[] retVals = new boolean[] {false, false};
		if(_msClickObj!=null){	
			//modify object that was clicked in by mouse motion
			_msClickObj.dragModVal(modAmt);
			if(_msClickObj.getIsDirty()) {
				retVals[1] = true;
				if(_msClickObj.shouldUpdateWin(false)){setUIWinVals(_msClickObj);}
			}
			retVals[0] = true;
		}	
		return retVals;	
	}
	
	/**
	 * Handle the mouse wheel changing providing interaction with UI objects.
	 * @param ticks
	 * @param mult
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseWheel(int ticks, float mult) {return _handleMouseModInternal(ticks * mult);}
	
	/**
	 * Handle the mouse being dragged from within the confines of a selected object
	 * @param delX
	 * @param delY
	 * @param shiftPressed
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseDrag(int delX, int delY, boolean shiftPressed) {return _handleMouseModInternal(delX+(delY*-(shiftPressed ? 50.0f : 5.0f)));}	
	
	/**
	 * Set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	 * @param j
	 */
	private void _setUIObjValFromClickAlone(Base_GUIObj obj) {		obj.clickModVal(_msBtnClicked * -2.0f + 1, AppMgr.clickValModMult());	}
	
	/**
	 * Handle UI functionality when mouse is released in owner
	 * @param objModified whether object was clicked on but not changed - this will change cause the release to increment the object's value
	 * @return whether or not _privBtnsToClear has buttons to clear.
	 */
	public final boolean handleMouseRelease(boolean objModified) {
		if(_msClickObj != null) {
			if(!objModified) {
				//_dispInfoMsg("handleMouseRelease", "Object : "+_msClickObj+" was clicked clicked but getUIObjMod was false");
				//means object was clicked in but not drag modified through drag or shift-clic - use this to modify by clicking
				_setUIObjValFromClickAlone(_msClickObj);
			} 		
			setAllUIWinVals();
			_msClickObj.clearFocus();
			_msClickObj = null;	
		}
		_msBtnClicked = -1;
		return _privBtnsToClear.size() > 0;
	}//handleMouseRelease
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// End Mouse and keyboard handling; Start UI object rendering	
	
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
	 * Draw the UI clickable region rectangle
	 */
	private final void _drawUIRect() {
		ri.setStrokeWt(2.0f);
		ri.setNoFill();
		ri.setColorValStroke(owner.getID() * 10, 255);
		ri.drawRect(_uiClkCoords[0], _uiClkCoords[1], _uiClkCoords[2]-_uiClkCoords[0], _uiClkCoords[3]-_uiClkCoords[1]);
	}
	
	/**
	 * Draw all gui objects, with appropriate highlights for debug and if object is being edited or not
	 * @param isDebug
	 * @param animTimeMod
	 */
	public final void drawGUIObjs(boolean isDebug, float animTimeMod) {
		ri.pushMatState();
		//draw UI Objs
		if(isDebug) {
			for(int i =0; i<_guiObjsAra.length; ++i){_guiObjsAra[i].drawDebug();}
			_drawUIRect();
		} else {			
			//mouse highlight
			if (_msClickObj != null) {	_msClickObj.drawHighlight();	}
			for(int i =0; i<_guiObjsAra.length; ++i){_guiObjsAra[i].draw();}
		}	
		ri.popMatState();	
	}//drawAllGuiObjs
	
	/**
	 * Draw a series of strings in a row
	 * @param txt
	 * @param loc
	 * @param clrAra
	 */
	private final void _dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
		ri.setFill(clrAra, clrAra[3]);
		ri.setColorValStroke(IRenderInterface.gui_Black,255);
		ri.drawRect(loc);		
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		//ri.translate(-xOff*.5f,-yOff*.5f);
		ri.showText(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
		//ri.translate(width, 0);
	}
	
	/**
	 * Draw application-specific flag buttons
	 * @param useRandBtnClrs
	 */
	private final void drawAppFlagButtons(boolean useRandBtnClrs) {
		ri.pushMatState();	
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		if(useRandBtnClrs){
			for(int i =0; i<_privModFlgIdxs.length; ++i){
				int btnFlagIdx = _privFlags.getFlag(_privModFlgIdxs[i])  ? 1 : 0;
				_dispBttnAtLoc(_privFlagButtonLabels[btnFlagIdx][i],_privFlagBtns[i],_privFlagButtonColors[btnFlagIdx][i]);	
			}
		} else {
			for(int i =0; i<_privModFlgIdxs.length; ++i){
				int btnFlagIdx = _privFlags.getFlag(_privModFlgIdxs[i])  ? 1 : 0;
				_dispBttnAtLoc(_privFlagButtonLabels[btnFlagIdx][i],_privFlagBtns[i],btnColors[btnFlagIdx]);	
			}	
		}		
		ri.popMatState();	
	}//drawAppFlagButtons
	
	/**
	 * What to do after the owner has finished draw command
	 */
	public final void postDraw(boolean clearPrivBtns) {
		//last thing per draw - clear btns that have been set to clear after 1 frame of display
		if (clearPrivBtns) {clearAllPrivBtns();}
		
	}
	
	
	/**
	 * Shorthand to display general information
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispInfoMsg(String funcName, String message) {msgObj.dispInfoMessage(dispMsgClassName, funcName, message);}
		
	/**
	 * Shorthand to display a debug message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispDbgMsg(String funcName, String message) {msgObj.dispDebugMessage(dispMsgClassName, funcName, message);}
	
	/**
	 * Shorthand to display a warning message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispWarnMsg(String funcName, String message) {msgObj.dispWarningMessage(dispMsgClassName, funcName, message);}
	
	/**
	 * Shorthand to display an error message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispErrMsg(String funcName, String message) {msgObj.dispErrorMessage(dispMsgClassName, funcName, message);}
	
	
	/**
	 * debug data to display on screen get string array for onscreen display of debug info for each object
	 * @return
	 */
	public final String[] getDebugData(){
		ArrayList<String> res = new ArrayList<String>();
		for(int j = 0; j<_guiObjsAra.length; j++){res.addAll(Arrays.asList(_guiObjsAra[j].getStrData()));}
		return res.toArray(new String[0]);	
	}
	
	/**
	 * Return the coordinates of the clickable region for this window's UI
	 * @return
	 */
	public float[] getUIClkCoords() {return _uiClkCoords;}
	
}//class uiObjectManager
