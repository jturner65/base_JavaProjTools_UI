package base_UI_Objects.windowUI.uiObjs.renderer.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * This construct will manage various configuration settings for an object's renderer
 */
public class GUIObjRenderer_Flags extends Base_BoolFlags {

	public static final int
		isMultiLineIDX           = _numBaseFlags,			    // Should be multiline
		isOneObjPerLineIDX       = _numBaseFlags + 1,			// One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
		forceStartNewLineIDX     = _numBaseFlags + 2,			// Force this object to be on a new row/line
		centerTextIDX            = _numBaseFlags + 3,			// Text should be centered 
		hasOutlineIDX            = _numBaseFlags + 4,			// An outline around the object should be rendered
		hasOrnamentIDX           = _numBaseFlags + 5,			// Should have ornament
		ornmntClrMatchIDX        = _numBaseFlags + 6;			// Ornament color should match label color
	private static final int numConfigFlags = _numBaseFlags + 7;	
	
	public GUIObjRenderer_Flags() {	super(numConfigFlags);}
	
	public GUIObjRenderer_Flags(boolean[] vals) {
		super(numConfigFlags);
		// Initialize values from an array
		for(int i=0;i<vals.length;++i) {
			setFlag(i+1, vals[i]);
		}
		
	}

	public GUIObjRenderer_Flags(GUIObjRenderer_Flags _otr) {
		super(_otr);
	}
	
	@Override
	protected void handleSettingDebug(boolean val) {	}

	@Override
	protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
		switch (idx) {//special actions for each flag
		case isMultiLineIDX			:{break;}
		case isOneObjPerLineIDX		:{break;}
		case forceStartNewLineIDX	:{break;}
		case centerTextIDX			:{break;}
		case hasOutlineIDX			:{break;}
		case hasOrnamentIDX			:{break;}
		case ornmntClrMatchIDX 		:{break;}
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
	 * @param singlePerLine
	 */
	public void setIsOneObjPerLine(boolean singlePerLine) {setFlag(isOneObjPerLineIDX, singlePerLine);}
	/**
	 * Get whether or not this object should be alone on a row in the UI space
	 * (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * @return
	 */
	public boolean getIsOneObjPerLine() {return getFlag(isOneObjPerLineIDX);}
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
