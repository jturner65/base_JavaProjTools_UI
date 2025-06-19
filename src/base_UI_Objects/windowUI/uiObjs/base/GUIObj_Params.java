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
	 * Array of strings corresponding to the list of values/states this object will manage (if listBox or button)
	 */
	private String[] listVals = new String[0];
	
	/**
	 * Format values to use to create and configure renderer
	 * 		idx 0 : Should be multiline
	 * 		idx 1 : Text should be centered (default is false)
	 * 		idx 2 : Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 		idx 3 : Should have ornament
	 * 		idx 4 : Ornament color should match label color 
	 */
	private final boolean[] renderCreationFrmtVals;
	
	/**
	 * Button type flags, for UI Buttons
	 */
	public final boolean[] buttonFlags;
	
	/**
	 * The flag index this object corresponds to, if it is a button
	 */
	public final int boolFlagIDX;
	
	/**
	 * renderColors is an array of stroke, fill and possibly text colors to be used to render the object. If only 2 elements, text is idx 1.
	 */
	private int[][] renderColors = new int[][]{
		{0,0,0,255}, // stroke (outline of box
		{0,0,0,255}, // fill/default text
		{0,0,0,255}, // text
	};
	
	/**
	 * The colors corresponding to the desired fill color for the various button states/labels, or null if not a button
	 */
	private int[][] btnFillColors = null;
	
	/**
	 * Build an info struct that describes a UI object
	 * @param _name
	 * @param _objType
	 * @param _boolFlagIDX
	 * @param _configFlags configuration/behavior values
	 * @param _renderCreationFrmtVals format values to describe and pass to renderer
	 * 		idx 0 : Should be multiline
	 * 		idx 1 : Should have ornament
	 * 		idx 2 : Ornament color should match label color 
	 * 		idx 3 : Text should be centered (default is false)
	 * @param _buttonFlags
	 */
	public GUIObj_Params(
			int _objIdx, 
			String _name, 
			GUIObj_Type _objType, 
			int _boolFlagIDX, 
			boolean[] _configFlags, 
			boolean[] _renderCreationFrmtVals, 
			boolean[] _buttonFlags) {
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
	/**
	 * Build an info struct that describes a UI object
	 * @param _name
	 * @param _objType
	 * @param _boolFlagIDX
	 * @param _configFlags configuration/behavior values
	 * @param _renderCreationFrmtVals format values to describe and pass to renderer
	 * 		idx 0 : Should be multiline
	 * 		idx 1 : Should have ornament
	 * 		idx 2 : Ornament color should match label color 
	 * 		idx 3 : Text should be centered (default is false)
	 * NOTE : passes empty array to button creation flags
	 */
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
		setStrkFillTextColors(otr.renderColors);
		setBtnFillColors(otr.btnFillColors);
	}
	
	
	/**
	 * Get the string labels the list/button object this construct describes use for data or state
	 * @return
	 */
	public final String[] getListVals() {return listVals;}
	
	public boolean[] getRenderCreationFormatVal() {return renderCreationFrmtVals;}
	
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
	
	/**
 	 * Set the fill colors used for each element in a button/switch list
	 * @param _colors
	 */
	public final void setBtnFillColors(int[][] _colors) {
		if(_colors.length != listVals.length) {
			Base_DispWindow.AppMgr.msgObj.dispErrorMessage(
					"GUIObj_Params ("+name+")", "setbtnFillColors", 
					"Setting button colors failed for object "+name+" due to not having the same number of colors (" +_colors.length +") as button states (" +listVals.length +").");
			return;
		}
		btnFillColors = new int[_colors.length][4];
		for(int i=0;i<_colors.length;++i) {	System.arraycopy(_colors[i], 0, btnFillColors[i], 0, _colors[i].length);}		
	}
	/**
	 * Get the fill colors used for each element in a button/switch list
	 * @return
	 */
	public final int[][] getBtnFillColors(){ return btnFillColors;}
	
	/**
	 * Set the colors to use for stroke, fill and optionally text for this object. If text is not specified, will use fill color
	 * @param _colors
	 */
	public final void setStrkFillTextColors(int[][] _colors) {
		renderColors = new int[_colors.length][];
		for(int i=0;i<_colors.length;++i) {			System.arraycopy(_colors[i], 0, renderColors[i], 0, _colors[i].length);	}	
	}
	
	/**
	 * Get the colors to use for stroke, fill and optionally text for this object. If text is not specified, will use fill color
	 * @return
	 */
	public final int[][] getStrkFillTextColors(){return renderColors;}

}// class GUIObj_Params
