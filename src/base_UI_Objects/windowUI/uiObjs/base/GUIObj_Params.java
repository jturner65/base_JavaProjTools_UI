package base_UI_Objects.windowUI.uiObjs.base;


import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.uiObjs.renderer.base.GUIObjRenderer_Flags;

/**
 * This class holds the parameters used to describe/construct a gui object. Some of the values not be defined for certain gui types.
 */
public class GUIObj_Params {
    
    /**
     * The name for the object
     */
    private String name;
    /**
     * The label for the UI object to display, if any
     */
    public final String label;
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
    private final GUIObjConfig_Flags uiObjCfgFlags;
    
    /**
     * Array of strings corresponding to the list of values/states this object will manage (if listBox or button)
     */
    private String[] listVals = new String[0];
    
    /**
     * The configuration flags that govern how the renderer will be constructed
     */
    private final GUIObjRenderer_Flags rendererCfgFlags;
    
    /**
     * Boolean array of button type format values 
     *          idx 0: Whether this button should stay enabled until next draw frame                                                
     *          idx 1: Whether this button waits for some external process to complete before returning to _offStateIDX State 
     */
    public final boolean[] buttonFlags;
    
    /**
     * The flag index this object corresponds to, if it is a button
     */
    public final int boolFlagIDX;
    
    /**
     * Array of stroke, fill and possibly text colors to be used to render the object. If only 2 elements, text is idx 0 (stroke).
     */
    private int[][] renderColors = new int[][]{
        {0,0,0,255},             // stroke (outline of box/default text)
        {255,255,255,255},         // fill
        {0,0,0,255},             // text
    };
    
    /**
     * The colors corresponding to the desired fill color for the various button states/labels, or null if not a button
     */
    private int[][] btnFillColors = null;
    
    /**
     * Whether or not this GUIObj_Params describes a collection of gui objects and not just a single one. If it is a collection, it's various parameters are ignored.
     */
    protected boolean _isAGroupOfObjs = false;
    
    
    /**
     * Build an info struct that describes a UI object
     * @param _label
     * @param _objType
     * @param _boolFlagIDX
     * @param _configFlags configuration/behavior values
     * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color 
     * @param _buttonFlags
     */
    public GUIObj_Params(
            int _objIdx, 
            String _label, 
            GUIObj_Type _objType, 
            int _boolFlagIDX, 
            boolean[] _configFlags, 
            GUIObjRenderer_Flags _rendererCfgFlags, 
            boolean[] _buttonFlags) {
        name = "unnamed";
        label = _label;
        objIdx = _objIdx;
        objType = _objType;
        boolFlagIDX = _boolFlagIDX;
        uiObjCfgFlags = new GUIObjConfig_Flags(_configFlags);
        rendererCfgFlags = new GUIObjRenderer_Flags(_rendererCfgFlags);
        buttonFlags = new boolean[_buttonFlags.length];
        System.arraycopy(_buttonFlags, 0, buttonFlags, 0, _buttonFlags.length);
    }
    /**
     * Build an info struct that describes a UI object
     * @param _label
     * @param _objType
     * @param _boolFlagIDX
     * @param _configFlags configuration/behavior values
     * @param _rendererCfgFlags structure that holds various renderer configuration data (i.e. : 
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color 
     * NOTE : passes empty array to button creation flags
     */
    public GUIObj_Params(int _objIdx, String _label, GUIObj_Type _objType, boolean[] _configFlags,    GUIObjRenderer_Flags _rendererCfgFlags) {
        this(_objIdx, _label, _objType, -1, _configFlags, _rendererCfgFlags, new boolean[0]);
    }
    
    /**
     * Copy ctor
     * @param otr
     */
    public GUIObj_Params(GUIObj_Params otr) {
        name=otr.name;
        label=otr.label;
        objIdx = otr.objIdx;
        objType=otr.objType;
        boolFlagIDX=otr.boolFlagIDX;
        System.arraycopy(otr.minMaxMod, 0, minMaxMod, 0, otr.minMaxMod.length);        
        rendererCfgFlags = new GUIObjRenderer_Flags(otr.rendererCfgFlags);
        uiObjCfgFlags = new GUIObjConfig_Flags(otr.uiObjCfgFlags);
        buttonFlags = new boolean[otr.buttonFlags.length];
        System.arraycopy(otr.buttonFlags, 0, buttonFlags, 0, otr.buttonFlags.length);
        listVals = new String[otr.listVals.length];
        System.arraycopy(otr.listVals, 0, listVals, 0, otr.listVals.length);
        setStrkFillTextColors(otr.renderColors);
        setBtnFillColors(otr.btnFillColors);
        _isAGroupOfObjs = otr._isAGroupOfObjs;
    }//copy ctor
    
    /**
     * Set whether the text in this object should be centered or not
     * @param isCentered
     */
    public final void setIsTextCentered(boolean isCentered) {rendererCfgFlags.setIsCentered(isCentered);}
    
    /**
     * Set that this GUIObj_Params describes an object that should be forced to be the first entry on a new row
     */
    public final void setIsFirstObjOnRow() {rendererCfgFlags.setForceStartNewLine(true);}
    
    /**
     * Whether or not this represents an object should be rendered as multi-line
     * @return
     */
    public final boolean isMultiLine() {return rendererCfgFlags.getIsMultiLine();}
    /**
     * Get the string labels the list/button object this construct describes use for data or state
     * @return
     */
    public final String[] getListVals() {return listVals;}
    
    /**
     * Return a copy of the renderer creation flags object
     * @return
     */
    public GUIObjRenderer_Flags getRenderCreationFormatFlags() {return new GUIObjRenderer_Flags(rendererCfgFlags);}
    
    /**
     * Return a copy of the UI Object Configuration flags
     * @return
     */
    public GUIObjConfig_Flags getUIObjConfigFlags() {return new GUIObjConfig_Flags(uiObjCfgFlags);}
    
    public void setMinMaxMod(double[] _minMaxMod) {for(int i=0;i<_minMaxMod.length;++i) {minMaxMod[i] = _minMaxMod[i];}}
    
    /**
     * Get the name of the GUI object this params will build
     * @param _label
     */    
    public final String getName() {return name;}
    
    /**
     * Set the name of the GUI object this params will build
     * @param _label
     */
    public final void setName(String _name) {name = _name;}
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
                    "GUIObj_Params ("+name+":`"+label+"`)", "setbtnFillColors", 
                    "Setting button colors failed for object "+name+" due to not having the same number of colors (" +_colors.length +") as button states (" +listVals.length +").");
            return;
        }
        btnFillColors = new int[_colors.length][4];
        for(int i=0;i<_colors.length;++i) {    System.arraycopy(_colors[i], 0, btnFillColors[i], 0, _colors[i].length);}        
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
        for(int i=0;i<_colors.length;++i) {            System.arraycopy(_colors[i], 0, renderColors[i], 0, _colors[i].length);    }    
    }
    
    /**
     * Get the colors to use for stroke, fill and optionally text for this object. If text is not specified, will use fill color
     * @return
     */
    public final int[][] getStrkFillTextColors(){return renderColors;}
    
    /**
     * Whether or not this object is a button or switch
     * @return
     */
    public final boolean isButton() {return (buttonFlags.length!=0);}
    
    /**
     * Whether or not this GUIObj_Params describes a collection of GUIObj_Params. If so, all this constructs values are ignored.
     * @return
     */
    public final boolean isAGroupOfObjs() {return _isAGroupOfObjs;}


}// class GUIObj_Params

