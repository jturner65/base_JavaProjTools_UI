package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import base_UI_Objects.windowUI.uiObjs.base.Base_ReadOnlyGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.collection.GUIObj_GroupParams;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_DispValue;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_Float;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_Int;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_Label;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_List;
import base_UI_Objects.windowUI.uiObjs.menuObjs.GUIObj_Switch;
import base_UI_Objects.windowUI.uiObjs.renderer.ButtonGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.MultiLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.SingleLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.GUIObjRenderer_Flags;
import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * This class manages all aspects of UI object creation, placement, rendering and interaction.
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
	 * Map for all multi-state button objects not backed by a flags construct, keyed by objIdx
	 */
	private Map<Integer,GUIObj_Button> _guiButtonIDXMap;
	
	/**
	 * Map of all 2-state switch toggle objects connected to privFlags structures, keyed by ***privFlags key*** (not objIdx)
	 */
	private Map<Integer,GUIObj_Switch> _guiSwitchIDXMap;
	
	/**
	 * Map of all idxs for float-based UI objects, keyed by objIdx
	 */
	private Map<Integer, GUIObj_Float> _guiFloatValIDXMap;
	
	/**
	 * Map of all idxs for integer/list-based objects, keyed by objIdx
	 * (This does not include buttons even though button inherits from list object)
	 */
	private Map<Integer,GUIObj_Int>  _guiIntValIDXMap;
	/**
	 * Map list of idxs for label/read-only objects, keyed by objIdx
	 */	
	private Map<Integer, Base_ReadOnlyGUIObj> _guiReadOnlyObjIDXMap;
	/**
	 * Map of all objects, keyed by objIdx
	 */
	private Map<Integer,Base_GUIObj> _guiObjsIDXMap;
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
	 *     	idx 0: value is sent to owning window,  
	 *     	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *     	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *     	idx 3: object is read only
	 */
	private static final boolean[] dfltUIBehaviorVals = new boolean[]{true,false,false,false};	
	/**
	 * Boolean array of default behavior boolean values for label/read-only constructs
	 *     	idx 0: value is sent to owning window,  
	 *     	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *     	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *     	idx 3: object is read only
	 */	
	private static final boolean[] dfltUIReadOnlyBehaviorVals = new boolean[] {false,false,false,true};
	/**
	 * Configuration structure of default renderer format values, if renderer configuration is not otherwise specified, for a single-line, non-switch/button object : 
	 * 			- Should be multiline
	 * 			- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 			- Force this object to be on a new row/line (For side-by-side layouts)
	 * 			- Text should be centered (default is false)
	 * 			- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 			- Should have ornament
	 * 			- Ornament color should match label color 
	 */
	private static final GUIObjRenderer_Flags dfltRndrCfgFmtVals = new GUIObjRenderer_Flags();
	static {
		dfltRndrCfgFmtVals.setIsMultiLine(false);
		dfltRndrCfgFmtVals.setIsOneObjPerLine(true);
		dfltRndrCfgFmtVals.setForceStartNewLine(false);
		dfltRndrCfgFmtVals.setIsCentered(false);
		dfltRndrCfgFmtVals.setHasOutline(false);
		dfltRndrCfgFmtVals.setHasOrnament(true);
		dfltRndrCfgFmtVals.setOrnmntClrMatch(false);
	}
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise specified, for a single-line, non-switch/button object that we wish to share a line/row with others
     *          - Should be multiline
     *          - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *          - Force this object to be on a new row/line (For side-by-side layouts)
     *          - Text should be centered (default is false)
     *          - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *          - Should have ornament
     *          - Ornament color should match label color 
     */
    private static final GUIObjRenderer_Flags dfltRndrInLineCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltRndrInLineCfgFmtVals.setIsMultiLine(false);
        dfltRndrInLineCfgFmtVals.setIsOneObjPerLine(false);
        dfltRndrInLineCfgFmtVals.setForceStartNewLine(false);
        dfltRndrInLineCfgFmtVals.setIsCentered(false);
        dfltRndrInLineCfgFmtVals.setHasOutline(false);
        dfltRndrInLineCfgFmtVals.setHasOrnament(false);
        dfltRndrInLineCfgFmtVals.setOrnmntClrMatch(false);
    }
		
	/**
	 * Configuration structure of default renderer format values, if renderer configuration is not otherwise specified, for a multi-line non-switch/button object : 
	 * 			- Should be multiline
	 * 			- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 			- Force this object to be on a new row/line (For side-by-side layouts)
	 * 			- Text should be centered (default is false)
	 * 			- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 			- Should have ornament
	 * 			- Ornament color should match label color 
	 */
	private static final GUIObjRenderer_Flags dfltMultiLineRndrCfgFmtVals = new GUIObjRenderer_Flags();
	static {
		dfltMultiLineRndrCfgFmtVals.setIsMultiLine(true);
		dfltMultiLineRndrCfgFmtVals.setIsOneObjPerLine(false);
		dfltMultiLineRndrCfgFmtVals.setForceStartNewLine(false);
		dfltMultiLineRndrCfgFmtVals.setIsCentered(true);
		dfltMultiLineRndrCfgFmtVals.setHasOutline(true);
		dfltMultiLineRndrCfgFmtVals.setHasOrnament(false);
		dfltMultiLineRndrCfgFmtVals.setOrnmntClrMatch(false);
	}
	
	/**
	 * Configuration structure of default renderer format values, if renderer configuration is not otherwise specified, for a toggle switch/button backed by a flags structure
	 * 			- Should be multiline
	 * 			- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 			- Force this object to be on a new row/line (For side-by-side layouts)
	 * 			- Text should be centered (default is false)
	 * 			- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 			- Should have ornament
	 * 			- Ornament color should match label color 
	 */
	private static final GUIObjRenderer_Flags dfltBtnRndrCfgFmtVals = new GUIObjRenderer_Flags();
	static {
		dfltBtnRndrCfgFmtVals.setIsMultiLine(false);
		dfltBtnRndrCfgFmtVals.setIsOneObjPerLine(false);
		dfltBtnRndrCfgFmtVals.setForceStartNewLine(false);
		dfltBtnRndrCfgFmtVals.setIsCentered(false);
		dfltBtnRndrCfgFmtVals.setHasOutline(true);
		dfltBtnRndrCfgFmtVals.setHasOrnament(false);
		dfltBtnRndrCfgFmtVals.setOrnmntClrMatch(false);
	}	
	
	/**
	 * Boolean array of default button type format values, if not otherwise specified 
	 *  		idx 0: Whether this button should stay enabled until next draw frame                                                
	 *  		idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
	 */
	private static final boolean[] dfltUIBtnTypeVals =  new boolean[] {false,false};	
	
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
	
	/**
	 * array of priv buttons to be cleared next frame - 
	 * should always be empty except when buttons need to be cleared
	 */
	private ArrayList<Integer> _privFlagsToClear;
	
	/**
	 *  Class name to use for any debugging messages
	 */
	private final String _dispMsgClassName;
	/**
	 * stroke and fill colors for rendering debug rectangle
	 */
	private final int[][] _dbgColors;
	
	public UIObjectManager(IRenderInterface _ri, IUIManagerOwner _owner, GUI_AppManager _AppMgr, MessageObject _msgObj) {
		ri = _ri;
		owner = _owner;
		_uiClkCoords = new float[4];
		_dispMsgClassName = "UIObjectManager ("+owner.getClassName()+")";
		AppMgr = _AppMgr;
		msgObj = _msgObj;
		_msClickObj = null;
		_msBtnClicked = -1;
		// stroke and fill colors for rendering debug rectangle
		_dbgColors = ri.getRndMatchedStrkFillClrs();
		// make fill alpha a bit lighter
		_dbgColors[1][3]=150;	
        _privFlagsToClear = new ArrayList<Integer>();       
        
        //initialize arrays to hold idxs of int and float items being created.
        _guiButtonIDXMap = new LinkedHashMap<Integer,GUIObj_Button>();
        
        _guiSwitchIDXMap = new LinkedHashMap<Integer,GUIObj_Switch>();
 
        _guiFloatValIDXMap = new LinkedHashMap<Integer, GUIObj_Float>();
        _guiIntValIDXMap = new LinkedHashMap<Integer,GUIObj_Int> ();
        _guiReadOnlyObjIDXMap = new LinkedHashMap<Integer, Base_ReadOnlyGUIObj>();
        
        _guiObjsIDXMap = new LinkedHashMap<Integer,Base_GUIObj>(); // list of modifiable gui objects
 	}
		
	/**
	 * UI object creation	
	 */
	public void initAllGUIObjects() {

		//////_guiReadOnlyObjIDXMap///////
		// build all UI objects using specifications from instancing window
		owner.initOwnerStateDispFlags();
		
		// Setup proper ui click coords
		setUIClkCoords(owner.getOwnerParentWindowUIClkCoords());
		
		//////////////////////////////
		//build ui objects and buttons
		// ui object values - keyed by object idx, value is object array of describing values
		LinkedHashMap<String, GUIObj_Params> tmpUIObjMap = new LinkedHashMap<String, GUIObj_Params>();
		//  Get configurations for all UI objects from owner implementation.
		//need to use the maximum object index in the map, not the size, since there may be some object IDs not in the map
		owner.setupOwnerGUIObjsAras(tmpUIObjMap);
		// ui button values : map keyed by objId of object arrays : {true label,false label, index in application}
		LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap = new LinkedHashMap<String, GUIObj_Params>();
		//  Get configurations for all UI buttons from owner implementation.
		owner.setupOwnerGUIBoolSwitchAras(tmpUIObjMap.size(), tmpUIBoolSwitchObjMap);
	
		//TODO merge this to build gui objs and priv buttons together (i.e. privButtons are gui objects)
		// Build UI Objects, return bottom edge y value of these UI objects
		_uiClkCoords[3] = _buildGUIObjsForMenu(tmpUIObjMap, tmpUIBoolSwitchObjMap, 2, _uiClkCoords);
		_uiClkCoords[3] += AppMgr.getRowStYOffset();
		
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
	 * This has to be called after UI structs are built and set - this creates and populates the 
	 * structure that serves to communicate UI data to consumer from UI Window.
	 */
	private void _buildUIUpdateStruct() {
		//set up UI->to->Consumer class communication object - only make instance of object here, 
		//initialize it after private flags are built and initialized
		_uiUpdateData = owner.buildOwnerUIDataUpdateObject();
		if (_uiUpdateData == null) {return;}
		TreeMap<Integer, Integer> intValues = new TreeMap<Integer, Integer>();    
		for (var entry : _guiIntValIDXMap.entrySet()) {			intValues.put(entry.getKey(), entry.getValue().getValueAsInt());}		
		//TODO put non-switch button values into int map
		//for (Integer idx : _guiButtonIDXs) {			intValues.put(idx, _guiObjsIDXMap.get(idx).getValueAsInt());}	
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		for (var entry :  _guiFloatValIDXMap.entrySet()) {		floatValues.put(entry.getKey(), entry.getValue().getValueAsFloat());}
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
		//TODO 
//		for (var switchIdxs : _guiSwitchIDXMap.entrySet()) {
//			int flagIdx = switchIdxs.getKey();
//			int idx = switchIdxs.getValue();
//			GUIObj_Switch toggleObj = ((GUIObj_Switch)_guiObjsIDXMap.get(idx));
//			boolValues.put(flagIdx, toggleObj.getValueAsBoolean());
//		}
		
		for(Integer i=0; i < _privFlags.numFlags;++i) {		boolValues.put(i, _privFlags.getFlag(i));}	
		_uiUpdateData.setAllVals(intValues, floatValues, boolValues); 
	}//_buildUIUpdateStruct	
	

	/**
	 * Build the renderer for a UI object 
	 * @param _owner the Base_GUIObj that will own this renderer
	 * @param _off offset in x,y for ornament, if exists
	 * @param _argObj : GUIObj_Params object holding all the configuration values used to build this renderer and the underlying UI Object
	 * @return
	 */
	private Base_GUIObjRenderer _buildObjRenderer(
			Base_GUIObj _owner, 
			double[] _off,
			GUIObj_Params _argObj
		) {		
		if (_argObj.isButton()) {		return new ButtonGUIObjRenderer(ri, (GUIObj_Button)_owner, _off, _argObj);}
		// Build multi-line renderer if multi-line non-button
		if (_argObj.isMultiLine()) {	return new MultiLineGUIObjRenderer(ri, _owner, _off, _argObj);} 
		// Single line is default
		return new SingleLineGUIObjRenderer(ri, _owner, _off, _argObj);			
	}//_buildObjRenderer
	/**
	 * Build the appropriate object based on the passed GUIObj_Params entry and assign it the given guiObjIDX
	 * @param guiObjIDX
	 * @param entry
	 */
	private final void _buildObj(
	        int guiObjIDX, 
	        Map.Entry<String, GUIObj_Params> guiObjEntry, 
	        Map<Integer,Base_GUIObj> guiSwitchIDXMap, 
	        Map<Integer,Base_GUIObj> guiNotSwitchIDXMap ) {
		GUIObj_Params argObj = guiObjEntry.getValue();
		// Set name to be string key in map
		argObj.setName(guiObjEntry.getKey());
		Base_GUIObj obj = null;
		boolean isSwitch = false;
		switch(argObj.objType) {
			case IntVal : {
				obj = new GUIObj_Int(guiObjIDX, argObj);
				_guiIntValIDXMap.put(guiObjIDX, (GUIObj_Int)obj);
				break;}
			case ListVal : {
				obj = new GUIObj_List(guiObjIDX, argObj);
				_guiIntValIDXMap.put(guiObjIDX, (GUIObj_List)obj);
				break;}
			case FloatVal : {
				obj = new GUIObj_Float(guiObjIDX, argObj);
				_guiFloatValIDXMap.put(guiObjIDX, (GUIObj_Float)obj);
				break;}
			case LabelVal :{
				obj = new GUIObj_Label(guiObjIDX, argObj);
				_guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_Label)obj);
				break;}
			case DispVal :{
				obj = new GUIObj_DispValue(guiObjIDX, argObj);
				_guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_DispValue)obj);
				break;}
			case Switch : {
				// Always 2 state toggle that has a flags structure backing it.
				obj = new GUIObj_Switch(guiObjIDX, argObj);
				_guiSwitchIDXMap.put(((GUIObj_Switch)obj).getBoolFlagIDX(), (GUIObj_Switch)obj);
				isSwitch = true;
				break;}
			case Button  :{
				// 2+ state button that does not have a flags structure backing it
				obj = new GUIObj_Button(guiObjIDX, argObj);
				_guiButtonIDXMap.put(guiObjIDX, (GUIObj_Button)obj);
				break;
			}
			default : {
				_dispWarnMsg("_buildObj", "Attempting to instantiate unknown UI object for a " + argObj.objType.toStrBrf());
				return;				
			}				
		}//switch
		// Set renderer
		_guiObjsIDXMap.put(guiObjIDX, obj);
		if(isSwitch) {        guiSwitchIDXMap.put(guiObjIDX, obj);}
		else {                guiNotSwitchIDXMap.put(guiObjIDX, obj);}
		obj.setRenderer(_buildObjRenderer(obj, AppMgr.getUIOffset(), argObj));		
	}//_buildObj
	
	/**
	 * 
	 * @param tmpUIObjMap
	 * @param tmpUIBtnMap
	 * @param objPerLine
	 * @param xStart
	 * @param yStart
	 * @return
	 */
	private final float _buildGUIObjsAndHotSpots(Map<String, GUIObj_Params> tmpUIObjMap, Map<String, GUIObj_Params> tmpUIBtnMap, int objPerLine, float xStart, float yStart) {
        Map<Integer,Base_GUIObj> guiSwitchIDXMap = new LinkedHashMap<Integer,Base_GUIObj>();
        Map<Integer,Base_GUIObj> guiNotSwitchIDXMap = new LinkedHashMap<Integer,Base_GUIObj>();
        // build non-flag-backed switch objects
        
        for (Map.Entry<String, GUIObj_Params> entry : tmpUIObjMap.entrySet()) {
            GUIObj_Params params = entry.getValue();
            if(params.isAGroupOfObjs()) {
                GUIObj_GroupParams grpParams = (GUIObj_GroupParams) params;
                // build a group of objects described within the argObj's group map as a single row of UI objects
                Map<String, GUIObj_Params> tmpUIColMap = grpParams.getParamsGroupMap();
                // Build all the gui objs and hotspots for this row of values
                yStart = _buildGUIObjsAndHotSpots(tmpUIColMap, new LinkedHashMap<String, GUIObj_Params>(),grpParams.getNumObjsPerLine(), xStart, yStart);
            } else {                
                int i = params.objIdx;
                _buildObj(i, entry, guiSwitchIDXMap, guiNotSwitchIDXMap);
            }
        }
        // build switch/button objects 
        for (Map.Entry<String, GUIObj_Params> entry : tmpUIBtnMap.entrySet()) {
            GUIObj_Params params = entry.getValue();
            int i = params.objIdx;
            _buildObj(i, entry, guiSwitchIDXMap, guiNotSwitchIDXMap);       
        }
        
        // offset for each element
        float yOffset = AppMgr.getCloseTextHeightOffset();   
        // main non-toggle switch button components
        if(guiNotSwitchIDXMap.size() > 0) {     yStart =_buildHotSpotRects(objPerLine, yOffset, xStart, yStart, guiNotSwitchIDXMap);  }
        // now address toggle buttons' clickable regions    
        if(guiSwitchIDXMap.size() > 0) {        
            yStart += AppMgr.getXOffsetHalf();
            yStart =_buildHotSpotRects(objPerLine, AppMgr.getSwitchTextHeightOffset(), xStart, yStart, guiSwitchIDXMap); 
        }
        return yStart;	    
	}//_buildGUIObjsAndHotSpots
	
	/**
	 * 
	 * @param tmpUIObjMap
	 * @param uiClkRect
	 * @return
	 */
	private final float _buildGUIObjsForMenu(Map<String, GUIObj_Params> tmpUIObjMap, Map<String, GUIObj_Params> tmpUIBtnMap, int objPerLine, float[] uiClkRect) {
		//build ui objects
		if((tmpUIObjMap.size() > 0) || (tmpUIBtnMap.size() > 0)) {		    
		    float yStart = 0;
		    yStart = _buildGUIObjsAndHotSpots(tmpUIObjMap, tmpUIBtnMap, objPerLine, uiClkRect[0], yStart);
            uiClkRect[3] += yStart;
		}// UI objects exist
		// return final y coordinate
		//uiClkRect[3] += AppMgr.getTextHeightOffset();
		return uiClkRect[3];
	}//_buildGUIObjsForMenu

	/**
	 * Recalculate the number of hotspot partitions for the object IDs in rowPartitionWidths to 
	 * be equally spaced in uiObjAreaWidth (uiObjAreaWidth/currLineHotSpots.size())
	 * @param uiObjAreaWidth total width of menu area
	 * @param rowPartitionWidths the calculated partition widths for all elements on the current, soon-to-be previous, row.
	 * @param ttlRowWidth total width of all the existing partition components for the row (i.e. sum of rowPartitionWidths elements)
	 */
	private void _reCalcPartitions(Float uiObjAreaWidth, TreeMap<Base_GUIObj, Float> rowPartitionWidths, float ttlRowWidth) {
		float ratio = uiObjAreaWidth/ttlRowWidth;
		//fancy-pants
		rowPartitionWidths.replaceAll((k,v) -> v *= ratio);
	}
		
	/**
	 * Build hot-spot rectangles for each rendered object based on number we wish per row and the dimensions of the object's data
	 * @param ttlNumPartitions max number of partitions of the menu width we want to have for objects. They may take up multiple subdivisions
	 * @param hotSpotStartXBorder leftmost x edge of UI placement area
	 * @param hotSpotStartY initial y location to place UI objects
	 * @param _uiObjMap A map holding all the constructed ui objects to build the hotspots for
	 * @return
	 */
	private float _buildHotSpotRects(int ttlNumPartitions, float yOffset, float hotSpotStartXBorder, float hotSpotStartY, Map<Integer,? extends Base_GUIObj> _uiObjMap){
		// offset per line
		float uiObjAreaWidth = 0.98f * AppMgr.getMenuWidth(), 
				perPartitionWidth = uiObjAreaWidth/ttlNumPartitions;			// width allowed per partition
		
		/////////////////////////////////////////////
		// Precompute map of number of partitions, keyed by the widths those partitions will encapsulate (aggregation)
		// Use this map by finding using the width of an object as a lookup in the 
		// map to find the number of partitions that object with require.
		TreeMap<Float, Integer> partitionWidths = new TreeMap<Float, Integer>();
		float ttlPartWidth = perPartitionWidth;
		for(int i=1;i<ttlNumPartitions;++i) {
			partitionWidths.put(ttlPartWidth, i);
			ttlPartWidth+=perPartitionWidth;	
		}
		// add final partition
		partitionWidths.put(uiObjAreaWidth, ttlNumPartitions);
		
		/////////////////////////////////////////////
		// Build partition size and row assignment for every object
		// per-row width of partitions for each object ID.
		TreeMap<Integer, TreeMap<Base_GUIObj,Float>> perRowPerObjPartSize = new TreeMap<Integer, TreeMap<Base_GUIObj,Float>>();
		// current line number of partitions used
		int currLineNumParts = 0;
		float currLineWidth = 0.0f;
		int curRowCount = 0;
		TreeMap<Base_GUIObj,Float> rowPerObjPartSize = new TreeMap<Base_GUIObj,Float>();
		Base_GUIObj uiObj;
		for(var entry : _uiObjMap.entrySet()) {
			uiObj = entry.getValue();
			// max width and height possible for this object
			float objWidth = uiObj.getMaxTextWidth();
			// Width of combined partitions required for object - smallest value larger than width
			Float objPartWidth = partitionWidths.ceilingKey(objWidth);
			if(objPartWidth == null) {				objPartWidth = uiObjAreaWidth;			}
			// Number of partitions required for object
			int objNumParts = partitionWidths.get(objPartWidth);
			// if only 1 per line, if this object should force to start a new line,  or if object is too wide for current line
			if ((uiObj.getIsOneObjPerLine()) || (uiObj.getForceStartNewLine()) || (objPartWidth + currLineWidth > uiObjAreaWidth)) {
				// too big for current row, put on a new row.
				if(currLineNumParts != ttlNumPartitions) {					
					// if fewer current partitions than allowable number, repartition rowPerObjPartSize to fill row
					_reCalcPartitions(uiObjAreaWidth, rowPerObjPartSize, currLineWidth);
				}
				// add rowPerObjPartSize to perRowPerObjPartSize, make a new row map, and clear values
				perRowPerObjPartSize.put(curRowCount++, rowPerObjPartSize);
				rowPerObjPartSize = new TreeMap<Base_GUIObj,Float>();	
				currLineNumParts = 0;
				currLineWidth = 0.0f;
			} 
			rowPerObjPartSize.put(uiObj, objPartWidth);			
			currLineNumParts += objNumParts;
			currLineWidth += objPartWidth;
		}
		// Need to add last row, after making sure objects fill out the available space
		_reCalcPartitions(uiObjAreaWidth, rowPerObjPartSize, currLineWidth);
		// add rowPerObjPartSize to perRowPerObjPartSize
		perRowPerObjPartSize.put(curRowCount++, rowPerObjPartSize);
		
		/////////////////////////////////////////////////
		// Build actual hotspots	
		// by here we have per row partition widths for each object - each row's values should sum to be uiObjAreaWidth
		// we can use this to determine the actual dimensions
		// object parition count, keyed by object id, will be used to build object hotspots once calculated
		TreeMap<Base_GUIObj, myPointf[]> hotSpotObjDimsMap = new TreeMap<Base_GUIObj, myPointf[]>();	
		myPointf[] hotSpotDims = new myPointf[2];
		//int numObjsProced = 0;
		for(Map.Entry<Integer, TreeMap<Base_GUIObj,Float>> perRowMapEntry :  perRowPerObjPartSize.entrySet()) {
			//int rowNum = perRowMapEntry.getKey();
			TreeMap<Base_GUIObj,Float> perRowMap = perRowMapEntry.getValue();
			float maxHeight = 0;
			for(Map.Entry<Base_GUIObj, Float> entry : perRowMap.entrySet()) {
				uiObj = entry.getKey();
				float objHeight = uiObj.getNumTextLines() * yOffset;
				maxHeight = (maxHeight < objHeight ? objHeight : maxHeight);
			}
			float hotSpotStartX = hotSpotStartXBorder;
			float hotSpotEndX = 0.0f;
			for(Map.Entry<Base_GUIObj, Float> entry : perRowMap.entrySet()) {
				uiObj = entry.getKey();
				hotSpotEndX += entry.getValue();
				hotSpotDims = new myPointf[] {new myPointf(hotSpotStartX, hotSpotStartY, 0.0f), new myPointf(hotSpotEndX, hotSpotStartY+maxHeight, 0.0f)};
				hotSpotStartX = hotSpotEndX;	
				hotSpotObjDimsMap.put(uiObj, hotSpotDims);
				//++numObjsProced;
			}
			// For next row
			hotSpotStartY += maxHeight;
		}//for each row
		
		/////////////////////////////////////////////////
		// Now assign hotspots to objects
		// Map-nanigans!
		hotSpotObjDimsMap.forEach((k,v)-> k.setHotSpot(v));

		//hotSpotStartY += AppMgr.getRowStYOffset();
		return hotSpotStartY;
	}//_buildHotSpotRects

	
	/**
	 * Set labels of GUI Switch objects for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new true label
	 * @param fLbl new false label
	 */
	public void setGUISwitchLabels(int idx, String tLbl, String fLbl) {_guiSwitchIDXMap.get(idx).setBooleanLabelVals(new String[] {fLbl, tLbl},false);}
	
	/**
	 * Pass all flag states to initialized structures in instancing window handler
	 */
	public final void refreshPrivFlags() {		_privFlags.refreshAllFlags();	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// UI object initialization and interaction
	
	//////////////////////////////////////////
	/// Start Build GUIObj_Params
	
	
	/**
	 * Build the GUIObj_Params that describes a label object
	 * @param label display label of the object
	 * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Label(int objIdx, String label) {
		return uiObjInitAra_Label(objIdx, label, dfltRndrCfgFmtVals);
	}		
    /**
     * Build the GUIObj_Params that describes a label object in line with multiple other labels
     * @param label display label of the object
     * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
     * @return
     */
    public final GUIObj_Params uiObjInitAra_LabelInLine(int objIdx, String label) {
        return uiObjInitAra_Label(objIdx, label, dfltRndrInLineCfgFmtVals);
    }
	
	/**
	 * Build the GUIObj_Params that describes a label object that is multiLine
	 * @param label display label of the object
	 * NOTE : this method uses the default renderer creation format boolean values for multi-line labels. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_LabelMultiLine(int objIdx, String label) {
		return uiObjInitAra_Label(objIdx, label, dfltMultiLineRndrCfgFmtVals);
	}		

	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param label display label of the object
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Label(int objIdx,  String label, GUIObjRenderer_Flags _rendererCfgFlags) {
		GUIObj_Params obj = new GUIObj_Params(objIdx, label, GUIObj_Type.LabelVal, dfltUIReadOnlyBehaviorVals, _rendererCfgFlags);
		//labels should ignore all values
		obj.initVal = 0;
		return obj;
	}
	/**
	 * Build the GUIObj_Params that describes a label object
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_DispOnly(int objIdx, double initVal, String label) {
		return uiObjInitAra_DispOnly(objIdx, initVal, label, dfltRndrCfgFmtVals);
	}		
	/**
	 * Build the GUIObj_Params that describes a label object that is multiLine
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default renderer creation format boolean values for multi-line labels. Label objects' behavior is restricted
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_DispOnlyMultiLine(int objIdx, double initVal, String label) {
		return uiObjInitAra_DispOnly(objIdx, initVal, label, dfltMultiLineRndrCfgFmtVals);
	}		

	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_DispOnly(int objIdx, double initVal, String label, GUIObjRenderer_Flags _rendererCfgFlags) {
		GUIObj_Params obj = new GUIObj_Params(objIdx, label, GUIObj_Type.DispVal, dfltUIReadOnlyBehaviorVals, _rendererCfgFlags);
		obj.initVal = initVal;
		return obj;
	}
		
	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Int(int objIdx, double[] minMaxMod, double initVal, String label) {
		return uiObjInitAra_Int(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltRndrCfgFmtVals);
	}	
	
    /**
     * Build the GUIObj_Params that describes a integer object
     * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
     * @param initVal initial value for the object
     * @param label display label of the object
     * NOTE : this method uses the default behavior and UI format boolean values
     * @return
     */
    public final GUIObj_Params uiObjInitAra_IntInLine(int objIdx, double[] minMaxMod, double initVal, String label) {
        return uiObjInitAra_Int(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltRndrInLineCfgFmtVals);
    }	
	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Int(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals) {
		return uiObjInitAra_Int(objIdx, minMaxMod, initVal, label, configBoolVals, dfltRndrCfgFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a integer object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_IntMultiLine(int objIdx, double[] minMaxMod, double initVal, String label) {
		return uiObjInitAra_Int(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltMultiLineRndrCfgFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a integer object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the defaultmulti-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_IntMultiLine(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals) {
		return uiObjInitAra_Int(objIdx, minMaxMod, initVal, label, configBoolVals, dfltMultiLineRndrCfgFmtVals);
	}	
	
	/**
	 * Build the GUIObj_Params that describes a integer object
	 * @param objIdx The predefined index constant for this gui object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color 
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Int(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals, GUIObjRenderer_Flags _rendererCfgFlags) {
		GUIObj_Params obj = new GUIObj_Params(objIdx, label, GUIObj_Type.IntVal, configBoolVals, _rendererCfgFlags);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		return obj;
	}
		
	/**
	 * Build the GUIObj_Params that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Float(int objIdx, double[] minMaxMod, double initVal, String label) {
		return uiObjInitAra_Float(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltRndrCfgFmtVals);
	}
	
    /**
     * Build the GUIObj_Params that describes a float object
     * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
     * @param initVal initial value for the object
     * @param label display label of the object
     * NOTE : this method uses the default behavior and UI format boolean values
     * @return
     */
    public final GUIObj_Params uiObjInitAra_FloatInLine(int objIdx, double[] minMaxMod, double initVal, String label) {
        return uiObjInitAra_Float(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltRndrInLineCfgFmtVals);
    }
	
	/**
	 * Build the GUIObj_Params that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Float(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals) {
		return uiObjInitAra_Float(objIdx, minMaxMod, initVal, label, configBoolVals, dfltRndrCfgFmtVals);
	}
		
	/**
	 * Build the GUIObj_Params that describes a float object that is multi-line
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * NOTE : this method uses the default behavior and multi-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_FloatMultiLine(int objIdx, double[] minMaxMod, double initVal, String label) {
		return uiObjInitAra_Float(objIdx, minMaxMod, initVal, label, dfltUIBehaviorVals, dfltMultiLineRndrCfgFmtVals);
	}

	/**
	 * Build the GUIObj_Params that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default multi-line enabled UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_FloatMultiLine(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals) {
		return uiObjInitAra_Float(objIdx, minMaxMod, initVal, label, configBoolVals, dfltMultiLineRndrCfgFmtVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param label display label of object
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color 
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Float(int objIdx, double[] minMaxMod, double initVal, String label, boolean[] configBoolVals, GUIObjRenderer_Flags _rendererCfgFlags) {
		GUIObj_Params obj = new GUIObj_Params(objIdx, label, GUIObj_Type.FloatVal, configBoolVals, _rendererCfgFlags);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		return obj;
	}

	/**
	 * Build the GUIObj_Params that describes a list object
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_List(int objIdx, double initVal, String label, String[] listElems) {
		return uiObjInitAra_List(objIdx, initVal, label, listElems, dfltUIBehaviorVals, dfltRndrCfgFmtVals);
	}

	/**
	 * Build the GUIObj_Params that describes a list object
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param list of elements this object manages
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_List(int objIdx, double initVal, String label, String[] listElems, boolean[] configBoolVals) {
		return uiObjInitAra_List(objIdx, initVal, label, listElems, configBoolVals, dfltRndrCfgFmtVals);
	}

	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param list of elements this object manages
	 * NOTE : this method uses the default behavior and renderer creation format boolean values for multi-line list box
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_ListMultiLine(int objIdx, double initVal, String label, String[] listElems) {
		return uiObjInitAra_List(objIdx, initVal, label, listElems, dfltUIBehaviorVals, dfltMultiLineRndrCfgFmtVals);
	}


	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param list of elements this object manages
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default renderer creation format boolean values for multi-line list box
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_ListMultiLine(int objIdx, double initVal, String label, String[] listElems, boolean[] configBoolVals) {
		return uiObjInitAra_List(objIdx, initVal, label, listElems, configBoolVals, dfltMultiLineRndrCfgFmtVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a list object that is multi-line
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param list of elements this object manages
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color 
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_ListMultiLine(int objIdx, double initVal, String label, String[] listElems, boolean[] configBoolVals, GUIObjRenderer_Flags _rendererCfgFlags) {
		return uiObjInitAra_List(objIdx, initVal, label, listElems, configBoolVals, _rendererCfgFlags);
	}
		
	/**
	 * Build the GUIObj_Params that describes a list object
	 * @param objIdx the index of this object in the guiObjAra
	 * @param initVal initial value for the object
	 * @param label display label of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_List(int objIdx, double initVal, String label, String[] listElems, boolean[] configBoolVals, GUIObjRenderer_Flags _rendererCfgFlags) {
		double[] minMaxMod = new double[] {0, listElems.length-1, 1};
		GUIObj_Params obj = new GUIObj_Params(objIdx, label, GUIObj_Type.ListVal, configBoolVals, _rendererCfgFlags);
		obj.setMinMaxMod(minMaxMod);
		obj.initVal = initVal;
		obj.setListVals(listElems);	
		return obj;	
	}	
	
	/**
	 * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this switch
	 * @param trueLabel - label for this switch's true state
	 * @param falseLabel - label for this switch's false state
	 * @param boolFlagIdx the index of the boolean flag that interacts with this switch
	 * NOTE : this method uses the default behavior and renderer creation format boolean values for switch object
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Switch(int objIdx, String label, String trueLabel, String falseLabel, int boolFlagIdx) {
		return uiObjInitAra_Switch(objIdx, label, trueLabel, falseLabel, 0, boolFlagIdx, dfltUIBehaviorVals, dfltUIBtnTypeVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param btnIdx the index of this button
	 * @param boolFlagIdx the index of the boolean flag that interacts with this button
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * @param buttonFlags Boolean array of button type format values 
	 *  		idx 0: Whether this button should stay enabled until next draw frame                                                
	 *  		idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
	 * NOTE : this method uses the default renderer creation format boolean values for button/switch
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Switch(int objIdx, String label, String trueLabel, String falseLabel, double initVal, int boolFlagIdx, boolean[] configBoolVals, boolean[] buttonFlags) {
		GUIObj_Params obj;
		String[] labels = new String[]{falseLabel, trueLabel};
		// boolean flag toggle, attached to a privFlags, 
		// TODO : develop multi-line renderer for buttons. Until then always use default
		obj = new GUIObj_Params(objIdx, label, GUIObj_Type.Switch, boolFlagIdx, configBoolVals, dfltBtnRndrCfgFmtVals, buttonFlags);
		obj.setMinMaxMod(new double[] {0, labels.length-1, 1});
		obj.initVal = initVal;
		obj.setListVals(labels);
		// set default 2-state button colors
		obj.setBtnFillColors(btnColors);
		return obj;		
	}
	
	/**
	 * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a listbox)
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * NOTE : this method uses the default UI behavior and button type behavior values for button object
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(int objIdx, String label, String[] labels) {
		return uiObjInitAra_Btn(objIdx, label, labels, 0, dfltUIBehaviorVals, dfltUIBtnTypeVals);
	}
	
	/**
	 * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a listbox)
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this button
	 * @param initVal the initial state this button should have
	 * @param labels the list of labels that describe the valid states for this button
	 * NOTE : this method uses the default UI behavior and button type behavior values for button object
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(int objIdx, double initVal, String label, String[] labels) {
		return uiObjInitAra_Btn(objIdx, label, labels, initVal, dfltUIBehaviorVals, dfltUIBtnTypeVals);
	}

	/**
	 * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a listbox)
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param initVal the initial value to be enabled for this button
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *           	idx 3: object is read only
	 * NOTE : this method uses the default renderer creation format boolean values for button/switch
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(int objIdx, double initVal, String label, String[] labels, boolean[] configBoolVals) {
		return uiObjInitAra_Btn(objIdx, label, labels, initVal, configBoolVals, dfltUIBtnTypeVals);		
	}

	/**
	 * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a listbox)
	 * @param objIdx the index of this object in the guiObjAra
	 * @param name the name of this button
	 * @param labels the list of labels that describe the valid states for this button
	 * @param initVal the initial value to be enabled for this button
	 * @param configBoolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * @param buttonFlags Boolean array of button type format values 
	 *  		idx 0: Whether this button should stay enabled until next draw frame                                                
	 *  		idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
	 * @return
	 */
	public final GUIObj_Params uiObjInitAra_Btn(int objIdx, String label, String[] labels, double initVal, boolean[] configBoolVals, boolean[] buttonFlags) {
		GUIObj_Params obj;
		// Not a toggle
		// TODO : develop multi-line renderer for buttons. Until then always use default
		obj = new GUIObj_Params(objIdx, label, GUIObj_Type.Button, -1, configBoolVals, dfltBtnRndrCfgFmtVals, buttonFlags);		
		obj.setMinMaxMod(new double[] {0, labels.length-1, 1});
		obj.initVal = (initVal >= 0 ? (initVal < labels.length ? initVal : labels.length) : 0);
		obj.setListVals(labels);
		// set random object state colors
		int[][] resClrs= new int[labels.length][4];
		for(int i=0;i<labels.length;++i) {resClrs[i] = MyMathUtils.randomIntClrAra(150, 100, 150);}
		obj.setBtnFillColors(resClrs);
		return obj;		
	}
	
	/**
	 * Build a GUIObj_GroupParams to be used to aggregate GUIObj_Params into a grouping(i.e. a single row, or a group of rows of GUIObj_Params)
	 * This will take all the GUIObj_Params existing in the passed map and put them into a GUIObj_GroupParams.
	 * @param tmpUIObjMap map of all GUIObj_Params to add to new GUIObj_GroupParams.
	 * @return the group object
	 */
	public final GUIObj_GroupParams buildUIObjGroupParams(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap) {
		GUIObj_GroupParams grp =  new GUIObj_GroupParams();
		for(var entry : tmpUIObjMap.entrySet()) {		grp.addObjectsToCollection(entry.getKey(), entry.getValue());	}
		tmpUIObjMap.clear();			
		return grp;
	}
		
	/**
	 * Convenience method to build debug button, since used as first button in many applications.
	 * @return
	 */
	public GUIObj_Params buildDebugButton(int objIdx, String trueLabel, String falseLabel) {
		return uiObjInitAra_Switch(objIdx, "Debug Button", trueLabel, falseLabel, Base_BoolFlags.debugIDX);
	}
   
    //////////////////////////////////////////
    /// End Build GUIObj_Params
    
	/**
	 * Called by _privFlags bool struct, to update _uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	public final void checkSetBoolAndUpdate(int idx, boolean val) {
		if((_uiUpdateData != null) && _uiUpdateData.checkAndSetBoolValue(idx, val)) {owner.updateOwnerCalcObjUIVals();}
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
	public final void updateIntValFromExecCode(int idx, int value) {		_guiObjsIDXMap.get(idx).setVal(value);_uiUpdateData.setIntValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateFloatValFromExecCode(int idx, float value) {	_guiObjsIDXMap.get(idx).setVal(value);_uiUpdateData.setFloatValue(idx, value);}
	
	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for an int-based UIobject (integer or list)
	 * @param UIobj object being set/modified
	 * @param UIidx the index of the object in _uiUpdateData
	 */
	public final void setUI_IntVal(Base_GUIObj UIobj, int UIidx) {
		int ival = UIobj.getValueAsInt();
		int origVal = _uiUpdateData.getIntValue(UIidx);
		if(checkAndSetIntVal(UIidx, ival)) {
			if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
			//Special per-obj int handling, if pertinent
			owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
		}
	}//setUI_IntVal
	
	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for an list-based UIobject (i.e. special case of int object)
	 * @param UIobj object being set/modified
	 * @param UIidx the index of the object in _uiUpdateData
	 */
	public final void setUI_ListVal(Base_GUIObj UIobj, int UIidx) {	setUI_IntVal(UIobj, UIidx);	}//setUI_ListVal

	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for a float-based UIobject
	 * @param UIobj object being set/modified
	 * @param UIidx the index of the object in _uiUpdateData
	 */
	public final void setUI_FloatVal(Base_GUIObj UIobj, int UIidx) {
		float val = UIobj.getValueAsFloat();
		float origVal = _uiUpdateData.getFloatValue(UIidx);
		if(checkAndSetFloatVal(UIidx, val)) {
			if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
			//Special per-obj float handling, if pertinent
			owner.setUI_OwnerFloatValsCustom(UIidx, val, origVal);
		}		
	}//setUI_FloatVal
	
	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for a label. Currently a NO-OP, since labels are not intended to be backed by UI updates.
	 * @param UIobj
	 * @param UIidx
	 */
	public final void setUI_LabelVal(Base_GUIObj UIobj, int UIidx) {
		_dispWarnMsg("setUI_LabelVal", "Attempting to process the value `" + UIobj.getValueAsString()+"` from the `" + UIobj.getName()+ "` read-only object.");	
	}
	
	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for a non-boolean-flag-based button object (i.e. special case of list object)
	 * @param UIobj object being set/modified
	 * @param UIidx the index of the object in _uiUpdateData
	 */
	public final void setUI_BtnVal(Base_GUIObj UIobj, int UIidx) {	setUI_ListVal(UIobj, UIidx);	}//setUI_ListVal
	
	/**
	 * Set the uiUpdateData structure and update the owner if the value has changed for a boolean switch backed by the privFlags structure
	 * @param UIobj object being set/modified
	 * @param UIidx the index of the object in _uiUpdateData
	 */
	public final void setUI_SwitchVal(Base_GUIObj UIobj, int UIidx) {
		// Let flag state drive everything
		GUIObj_Switch switchObj = ((GUIObj_Switch)UIobj);
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
	}//setUI_SwitchVal
		
	/***
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIobj object to set value of
	 * @param UIidx index of object within gui obj ara
	 */
	private final void _setUIWinValsInternal(Base_GUIObj UIobj, int UIidx) {
		//Determine whether int (int or list) or float
		GUIObj_Type objType = UIobj.getObjType();
		switch (objType) {
			case IntVal : {			setUI_IntVal(UIobj, UIidx);			break;}
			case ListVal : {			setUI_ListVal(UIobj, UIidx);			break;}
			case FloatVal : {		setUI_FloatVal(UIobj, UIidx);		break;}
			case LabelVal : {		setUI_LabelVal(UIobj, UIidx);		break;}
			case DispVal : {			setUI_LabelVal(UIobj, UIidx);		break;}
			case Button : {			setUI_BtnVal(UIobj, UIidx);			break;}
			case Switch : {			setUI_SwitchVal(UIobj, UIidx);		break;}
			default : {	_dispWarnMsg("setUIWinVals", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());	break;}			
		}//switch on obj type	
	}//_setUIWinValsInternal
	
	/**
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIidx index of object within gui obj ara
	 */
	public final void setUIWinVals(int UIidx) {			_setUIWinValsInternal(_guiObjsIDXMap.get(UIidx), UIidx);	}//setUIWinVals	
	
	/**
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIidx index of object within gui obj ara
	 */
	public final void setUIWinVals(Base_GUIObj UIobj) {	_setUIWinValsInternal(UIobj, UIobj.getObjID());	}//setUIWinVals	
	
	/**
	 * Reset guiObj given by passed index to starting value
	 * @param UIidx
	 */
	public final void resetUIObj(int UIidx) {				_guiObjsIDXMap.get(UIidx).resetToInit();setUIWinVals(UIidx);}
	
	/**
	 * Reset all values to be initial values. 
	 * @param forceVals If true, this will bypass setUIWinVals, if false, will call set vals, to propagate changes to window vars 
	 */
	public final void resetUIVals(boolean forceVals){
		for (var entry : _guiObjsIDXMap.entrySet()) {		entry.getValue().resetToInit();		}
		if (!forceVals) {			setAllUIWinVals();		}
	}//resetUIVals	
		
	/**
	 * this sets the value of a gui object from the data held in a string
	 * @param str
	 */
	public final void setValFromFileStr(String str){
		String[] toks = str.trim().split("\\|");
		//window has no data values to load
		if(toks.length==0){return;}
		int UIidx = Integer.parseInt(toks[0].split("\\s")[1].trim());
		_guiObjsIDXMap.get(UIidx).setValFromStrTokens(toks);
		setUIWinVals(UIidx);//update window's values with UI construct's values
	}//setValFromFileStr
	
	/**
	 * set all window values for UI objects
	 */
	public final void setAllUIWinVals() {
	    for(var entry : _guiObjsIDXMap.entrySet()) {
	        var obj = entry.getValue();
	        if(obj.shouldUpdateWin(true)){setUIWinVals(obj.getObjID());}
	    }
	}
		
	/**
	 * call after single draw - will clear window-based priv buttons that are momentary
	 */
	public final void clearAllPrivBtns() {
		if(_privFlagsToClear.size() == 0) {return;}
		// only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : _privFlagsToClear) {if (_privFlags.getFlag(idx)) {setPrivFlag(idx,false);}}
		_privFlagsToClear.clear();
	}//clearPrivBtns
	
	/**
	 * clear button next frame - to act like momentary switch.  will also clear UI object
	 * @param idx
	 */
	public final void clearSwitchNextFrame(int idx) {addPrivSwitchToClear(idx);		checkAndSetBoolValue(idx,false);}
		
	/**
	 * add a button to clear after next draw
	 * @param idx index of button to clear
	 */
	public final void addPrivSwitchToClear(int idx) {		_privFlagsToClear.add(idx);	}

	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	private void _initPassedPrivFlagsToTrue(int[] idxs) { 	
		_privFlags.setAllFlagsToTrue(idxs);
		for(int idx=0;idx<idxs.length;++idx) {
			GUIObj_Switch obj = _guiSwitchIDXMap.get(idxs[idx]);
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
		GUIObj_Switch obj = _guiSwitchIDXMap.get(idx);
		if (obj != null) {	obj.setValueFromBoolean(val);}
	}
		
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param enable
	 */
	public final void handlePrivFlagsDebugMode(boolean enable) {
		owner.handleOwnerPrivFlagsDebugMode(enable);
	}
	
	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from UI Manager
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	public void handlePrivFlags(int idx, boolean val, boolean oldVal) {
		owner.handleOwnerPrivFlags(idx, val, oldVal);
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
			_dispErrMsg(callFunc, 
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
			_dispErrMsg(callFunc, 
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
			_dispErrMsg(callFunc, 
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
			_dispErrMsg(callFunc, 
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
			_dispErrMsg(callFunc, 
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
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setNewUIMaxVal", "set its max value")) {_guiObjsIDXMap.get(idx).setNewMax(maxVal);return true;}	
		return false;
	}	
	
	/**
	 * Sets the passed UI object's new min value
	 * @param idx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes.
	 * @param minVal
	 * @return whether modification was performed or not
	 */
	public boolean setNewUIMinVal(int idx, double minVal) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setNewUIMinVal", "set its min value")) {_guiObjsIDXMap.get(idx).setNewMin(minVal);return true;}
		return false;
	}
	
	/**
	 * Force a value to be set in the numeric UI object at the passed idx
	 * @param idx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes and returns -Double.MAX_VALUE
	 * @param val
	 * @return value being set, or -Double.MAX_VALUE if idx is out of range
	 */
	public double setNewUIValue(int idx, double val) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setNewUIValue", "set its value")) {return _guiObjsIDXMap.get(idx).setVal(val);}
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
			if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setNewUIDispText", "set its display text")) {_guiObjsIDXMap.get(idx).setLabel(str);}
			return;
		} else {
			//TODO support boolean UI objects
			if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setNewUIDispText", "set its display text")) {_guiObjsIDXMap.get(idx).setLabel(str);}
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
		if ((!_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setDispUIListVal", "display passed value")) || 
				(!_validateIdxIsListObj(_guiObjsIDXMap.get(idx), "setDispUIListVal", "display passed value"))){return new int[0];}
		return ((GUIObj_List) _guiObjsIDXMap.get(idx)).setValInList(val);
	}
	
	/**
	 * Set all the values in the uiObjIdx List UI Object, if it exists, and is a list object
	 * @param uiObjIdx the list obj's index
	 * @param values the list of values to set
	 * @param setAsDefault whether or not these new values should be set as the default values
	 * @return
	 */
	public int setAllUIListValues(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if ((!_validateUIObjectIdx(uiObjIdx, _guiObjsIDXMap.size(), "setAllUIListValues", "set/replace all list values")) || 
				(!_validateIdxIsListObj(_guiObjsIDXMap.get(uiObjIdx), "setAllUIListValues", "set/replace all list values"))){return -1;}
		return ((GUIObj_List)_guiObjsIDXMap.get(uiObjIdx)).setListVals(values, setAsDefault);
	}
	
	/**
	 * Specify a state for a button to be in based on the passed string
	 * @param idx
	 * @param val
	 * @return
	 */
	public int[] setDispUIButtonState(int idx, String val) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "setDispUIButtonState", "display passed state")) || 
				(!_validateIdxIsButtonObj(_guiObjsIDXMap.get(idx), "setDispUIButtonState", "display passed state"))){return new int[0];}
		return ((GUIObj_Button) _guiObjsIDXMap.get(idx)).setStateByLabel(val);
	}
	
	/**
	 * Set all the state names in the uiObjIdx Button Object, if it exists, and is a button
	 * @param uiObjIdx the button obj's index
	 * @param values the new state names to set for the button
	 * @param setAsDefault whether or not these new values should be set as the default states for this button
	 * @return
	 */
	public int setAllUIButtonStates(int uiObjIdx, String[] values, boolean setAsDefault) {		
		if ((!_validateUIObjectIdx(uiObjIdx, _guiObjsIDXMap.size(), "setAllUIButtonStates", "set/replace all button states")) || 
				(!_validateIdxIsButtonObj(_guiObjsIDXMap.get(uiObjIdx), "setAllUIButtonStates", "set/replace all button states"))){return -1;}
		return ((GUIObj_Button)_guiObjsIDXMap.get(uiObjIdx)).setStateLabels(values, setAsDefault);
	}	
	
	/**
	 * Specify the state for a 2-state toggle switch object backed by privFlags to be in based on the passed string
	 * @param uiObjIdx
	 * @param val
	 * @return
	 */
	public int[] setDispUISwitchState(int uiObjIdx, String val) {		
		if ((!_validateUIObjectIdx(uiObjIdx, _guiObjsIDXMap.size(), "setDispUISwitchState", "display passed state")) || 
				(!_validateIdxIsSwitchObj(_guiObjsIDXMap.get(uiObjIdx), "setDispUISwitchState", "display passed state"))){return new int[0];}
		return ((GUIObj_Switch)_guiObjsIDXMap.get(uiObjIdx)).setStateByLabel(val);
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
				(!_validateUIObjectIdx(uiObjIdx, _guiObjsIDXMap.size(), "setAllUISwitchStates", "set/replace both switch states")) || 
				(!_validateIdxIsSwitchObj(_guiObjsIDXMap.get(uiObjIdx), "setAllUISwitchStates", "set/replace both switch states"))){return -1;}
		return ((GUIObj_Switch)_guiObjsIDXMap.get(uiObjIdx)).setStateLabels(values, setAsDefault);
	}
		
	/**
	 * Retrieve the min value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return min value allowed, or Double.MAX_VALUE if idx out of range
	 */
	public double getMinUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getMinUIValue","get its min value")) {return _guiObjsIDXMap.get(idx).getMinVal();}
		return Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the max value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return max value allowed, or -Double.MAX_VALUE if idx out of range
	 */
	public double getMaxUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getMaxUIValue","get its max value")){return _guiObjsIDXMap.get(idx).getMaxVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the mod step value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return mod value of UI object, or 0 if idx out of range
	 */
	public double getModStep(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getModStep", "get its mod value")) {return _guiObjsIDXMap.get(idx).getModStep();}
		return 0;
	}
	
	/**
	 * Retrieve the value of a numeric UI object
	 * @param idx index in numeric UI object array for the object to access.
	 * @return the current value of the UI object, or -Double.MAX_VALUE if idx out of range
	 */
	public double getUIValue(int idx) {
		if (_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getUIValue", "get its value")) {return _guiObjsIDXMap.get(idx).getVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Get the string representation of the passed integer listIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param listIdx index in list of elements to access
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getListValStr(int idx, int listIdx) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getListValStr", "get a list value at specified idx")) || 
				(!_validateIdxIsListObj(_guiObjsIDXMap.get(idx), "getListValStr", "get a list value at specified idx"))){return "";}
		return ((GUIObj_List)_guiObjsIDXMap.get(idx)).getListValStr(listIdx);
	}
	
	/**
	 * Get the string representation of the passed integer buttonIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param buttonStIdx index in list of elements to access
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getButtonStateStr(int idx, int buttonStIdx) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getButtonStateStr", "get a button state at specified idx")) || 
				(!_validateIdxIsButtonObj(_guiObjsIDXMap.get(idx), "getButtonStateStr", "get a button state at specified idx"))){return "";}
		return ((GUIObj_Button)_guiObjsIDXMap.get(idx)).getStateLabel(buttonStIdx);
	}
		
	/**
	 * Get the string representation of the passed integer switchIdx from the UI Object at UIidx
	 * @param UIidx index in numeric UI object array for the object to access.
	 * @param switchStIdx either 0 or 1
	 * @return the string value at the requested index, or "" if not a valid request
	 */
	public String getSwitchStateStr(int idx, int switchStIdx) {		
		if ((!_validateUIObjectIdx(idx, _guiObjsIDXMap.size(), "getSwitchStateStr", "get a switch state at specified idx")) || 
				(!_validateIdxIsSwitchObj(_guiObjsIDXMap.get(idx), "getSwitchStateStr", "get a switch state at specified idx"))){return "";}
		return ((GUIObj_Button)_guiObjsIDXMap.get(idx)).getStateLabel(switchStIdx);
	}
		
	/**
	 * Get the _uiUpdateData used by the owner
	 * @return
	 */
	public UIDataUpdater getUIDataUpdater() {return _uiUpdateData;}

	
	/**
	 * debug data to display on screen get string array for onscreen display of debug info for each object
	 * @return
	 */
	public final String[] getDebugData(){
		ArrayList<String> res = new ArrayList<String>();
		for(var entry : _guiObjsIDXMap.entrySet()) {res.addAll(Arrays.asList(entry.getValue().getStrData()));}
		return res.toArray(new String[0]);	
	}
	
	/**
	 * Return the coordinates of the clickable region for the UI managed by this object manager
	 * @return
	 */
	public float[] getUIClkCoords() {return Arrays.copyOf(_uiClkCoords, _uiClkCoords.length);}
	/**
	 * Set _uiClkCoords to be passed array
	 * @param cpy
	 */
	public final void setUIClkCoords(float[] cpy){System.arraycopy(cpy, 0, _uiClkCoords, 0, _uiClkCoords.length);}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Start Mouse and keyboard handling	
	/**
	 * updates values in UI with programatic changes 
	 * @param idx
	 * @param val
	 * @return
	 */
	public final boolean setWinToUIVals(int idx, double val){return val == _guiObjsIDXMap.get(idx).setVal(val);}
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
	 * @param isClickModUIVal whether criteria for modifying click without dragging have been specified for this application (i.e. shift is pressed or alt is pressed) 
	 * @param retVals : idx 0 is if an object has been modified
	 * 					idx 1 is if we should set "setUIObjMod" to true
	 * @return _msClickObj
	 */
	public final boolean handleMouseClick(int mouseX, int mouseY, int mseBtn, boolean isClickModUIVal, boolean[] retVals){
		_msClickObj = null;
		//TODO TRACK UI COLLECTION OBJECT THAT IS CURRENTLY ACTIVE
		if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			//modify mouseX and mouseY to be relative to beginning of UI click region
			int idx = _checkInAllObjs(mouseX-(int) _uiClkCoords[0], mouseY-(int) _uiClkCoords[1]);
			if(idx >= 0) {
				//found in list of UI objects
				_msBtnClicked = mseBtn; 
				_msClickObj = _guiObjsIDXMap.get(idx);
				_msClickObj.setIsClicked();
				if(isClickModUIVal){//allows for click-mod without dragging
					_setUIObjValFromClickAlone(_msClickObj);
					//Check if modification from click has changed the value of the object
					if(_msClickObj.getIsDirty()) {retVals[1] = true;}
				} 				
				retVals[0] = true;
			}
		}			
		return _msClickObj != null;
	}//handleMouseClick
	
	/**
	 * Check inside all objects to see if passed mouse x,y is within hotspot
	 * @param mouseX mouse x relative to upper left corner x of the ui click region
	 * @param mouseY mouse y relative to upper left corner y of the ui click region
	 * @return idx of object that mouse resides in, or -1 if none
	 */
	private final int _checkInAllObjs(int mouseX, int mouseY) {
		for(var entry : _guiObjsIDXMap.entrySet()) {	if(entry.getValue().checkIn(mouseX, mouseY)){ return entry.getKey();}}
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
		boolean[] retVals = new boolean[] {false,false};
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
	public final boolean[] handleMouseWheel(int ticks, float mult) {
		//TODO USE UI COLLECTION OBJECT THAT IS CURRENTLY ACTIVE
		return _handleMouseModInternal(ticks * mult);}
	
	/**
	 * Handle the mouse being dragged (i.e. moved with a button pressed) from within the confines of a selected object
	 * @param delX
	 * @param delY
	 * @param shiftPressed
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseDrag(int delX, int delY, int mseBtn, boolean shiftPressed) {
		//TODO USE UI COLLECTION OBJECT THAT IS CURRENTLY ACTIVE		
		return _handleMouseModInternal(delX+(delY*-(shiftPressed ? 50.0f : 5.0f)));}	
	
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
			_msClickObj.clearIsClicked();
			_msClickObj = null;	
		}
		_msBtnClicked = -1;
		return _privFlagsToClear.size() > 0;
	}//handleMouseRelease
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// End Mouse and keyboard handling; Start UI object rendering	
	
	/**
	 * Draw the UI clickable region rectangle (for debugging)
	 */
	private final void _drawUIRect() {
		ri.setStrokeWt(2.0f);
		ri.setStroke(_dbgColors[0], _dbgColors[0][3]);
		ri.setFill(_dbgColors[1], _dbgColors[1][3]);
		ri.drawRect(0,0, _uiClkCoords[2]-_uiClkCoords[0], _uiClkCoords[3]-_uiClkCoords[1]);
	}
	
	/**
	 * Draw all gui objects
	 * @param isDebug
	 * @param animTimeMod
	 */
	public final void drawGUIObjs(boolean isDebug, float animTimeMod) {
		ri.pushMatState();
		// move to beginning of UI region
		ri.translate(_uiClkCoords[0], _uiClkCoords[1], 0);
		//draw UI Objs
		if(isDebug) {
			for (var entry : _guiObjsIDXMap.entrySet()) {entry.getValue().drawDebug();}
			_drawUIRect();
		} else {			
			for (var entry : _guiObjsIDXMap.entrySet()) {entry.getValue().draw();}
		}
		ri.popMatState();	
	}//drawAllGuiObjs
	
	/**
	 * What to do after the owner has finished draw command
	 */
	public final void postDraw(boolean clearPrivBtns) {
		//last thing per draw - clear btns that have been set to clear after 1 frame of display
		if (clearPrivBtns) {clearAllPrivBtns();}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// End UI object rendering	
	
	/**
	 * Shorthand to display general information
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	public final void _dispInfoMsg(String funcName, String message) {msgObj.dispInfoMessage(_dispMsgClassName, funcName, message);}
		
	/**
	 * Shorthand to display a debug message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	public final void _dispDbgMsg(String funcName, String message) {msgObj.dispDebugMessage(_dispMsgClassName, funcName, message);}
	
	/**
	 * Shorthand to display a warning message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	public final void _dispWarnMsg(String funcName, String message) {msgObj.dispWarningMessage(_dispMsgClassName, funcName, message);}
	
	/**
	 * Shorthand to display an error message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	public final void _dispErrMsg(String funcName, String message) {msgObj.dispErrorMessage(_dispMsgClassName, funcName, message);}

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
		for(var entry : _guiObjsIDXMap.entrySet()) {	res.add(entry.getValue().getStrFromUIObj());}		
		//bound for custom components
		res.add(winName + "_custUIComps");
		//add blank space
		res.add("");
		return res;
	}//
	
}//class uiObjectManager
