package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.IUIManagerOwner;
import base_UI_Objects.windowUI.base.WinAppPrivStateFlags;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_DispValue;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Float;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Int;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_List;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Switch;
import base_UI_Objects.windowUI.uiObjs.renderer.ButtonGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.MultiLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.SingleLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;
import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

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
	* array list of idxs for multi-state objects not backed by a flags construct
	*/	
	private ArrayList<Integer> _guiButtonIDXs;
	
	/**
	 * Map of 2-state switch toggle objects connected to privFlags structures, keyed by privFlags key;
	 */
	private TreeMap<Integer,MenuGUIObj_Switch> _guiSwitchIDXMap;
	
	/**
	 * array list of idxs for float-based UI objects.
	 */
	private ArrayList<Integer> _guiFloatValIDXs;
	
	/**
	 * array list of idxs for integer/list-based objects
	 */
	private ArrayList<Integer> _guiIntValIDXs;
	/**
	 * array list of idxs for label/read-only objects
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
	/**
	 * Boolean array of default UI format values for buttons, if formatting is not otherwise specified : 
	 *  idx 0: whether multi-line(stacked) or not                                                  
	 *  idx 1: if true, build prefix ornament                                                      
	 *  idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	private static final boolean[] dfltUIButtonFmtVals =  new boolean[] {false, false, false};
	/**
	 * Boolean array of default button type format values, if not otherwise specified 
	 *  idx 0: Whether this button should stay enabled until next draw frame                                                
	 *  idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
	 */
	private static final boolean[] dfltUIButtonTypeVals =  new boolean[] {false, false};
	
	
	////////////////////////
	/// owner's private state/functionality flags, (displayed in grid of 2-per-column buttons)
	
	/**
	 * UI Application-specific flags and UI components (buttons)
	 */	
	private WinAppPrivStateFlags _privFlags;		
	/**
	 * Non random button color for true (idx 1) and false (idx 0);
	 */
	private final int[][] btnColors = new int[][] {new int[]{255,215,215,255}, new int[]{220,255,220,255}};
	
//	/**
//	 * False button color to use if button labels are the same and using random colors
//	 */
//	private static final int[] baseBtnFalseClr = new int[]{180,180,180, 255};
	/**
	 * array of priv buttons to be cleared next frame - 
	 * should always be empty except when buttons need to be cleared
	 */
	private ArrayList<Integer> _privFlagsToClear;
	
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
		_privFlagsToClear = new ArrayList<Integer>();
		//initialize arrays to hold idxs of int and float items being created.
		_guiButtonIDXs = new ArrayList<Integer>();
		_guiSwitchIDXMap = new TreeMap<Integer,MenuGUIObj_Switch>();
		_guiFloatValIDXs = new ArrayList<Integer>();
		_guiIntValIDXs = new ArrayList<Integer>();
		_guiLabelValIDXs = new ArrayList<Integer>();
		//////////////
		// build all UI objects using specifications from instancing window
		owner.initOwnerStateDispFlags();
		
		// Setup proper ui click coords
		float[] _uiClickCoords = owner.getOwnerParentWindowUIClkCoords();
		System.arraycopy(_uiClickCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);
		
		//////////////////////////////
		//build ui objects and buttons
		// ui object values - keyed by object idx, value is object array of describing values
		TreeMap<Integer, GUIObj_Params> tmpUIObjMap = new TreeMap<Integer, GUIObj_Params>();
		// ui button values : map keyed by objId of object arrays : {true label, false label, index in application}
		TreeMap<Integer, GUIObj_Params> tmpBtnNamesArray = new TreeMap<Integer, GUIObj_Params>();
		//  set up all gui objects for this window
		//setup all ui objects and record final y value in sidebar menu for UI Objects in this window
		// also set up all window-specific boolean buttons for this window
		owner.setupOwnerGUIObjsAras(tmpUIObjMap, tmpBtnNamesArray);
		// TODO : eventually get rid of this stuff
		for (Map.Entry<Integer, GUIObj_Params> entry : tmpBtnNamesArray.entrySet()) {
			tmpUIObjMap.put(entry.getKey(),new GUIObj_Params(entry.getValue()));
		}
		
		//TODO merge this to build gui objs and priv buttons together (i.e. privButtons are gui objects)
		//build ui objects
		_guiObjsAra = new Base_GUIObj[tmpUIObjMap.size()]; // list of modifiable gui objects
		// Build UI Objects
		_uiClkCoords[3] = _buildGUIObjsForMenu(tmpUIObjMap, _uiClkCoords);
		
		// Get total number of booleans (not just buttons) for application
		int _numPrivFlags = owner.getTotalNumOfPrivBools();
		
		// init specific application state flags and UI booleans
		_privFlags = new WinAppPrivStateFlags(this,_numPrivFlags);
		
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
	 * Build the GUIObj_Params that describes a label object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Label(double initVal, String name) {
		return uiObjInitAra_Label(initVal, name, dfltUIFmtVals);
	}		
	/**
	 * Build the GUIObj_Params that describes a label object that is multiLine
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default UI format boolean values for multi-line labels. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_LabelMultiLine(double initVal, String name) {
		return uiObjInitAra_Label(initVal, name, dfltMultiLineUIFmtVals);
	}		

	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Label(double initVal, String name, boolean[] boolFmtVals) {
		return new GUIObj_Params(name, GUIObj_Type.LabelVal, new boolean[] {false,false,false}, boolFmtVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Int(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}	
	/**
	 * Build the GUIObj_Params that describes a integer object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_IntMultiLine(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a integer object
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
	public final GUIObj_Params uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a integer object
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
	public final GUIObj_Params uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		GUIObj_Params obj = new GUIObj_Params(name, GUIObj_Type.IntVal, boolVals, boolFmtVals);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		return obj;
	}
	
	/**
	 * Build the GUIObj_Params that describes a float object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_FloatMultiLine(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
	}
	
	
	/**
	 * Build the GUIObj_Params that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Float(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}

	
	/**
	 * Build the GUIObj_Params that describes a float object
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
	public final GUIObj_Params uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a float object
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
	public final GUIObj_Params uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		GUIObj_Params obj = new GUIObj_Params(name, GUIObj_Type.FloatVal, boolVals, boolFmtVals);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		return obj;
	}

	/**
	 * Build the GUIObj_Params that describes a list object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_List(double initVal, String name, String[] listElems) {
		return uiObjInitAra_List(new double[] {0, listElems.length-1, 1}, initVal, name, listElems);
	}

	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and UI format boolean values for multi-line list box
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_ListMultiLine(double initVal, String name, String[] listElems) {
		return uiObjInitAra_List(new double[] {0, listElems.length-1, 1}, initVal, name, listElems);
	}


	/**
	 * Build the GUIObj_Params that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_List(double[] minMaxMod, double initVal, String name, String[] listElems) {
		return uiObjInitAra_List(minMaxMod, initVal, name, listElems, dfltUIBehaviorVals, dfltUIFmtVals);
	}

	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and UI format boolean values for multi-line list box
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_ListMultiLine(double[] minMaxMod, double initVal, String name, String[] listElems) {
		return uiObjInitAra_List(minMaxMod, initVal, name, listElems, dfltUIBehaviorVals, dfltMultiLineUIFmtVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a list object
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
	public final GUIObj_Params uiObjInitAra_List(double[] minMaxMod, double initVal, String name, String[] listElems, boolean[] boolVals) {
		return uiObjInitAra_List(minMaxMod, initVal, name, listElems, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
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
	public final GUIObj_Params uiObjInitAra_ListMultiLine(double[] minMaxMod, double initVal, String name, String[] listElems, boolean[] boolVals) {
		return uiObjInitAra_List(minMaxMod, initVal, name, listElems, boolVals, dfltMultiLineUIFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a list object
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
	public final GUIObj_Params uiObjInitAra_List(double[] minMaxMod, double initVal, String name, String[] listElems, boolean[] boolVals, boolean[] boolFmtVals) {
		GUIObj_Params obj = new GUIObj_Params(name, GUIObj_Type.ListVal, boolVals, boolFmtVals);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		obj.setListVals(listElems);	
		return obj;	
	}	
	/**
	 * Build the GUIObj_Params that describes a button object - boolean button
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(String name, String trueLabel, String falseLabel, int boolFlagIdx) {
		return uiObjInitAra_Btn(name, new String[]{falseLabel, trueLabel}, 0, boolFlagIdx, dfltUIBehaviorVals, dfltUIButtonTypeVals);
	}

	/**
	 * Build the GUIObj_Params that describes a button object
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(String name, String[] labels, int boolFlagIdx) {
		return uiObjInitAra_Btn(name, labels, 0, boolFlagIdx, dfltUIBehaviorVals, dfltUIButtonTypeVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a button object
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param initVal the initial state this button should have
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(String name, String[] labels, double initVal, int boolFlagIdx) {
		return uiObjInitAra_Btn(name, labels, initVal, boolFlagIdx, dfltUIBehaviorVals, dfltUIButtonTypeVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a button object
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param btnIdx the index of this button
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @param boolVals 
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(String name, String[] labels, double initVal, int boolFlagIdx, boolean[] configBoolVals) {
		return uiObjInitAra_Btn(name, labels, initVal, boolFlagIdx, configBoolVals, dfltUIButtonTypeVals);		
	}
	/**
	 * Build the GUIObj_Params that describes a button object
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param btnIdx the index of this button
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @param boolVals
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(String name, String[] labels, double initVal, int boolFlagIdx, boolean[] configBoolVals, boolean[] buttonFlags) {
		//String _name, GUIObj_Type _objType, int _boolFlagIDX, boolean[] _configFlags, boolean[] _formatVals, boolean[] _buttonFlags
		GUIObj_Params obj;
		if (boolFlagIdx == -1) { 
			// Not a toggle
			obj = new GUIObj_Params(name, GUIObj_Type.Button, boolFlagIdx, configBoolVals, dfltUIButtonFmtVals, dfltUIButtonTypeVals);
		} else {
			// boolean flag toggle, attached to privFlags
			obj = new GUIObj_Params(name, GUIObj_Type.Switch, boolFlagIdx, configBoolVals, dfltUIButtonFmtVals, dfltUIButtonTypeVals);
		}
		obj.setMinMaxMod(new double[] {0, labels.length-1, 1});
		obj.initVal = initVal;
		obj.setListVals(labels);
		// set object colors
		obj.setBtnColors(_getButtonColors(labels.length));
		return obj;		
	}
	
	private int[][] _getButtonColors(int numBtns){
		if(numBtns == 2) {			return btnColors;		}
		int[][] res= new int[numBtns][4];
		for(int i=0;i<numBtns;++i) {res[i] = MyMathUtils.randomIntClrAra(150, 100, 150);}
		return res;
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
	private Base_GUIObjRenderer _buildObjRenderer(
			Base_GUIObj _owner, 
			double[] _off,
			float _menuWidth,
			int[][] _colors, 
			boolean[] guiFormatBoolVals, int[][] _btnColors) {
		
		int[] _strkClr = _colors[0];
		int[] _fillClr= _colors[1]; 
		if (_btnColors != null) {
			return new ButtonGUIObjRenderer(ri, (MenuGUIObj_Button)_owner, _off, _menuWidth, _strkClr, _btnColors);
		}
		if (guiFormatBoolVals[0]) {
			return new MultiLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);
		} else {
			return new SingleLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);			
		}
	}	
	
	private final float _buildGUIObjsForMenu(TreeMap<Integer, GUIObj_Params> tmpUIObjMap, float[] uiClkRect) {
		if(tmpUIObjMap.size() > 0) {

			float textHeightOffset = AppMgr.getTextHeightOffset();			
			//TODO Get guiColors from user input 
			for (Map.Entry<Integer, GUIObj_Params> entry : tmpUIObjMap.entrySet()) {
				int i = entry.getKey();
				GUIObj_Params argObj = entry.getValue();
				//Stroke and fill colors for renderer
				int[][] guiColors = new int[][] {
					{0,0,0,255}, //stroke
					{0,0,0,255}, // fill
				};			
				switch(argObj.objType) {
					case IntVal : {
						_guiObjsAra[i] = new MenuGUIObj_Int(i, argObj);
						_guiIntValIDXs.add(i);
						break;}
					case ListVal : {
						_guiObjsAra[i] = new MenuGUIObj_List(i, argObj);
						_guiIntValIDXs.add(i);
						break;}
					case FloatVal : {
						_guiObjsAra[i] = new MenuGUIObj_Float(i, argObj);
						_guiFloatValIDXs.add(i);
						break;}
					case LabelVal :{
						_guiObjsAra[i] = new MenuGUIObj_DispValue(i, argObj);					
						_guiLabelValIDXs.add(i);
						break;}
					case Switch : {						
						_guiObjsAra[i] = new MenuGUIObj_Switch(i, argObj);
						_guiSwitchIDXMap.put(((MenuGUIObj_Switch)_guiObjsAra[i]).getBoolFlagIDX(), ((MenuGUIObj_Switch)_guiObjsAra[i]));
						break;}
					case Button  :{ 
						_guiObjsAra[i] = new MenuGUIObj_Button(i, argObj);
						_guiButtonIDXs.add(i);
						//_dispWarnMsg("_buildGUIObjsForMenu", "Instantiating a Button UI object not yet supported for ID : "+i);
						break;
					}
					default : {
						_dispWarnMsg("_buildGUIObjsForMenu", "Attempting to instantiate unknown UI object for a " + argObj.objType.toStrBrf());
						break;				
					}				
				}//switch
				var renderer = _buildObjRenderer(_guiObjsAra[i], AppMgr.getUIOffset(), uiClkRect[2], guiColors, argObj.getCreationFormatVal(), argObj.getBtnColors());
				_guiObjsAra[i].setRenderer(renderer);			
			}		
			
			//Objects are created by here and assigned renderers
			// Assign hotspots for UI components
			myPointf newStPt = new myPointf(uiClkRect[0], uiClkRect[1], 0);
			boolean lastObjWasMultiLine = false;
			for (int i = 0; i < _guiObjsAra.length; ++i) {
				boolean isGUIBtn = (_guiObjsAra[i].getObjType() == GUIObj_Type.Button);
				if (lastObjWasMultiLine && (!_guiObjsAra[i].isMultiLine())) {
					newStPt.x = uiClkRect[0];
					newStPt.y = _guiObjsAra[i-1].getEnd().y;
				}
				float txHtOffset = 
						isGUIBtn ||(_guiObjsAra[i].getObjType() == GUIObj_Type.LabelVal) ? 
								AppMgr.getLabelTextHeightOffset() : 
									AppMgr.getTextHeightOffset();
				// Get next newStPt as we calculate the hotspot region for every UI object
				newStPt = _guiObjsAra[i].reCalcHotSpot(newStPt, txHtOffset, uiClkRect[0], uiClkRect[2]);		
				lastObjWasMultiLine = _guiObjsAra[i].isMultiLine();
			}
			//specify the end of this block of UI clickable coordinates based on if last object was multi-line or not
			uiClkRect[3] = lastObjWasMultiLine ?  _guiObjsAra[_guiObjsAra.length-1].getEnd().y : newStPt.y;							
			uiClkRect[3] -= .5f*textHeightOffset;
		}//more than 1 UI object
		// return final y coordinate
		uiClkRect[3] += AppMgr.getRowStYOffset();
		return uiClkRect[3];
	}//_buildGUIObjsForMenu
		
	/**
	 * Convenience method to build debug button, since used as first button in many applications.
	 * @return
	 */
	public GUIObj_Params buildDebugButton(String trueLabel, String falseLabel) {
		return uiObjInitAra_Btn("Debug Button", trueLabel, falseLabel, Base_BoolFlags.debugIDX);
	}
	
	/**
	 * Set labels of GUI Switch objects for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new true label
	 * @param fLbl new false label
	 */
	public void setGUISwitchLabels(int idx, String tLbl, String fLbl) {
		this._guiSwitchIDXMap.get(idx).setBooleanLabelVals(new String[] {fLbl, tLbl}, false);
	}
	
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
	public final void updateBoolValFromExecCode(int idx, boolean value) {setPrivFlag(idx, value);_uiUpdateData.setBoolValue(idx, value);}
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
			case Switch : {
				// Let flag state drive everything
				MenuGUIObj_Switch switchObj = ((MenuGUIObj_Switch)UIobj);
				boolean boolVal = switchObj.getValueAsBoolean();
				// Don't use object UI idx, use priv flags idx
				int flagIDX = switchObj.getBoolFlagIDX();
				//boolean origVal = _uiUpdateData.getBoolValue(flagIDX);
				setPrivFlag(flagIDX, boolVal);
				if(checkAndSetBoolValue(flagIDX, boolVal)) {
					if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//was Special per-obj boolean handling, if pertinent
					// ---FLAGS STRUCTURE SHOULD HANDLE THIS ALREADY---
				}								
				//_dispWarnMsg("setUIWinVals", "Attempting set " +boolVal + " as the value for the Switch boolean button UI object : " + objType.toStrBrf());
				break;}
			case Button : {
				// button acts like integer input
				int ival = UIobj.getValueAsInt();
				int origVal = _uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
					//Special per-obj int handling, if pertinent
					owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
				}
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
		if(_privFlagsToClear.size() == 0) {return;}
		// only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : _privFlagsToClear) {if (_privFlags.getFlag(idx)) {setPrivFlag(idx, false);}}
		_privFlagsToClear.clear();
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
	public final void addPrivBtnToClear(int idx) {		_privFlagsToClear.add(idx);	}

	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	private void _initPassedPrivFlagsToTrue(int[] idxs) { 	
		_privFlags.setAllFlagsToTrue(idxs);
		for(int idx=0;idx<idxs.length;++idx) {
			MenuGUIObj_Switch obj = _guiSwitchIDXMap.get(idxs[idx]);
			if (obj != null) {	obj.setValueFromBoolean(true);}
		}
	}	
	
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
	 * Set private flag values. Make sure UI object follows flag state if exists for this falg
	 * @param idx
	 * @param val
	 */
	public final void setPrivFlag(int idx, boolean val) {
		_privFlags.setFlag(idx, val);
		MenuGUIObj_Switch obj = _guiSwitchIDXMap.get(idx);
		if (obj != null) {	obj.setValueFromBoolean(val);}
	}
		
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param enable
	 */
	public final void handlePrivFlagsDebugMode(boolean enable) {
		_dispDbgMsg("handlePrivFlagsDebugMode", "Start App-specific Debug, called from App-specific Debug flags with value "+ enable +".");
		owner.handleOwnerPrivFlagsDebugMode(enable);
		_dispDbgMsg("handlePrivFlagsDebugMode", "End App-specific Debug, called from App-specific Debug flags with value "+ enable +".");
	}
	
	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from UI Manager
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	public void handlePrivFlags(int idx, boolean val, boolean oldVal) {
		_dispDbgMsg("handlePrivFlagsDebugMode", "Start App-specific boolean flag handling, called for idx "+ idx+ " with val " +val +" old value "+ oldVal +".");
		owner.handleOwnerPrivFlags(idx, val, oldVal);
		_dispDbgMsg("handlePrivFlagsDebugMode", "End App-specific boolean flag handling, called for idx "+ idx+ " with val " +val +" old value "+ oldVal +".");
	}
	
	
	/**
	 * Validate that the passed idx exists in the list of objects 
	 * @param idx index of potential objects
	 * @param len number of objects stored in object array
	 * @param callFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether or not the passed index corresponds to a valid location in the array of UI objects
	 */
	private boolean _validateUIObjectIdx(int idx, int len, String callFunc, String desc) {
		if (!MyMathUtils.inRange(idx, 0, len)){
			msgObj.dispErrorMessage(dispMsgClassName, callFunc, 
				"Attempting to access illegal Numeric UI object to "+desc+" (idx :"+idx+" is out of range). Aborting.");
			return false;
		}		
		return true;
	}
	
	/**
	 * Validate whether the passed UI object is a listVal object
	 * @param obj the object to check
	 * @param callFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether the passed UI object is a listVal object
	 */
	private boolean _validateIdxIsListObj(Base_GUIObj obj, String callFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.ListVal) {
			msgObj.dispErrorMessage(dispMsgClassName, callFunc, 
					"Attempting to access illegal List UI object to "+desc+" (object :"+obj.getName()+" is not a list object). Aborting.");
			return false;
		}
		return true;
	}	
	
	/**
	 * Validate whether the passed UI object is a button object
	 * @param obj the object to check
	 * @param callFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether the passed UI object is a listVal object
	 */
	private boolean _validateIdxIsButtonObj(Base_GUIObj obj, String callFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.Button) {
			msgObj.dispErrorMessage(dispMsgClassName, callFunc, 
					"Attempting to access illegal Button object to "+desc+" (object :"+obj.getName()+" is not a button). Aborting.");
			return false;
		}
		return true;
	}	
	
	/**
	 * Validate whether the passed UI object is a 2-state toggleable switch object backed by privFlags
	 * @param obj the object to check
	 * @param callFunc the name of the calling function (for error message) 
	 * @param desc the process being attempted on the UI object (for error message)
	 * @return whether the passed UI object is a listVal object
	 */
	private boolean _validateIdxIsSwitchObj(Base_GUIObj obj, String callFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.Switch) {
			msgObj.dispErrorMessage(dispMsgClassName, callFunc, 
					"Attempting to access illegal Switch object to "+desc+" (object :"+obj.getName()+" is not a 2-state switch). Aborting.");
			return false;
		}
		return true;
	}
	
	/**
	 * Validate that the passed list of values to be used for a toggle switch is the appropriate length (must be 2)
	 * @param listVals
	 * @param callFunc
	 * @param desc
	 * @return
	 */
	private boolean _validateSwitchListValues(String[] listVals, String callFunc, String desc) {
		if(listVals.length != 2) {
			msgObj.dispErrorMessage(dispMsgClassName, callFunc, 
					"Attempting to access illegal Switch object to "+desc+" (the length of the list of values (" + listVals.length+ ") must be 2). Aborting.");
			return false;
		}
		return true;
	}
		
	
	/**
	 * Sets the passed UI object's new max value
	 * @param idx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes
	 * @param maxVal
	 * @return whether modification was performed or not
	 */
	public boolean setNewUIMaxVal(int idx, double maxVal) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIMaxVal", "set its max value")) {_guiObjsAra[idx].setNewMax(maxVal);return true;}	
		return false;
	}
	
	
	/**
	 * Sets the passed UI object's new min value
	 * @param idx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes.
	 * @param minVal
	 * @return whether modification was performed or not
	 */
	public boolean setNewUIMinVal(int idx, double minVal) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "setNewUIMinVal", "set its min value")) {_guiObjsAra[idx].setNewMin(minVal);return true;}
		return false;
	}
	
	/**
	 * Force a value to be set in the numeric UI object at the passed idx
	 * @param idx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes and returns -Double.MAX_VALUE
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
	 * Specify a state for a button to be in based on the passed string
	 * @param idx
	 * @param val
	 * @return
	 */
	public int[] setDispUIButtonState(int idx, String val) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsAra.length, "setDispUIButtonState", "display passed state")) || 
				(!_validateIdxIsButtonObj(_guiObjsAra[idx], "setDispUIButtonState", "display passed state"))){return new int[0];}
		return ((MenuGUIObj_Button) _guiObjsAra[idx]).setStateByLabel(val);
	}
	
	/**
	 * Set all the state names in the uiObjIdx Button Object, if it exists, and is a button
	 * @param uiObjIdx the button obj's index
	 * @param values the new state names to set for the button
	 * @param setAsDefault whether or not these new values should be set as the default states for this button
	 * @return
	 */
	public int setAllUIButtonStates(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if ((!_validateUIObjectIdx(uiObjIdx, _guiObjsAra.length, "setAllUIButtonStates", "set/replace all button states")) || 
				(!_validateIdxIsButtonObj(_guiObjsAra[uiObjIdx], "setAllUIButtonStates", "set/replace all button states"))){return -1;}
		return ((MenuGUIObj_Button) _guiObjsAra[uiObjIdx]).setStateLabels(values, setAsDefault);
	}	
	
	/**
	 * Specify the state for a 2-state toggle switch object backed by privFlags to be in based on the passed string
	 * @param idx
	 * @param val
	 * @return
	 */
	public int[] setDispUISwitchState(int idx, String val) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsAra.length, "setDispUISwitchState", "display passed state")) || 
				(!_validateIdxIsSwitchObj(_guiObjsAra[idx], "setDispUISwitchState", "display passed state"))){return new int[0];}
		return ((MenuGUIObj_Switch) _guiObjsAra[idx]).setStateByLabel(val);
	}
	
	/**
	 * Set all the state names in the uiObjIdx 2-state toggle switch object backed by privFlags, if it exists, and is a button
	 * @param uiObjIdx the button obj's index
	 * @param values the new state names to set for the button
	 * @param setAsDefault whether or not these new values should be set as the default states for this button
	 * @return
	 */
	public int setAllUISwitchStates(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if (!_validateSwitchListValues(values, "setAllUISwitchStates","set/replace both switch states") ||			
				(!_validateUIObjectIdx(uiObjIdx, _guiObjsAra.length, "setAllUISwitchStates", "set/replace both switch states")) || 
				(!_validateIdxIsSwitchObj(_guiObjsAra[uiObjIdx], "setAllUISwitchStates", "set/replace both switch states"))){return -1;}
		return ((MenuGUIObj_Switch) _guiObjsAra[uiObjIdx]).setStateLabels(values, setAsDefault);
	}
		
	/**
	 * Retrieve the min value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return min value allowed, or Double.MAX_VALUE if idx out of range
	 */
	public double getMinUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getMinUIValue","get its min value")) {return _guiObjsAra[idx].getMinVal();}
		return Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the max value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return max value allowed, or -Double.MAX_VALUE if idx out of range
	 */
	public double getMaxUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getMaxUIValue","get its max value")){return _guiObjsAra[idx].getMaxVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the mod step value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return mod value of UI object, or 0 if idx out of range
	 */
	public double getModStep(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getModStep", "get its mod value")) {return _guiObjsAra[idx].getModStep();}
		return 0;
	}
	
	/**
	 * Retrieve the value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return the current value of the UI object, or -Double.MAX_VALUE if idx out of range
	 */
	public double getUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsAra.length, "getUIValue", "get its value")) {return _guiObjsAra[idx].getVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Get the string representation of the passed integer listIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param listIdx index in list of elements to access
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getListValStr(int UIidx, int listIdx) {		
		if ((!_validateUIObjectIdx(UIidx, _guiObjsAra.length, "getListValStr", "get a list value at specified idx")) || 
				(!_validateIdxIsListObj(_guiObjsAra[UIidx], "getListValStr", "get a list value at specified idx"))){return "";}
		return ((MenuGUIObj_List) _guiObjsAra[UIidx]).getListValStr(listIdx);
	}
	
	/**
	 * Get the string representation of the passed integer buttonIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param buttonStIdx index in list of elements to access
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getButtonStateStr(int UIidx, int buttonStIdx) {		
		if ((!_validateUIObjectIdx(UIidx, _guiObjsAra.length, "getButtonStateStr", "get a button state at specified idx")) || 
				(!_validateIdxIsButtonObj(_guiObjsAra[UIidx], "getButtonStateStr", "get a button state at specified idx"))){return "";}
		return ((MenuGUIObj_Button) _guiObjsAra[UIidx]).getStateLabel(buttonStIdx);
	}
		
	/**
	 * Get the string representation of the passed integer switchIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param switchStIdx either 0 or 1
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getSwitchStateStr(int UIidx, int switchStIdx) {		
		if ((!_validateUIObjectIdx(UIidx, _guiObjsAra.length, "getSwitchStateStr", "get a switch state at specified idx")) || 
				(!_validateIdxIsSwitchObj(_guiObjsAra[UIidx], "getSwitchStateStr", "get a switch state at specified idx"))){return "";}
		return ((MenuGUIObj_Button) _guiObjsAra[UIidx]).getStateLabel(switchStIdx);
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
		for (Integer idx : _guiIntValIDXs) {			intValues.put(idx, _guiObjsAra[idx].getValueAsInt());}		
		//TODO 
		//for (Integer idx : _guiButtonIDXs) {			intValues.put(idx, _guiObjsAra[idx].getValueAsInt());}	
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		for (Integer idx : _guiFloatValIDXs) {			floatValues.put(idx, _guiObjsAra[idx].getValueAsFloat());}
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
		//TODO 
//		for (var switchIdxs : _guiSwitchIDXMap.entrySet()) {
//			int flagIdx = switchIdxs.getKey();
//			int idx = switchIdxs.getValue();
//			MenuGUIObj_Switch toggleObj = ((MenuGUIObj_Switch)_guiObjsAra[idx]);
//			boolValues.put(flagIdx, toggleObj.getValueAsBoolean());
//		}
		
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
		//if(!retVals[0]) {			retVals[0] = _checkUIButtons(mouseX, mouseY);	}
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
	 * Handle mouse move (without a button pressed) over the window - returns the object ID of the object the mouse is over
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
	 * Handle the mouse being dragged (i.e. moved with a button pressed) from within the confines of a selected object
	 * @param delX
	 * @param delY
	 * @param shiftPressed
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseDrag(int delX, int delY, int mseBtn, boolean shiftPressed) {return _handleMouseModInternal(delX+(delY*-(shiftPressed ? 50.0f : 5.0f)));}	
	
	/**
	 * Set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	 * @param j
	 */
	private void _setUIObjValFromClickAlone(Base_GUIObj obj) {		obj.clickModVal(_msBtnClicked * -2.0f + 1, AppMgr.clickValModMult());	}
	
	/**
	 * Handle UI functionality when mouse is released in owner
	 * @param objModified whether object was clicked on but not changed - this will change cause the release to increment the object's value
	 * @return whether or not _privFlagsToClear has buttons to clear.
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
		return _privFlagsToClear.size() > 0;
	}//handleMouseRelease
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// End Mouse and keyboard handling; Start UI object rendering	
	
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
	
//	/**
//	 * Draw a series of strings in a row
//	 * @param txt
//	 * @param loc
//	 * @param clrAra
//	 */
//	private final void _dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
//		ri.setFill(clrAra, clrAra[3]);
//		ri.setColorValStroke(IRenderInterface.gui_Black,255);
//		ri.drawRect(loc);		
//		ri.setColorValFill(IRenderInterface.gui_Black,255);
//		//ri.translate(-xOff*.5f,-yOff*.5f);
//		ri.showText(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
//		//ri.translate(width, 0);
//	}
//	
//	/**
//	 * Draw application-specific flag buttons
//	 * @param useRandBtnClrs
//	 */
//	private final void drawAppFlagButtons(boolean useRandBtnClrs) {
//		ri.pushMatState();	
//		ri.setColorValFill(IRenderInterface.gui_Black,255);
//		if(useRandBtnClrs){
//			for(int i =0; i<_privModFlgIdxs.length; ++i){
//				int btnFlagIdx = _privFlags.getFlag(_privModFlgIdxs[i])  ? 1 : 0;
//				_dispBttnAtLoc(_privFlagButtonLabels[btnFlagIdx][i],_privFlagBtns[i],_privFlagButtonColors[btnFlagIdx][i]);	
//			}
//		} else {
//			for(int i =0; i<_privModFlgIdxs.length; ++i){
//				int btnFlagIdx = _privFlags.getFlag(_privModFlgIdxs[i])  ? 1 : 0;
//				_dispBttnAtLoc(_privFlagButtonLabels[btnFlagIdx][i],_privFlagBtns[i],btnColors[btnFlagIdx]);	
//			}	
//		}		
//		ri.popMatState();	
//	}//drawAppFlagButtons
	
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
