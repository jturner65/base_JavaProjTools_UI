package base_UI_Objects.windowUI.uiObjs.collection;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;

//import java.util.ArrayList;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.UIObjectManager;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.Base_ReadOnlyGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
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

/**
 * Implementations of this class will own 1 or more UI objects to be displayed
 * together as a group/collection
 */
public class GUIObj_Collection {	
    /**
     * Owning UI Object manager
     */
    private UIObjectManager uiObjMgr;
    /**
	 * Render interface
	 */
	public static IRenderInterface ri;
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	/**
	 * subregion of window where UI objects this collection manages may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	protected float[] _uiClkCoords;
	
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
	
	//////////////////////////////////////
	// Mouse interaction handling
	//////////////////////////////////////
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
	private int _msOvrObjIDX;	
	
	//////////////////////////////////////
	// End Mouse interaction handling
	//////////////////////////////////////
	
	// Class name to use for any debugging messages
	private final String _dispMsgClassName;
	
	/**
	 * stroke and fill colors for rendering debug rectangle
	 */
	private final int[][] _dbgColors;

	public GUIObj_Collection(UIObjectManager _uiObjMgr, float[] __uiClkCoords, LinkedHashMap<String, GUIObj_Params> _GUIObjMap) {
		uiObjMgr = _uiObjMgr;
		ri=UIObjectManager.ri;
		AppMgr = UIObjectManager.AppMgr;
		System.arraycopy(__uiClkCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);
		_dispMsgClassName = this.getClass().getSimpleName();
		_msClickObj = null;
        //initialize maps to hold idxs of int and float items being created.
        _guiButtonIDXMap = new LinkedHashMap<Integer,GUIObj_Button>();
        
        _guiSwitchIDXMap = new LinkedHashMap<Integer,GUIObj_Switch>();
 
        _guiFloatValIDXMap = new LinkedHashMap<Integer, GUIObj_Float>();
        _guiIntValIDXMap = new LinkedHashMap<Integer,GUIObj_Int> ();
        _guiReadOnlyObjIDXMap = new LinkedHashMap<Integer, Base_ReadOnlyGUIObj>();
        
        _guiObjsIDXMap = new LinkedHashMap<Integer,Base_GUIObj>(); // list of modifiable gui objects
		
		_uiClkCoords[3] = _buildGUIObjsForMenu(_GUIObjMap, 2, _uiClkCoords);	
		_uiClkCoords[3] += AppMgr.getRowStYOffset();
		// stroke and fill colors for rendering debug rectangle
		_dbgColors = ri.getRndMatchedStrkFillClrs();
		// make fill alpha a bit lighter
		_dbgColors[1][3]=150;	
	}

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
        if (_argObj.isButton()) {       return new ButtonGUIObjRenderer(ri, (GUIObj_Button)_owner, _off, _argObj);}
        // Build multi-line renderer if multi-line non-button
        if (_argObj.isMultiLine()) {    return new MultiLineGUIObjRenderer(ri, _owner, _off, _argObj);} 
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
                uiObjMgr._dispWarnMsg("_buildObj", "Attempting to instantiate unknown UI object for a " + argObj.objType.toStrBrf());
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
    private final float _buildGUIObjsAndHotSpots(Map<String, GUIObj_Params> tmpUIObjMap, int objPerLine, float xStart, float yStart) {
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
                yStart = _buildGUIObjsAndHotSpots(tmpUIColMap, grpParams.getNumObjsPerLine(), xStart, yStart);
            } else {                
                int i = params.objIdx;
                _buildObj(i, entry, guiSwitchIDXMap, guiNotSwitchIDXMap);
            }
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
	
	
	private final float _buildGUIObjsForMenu(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap, int objPerLine, float[] uiClkRect) {

		if(tmpUIObjMap.size() > 0) {
            float yStart = 0;
            yStart = _buildGUIObjsAndHotSpots(tmpUIObjMap, objPerLine, uiClkRect[0], yStart);
            uiClkRect[3] += yStart;
		}// UI objects exist	
		// return final y coordinate
		uiClkRect[3] += .5f*AppMgr.getTextHeightOffset();
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
                perPartitionWidth = uiObjAreaWidth/ttlNumPartitions;            // width allowed per partition
        
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
            if(objPartWidth == null) {              objPartWidth = uiObjAreaWidth;          }
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
	
	public final void _buildInitUIUpdateObjVals(TreeMap<Integer, Integer> intValues, TreeMap<Integer, Float> floatValues, TreeMap<Integer, Boolean> boolValues) {
		//integer and list values
		for (var entry : _guiIntValIDXMap.entrySet()) {			intValues.put(entry.getKey(), entry.getValue().getValueAsInt());}		
	}
	
	////////////////////////////////////////
	/// Draw functions
	///
	/**
	 * Draw the UI clickable region rectangle
	 */
	private final void _drawUIRect() {
		ri.setStrokeWt(2.0f);
		ri.setStroke(_dbgColors[0], _dbgColors[0][3]);
		ri.setFill(_dbgColors[1], _dbgColors[1][3]);
		ri.drawRect(0, 0, _uiClkCoords[2]-_uiClkCoords[0], _uiClkCoords[3]-_uiClkCoords[1]);
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
	
	////////////////////////////////////////
	/// End Draw functions	

	
	////////////////////////////////////////
	/// Mouse interaction
	/**
	 * Check if the screen location [x,y] lies within the UI region this object manages
	 * @param x
	 * @param y
	 * @return
	 */
	private final boolean _checkInUIClckCoords(int x, int y) {return ((x > _uiClkCoords[0])&&(x <= _uiClkCoords[2]) && (y > _uiClkCoords[1])&&(y <= _uiClkCoords[3]));}
	
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
	 * Set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	 * @param j
	 */
	private void _setUIObjValFromClickAlone(Base_GUIObj obj) {		obj.clickModVal(_msBtnClicked * -2.0f + 1, AppMgr.clickValModMult());	}	
	
	/**
	 * Handle checking for a mouse click or move in this region
	 * @param mouseX x location on screen
	 * @param mouseY y location on screen
	 * @param mseBtn which button is pressed : 0 is left, 1 is right
	 * @param [out] retVals : 
	 * 				idx 0 is if an object has been modified
	 * 				idx 1 is if we should set "setUIObjMod" to true
	 * @return whether _msClickObj is not null
	 */
	public final boolean checkClickInUIRegion(int mouseX, int mouseY, int mseBtn, boolean isClicModUIVal, boolean[] retVals) {
		_msClickObj = null;
		if (_checkInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			//modify mouseX and mouseY to be relative to beginning of UI click region
			int idx = _checkInAllObjs(mouseX-(int) _uiClkCoords[0], mouseY-(int) _uiClkCoords[1]);
			if(idx >= 0) {
				//found in list of UI objects
				_msBtnClicked = mseBtn; 
				_msClickObj = _guiObjsIDXMap.get(idx);
				_msClickObj.setIsClicked();
				if(isClicModUIVal){//allows for click-mod without dragging
					_setUIObjValFromClickAlone(_msClickObj);
					//Check if modification from click has changed the value of the object
					if(_msClickObj.getIsDirty()) {retVals[1] = true;}
				} 				
				retVals[0] = true;
			}
		}			
		return _msClickObj != null;	
	}
	
	/**
	 * Handle mouse move (without a button pressed) over the window - returns the object ID of the object the mouse is over
	 * @param mouseX
	 * @param mouseY
	 * @return Whether or not the mouse has moved over a valid UI object
	 */
	public final boolean checkMoveInUIRegion(int mouseX, int mouseY){
		if(_checkInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			_msOvrObjIDX = _checkInAllObjs(mouseX, mouseY);
		} else {			_msOvrObjIDX = -1;		}
		return _msOvrObjIDX != -1;
	}//handleMouseMov

	
	/**
	 * Handle mouse-driven modification to a UI object, by modAmt
	 * @param modAmt the amount to modify the UI object
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	private final boolean[] _handleMouseModInternal(double modAmt, boolean[] retVals) {
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
	}//_handleMouseModInternal
	
	/**
	 * Handle the mouse wheel changing providing interaction with UI objects.
	 * @param ticks
	 * @param mult
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseWheelInUIRegion(int ticks, float mult) {
		// idx 0 is if an object has been modified
		// idx 1 is if we should set "setUIObjMod" to true
		boolean[] retVals = new boolean[] {false, false};
		return _handleMouseModInternal(ticks * mult, retVals);
	}
	
	/**
	 * Handle the mouse being dragged (i.e. moved with a button pressed) from within the confines of a selected object
	 * @param delX
	 * @param delY
	 * @param shiftPressed
	 * @return boolean array : 
	 * 			idx 0 is if an object has been modified
	 * 			idx 1 is if we should set setUIObjMod to true in caller 
	 */
	public final boolean[] handleMouseDragInUIRegion(int delX, int delY, int mseBtn, boolean shiftPressed) {
		// idx 0 is if an object has been modified
		// idx 1 is if we should set "setUIObjMod" to true
		boolean[] retVals = new boolean[] {false, false};
		return _handleMouseModInternal(delX+(delY*-(shiftPressed ? 50.0f : 5.0f)), retVals);
	}
	
	/**
	 * Handle UI functionality when mouse is released - reset object tracking vars
	 * @param objModified whether object was clicked on but not changed - this will change cause the release to increment the object's value
	 * @return whether or not _privFlagsToClear has buttons to clear.
	 */
	public final void handleMouseReleaseInRegion(boolean objModified) {
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
	}//handleMouseRelease
	
	////////////////////////////////////////
	/// End Mouse interaction
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateIntValFromExecCode(int idx, int value) {		_guiObjsIDXMap.get(idx).setVal(value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateFloatValFromExecCode(int idx, float value) {	_guiObjsIDXMap.get(idx).setVal(value);}
	
	/***
	 * Set UI values by object type, sending value to owner and updater
	 * @param UIobj object to set value of
	 * @param UIidx index of object within gui obj ara
	 */
	private final void _setUIWinValsInternal(Base_GUIObj UIobj, int UIidx) {
		//Determine whether int (int or list) or float
		GUIObj_Type objType = UIobj.getObjType();
		switch (objType) {
			case IntVal : {			uiObjMgr.setUI_IntVal(UIobj, UIidx);		break;}
			case ListVal : {			uiObjMgr.setUI_ListVal(UIobj, UIidx);		break;}
			case FloatVal : {		uiObjMgr.setUI_FloatVal(UIobj, UIidx);	break;}
			case LabelVal : {		uiObjMgr.setUI_LabelVal(UIobj, UIidx);	break;}
			case Button : {			uiObjMgr.setUI_BtnVal(UIobj, UIidx);		break;}
			case Switch : {			uiObjMgr.setUI_SwitchVal(UIobj, UIidx);	break;}
			default : {
				uiObjMgr._dispWarnMsg("setUIWinVals ("+_dispMsgClassName+")", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());
				break;}
			
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
	public final void setAllUIWinVals() {for(var entry : _guiObjsIDXMap.entrySet()) {
		var obj = entry.getValue();
		if(obj.shouldUpdateWin(true)){setUIWinVals(obj.getObjID());}}
	}
	
	/**
	 * Return the coordinates of the clickable region for the UI managed by this object manager
	 * @return
	 */
	public float[] getUIClkCoords() {return Arrays.copyOf(_uiClkCoords, _uiClkCoords.length);}
	
	/**
	 * Set the hotspot for this collection
	 * @param __uiClkCoords
	 */
	public final void setUIClkCoords(float[] __uiClkCoords) {System.arraycopy(__uiClkCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);}	
	
	/**
	 * The class name for this collection
	 * @return
	 */
	public final String getClassName() {return _dispMsgClassName;}
	
	/**
	 * Get number of objects created for this collection
	 * @return
	 */
	public final int getNumObjs() {return _guiObjsIDXMap.size();} 
	/**
	 * Set labels of GUI Switch objects for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new true label
	 * @param fLbl new false label
	 */
	public void setGUISwitchLabels(int idx, String tLbl, String fLbl) {_guiSwitchIDXMap.get(idx).setBooleanLabelVals(new String[] {fLbl, tLbl}, false);}
	
	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	public final void initPassedSwitchesToTrue(int[] idxs) {
		for(int idx=0;idx<idxs.length;++idx) {
			GUIObj_Switch obj = _guiSwitchIDXMap.get(idxs[idx]);
			if (obj != null) {	obj.setValueFromBoolean(true);}
		}
	}
	
	/**
	 * Set switch values tjat are backed by a flags structure. Make sure UI object follows flag state if exists for this falg
	 * @param idx
	 * @param val
	 */
	public final void setPrivFlag(int idx, boolean val) {
		GUIObj_Switch obj = _guiSwitchIDXMap.get(idx);
		if (obj != null) {	obj.setValueFromBoolean(val);}
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
			uiObjMgr._dispErrMsg(callFunc, 
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
			uiObjMgr._dispErrMsg(callFunc, 
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
			uiObjMgr._dispErrMsg(callFunc, 
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
			uiObjMgr._dispErrMsg(callFunc, 
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
			uiObjMgr._dispErrMsg(callFunc, 
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
	
}//class GUIObj_Collection
