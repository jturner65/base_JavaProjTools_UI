package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.IUIManagerOwner;
import base_UI_Objects.windowUI.base.WinAppPrivStateFlags;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObjConfig_Flags;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.collection.GUIObj_GroupParams;
import base_UI_Objects.windowUI.uiObjs.menuObjs.buttons.GUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.menuObjs.buttons.GUIObj_Switch;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_Float;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_Int;
import base_UI_Objects.windowUI.uiObjs.menuObjs.numeric.GUIObj_List;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.GUIObj_DispList;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.GUIObj_DispFloat;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.GUIObj_DispInt;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.GUIObj_Label;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.GUIObj_Spacer;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.IReadOnlyGUIObj;
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
    public static IGraphicsAppInterface ri;
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
    private Map<Integer, IReadOnlyGUIObj> _guiReadOnlyObjIDXMap;
    /**
     * Map of all objects, keyed by objIdx
     */
    private Map<Integer,Base_GUIObj> _guiObjsIDXMap;
    /**
     * Map list of idxs for spacer objects, keyed by objIdx
     */    
    private Map<Integer, GUIObj_Spacer> _guiSpacerIDXMap;
    //decrement for each spacer, always put them at the back of the IDXMaps
    private int _spacerIdx = Integer.MAX_VALUE;
    /**
     * Base_GUIObj that was clicked on for modification
     */
    private Base_GUIObj _msClickObj;
    
    /**
     * mouse button clicked - consumed for individual click mod : 0 is left, 1 is right, -1 is none
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
     * Boolean array of default button type format values, if not otherwise specified 
     *          idx 0: Whether this button should stay enabled until next draw frame                                                
     *          idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
     */
    private static final boolean[] dfltUIBtnTypeVals =  new boolean[] {false,false};    
    
    ////////////////////////
    /// owner's private state/functionality flags, (displayed in grid of 2-per-column buttons)
    
    /**
     * UI Application-specific flags and UI components (switch buttons)
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
    
    public UIObjectManager(IGraphicsAppInterface _ri, IUIManagerOwner _owner, GUI_AppManager _AppMgr, MessageObject _msgObj) {
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
     }
        
    /**
     * UI object creation    
     */
    public void initAllGUIObjects() {
        _privFlagsToClear = new ArrayList<Integer>();       
        
        // Linked maps to preserve order of items being inserted
        _guiButtonIDXMap = new LinkedHashMap<Integer,GUIObj_Button>();        
        _guiSwitchIDXMap = new LinkedHashMap<Integer,GUIObj_Switch>(); 
        _guiFloatValIDXMap = new LinkedHashMap<Integer, GUIObj_Float>();
        _guiIntValIDXMap = new LinkedHashMap<Integer,GUIObj_Int> ();
        _guiReadOnlyObjIDXMap = new LinkedHashMap<Integer, IReadOnlyGUIObj>();        
        _guiSpacerIDXMap = new LinkedHashMap<Integer, GUIObj_Spacer>();
        _guiObjsIDXMap = new LinkedHashMap<Integer,Base_GUIObj>(); // list of modifiable gui objects        

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
        
        // Get total number of private booleans
        // (not just switches) used by the application
        int _numPrivFlags = owner.getTotalNumOfPrivBools();
        
        // init specific application state flags and UI switch booleans
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
        for (var entry : _guiIntValIDXMap.entrySet()) {            intValues.put(entry.getKey(), entry.getValue().getValueAsInt());}        
        //Multi-state buttons (i.e. variant of listboxes)
        for (var entry : _guiButtonIDXMap.entrySet()) {            intValues.put(entry.getKey(), entry.getValue().getValueAsInt());}        
        TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
        for (var entry :  _guiFloatValIDXMap.entrySet()) {          floatValues.put(entry.getKey(), entry.getValue().getValueAsFloat());}
        TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
        //NOTE : UI switches are a subset of possible privFlags values, so we are building off the privFlags structure
        
        for(Integer i=0; i < _privFlags.numFlags;++i) {        boolValues.put(i, _privFlags.getFlag(i));}    
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
        if (_argObj.isButton()) {        return new ButtonGUIObjRenderer(ri, (GUIObj_Button)_owner, _off, _argObj);}
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
        double[] _offset = new double[] {AppMgr.getTextHeightOffset(), AppMgr.getTextHeight()};
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
            case SpacerObj :{
                obj = new GUIObj_Spacer(guiObjIDX, argObj);
                _guiSpacerIDXMap.put(guiObjIDX,(GUIObj_Spacer)obj);
                break;}
            case LabelVal :{
                obj = new GUIObj_Label(guiObjIDX, argObj);
                _guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_Label)obj);
                break;}
            case DispIntVal :{
                obj = new GUIObj_DispInt(guiObjIDX, argObj);
                _guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_DispInt)obj);
                break;}
            case DispFloatVal :{
                obj = new GUIObj_DispFloat(guiObjIDX, argObj);
                _guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_DispFloat)obj);
                break;}            
            case DispStr : {
                obj = new GUIObj_DispList(guiObjIDX, argObj);
                _guiReadOnlyObjIDXMap.put(guiObjIDX, (GUIObj_DispList)obj);
                break;}
            case Switch : {
                // Always 2 state toggle that is backed by the _privFlags structure
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
        obj.setRenderer(_buildObjRenderer(obj, _offset, argObj));        
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
            params.setName(entry.getKey());
            if(params.isAGroupOfObjs()) {
                //Recurse if a group of objects
                GUIObj_GroupParams grpParams = (GUIObj_GroupParams) params;
                // build a group of objects described within the argObj's group map as a single row of UI objects
                Map<String, GUIObj_Params> tmpUIColMap = grpParams.getParamsGroupMap();
                // Build all the gui objs and hotspots for this row of values
                yStart = _buildGUIObjsAndHotSpots(tmpUIColMap, new LinkedHashMap<String, GUIObj_Params>(), grpParams.getNumObjsPerLine(), xStart, yStart);
            } else {                
                int i = params.objIdx;
                _buildObj(i, entry, guiSwitchIDXMap, guiNotSwitchIDXMap);
            }
        }
        // build switch/button objects 
        for (Map.Entry<String, GUIObj_Params> entry : tmpUIBtnMap.entrySet()) {
            GUIObj_Params params = entry.getValue();
            params.setName(entry.getKey());
            if(params.isAGroupOfObjs()) {
                //Recurse if a group of objects
                GUIObj_GroupParams grpParams = (GUIObj_GroupParams) params;
                // build a group of objects described within the argObj's group map as a single row of UI objects
                Map<String, GUIObj_Params> tmpUIColSwitchMap = grpParams.getParamsGroupMap();
                // Build all the gui objs and hotspots for this row of values
                yStart = _buildGUIObjsAndHotSpots(new LinkedHashMap<String, GUIObj_Params>(), tmpUIColSwitchMap, grpParams.getNumObjsPerLine(), xStart, yStart);               
                
            } else {
                int i = params.objIdx;
                _buildObj(i, entry, guiSwitchIDXMap, guiNotSwitchIDXMap);
            }
        }
        
        // offset for each element
        // main non-toggle switch button components
        if(guiNotSwitchIDXMap.size() > 0) {     yStart =_buildHotSpotRects(objPerLine, AppMgr.getCloseTextHeightOffset(), xStart, yStart, guiNotSwitchIDXMap);  }
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
            if(objPartWidth == null) {                objPartWidth = uiObjAreaWidth;            }
            // Number of partitions required for object
            int objNumParts = partitionWidths.get(objPartWidth);
            // if only 1 per line, if this object should force to start a new line,  or if object is too wide for current line
            if ((uiObj.getIsOneObjPerRow()) || (uiObj.getForceStartNewLine()) || (objPartWidth + currLineWidth > uiObjAreaWidth)) {
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
                float objHeight = uiObj.getMaxTextHeight(yOffset);
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
    public final void refreshPrivFlags() {        _privFlags.refreshAllFlags();    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// UI object initialization and interaction
       
    /**
     * Build a GUIObjConfig_Flags used to format/configure a UI object. Returns a construct set
     * with default values based on passed flags that can be subsequently modified if necessary by the consumer
     * 
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window == !isReadOnly && !isRangeObject 
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), == default is false
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent), == default is false
     *         objectIsReadOnlyIDX    : object is read only == isReadOnly || isRangeObject
     *         isValueRangeIDX        : object should display a value range and not a value == isRangeObject
     *         
     * @param isReadOnly if the UI object should be read-only or data enterable
     * @param isRangeObject if the UI object should be display the min/max range of the object instead of a value.
     *          TODO : Support for non-read-only range object (i.e. to be used in sampling perhaps).
     *          needs to be added. Until then, specifying isRanged forces isReadOnly to be true.
     * @return
     */
    public final GUIObjConfig_Flags buildGUIObjConfigFlags(boolean isReadOnly, boolean isRangeObject) {
        return new GUIObjConfig_Flags(isReadOnly, isRangeObject);     
    }
    /**
     * Build a GUIObjConfig_Flags with default values used to format/configure an interactive UI object. Returns a construct set
     * with default values based on passed flags that can be subsequently modified if necessary by the consumer
     * 
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  == default is true
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), == default is false
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),== default is false
     *         objectIsReadOnlyIDX    : object is read only == default is false
     *         isValueRangeIDX        : object should display a value range and not a value == default is false
     *         
     * @param isReadOnly if the UI object should be read-only or data enterable
     * @param isRanged if the UI object should be display the min/max range of the object instead of a value.
     *          TODO : Support for non-read-only range object (i.e. to be used in sampling perhaps).
     *          needs to be added. Until then, specifying isRanged forces isReadOnly to be true.
     * @return
     */
    public final GUIObjConfig_Flags buildDefaultGUIObjConfigFlags() {
        return new GUIObjConfig_Flags(false, false);     
    }
        
    /**
     * Build a GUIObjRenderer_Flags used to construct an appropriate renderer for a UI object. Returns a construct set
     * with default values based on passed flags that can be subsequently modified if necessary by the consumer
     * 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color                                                                               
     *                                                                                                                                                                                           
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whehter this object spans an entire UI row 
     * @param _isButton whether the object is a button/switch or a direct-input object
     * @param _isReadOnly whether the object is readonly or modifiable
     * @return
     */
    public final GUIObjRenderer_Flags buildGUIObjRendererFlags(boolean _isMultiLine, boolean _isOnePerRow, boolean _isButton, boolean _isReadOnly) {
        return new GUIObjRenderer_Flags(_isMultiLine, _isOnePerRow, _isButton, _isReadOnly);
    }
    
    //////////////////////////////////////////
    /// Start Build GUIObj_Params

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Whitespace UI object initialization
    
    /**
     * A spacer object is used to space the next, and subsequent, UI objects on a particular row. It takes up the given 
     * width but does not actually represent a UI object, is not drawn and not able to be interacted with. It is only 
     * consumed when the hotspots are created.
     */   
    public final GUIObj_Params uiObjectInitAra_Spacer() {    return uiObjectInitAra_Spacer(new float[] {0.0f, 0.0f}); }
    
    /**
     * A spacer object is used to space the next, and subsequent, UI objects on a particular row. It takes up the given 
     * width but does not actually represent a UI object, is not drawn and not able to be interacted with. It is only 
     * consumed when the hotspots are created.
     * @param dims desired width (idx 0) and height (idx 1) this object should move the next UI object by
     * @return
     */
    public final GUIObj_Params uiObjectInitAra_Spacer(float[] dims) {
        float width = dims[0] > 0 ? dims[0] : _uiClkCoords[2]-_uiClkCoords[0];
        float height = dims[1] > 0 ? dims[1] : AppMgr.getCloseTextHeightOffset();
        GUIObj_Params obj = new GUIObj_Params(_spacerIdx--, "", GUIObj_Type.SpacerObj, buildGUIObjConfigFlags(true, false), buildGUIObjRendererFlags(false, false, false, true)); 
        obj.setSpacerDims(width, height);
        // set color to be transparent
        obj.setReadOnlyColors(new int[] {255,255,255,0});
        return obj;
    }

    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Label UI object initialization and interaction    
    
    /**
     * Build a label with default configuration - single line, does not span entire row
     * @param _objIdx the desired object index
     * @param _label display label used for object
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Label(int _objIdx, String _label) {
        GUIObjRenderer_Flags _rendererCfgFlags = buildGUIObjRendererFlags(false, false,false, true);
        //No ornament for these please.
        _rendererCfgFlags.setHasOrnament(false);
        return uiObjInitAra_Label(_objIdx, _label, buildGUIObjConfigFlags(true, false), _rendererCfgFlags);
    }
    /**
     * Build a label with specified constraints
     * @param _objIdx the desired object index
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whehter this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Label(int _objIdx, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
        GUIObjRenderer_Flags _rendererCfgFlags = buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true);
        //No ornament for these please.
        _rendererCfgFlags.setHasOrnament(false);       
        return uiObjInitAra_Label(_objIdx, _label, buildGUIObjConfigFlags(true, false), _rendererCfgFlags);
    }        

    /**
     * Build the GUIObj_Params that describes a label object
     * @param _objIdx object index
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Label(
            int _objIdx, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.LabelVal, _uiObjConfigFlags, _rendererCfgFlags);
        //labels should ignore all values
        obj.initVal = 0;
        return obj;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// ReadOnly Integer/Integer Range UI object initialization and interaction      
   
    /**
     * Build the GUIObj_Params that describes a readon-only integer
     * @param _objIdx object index
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whehter this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispInt(int _objIdx, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {       
        return uiObjInitAra_DispInt(_objIdx, _initVal, _label, buildGUIObjConfigFlags(true, false), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true));
    }

    /**
     * Build the GUIObj_Params that describes a read-only integer object
     * @param _objIdx object index
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispInt(
            int _objIdx, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.DispIntVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.initVal = _initVal;
        return obj;
    }    
    /**
     * Build the GUIObj_Params that describes a readon-only integer range of values
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispIntRange(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
        return uiObjInitAra_DispIntRange(_objIdx, _minMaxMod, _initVal, _label, buildGUIObjConfigFlags(true, true), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true));
    }
    /**
     * Build the GUIObj_Params that describes a read-only integer object showing a range of values
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value     * 
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispIntRange(
            int _objIdx, 
            double[] _minMaxMod, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.DispIntVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.setMinMaxMod(_minMaxMod);
        obj.initVal = _initVal;
        return obj;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// ReadOnly Float/Float Range UI object initialization and interaction       

    /**
     * Build the GUIObj_Params that describes a read-only floating-point number object
     * @param _objIdx object index
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispFloat(int _objIdx, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
       return uiObjInitAra_DispFloat(_objIdx, _initVal, _label, buildGUIObjConfigFlags(true, false), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true));
    }  
    /**
     * Build the GUIObj_Params that describes a read-only floating-point number object
     * @param _objIdx object index
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispFloat(
            int _objIdx, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
       GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.DispFloatVal, _uiObjConfigFlags, _rendererCfgFlags);
       obj.initVal = _initVal;
       return obj;
    }

    /**
     * Build the GUIObj_Params that describes a read-only floating-point number range of values object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispFloatRange(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
        return uiObjInitAra_DispFloatRange(_objIdx, _minMaxMod, _initVal, _label, buildGUIObjConfigFlags(true, true), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true));
    }

    /**
     * Build the GUIObj_Params that describes a read-only floating-point number range of values object 
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispFloatRange(
            int _objIdx, 
            double[] _minMaxMod, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.DispFloatVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.setMinMaxMod(_minMaxMod);
        obj.initVal = _initVal;
        return obj;
    }    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// ReadOnly Label:String UI object initialization and interaction

    /**
     * Build the GUIObj_Params that describes a read-only object that displays a specific string from a list of possibilities (i.e. read-only list box)
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispString(int _objIdx, double _initVal, String _label, String[] _listElems, boolean _isMultiLine, boolean _isOnePerRow) {
        GUIObjRenderer_Flags _rendererCfgFlags = buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, true);
        //No ornament for these please.
        _rendererCfgFlags.setHasOrnament(false);
        return uiObjInitAra_DispString(_objIdx, _initVal, _label, _listElems, buildGUIObjConfigFlags(true, false), _rendererCfgFlags);
    }

    /**
     * Build the GUIObj_Params that describes a read-only object that displays a specific string from a list of possibilities (i.e. read-only list box)
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_DispString(
            int _objIdx, 
            double _initVal, 
            String _label, 
            String[] _listElems,  
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.DispStr, _uiObjConfigFlags, _rendererCfgFlags);
        obj.initVal = _initVal;
        obj.setListVals(_listElems);    
        return obj;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Interactive Integer-based UI Object  
    
    /**
     * Build the GUIObj_Params that describes an integer UI object with default configurations - 
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Int(int _objIdx, double[] _minMaxMod, double _initVal, String _label) {
        return uiObjInitAra_Int(_objIdx, _minMaxMod, _initVal, _label, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(false, true, false, false));
    }    
    /**
     * Build the GUIObj_Params that describes an integer UI object with given renderer configurations
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Int(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
        return uiObjInitAra_Int(_objIdx, _minMaxMod, _initVal, _label, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }    
    /**
     * Build the GUIObj_Params that describes an integer UI object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Int(int _objIdx, double[] _minMaxMod, double _initVal, String _label, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Int(_objIdx, _minMaxMod, _initVal, _label, _uiObjConfigFlags, buildGUIObjRendererFlags(false, true, false, false));
    }     
    /**
     * Build the GUIObj_Params that describes an integer UI object 
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Int(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Int(_objIdx, _minMaxMod, _initVal, _label, _uiObjConfigFlags, buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }
    
    /**
     * Build the GUIObj_Params that describes a integer object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Int(
            int _objIdx, 
            double[] _minMaxMod, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.IntVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.setMinMaxMod(_minMaxMod);
        obj.initVal = _initVal;
        return obj;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Interactive Floating-point-based UI Object  
       
    /**
     * Build the GUIObj_Params that describes a floating-point UI object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Float(int _objIdx, double[] _minMaxMod, double _initVal, String _label) {
        return uiObjInitAra_Float(_objIdx, _minMaxMod, _initVal, _label, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(false, true, false, false));
    }
    
    /**
     * Build the GUIObj_Params that describes a floating-point UI object with given renderer configurations
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Float(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow) {
        return uiObjInitAra_Float(_objIdx, _minMaxMod, _initVal, _label, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }
    
    /**
     * Build the GUIObj_Params that describes a floating-point UI object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Float(int _objIdx, double[] _minMaxMod, double _initVal, String _label, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Float(_objIdx, _minMaxMod, _initVal, _label, _uiObjConfigFlags, buildGUIObjRendererFlags(false, true, false, false));
    }
    
    /**
     * Build the GUIObj_Params that describes a floating-point UI object
     * @param _objIdx object index
     * @param _minMaxMod array holding the min and max values (mod is ignored)
     * @param _initVal the initial value to display
     * @param _label display label used for object
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Float(int _objIdx, double[] _minMaxMod, double _initVal, String _label, boolean _isMultiLine, boolean _isOnePerRow, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Float(_objIdx, _minMaxMod, _initVal, _label, _uiObjConfigFlags, buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }
   
    /**
     * Build the GUIObj_Params that describes a floating-point UI object
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Float(
            int _objIdx, 
            double[] _minMaxMod, 
            double _initVal, 
            String _label, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.FloatVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.setMinMaxMod(_minMaxMod);
        obj.initVal = _initVal;
        return obj;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Interactive List box UI object

    /**
     * Build the GUIObj_Params that describes a list object with default configuration values
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @return
     */
    public final GUIObj_Params uiObjInitAra_List(int _objIdx, double _initVal, String _label, String[] _listElems) {
        return uiObjInitAra_List(_objIdx, _initVal, _label, _listElems, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(false, true, false, false));
    }

    /**
     * Build the GUIObj_Params that describes a list object 
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a values
     * @return
     */
    public final GUIObj_Params uiObjInitAra_List(int _objIdx, double _initVal, String _label, String[] _listElems, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_List(_objIdx, _initVal, _label, _listElems, _uiObjConfigFlags, buildGUIObjRendererFlags(false, true, false, false));
    }

    /**
     * Build the GUIObj_Params that describes a list object 
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _isMultiLine Whether this object exists on multiple rows
     * @param _isOnePerRow Whether this object spans an entire UI row
     * @return
     */
    public final GUIObj_Params uiObjInitAra_List(int _objIdx, double _initVal, String _label, String[] _listElems, boolean _isMultiLine, boolean _isOnePerRow) {
        return uiObjInitAra_List(_objIdx, _initVal, _label, _listElems, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }

    /**
     * Build the GUIObj_Params that describes a list object 
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a values
     * @return
     */
    public final GUIObj_Params uiObjInitAra_List(int _objIdx, double _initVal, String _label, String[] _listElems, boolean _isMultiLine, boolean _isOnePerRow, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_List(_objIdx, _initVal, _label, _listElems, _uiObjConfigFlags, buildGUIObjRendererFlags(_isMultiLine, _isOnePerRow, false, false));
    }

    /**
     * Build the GUIObj_Params that describes a list object
     * @param _objIdx object index
     * @param _initVal the index in the list of possibilities to use as the initial 
     * @param _label display label used for object
     * @param _listElems list of elements this object manages
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_List(
            int _objIdx, 
            double _initVal, 
            String _label, 
            String[] _listElems, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.ListVal, _uiObjConfigFlags, _rendererCfgFlags);
        obj.setMinMaxMod(new double[] {0, _listElems.length-1, 1});
        obj.initVal = _initVal;
        obj.setListVals(_listElems);    
        return obj;    
    }    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Interactive Switch/2-state button UI object backed by flags construct in owner
    /**
     * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure, with default configurations
     * @param _objIdx object index
     * @param _label display label used for object
     * @param _trueLabel the label for this switch's true state
     * @param _falseLabel the label for this switch's false state
     * @param _boolFlagIdx the index of the boolean flag that interacts with this switch
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Switch(int _objIdx, String _label, String _trueLabel, String _falseLabel, int _boolFlagIdx) {
        return uiObjInitAra_Switch(_objIdx, _label, _trueLabel, _falseLabel, _boolFlagIdx, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(false, false, true, false), dfltUIBtnTypeVals);
    }
    /**
     * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure, used for the main application booleans
     * @param _objIdx object index
     * @param _label display label used for object
     * @param _trueLabel the label for this switch's true state
     * @param _falseLabel the label for this switch's false state
     * @param _boolFlagIdx the index of the boolean flag that interacts with this switch
     * @return
     */
    public final GUIObj_Params uiObjInitAra_SwitchMainBools(int _objIdx, String _label, String _trueLabel, String _falseLabel, int _boolFlagIdx) {
        // main booleans should be one per line and not have 
        var btnRndrObj = buildGUIObjRendererFlags(false, true, true, false);
        btnRndrObj.setHasOutline(false);
        btnRndrObj.setHasOrnament(true);
        return uiObjInitAra_Switch(_objIdx, _label, _trueLabel, _falseLabel, _boolFlagIdx, buildDefaultGUIObjConfigFlags(), btnRndrObj, dfltUIBtnTypeVals);
    }
    
    /**
     * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure, with default configurations
     * @param _objIdx object index
     * @param _label display label used for object
     * @param _trueLabel the label for this switch's true state
     * @param _falseLabel the label for this switch's false state
     * @param _boolFlagIdx the index of the boolean flag that interacts with this switch
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Switch(int _objIdx, String _label, String _trueLabel, String _falseLabel, int _boolFlagIdx, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Switch(_objIdx, _label, _trueLabel, _falseLabel, _boolFlagIdx, _uiObjConfigFlags, buildGUIObjRendererFlags(false, false, true, false), dfltUIBtnTypeVals);
    }
    
    /**
     * Build the GUIObj_Params that describes a boolean switch object, backed by a flag structure
     * @param _objIdx object index
     * @param _label display label used for object
     * @param _trueLabel the label for this switch's true state
     * @param _falseLabel the label for this switch's false state
     * @param _boolFlagIdx the index of the boolean flag that interacts with this switch
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Switch(
            int _objIdx, 
            String _label, 
            String _trueLabel, 
            String _falseLabel,
            int _boolFlagIdx, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags,
            boolean[] buttonFlags) {
        String[] labels = new String[]{_falseLabel, _trueLabel};
        // boolean flag toggle, attached to a privFlags, 
        // TODO : develop multi-line renderer for buttons. Until then always use default
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.Switch, _boolFlagIdx, _uiObjConfigFlags, _rendererCfgFlags, buttonFlags);
        obj.setMinMaxMod(new double[] {0, labels.length-1, 1});
        obj.setListVals(labels);
        // set default 2-state button colors
        obj.setBtnFillColors(btnColors);
        return obj;        
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Interactive Multi-state button (i.e. variant of a list box)

    /**
     * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a list box)
     * @param _objIdx object index
     * @param _initVal the initial selection in _labels to display
     * @param _label display label used for object
     * @param _labels the list of labels that describe the valid states for this button
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Btn(int _objIdx, double _initVal, String _label, String[] _labels) {
        return uiObjInitAra_Btn(_objIdx, _label, _labels, _initVal, buildDefaultGUIObjConfigFlags(), buildGUIObjRendererFlags(false, false, true, false), dfltUIBtnTypeVals);
    }

    /**
     * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a list box)
     * @param _objIdx object index
     * @param _initVal the initial selection in _labels to display
     * @param _label display label used for object
     * @param _labels the list of labels that describe the valid states for this button
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Btn(int _objIdx, double _initVal, String _label, String[] _labels, GUIObjConfig_Flags _uiObjConfigFlags) {
        return uiObjInitAra_Btn(_objIdx, _label, _labels, _initVal, _uiObjConfigFlags, buildGUIObjRendererFlags(false, false, true, false), dfltUIBtnTypeVals);        
    }

    /**
     * Build the GUIObj_Params that describes a button object that is not backed by a flags structure (i.e. a special variant of a list box)
     * @param _objIdx object index
     * @param _initVal the initial selection in _labels to display
     * @param _label display label used for object
     * @param _labels the list of labels that describe the valid states for this button
     * @param _uiObjConfigFlags Object configuration flags
     * Current flags are : 
     *         usedByWinsIDX          : value is sent to owning window,  
     *         updateWhileModIDX      : value is sent on any modifications (while being modified, not just on release), 
     *         explicitUIDataUpdateIDX: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         objectIsReadOnlyIDX    : object is read only
     *         isValueRangeIDX        : object should display a value range and not a value
     * @param _rendererCfgFlags structure that holds various renderer configuration data 
     * Current flags are : 
     *         isMultiLineIDX           : Should be multiline                                                                                                   
     *         isOneObjPerRowIDX        : One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *         forceStartNewLineIDX     : Force this object to be on a new row/line                                                                             
     *         centerTextIDX            : Text should be centered                                                                                               
     *         hasOutlineIDX            : An outline around the object should be rendered                                                                       
     *         hasOrnamentIDX           : Should have ornament                                                                                                  
     *         ornmntClrMatchIDX        : Ornament color should match label color
     * @param buttonFlags Boolean array of button type format values 
     *          idx 0: Whether this button should stay enabled until next draw frame                                                
     *          idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
     * @return
     */
    public final GUIObj_Params uiObjInitAra_Btn(
            int _objIdx, 
            String _label, 
            String[] _labels, 
            double _initVal, 
            GUIObjConfig_Flags _uiObjConfigFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags,
            boolean[] buttonFlags) {
        GUIObj_Params obj = new GUIObj_Params(_objIdx, _label, GUIObj_Type.Button, -1, _uiObjConfigFlags, _rendererCfgFlags, buttonFlags);        
        obj.setMinMaxMod(new double[] {0, _labels.length-1, 1});
        obj.initVal = (_initVal >= 0 ? (_initVal < _labels.length ? _initVal : _labels.length) : 0);
        obj.setListVals(_labels);
        // set random object state colors
        int[][] resClrs= new int[_labels.length][4];
        for(int i=0;i<_labels.length;++i) {resClrs[i] = MyMathUtils.randomIntClrAra(150, 100, 150);}
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
        for(var entry : tmpUIObjMap.entrySet()) {        grp.addObjectsToCollection(entry.getKey(), entry.getValue());    }
        tmpUIObjMap.clear();            
        return grp;
    }
        
    /**
     * Convenience method to build debug button, since used as first button in many applications.
     * @return
     */
    public GUIObj_Params buildDebugButton(int _objIdx, String _trueLabel, String _falseLabel) {
        return uiObjInitAra_Switch(_objIdx, "Debug Button", _trueLabel, _falseLabel, Base_BoolFlags.debugIDX);
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
     * These are called externally from execution code object to synchronize ui values that might change during execution from UI data updater
     * @param idx of particular type of object
     * @param value value to set
     */
    public final void updateBoolValFromExecCode(int idx, boolean value) {setPrivFlag(idx, value);_uiUpdateData.setBoolValue(idx, value);}
    /**
     * These are called externally from execution code object to synchronize ui values that might change during execution from UI data updater
     * @param idx of particular type of object
     * @param value value to set
     */
    public final void updateIntValFromExecCode(int idx, int value) {        _guiObjsIDXMap.get(idx).setVal(value);_uiUpdateData.setIntValue(idx, value);}
    /**
     * These are called externally from execution code object to synchronize ui values that might change during execution from UI data updater
     * @param idx of particular type of object
     * @param value value to set
     */
    public final void updateFloatValFromExecCode(int idx, float value) {    _guiObjsIDXMap.get(idx).setVal(value);_uiUpdateData.setFloatValue(idx, value);}

    /**
     * Sets the passed UI object's new max value. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes
     * @param maxVal
     * @return whether modification was performed or not
     */
    public boolean forceNewUIMaxVal(int uiObjIdx, double maxVal) {
        if (_validateUIObjectIdx(uiObjIdx, "forceNewUIMaxVal", "set its max value")) {
            Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);
            obj.setNewMax(maxVal);
            if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
            return true;
        }    
        return false;
    }    
    
    /**
     * Sets the passed UI object's new min value. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes.
     * @param minVal
     * @return whether modification was performed or not
     */
    public boolean forceNewUIMinVal(int uiObjIdx, double minVal) {
        if (_validateUIObjectIdx(uiObjIdx, "forceNewUIMinVal", "set its min value")) {
            Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);
            obj.setNewMin(minVal);
            if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
            return true;
        }
        return false;
    }
    
    /**
     * Force a value to be set in the numeric UI object at the passed idx. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx index in numeric UI object array for the object to access. If out of range, aborts without performing any changes and returns -Double.MAX_VALUE
     * @param val
     * @return value being set, or -Double.MAX_VALUE if idx is out of range
     */
    public double forceNewUIValue(int uiObjIdx, double val) {
        if (_validateUIObjectIdx(uiObjIdx, "forceNewUIValue", "set its value")) {
            Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);
            double setVal = obj.setVal(val);
            if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
            return setVal;
        }
        return -Double.MAX_VALUE;
    }        
    
    /**
     * Set the display text of the passed UI Object, either numeric or boolean. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx
     * @param isNumeric
     * @param str
     */
    public void forceNewUIDispText(int uiObjIdx, boolean isNumeric, String str) {
        if (isNumeric) {
            if (_validateUIObjectIdx(uiObjIdx, "forceNewUIDispText", "set its display text")) {_guiObjsIDXMap.get(uiObjIdx).setLabel(str);}
        } else {
            //TODO support boolean UI objects?
            if (_validateUIObjectIdx(uiObjIdx, "forceNewUIDispText", "set its display text")) {_guiObjsIDXMap.get(uiObjIdx).setLabel(str);}
        }
    }//forceNewUIDispText
    /**
     * Specify a string to display in the idx'th List UI Object, if it exists, and is a list object. Updates UIDataUpdater if appropriate for object state.
     * @param idx
     * @param val
     * @return
     */
    public int[] forceNewUIDispListVal(int uiObjIdx, String val) {        
        if (!_validateUIObjectIdx(uiObjIdx, "forceNewUIDispListVal", "display passed value")){return new int[0];}
        Base_GUIObj obj =  _guiObjsIDXMap.get(uiObjIdx); 
        if(!_validateIdxIsListObj(obj, "forceNewUIDispListVal", "display passed value")){return new int[0];}
        int[] res = ((GUIObj_List)obj).setValInList(val);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIDispListVal
    
    /**
     * Set all the values in the uiObjIdx List UI Object, if it exists, and is a list object. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx the list obj's index
     * @param values the list of values to set
     * @param setAsDefault whether or not these new values should be set as the default values
     * @return
     */
    public int forceNewUIAllListValues(int uiObjIdx, String[] values, boolean setAsDefault) {        
        if (!_validateUIObjectIdx(uiObjIdx, "forceNewUIAllListValues", "set/replace all list values")){return -1;}
        Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx); 
        if (!_validateIdxIsListObj(obj, "forceNewUIAllListValues", "set/replace all list values")){return -1;}
        int res = ((GUIObj_List)obj).setListVals(values, setAsDefault);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIAllListValues
    
    /**
     * Specify a state for a button to be in based on the passed string. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx
     * @param val
     * @return
     */
    public int[] forceNewUIDispButtonState(int uiObjIdx, String val) {        
        if (!_validateUIObjectIdx(uiObjIdx, "forceNewUIDispButtonState", "display passed state")){return new int[0];}
        Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);
        if (!_validateIdxIsButtonObj(obj, "forceNewUIDispButtonState", "display passed state")){return new int[0];}
        int[] res = ((GUIObj_Button)obj).setStateByLabel(val);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIDispButtonState
    
    /**
     * Set all the state names in the uiObjIdx Button Object, if it exists, and is a button. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx the button obj's index
     * @param values the new state names to set for the button
     * @param setAsDefault whether or not these new values should be set as the default states for this button
     * @return
     */
    public int forceNewUIAllButtonStates(int uiObjIdx, String[] values, boolean setAsDefault) {        
        if (!_validateUIObjectIdx(uiObjIdx, "forceNewUIAllButtonStates", "set/replace all button states")) {return -1;}
        Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);  
        if (!_validateIdxIsButtonObj(obj, "forceNewUIAllButtonStates", "set/replace all button states")){return -1;}
        int res = ((GUIObj_Button)obj).setStateLabels(values, setAsDefault);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIAllButtonStates    
    
    /**
     * Specify the state for a 2-state toggle switch object backed by privFlags to be in based on the passed string. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx
     * @param val
     * @return
     */
    public int[] forceNewUIDispSwitchState(int uiObjIdx, String val) {        
        if (!_validateUIObjectIdx(uiObjIdx, "forceNewUIDispSwitchState", "display passed state")){return new int[0];}
        Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);
        if (!_validateIdxIsSwitchObj(obj, "forceNewUIDispSwitchState", "display passed state")){return new int[0];}
        int[] res =  ((GUIObj_Switch)obj).setStateByLabel(val);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIDispSwitchState
    
    /**
     * Set all the state names in the uiObjIdx 2-state toggle switch object backed by privFlags, if it exists, and is a button. Updates UIDataUpdater if appropriate for object state.
     * @param uiObjIdx the button obj's index
     * @param values the new state names to set for the button
     * @param setAsDefault whether or not these new values should be set as the default states for this button
     * @return
     */
    public int forceNewUIAllSwitchStates(int uiObjIdx, String[] values, boolean setAsDefault) {        
        if (!_validateSwitchListValues(values, "forceNewUIAllSwitchStates","set/replace both switch states") ||            
                (!_validateUIObjectIdx(uiObjIdx, "forceNewUIAllSwitchStates", "set/replace both switch states")))  {return -1;}
        Base_GUIObj obj = _guiObjsIDXMap.get(uiObjIdx);  
        if (!_validateIdxIsSwitchObj(obj, "forceNewUIAllSwitchStates", "set/replace both switch states")){return -1;}
        int res = ((GUIObj_Switch)obj).setStateLabels(values, setAsDefault);
        if(obj.shouldUpdateWin(true)) {updateOwnerWithUIVal(obj);}
        return res;
    }//forceNewUIAllSwitchStates
    
    
    
    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for an int-based UIobject (integer or list)
     * @param UIobj object being set/modified
     * @param UIidx the index of the object in _uiUpdateData
     */
    private final void _setUI_IntVal(Base_GUIObj UIobj, int UIidx) {
        int ival = UIobj.getValueAsInt();
        int origVal = _uiUpdateData.getIntValue(UIidx);
        if(checkAndSetIntVal(UIidx, ival)) {
            if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
            //Special per-obj int handling, if pertinent
            owner.setUI_OwnerIntValsCustom(UIidx, ival, origVal);
        }
    }//_setUI_IntVal
    
    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for an list-based UIobject (i.e. special case of int object)
     * @param UIobj object being set/modified
     * @param UIidx the index of the object in _uiUpdateData
     */
    private final void _setUI_ListVal(Base_GUIObj UIobj, int UIidx) { _setUI_IntVal(UIobj, UIidx);    }//_setUI_ListVal

    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for a float-based UIobject
     * @param UIobj object being set/modified
     * @param UIidx the index of the object in _uiUpdateData
     */
    private final void _setUI_FloatVal(Base_GUIObj UIobj, int UIidx) {
        float val = UIobj.getValueAsFloat();
        float origVal = _uiUpdateData.getFloatValue(UIidx);
        if(checkAndSetFloatVal(UIidx, val)) {
            if(UIobj.shouldUpdateConsumer()) {owner.updateOwnerCalcObjUIVals();}
            //Special per-obj float handling, if pertinent
            owner.setUI_OwnerFloatValsCustom(UIidx, val, origVal);
        }        
    }//_setUI_FloatVal
    
    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for a label. Currently a NO-OP, since labels are not intended to be backed by UI updates.
     * @param UIobj
     * @param UIidx
     */
    private final void _setUI_LabelVal(Base_GUIObj UIobj, int UIidx) {
        _dispWarnMsg("setUI_LabelVal", "Attempting to process the value `" + UIobj.getValueAsString()+"` from the `" + UIobj.getName()+ "` read-only object.");    
    }//_setUI_LabelVal
    
    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for a non-boolean-flag-based button object (i.e. special case of list object)
     * @param UIobj object being set/modified
     * @param UIidx the index of the object in _uiUpdateData
     */
    private final void _setUI_BtnVal(Base_GUIObj UIobj, int UIidx) { _setUI_ListVal(UIobj, UIidx);    }//setUI_ListVal
    
    /**
     * Set the uiUpdateData structure and update the owner if the value has changed for a boolean switch backed by the privFlags structure
     * @param UIobj object being set/modified
     * @param UIidx the index of the object in _uiUpdateData
     */
    private final void _setUI_SwitchVal(Base_GUIObj UIobj, int UIidx) {
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
    }//_setUI_SwitchVal
        
    /***
     * Set UI values by object type, sending value to owner and updater
     * @param UIobj object to set value of
     * @param UIidx index of object within gui obj ara
     */
    private final void _setUIWinValsInternal(Base_GUIObj UIobj, int UIidx) {
        //Determine whether int (int or list) or float
        GUIObj_Type objType = UIobj.getObjType();
        switch (objType) {
            case IntVal         : { _setUI_IntVal(UIobj, UIidx);             break;}
            case ListVal        : { _setUI_ListVal(UIobj, UIidx);            break;}
            case FloatVal       : { _setUI_FloatVal(UIobj, UIidx);           break;}
            case LabelVal       : { _setUI_LabelVal(UIobj, UIidx);           break;}
            case SpacerObj      : { /*spacers will never be interactable */  break;}
            case DispIntVal     : { _setUI_LabelVal(UIobj, UIidx);           break;}
            case DispFloatVal   : { _setUI_LabelVal(UIobj, UIidx);           break;}
            case DispStr        : { _setUI_LabelVal(UIobj, UIidx);           break;}
            case Button         : { _setUI_BtnVal(UIobj, UIidx);             break;}
            case Switch         : { _setUI_SwitchVal(UIobj, UIidx);          break;}
            default : {    _dispWarnMsg("setUIWinVals", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());    break;}            
        }//switch on obj type    
    }//_setUIWinValsInternal
    
    /**
     * Update the data adapter's data from the UI Object with the passed idx. Mostly called after a UI value has changed through user input.
     * @param UIidx index of object within gui obj ara
     */
    protected final void updateOwnerWithUIVal(int UIidx) {            
        // _dispErrMsg("updateOwnerWithUIVal", "Updating UIobj with idx :"+UIidx);
        _setUIWinValsInternal(_guiObjsIDXMap.get(UIidx), UIidx);    }//setUIWinVals    
    
    /**
     * Update the data adapter's data from the passed UI Object. Mostly called after a UI value has changed through user input.
     * @param UIidx index of object within gui obj ara
     */
    protected final void updateOwnerWithUIVal(Base_GUIObj UIobj) {    
        // _dispErrMsg("updateOwnerWithUIVal", "Updating UIobj :"+UIobj.getUIDispAsSingleLine());
        _setUIWinValsInternal(UIobj, UIobj.getObjID());    }//setUIWinVals    

    /**
     * Send all UI values to UIUpdater that have changed since last update
     */
    private final void updateOwnerWithAllNewUIVals(String src) {
        // _dispErrMsg("updateOwnerWithAllNewUIVals", "Updating all objs called from "+src );
        for(var entry : _guiObjsIDXMap.entrySet()) {
            var obj = entry.getValue();
            if(obj.shouldUpdateWin(true)){updateOwnerWithUIVal(obj);}
        }
    }
        
    
    /**
     * Reset guiObj given by passed index to starting/default value
     * @param UIidx
     */
    public final void resetUIObj(int UIidx) {  
        Base_GUIObj obj = _guiObjsIDXMap.get(UIidx); 
        obj.resetToDefault();
        updateOwnerWithUIVal(obj);
    }
    
    /**
     * Reset all values to be initial values. 
     * @param forceVals If true, this will bypass setUIWinVals, if false, will call set vals, to propagate changes to window vars 
     */
    public final void resetUIVals(boolean forceVals){
        for (var entry : _guiObjsIDXMap.entrySet()) {        entry.getValue().resetToDefault();        }
        if (!forceVals) {            updateOwnerWithAllNewUIVals("resetUIVals");        }
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
        Base_GUIObj obj = _guiObjsIDXMap.get(UIidx); 
        obj.setValFromStrTokens(toks);
        updateOwnerWithUIVal(obj);//update window's values with UI construct's values
    }//setValFromFileStr

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
    public final void clearSwitchNextFrame(int idx) {addPrivSwitchToClear(idx);        checkAndSetBoolValue(idx,false);}
        
    /**
     * add a button to clear after next draw
     * @param idx index of button to clear
     */
    public final void addPrivSwitchToClear(int idx) {        _privFlagsToClear.add(idx);    }

    /**
     * sets flag values without calling instancing window flag handler - only for init!
     * @param idxs
     * @param val
     */
    private void _initPassedPrivFlagsToTrue(int[] idxs) {     
        _privFlags.setAllFlagsToTrue(idxs);
        for(int idx=0;idx<idxs.length;++idx) {
            GUIObj_Switch obj = _guiSwitchIDXMap.get(idxs[idx]);
            if (obj != null) {    obj.setDefaultValueFromBoolean(true);}
        }
    }    
    
    /**
     * Access private flag values
     * @param idx
     * @return
     */
    public final boolean getPrivFlag(int idx) {                return _privFlags.getFlag(idx);}
    
    /**
     * Whether or not the _privFlags structure is in debug mode
     * @return
     */
    public final boolean getPrivFlagIsDebug() {                return _privFlags.getIsDebug();}
    
    /**
     * Retrieve the integer representation of the bitflags - the idx'ithed 32 flag bits.
     * @param idx
     * @return
     */
    public final int getPrivFlagAsInt(int idx) {            return _privFlags.getFlagsAsInt(idx);}
    
    /**
     * Set private flag values. Make sure UI object follows flag state if exists for this flag
     * @param idx
     * @param val
     */
    public final void setPrivFlag(int idx, boolean val) {
        _privFlags.setFlag(idx, val);
        GUIObj_Switch obj = _guiSwitchIDXMap.get(idx);
        if (obj != null) {    obj.setValueFromBoolean(val);}
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
    private boolean _validateUIObjectIdx(int idx, String callFunc, String desc) {
        if (!_guiObjsIDXMap.containsKey(idx)){
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
     * Retrieve the min value of a numeric UI object
     * @param idx index in numeric UI object array for the object to access.
     * @return min value allowed, or Double.MAX_VALUE if idx out of range
     */
    public double getMinUIValue(int idx) {
        if (_validateUIObjectIdx(idx, "getMinUIValue","get its min value")) {return _guiObjsIDXMap.get(idx).getMinVal();}
        return Double.MAX_VALUE;
    }
    
    /**
     * Retrieve the max value of a numeric UI object
     * @param idx index in numeric UI object array for the object to access.
     * @return max value allowed, or -Double.MAX_VALUE if idx out of range
     */
    public double getMaxUIValue(int idx) {
        if (_validateUIObjectIdx(idx, "getMaxUIValue","get its max value")){return _guiObjsIDXMap.get(idx).getMaxVal();}
        return -Double.MAX_VALUE;
    }
    
    /**
     * Retrieve the mod step value of a numeric UI object
     * @param idx index in numeric UI object array for the object to access.
     * @return mod value of UI object, or 0 if idx out of range
     */
    public double getModStep(int idx) {
        if (_validateUIObjectIdx(idx, "getModStep", "get its mod value")) {return _guiObjsIDXMap.get(idx).getModStep();}
        return 0;
    }
    
    /**
     * Retrieve the value of a numeric UI object
     * @param idx index in numeric UI object array for the object to access.
     * @return the current value of the UI object, or -Double.MAX_VALUE if idx out of range
     */
    public double getUIValue(int idx) {
        if (_validateUIObjectIdx(idx, "getUIValue", "get its value")) {return _guiObjsIDXMap.get(idx).getVal();}
        return -Double.MAX_VALUE;
    }
    
    /**
     * Get the string representation of the passed integer listIdx from the UI Object at UIidx
     * @param UIidx index in numeric UI object array for the object to access.
     * @param listIdx index in list of elements to access
     * @return the string value at the requested index, or "" if not a valid request
     */
    public String getListValStr(int idx, int listIdx) {        
        if ((!_validateUIObjectIdx(idx, "getListValStr", "get a list value at specified idx")) || 
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
        if ((!_validateUIObjectIdx(idx, "getButtonStateStr", "get a button state at specified idx")) || 
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
        if ((!_validateUIObjectIdx(idx, "getSwitchStateStr", "get a switch state at specified idx")) || 
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
     * Check if passed mouse location is within this object's _uiClkCoords.
     * @param x
     * @param y
     * @return
     */
    public final boolean msePtInUIClckCoords(int x, int y){
        return ((x > _uiClkCoords[0])&&(x <= _uiClkCoords[2])&&(y > _uiClkCoords[1])&&(y <= _uiClkCoords[3]));
    }    
    
    /**
     * handle a mouse click
     * @param mouseX x location on screen
     * @param mouseY y location on screen
     * @param mseBtn which button is pressed : 0 is left, 1 is right
     * @param isClickModUIVal whether criteria for modifying click without dragging have been specified for this application (i.e. shift is pressed or alt is pressed) 
     * @param retVals : idx 0 is if an object has been clicked in
     *                  idx 1 is if we should set "setUIObjMod" to true
     * @return _msClickObj is not equal to null
     */
    public final boolean handleMouseClick(int mouseX, int mouseY, int mseBtn, boolean isClickModUIVal, boolean[] retVals){
        _msClickObj = null;
        //_dispInfoMsg("handleMouseClick", "Start mouse click with mse xy : ["+mouseX+","+mouseY+"] btn : "+mseBtn+" | isClickModUIVal :"+isClickModUIVal);
        if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
            //_dispInfoMsg("handleMouseClick", "Mse xy : ["+mouseX+","+mouseY+"] in UI ClckCoords : now finding object using ["+(mouseX-(int)_uiClkCoords[0])+","+(mouseY-(int)_uiClkCoords[1])+"]");
            //modify mouseX and mouseY to be relative to beginning of UI click region
            int idx = _checkInAllObjs(mouseX-(int)_uiClkCoords[0], mouseY-(int)_uiClkCoords[1]);
            if(idx >= 0) {
                //found in list of UI objects
                _msBtnClicked = mseBtn; 
                _msClickObj = _guiObjsIDXMap.get(idx);
                _msClickObj.setIsClicked();
                retVals[0] = true;
                if(isClickModUIVal){//allows for click-mod without dragging
                    _setUIObjValFromClickAlone(_msClickObj);
                    //Check if modification from click has changed the value of the object
                    if(_msClickObj.getIsDirty()) {retVals[1] = true;}
                }                 
            }
            return _msClickObj != null;    
        }
        //_dispInfoMsg("handleMouseClick", "Mse xy : ["+mouseX+","+mouseY+"] not in UI ClckCoords");

        return false;
    }//handleMouseClick
    
    /**
     * Check inside all objects to see if passed mouse x,y is within hotspot
     * @param mouseX mouse x relative to upper left corner x of the ui click region
     * @param mouseY mouse y relative to upper left corner y of the ui click region
     * @return idx of object that mouse resides in, or -1 if none
     */
    private final int _checkInAllObjs(int mouseX, int mouseY) {
        for(var entry : _guiObjsIDXMap.entrySet()) {    if(entry.getValue().checkIn(mouseX, mouseY)){ return entry.getKey();}}
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
        } else {            _msOvrObj = -1;        }
        return _msOvrObj != -1;
    }//handleMouseMov
    
    /**
     * Handle mouse-driven modification to a UI object, by modAmt
     * @param modAmt the amount to modify the UI object
     * @return boolean array : 
     *             idx 0 is if an object has been modified
     *             idx 1 is if we should set setUIObjMod to true in caller 
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
                if(_msClickObj.shouldUpdateWin(false)){ updateOwnerWithUIVal(_msClickObj);}
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
     *             idx 0 is if an object has been modified
     *             idx 1 is if we should set setUIObjMod to true in caller 
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
     *             idx 0 is if an object has been modified
     *             idx 1 is if we should set setUIObjMod to true in caller 
     */
    public final boolean[] handleMouseDrag(int delX, int delY, int mseBtn, boolean shiftPressed) {
        //TODO USE UI COLLECTION OBJECT THAT IS CURRENTLY ACTIVE  
        // _dispErrMsg("handleMouseDrag","dragging");
        return _handleMouseModInternal(delX+(delY*-(shiftPressed ? 50.0f : 5.0f)));}    
    
    /**
     * Set UI value for object based on non-drag modification such as click - either at initial click or when click is released
     * @param j
     */
    private void _setUIObjValFromClickAlone(Base_GUIObj obj) {        obj.clickModVal(_msBtnClicked * -2.0f + 1, AppMgr.clickValModMult());    }
    
    /**
     * Handle UI functionality when mouse is released in owner
     * @param objModified whether object was clicked on but not changed - this will change cause the release to increment the object's value
     * @return whether or not _privFlagsToClear has buttons to clear.
     */
    public final boolean handleMouseRelease(boolean objModified) {
        if(_msClickObj != null) {
            // If no modification has occurred, set value from a click
            if(!objModified) {
                //_dispInfoMsg("handleMouseRelease", "Object : "+_msClickObj+" was clicked clicked but getUIObjMod was false");
                //means object was clicked in but not drag modified through drag or shift-clic - use this to modify by clicking
                _setUIObjValFromClickAlone(_msClickObj);
            }         
            //updateOwnerWithAllNewUIVals("mse release");
            // Will only have modified one object by mouse release
            if(_msClickObj.getIsDirty()) {            updateOwnerWithUIVal(_msClickObj);            }
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
     * move to beginning of UI region
     */
    public final void moveToUIRegion() {       ri.translate(_uiClkCoords[0], _uiClkCoords[1], 0);   }
    
    /**
     * Draw all gui objects
     * @param isDebug
     * @param animTimeMod
     */
    public final void drawGUIObjs(float animTimeMod, boolean isGlbDebug) {
        ri.pushMatState();
        moveToUIRegion();
        //draw UI Objs
        if(isGlbDebug) {
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
            if(vals[stIdx[0]].trim() != ""){    setValFromFileStr(vals[stIdx[0]]);    }
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
        for(var entry : _guiObjsIDXMap.entrySet()) {    res.add(entry.getValue().getStrFromUIObj());}        
        //bound for custom components
        res.add(winName + "_custUIComps");
        //add blank space
        res.add("");
        return res;
    }//
    
}//class uiObjectManager
