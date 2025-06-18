package base_UI_Objects.windowUI.uiObjs.collection;

import java.util.Map;
import java.util.TreeMap;

//import java.util.ArrayList;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.UIObjectManager;
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

/**
 * Implementations of this class will own 1 or more UI objects to be displayed
 * together as a group/collection
 */
public class GUIObj_Collection {
	/**
	 * Render interface
	 */
	public static IRenderInterface ri;
	/**
	 * subregion of window where UI objects this collection manages may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	protected float[] _uiClkCoords;
	/**
	 * Map for all multi-state button objects not backed by a flags construct, keyed by objIdx
	 */
	private TreeMap<Integer,MenuGUIObj_Button> _guiButtonIDXMap;
	
	/**
	 * Map of all 2-state switch toggle objects connected to privFlags structures, keyed by ***privFlags key*** (not objIdx)
	 */
	private TreeMap<Integer,MenuGUIObj_Switch> _guiSwitchIDXMap;
	
	/**
	 * Map of all idxs for float-based UI objects, keyed by objIdx
	 */
	private TreeMap<Integer, MenuGUIObj_Float> _guiFloatValIDXMap;
	
	/**
	 * Map of all idxs for integer/list-based objects, keyed by objIdx
	 * (This does not include buttons even though button inherits from list object)
	 */
	private TreeMap<Integer,MenuGUIObj_Int>  _guiIntValIDXMap;
	/**
	 * array list of idxs for label/read-only objects, keyed by objIdx
	 */	
	private TreeMap<Integer, MenuGUIObj_DispValue> _guiLabelValIDXMap;
	
	/**
	 * UI objects in this collection
	 */
	private Base_GUIObj[] _guiObjsAra;
	
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
	
	/**
	 * Owning UI Object manager
	 */
	private UIObjectManager uiObjMgr;
	
	// Class name to use for any debugging messages
	private final String _dispMsgClassName;

	public GUIObj_Collection(UIObjectManager _uiObjMgr, float[] __uiClkCoords) {
		uiObjMgr = _uiObjMgr;
		ri=UIObjectManager.ri;
		System.arraycopy(__uiClkCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);
		_dispMsgClassName = this.getClass().getSimpleName();
		_msClickObj = null;
		initObjAras();
		
	}
	
	public final void initObjAras() {
		_guiObjsAra = new Base_GUIObj[0];
		//initialize arrays to hold idxs of int and float items being created.
		_guiButtonIDXMap = new TreeMap<Integer,MenuGUIObj_Button>();
		_guiSwitchIDXMap = new TreeMap<Integer,MenuGUIObj_Switch>();
		_guiFloatValIDXMap = new TreeMap<Integer, MenuGUIObj_Float>();
		_guiIntValIDXMap = new TreeMap<Integer,MenuGUIObj_Int> ();
		_guiLabelValIDXMap = new TreeMap<Integer, MenuGUIObj_DispValue>();	
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
	protected Base_GUIObjRenderer _buildObjRenderer(
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
	}//_buildObjRenderer
	
	/**
	 * Build the appropriate object based on the passed GUIObj_Params entry and assign it the given guiObjIDX
	 * @param guiObjIDX
	 * @param entry
	 * @param uiClkRect
	 */
	protected final void _buildObj(int guiObjIDX, Map.Entry<String, GUIObj_Params> entry, float[] uiClkRect) {
		GUIObj_Params argObj = entry.getValue();
		//Stroke and fill colors for renderer
		int[][] guiColors = new int[][] {
			{0,0,0,255}, //stroke
			{0,0,0,255}, // fill
		};			
		switch(argObj.objType) {
			case IntVal : {
				_guiObjsAra[guiObjIDX] = new MenuGUIObj_Int(guiObjIDX, argObj);
				_guiIntValIDXMap.put(guiObjIDX, ((MenuGUIObj_Int)_guiObjsAra[guiObjIDX]));
				break;}
			case ListVal : {
				_guiObjsAra[guiObjIDX] = new MenuGUIObj_List(guiObjIDX, argObj);
				_guiIntValIDXMap.put(guiObjIDX, ((MenuGUIObj_List)_guiObjsAra[guiObjIDX]));
				break;}
			case FloatVal : {
				_guiObjsAra[guiObjIDX] = new MenuGUIObj_Float(guiObjIDX, argObj);
				_guiFloatValIDXMap.put(guiObjIDX, ((MenuGUIObj_Float)_guiObjsAra[guiObjIDX]));
				break;}
			case LabelVal :{
				_guiObjsAra[guiObjIDX] = new MenuGUIObj_DispValue(guiObjIDX, argObj);					
				_guiLabelValIDXMap.put(guiObjIDX,((MenuGUIObj_DispValue)_guiObjsAra[guiObjIDX]));
				break;}
			case Switch : {						
				MenuGUIObj_Switch obj = new MenuGUIObj_Switch(guiObjIDX, argObj);
				_guiButtonIDXMap.put(guiObjIDX, obj);
				_guiSwitchIDXMap.put(obj.getBoolFlagIDX(), obj);
				_guiObjsAra[guiObjIDX] = obj;
				break;}
			case Button  :{ 
				_guiObjsAra[guiObjIDX] = new MenuGUIObj_Button(guiObjIDX, argObj);
				_guiButtonIDXMap.put(guiObjIDX, ((MenuGUIObj_Button)_guiObjsAra[guiObjIDX]));
				break;
			}
			default : {
				uiObjMgr._dispErrMsg("_buildObj ("+_dispMsgClassName+")", "Attempting to instantiate unknown UI object for a " + argObj.objType.toStrBrf());
				break;				
			}				
		}//switch
		Base_GUIObjRenderer renderer = _buildObjRenderer(_guiObjsAra[guiObjIDX], UIObjectManager.AppMgr.getUIOffset(), uiClkRect[2], guiColors, argObj.getCreationFormatVal(), argObj.getBtnColors());
		_guiObjsAra[guiObjIDX].setRenderer(renderer);		
	}//_buildObj
	
	
	public final void _buildInitUIUpdateObjVals(TreeMap<Integer, Integer> intValues, TreeMap<Integer, Float> floatValues, TreeMap<Integer, Boolean> boolValues) {
		//integer and list values
		for (var entry : _guiIntValIDXMap.entrySet()) {			intValues.put(entry.getKey(), entry.getValue().getValueAsInt());}		
	}
	
	////////////////////////////////////////
	/// Draw functions
	
	/**
	 * Draw the UI clickable region rectangle, for debug
	 */
	private final void _drawUIRect() {
		ri.setStrokeWt(2.0f);
		ri.setNoFill();
		ri.setColorValStroke(IRenderInterface.gui_DarkCyan, 255);
		ri.drawRect(_uiClkCoords[0], _uiClkCoords[1], _uiClkCoords[2]-_uiClkCoords[0], _uiClkCoords[3]-_uiClkCoords[1]);
	}
	
	
	protected void drawDbgGUIObjsInternal(float animTimeMod) {
		ri.pushMatState();	
			for(int i =0; i<_guiObjsAra.length; ++i){_guiObjsAra[i].drawDebug();}
		ri.popMatState();			
	}

	protected void drawGUIObjsInternal(int msClkObj, float animTimeMod) {
		ri.pushMatState();	
			//mouse highlight
			if (msClkObj != -1) {	_guiObjsAra[msClkObj].drawHighlight();	}
			for(int i =0; i<_guiObjsAra.length; ++i){_guiObjsAra[i].draw();}
		ri.popMatState();	
	}
	
	/**
	 * Draw all gui objects, with appropriate highlights for debug and if object is being edited or not
	 * @param isDebug
	 * @param animTimeMod
	 */
	public void drawGUIObjs(boolean isDebug, int msClkObj, float animTimeMod) {
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
	 * @param mouseX
	 * @param mouseY
	 * @return idx of object that mouse resides in, or -1 if none
	 */
	private final int _checkInAllObjs(int mouseX, int mouseY) {
		for(int j=0; j<_guiObjsAra.length; ++j){if(_guiObjsAra[j].checkIn(mouseX, mouseY)){ return j;}}
		return -1;
	}
	
	/**
	 * Set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	 * @param j
	 */
	private void _setUIObjValFromClickAlone(Base_GUIObj obj) {		obj.clickModVal(_msBtnClicked * -2.0f + 1, UIObjectManager.AppMgr.clickValModMult());	}	
	
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
			int idx = _checkInAllObjs(mouseX, mouseY);
			if(idx >= 0) {
				//found in list of UI objects
				_msBtnClicked = mseBtn; 
				_msClickObj = _guiObjsAra[idx];
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
			_msClickObj.clearFocus();
			_msClickObj = null;	
		}
		_msBtnClicked = -1;
	}//handleMouseRelease
	
	////////////////////////////////////////
	/// End Mouse interaction
	
	
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
			case ListVal : {		uiObjMgr.setUI_ListVal(UIobj, UIidx);		break;}
			case FloatVal : {		uiObjMgr.setUI_FloatVal(UIobj, UIidx);		break;}
			case LabelVal : {		uiObjMgr.setUI_LabelVal(UIobj, UIidx);		break;}
			case Button : {			uiObjMgr.setUI_BtnVal(UIobj, UIidx);		break;}
			case Switch : {			uiObjMgr.setUI_SwitchVal(UIobj, UIidx);		break;}
			default : {
				uiObjMgr._dispWarnMsg("setUIWinVals ("+_dispMsgClassName+")", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());
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
	 * set all window values for UI objects
	 */
	public final void setAllUIWinVals() {for(int i=0;i<_guiObjsAra.length;++i){if(_guiObjsAra[i].shouldUpdateWin(true)){setUIWinVals(i);}}}
		
	/**
	 * Retrieve the hotspot for this collection
	 * @return
	 */
	public final float[] getUIClkCoords() {		return _uiClkCoords;	}
	
	/**
	 * Set the hotspot for this collection
	 * @param __uiClkCoords
	 */
	public final void setUIClkCoords(float[] __uiClkCoords) {		
		System.arraycopy(__uiClkCoords, 0, _uiClkCoords, 0, _uiClkCoords.length);	
	}	
	
	/**
	 * The class name for this collection
	 * @return
	 */
	public final String getClassName() {return _dispMsgClassName;}
	
	/**
	 * Get number of objects created for this collection
	 * @return
	 */
	public final int getNumObjs() {return _guiObjsAra.length;} 
	/**
	 * Set labels of GUI Switch objects for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new true label
	 * @param fLbl new false label
	 */
	public void setGUISwitchLabels(int idx, String tLbl, String fLbl) {
		_guiSwitchIDXMap.get(idx).setBooleanLabelVals(new String[] {fLbl, tLbl}, false);
	}
		
	
}//class GUIObj_Collection
