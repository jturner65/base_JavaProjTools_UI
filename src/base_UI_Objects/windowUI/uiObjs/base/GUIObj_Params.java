package base_UI_Objects.windowUI.uiObjs.base;

import base_UI_Objects.windowUI.base.Base_DispWindow;

/**
 * This class holds the parameters used to describe/construct a gui object. Some of the values not be defined for certain gui types.
 */
public class GUIObj_Params {
	
	/**
	 * The name for the object
	 */
	public final String name;
	/**
	 * The unique object index
	 */
	public final int objIdx;
	/**
	 * Type of the gui object to build
	 */
	public final GUIObj_Type objType;
	
	/**
	 * The initial value this object should have
	 */
	public double initVal = 0;
	
	/**
	 * Min and max values for this object, and the modifier per change to use
	 */
	public double[] minMaxMod = new double[3];

	/**
	 * Configuration flags for the UI object
	 */
	public final boolean[] configFlags;
	
	/**
	 * Button type flags, for UI Buttons
	 */
	public final boolean[] buttonFlags;
	
	/**
	 * Array of strings corresponding to the list of values/states this object will manage (if listBox or button)
	 */
	private String[] listVals = new String[0];
	
	/**
	 * Format values to use to create renderer
	 */
	private final boolean[] renderCreationFrmtVals;
	
	/**
	 * The flag index this object corresponds to, if it is a button
	 */
	public final int boolFlagIDX;
	
	/**
	 * The colors corresponding to the various button states
	 */
	private int[][] btnColors = null;
	
	/**
	 * Build an info struct that describes a UI object
	 * @param _name
	 * @param _objType
	 * @param _boolFlagIDX
	 * @param _configFlags configuration/behavior values
	 * @param _renderCreationFrmtVals format values to describe and pass to renderer
	 * @param _buttonFlags
	 */
	public GUIObj_Params(int _objIdx, String _name, GUIObj_Type _objType, int _boolFlagIDX, boolean[] _configFlags, boolean[] _renderCreationFrmtVals, boolean[] _buttonFlags) {
		name = _name;
		objIdx = _objIdx;
		objType = _objType;
		boolFlagIDX = _boolFlagIDX;
		renderCreationFrmtVals = new boolean[_renderCreationFrmtVals.length];
		System.arraycopy(_renderCreationFrmtVals, 0, renderCreationFrmtVals, 0, _renderCreationFrmtVals.length);
		configFlags = new boolean[_configFlags.length];
		System.arraycopy(_configFlags, 0, configFlags, 0, _configFlags.length);
		buttonFlags = new boolean[_buttonFlags.length];
		System.arraycopy(_buttonFlags, 0, buttonFlags, 0, _buttonFlags.length);
	}

	public GUIObj_Params(int _objIdx, String _name, GUIObj_Type _objType, boolean[] _configFlags, boolean[] _renderCreationFrmtVals) {
		this(_objIdx, _name, _objType, -1, _configFlags, _renderCreationFrmtVals, new boolean[0]);
	}
	
	/**
	 * Copy ctor
	 * @param otr
	 */
	public GUIObj_Params(GUIObj_Params otr) {
		name=otr.name;
		objIdx = otr.objIdx;
		objType=otr.objType;
		boolFlagIDX=otr.boolFlagIDX;
		System.arraycopy(otr.minMaxMod, 0, minMaxMod, 0, otr.minMaxMod.length);		
		renderCreationFrmtVals = new boolean[otr.renderCreationFrmtVals.length];
		System.arraycopy(otr.renderCreationFrmtVals, 0, renderCreationFrmtVals, 0, otr.renderCreationFrmtVals.length);
		configFlags = new boolean[otr.configFlags.length];
		System.arraycopy(otr.configFlags, 0, configFlags, 0, otr.configFlags.length);
		buttonFlags = new boolean[otr.buttonFlags.length];
		System.arraycopy(otr.buttonFlags, 0, buttonFlags, 0, otr.buttonFlags.length);
		listVals = new String[otr.listVals.length];
		System.arraycopy(otr.listVals, 0, listVals, 0, otr.listVals.length);
		setBtnColors(otr.btnColors);
	}
	
	
	/**
	 * Get the string labels the list/button object this construct describes use for data or state
	 * @return
	 */
	public final String[] getListVals() {return listVals;}
	
	public boolean[] getCreationFormatVal() {return renderCreationFrmtVals;}
	
	public void setMinMaxMod(double[] _minMaxMod) {for(int i=0;i<_minMaxMod.length;++i) {minMaxMod[i] = _minMaxMod[i];}}
	
	/**
	 * Set the string labels the list/button object this construct describes use for data or state.
	 * This also sets the min/max/mod to be appropriate for a list-backed construct
	 * @param _listVals
	 */
	public final void setListVals(String[] _listVals) {
		listVals = new String[_listVals.length];
		System.arraycopy(_listVals, 0, listVals, 0, _listVals.length);
		minMaxMod = new double[] {0, _listVals.length-1, 1};		
	}//setListVals
	
	public final void setBtnColors(int[][] _colors) {
		if(_colors.length != listVals.length) {
			Base_DispWindow.AppMgr.msgObj.dispErrorMessage(
					"GUIObj_Params ("+name+")", "setBtnColors", 
					"Setting button colors failed for object "+name+" due to not having the same number of colors (" +_colors.length +") as button states (" +listVals.length +").");
			return;
		}
		btnColors = new int[_colors.length][4];
		for(int i=0;i<_colors.length;++i) {	System.arraycopy(_colors[i], 0, btnColors[i], 0, _colors[i].length);}		
	}
	
	public final int[][] getBtnColors(){ return btnColors;}
	


}// class GUIObj_Params
