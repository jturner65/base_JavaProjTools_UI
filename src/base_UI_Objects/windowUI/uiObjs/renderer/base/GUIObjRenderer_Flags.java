package base_UI_Objects.windowUI.uiObjs.renderer.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * This construct will manage various configuration settings for an object's renderer
 */
public class GUIObjRenderer_Flags extends Base_BoolFlags {

    public static final int
        isMultiLineIDX           = _numBaseFlags,                // Should be multiline
        isOneObjPerRowIDX       = _numBaseFlags + 1,             // One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
        forceStartNewLineIDX     = _numBaseFlags + 2,            // Force this object to be on a new row/line
        centerTextIDX            = _numBaseFlags + 3,            // Text should be centered 
        hasOutlineIDX            = _numBaseFlags + 4,            // Object should be rendered with outline (default for btns is true, for non-buttons is false)
        hasOrnamentIDX           = _numBaseFlags + 5,            // Should have ornament
        ornmntClrMatchIDX        = _numBaseFlags + 6;            // Ornament color should match label color - layout specific so defaults to false
    private static final int numConfigFlags = _numBaseFlags + 7;  
    
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise 
     * specified, for a single-line, non-switch/button object : 
     *             - Should be multiline
     *             - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *             - Force this object to be on a new row/line (For side-by-side layouts) - layout specific so defaults to false
     *             - Text should be centered (default is false)
     *             - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *             - Should have ornament
     *             - Ornament color should match label color - layout specific so defaults to false
     */
    private static final GUIObjRenderer_Flags dfltRndrCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltRndrCfgFmtVals.setIsMultiLine(false);
        dfltRndrCfgFmtVals.setIsOneObjPerRow(true);
        dfltRndrCfgFmtVals.setIsCentered(false);
        dfltRndrCfgFmtVals.setHasOutline(false);
        dfltRndrCfgFmtVals.setHasOrnament(true);
    }
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise 
     * specified, for a single-line, non-switch/button object that we wish to share a line/row with others
     *          - Should be multiline
     *          - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *          - Force this object to be on a new row/line (For side-by-side layouts) - layout specific so defaults to false
     *          - Text should be centered (default is false)
     *          - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *          - Should have ornament
     *          - Ornament color should match label color - layout specific so defaults to false
     */
    private static final GUIObjRenderer_Flags dfltRndrInLineCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltRndrInLineCfgFmtVals.setIsMultiLine(false);
        dfltRndrInLineCfgFmtVals.setIsOneObjPerRow(false);
        dfltRndrInLineCfgFmtVals.setIsCentered(false);
        dfltRndrInLineCfgFmtVals.setHasOutline(false);
        dfltRndrInLineCfgFmtVals.setHasOrnament(false);
    }
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise 
     * specified, for a multi-line non-switch/button object : 
     *             - Should be multiline
     *             - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *             - Force this object to be on a new row/line (For side-by-side layouts) - layout specific so defaults to false
     *             - Text should be centered (default is false)
     *             - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *             - Should have ornament
     *             - Ornament color should match label color - layout specific so defaults to false
     */
    private static final GUIObjRenderer_Flags dfltMultiLineRndrCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltMultiLineRndrCfgFmtVals.setIsMultiLine(true);
        dfltMultiLineRndrCfgFmtVals.setIsOneObjPerRow(false);
        dfltMultiLineRndrCfgFmtVals.setIsCentered(true);
        dfltMultiLineRndrCfgFmtVals.setHasOutline(true);
        dfltMultiLineRndrCfgFmtVals.setHasOrnament(false);
    }
    
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise 
     * specified, for a multi-line display-only/label object : 
     *             - Should be multiline
     *             - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *             - Force this object to be on a new row/line (For side-by-side layouts) - layout specific so defaults to false
     *             - Text should be centered (default is false)
     *             - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *             - Should have ornament
     *             - Ornament color should match label color - layout specific so defaults to false
     */
    private static final GUIObjRenderer_Flags dfltMultiLineReadOnlyRndrCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltMultiLineReadOnlyRndrCfgFmtVals.setIsMultiLine(true);
        dfltMultiLineReadOnlyRndrCfgFmtVals.setIsOneObjPerRow(false);
        dfltMultiLineReadOnlyRndrCfgFmtVals.setIsCentered(true);
        dfltMultiLineReadOnlyRndrCfgFmtVals.setHasOutline(false);
        dfltMultiLineReadOnlyRndrCfgFmtVals.setHasOrnament(false);
    }      
    
    /**
     * Configuration structure of default renderer format values, if renderer configuration is not otherwise 
     * specified, for a toggle switch/button backed by a flags structure
     *             - Should be multiline
     *             - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *             - Force this object to be on a new row/line (For side-by-side layouts) - layout specific so defaults to false
     *             - Text should be centered (default is false)
     *             - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *             - Should have ornament
     *             - Ornament color should match label color - layout specific so defaults to false
     */
    private static final GUIObjRenderer_Flags dfltBtnRndrCfgFmtVals = new GUIObjRenderer_Flags();
    static {
        dfltBtnRndrCfgFmtVals.setIsMultiLine(false);
        dfltBtnRndrCfgFmtVals.setIsOneObjPerRow(false);
        dfltBtnRndrCfgFmtVals.setIsCentered(false);
        dfltBtnRndrCfgFmtVals.setHasOutline(true);
        dfltBtnRndrCfgFmtVals.setHasOrnament(false);
    } 
    
    /**
     * Build an empty GUIObjRenderer_Flags
     */
    public GUIObjRenderer_Flags() {    super(numConfigFlags);}
    
    /**
     * Build a GUIObjRenderer_Flags construct preset with common default settings
     * @param isMultiLine whether or not the object is multi-line
     * @param isButton whether the object is a button/switch or a direct-input object
     * @param isReadOnly whether the object is readonly or modifiable
     */
    /**
     * Build a GUIObjRenderer_Flags construct preset with common default settings
     * @param _isMultiLine whether or not the object is multi-line 
     * @param _isOnePerRow
     * @param _isButton whether the object is a button/switch or a direct-input object
     * @param _isReadOnly
     */
    public GUIObjRenderer_Flags(boolean _isMultiLine, boolean _isOnePerRow, boolean _isButton, boolean _isReadOnly) {
        super(numConfigFlags);
        setIsMultiLine(_isMultiLine);
        // only multi-line objects should default to centered
        setIsCentered(_isMultiLine);
        // set whether we want the object to span an entire row in the UI or have multiple per row
        setIsOneObjPerRow(_isOnePerRow);
        // multi-line non-readonly and button objects should default to having a border
        setHasOutline((_isMultiLine && !_isReadOnly) || _isButton);
        // defaults to true for non-multi-line, non-button objects
        boolean isSingleLineObj = !_isMultiLine && !_isButton;
        // default single line objects are one obj per line and should have an ornament
        setHasOrnament(isSingleLineObj);
    }
    
    /**
     * Build a GUIObjRenderer_Flags with specified booleans set based on array
     * NOTE skips debug, so idx 0 in array corresponds to idx 1 in flags construct
     * @param vals
     */
    public GUIObjRenderer_Flags(boolean[] vals) {
        super(numConfigFlags);
        // Initialize values from an array
        for(int i=0;i<vals.length;++i) {
            setFlag(i+_numBaseFlags, vals[i]);
        }        
    }

    public GUIObjRenderer_Flags(GUIObjRenderer_Flags  _otr) {super(_otr);}
    
    @Override
    protected void handleSettingDebug(boolean val) {    }

    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
        switch (idx) {//special actions for each flag
    case isMultiLineIDX             :{break;}
        case isOneObjPerRowIDX     :{break;}
        case forceStartNewLineIDX   :{break;}
        case centerTextIDX          :{break;}
        case hasOutlineIDX          :{break;}
        case hasOrnamentIDX         :{break;}
        case ornmntClrMatchIDX      :{break;}
        }        
    }//handleFlagSet_Indiv    
    
    /**
     * Set whether or not this object should be multiline
     * @param isMultiLine
     */
    public void setIsMultiLine(boolean isMultiLine) {setFlag(isMultiLineIDX, isMultiLine);}
    /**
     * Get whether or not this object should be multiline
     */
    public boolean getIsMultiLine() {return getFlag(isMultiLineIDX);}
    /**
     * Set whether or not this object should be alone on a row in the UI space
     * (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     * NOTE : setting this to false explicitly will set hasOrnament to false as well. This can be
     * subsequently overridden if we want an ornament on a multi-obj-per-line
     * @param singlePerLine
     */
    public void setIsOneObjPerRow(boolean singlePerLine) {
        setFlag(isOneObjPerRowIDX, singlePerLine);
        if(!singlePerLine) {setFlag(hasOrnamentIDX, false);}
    }
    /**
     * Get whether or not this object should be alone on a row in the UI space
     * (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     * @return
     */
    public boolean getIsOneObjPerRow() {return getFlag(isOneObjPerRowIDX);}
    /**
     * Set whether or not this object should be forced to start a new row
     * @param startNewLine
     */
    public void setForceStartNewLine(boolean startNewLine) {setFlag(forceStartNewLineIDX, startNewLine);}
    /**
     * Get whether or not this object should be forced to start a new row
     * @return
     */
    public boolean getForceStartNewLine() {return getFlag(forceStartNewLineIDX);}
    /**
     * Set whether or not this object's text should be centered
     * @param isCentered
     */
    public void setIsCentered(boolean isCentered) {setFlag(centerTextIDX, isCentered);}
    /**
     * Get whether or not this object's text should be centered
     * @return
     */
    public boolean getIsCentered() {return getFlag(centerTextIDX);}
    /**
     * Set whether this object should be rendered with an outline around the hotspot
     * @param hasOutline
     */
    public void setHasOutline(boolean hasOutline) {setFlag(hasOutlineIDX, hasOutline);}
    /**
     * Get whether this object should be rendered with an outline around the hotspot
     * @return
     */
    public boolean getHasOutline() {return getFlag(hasOutlineIDX);}
    /**
     * Set whether or not this object should be rendered with a prefix ornament
     * @param hasOrnament
     */
    public void setHasOrnament(boolean hasOrnament) {setFlag(hasOrnamentIDX, hasOrnament);}
    /**
     * Get whether or not this object should be rendered with a prefix ornament
     * @return
     */
    public boolean getHasOrnament() {return getFlag(hasOrnamentIDX);}
    /**
     * Set whether or not the rendered object's prefix ornament should be the same color as the object's text.
     * This is ignored if no ornament is present
     * @param ornClr
     */
    public void setOrnmntClrMatch(boolean ornClr) {setFlag(ornmntClrMatchIDX, ornClr);}
    /**
     * Get whether or not the rendered object's prefix ornament should be the same color as the object's text.
     * This is ignored if no ornament is present
     * @return
     */
    public boolean getOrnmntClrMatch() {return getFlag(ornmntClrMatchIDX);}  
    
}//GUIObjRenderer_Flags
