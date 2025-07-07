package base_UI_Objects.windowUI.uiObjs.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

public class GUIObjConfig_Flags extends Base_BoolFlags {
    public static final int
        usedByWinsIDX               = _numBaseFlags,                // value is sent to window
        updateWhileModIDX           = _numBaseFlags + 1,            // value is sent to window on any change, not just release
        explicitUIDataUpdateIDX     = _numBaseFlags + 2,            // if true does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
        objectIsReadOnlyIDX         = _numBaseFlags + 3,            // ui object is not user-modifiable, just read only
        isValueRangeIDX             = _numBaseFlags + 4;            // whether object is value-range instead of just a single value (uses min and max instead of val) 
    public static final int numConfigFlags = _numBaseFlags + 5;    
       
    /**
     * Boolean array of default behavior boolean values, if formatting is not otherwise specified
     *         idx 0: value is sent to owning window,  
     *         idx 1: value is sent on any modifications (while being modified, not just on release), 
     *         idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         idx 3: object is read only
     *         idx 4: object should display a value range and not a value
     */
    private static final GUIObjConfig_Flags dfltUIBehaviorVals = new GUIObjConfig_Flags();//new boolean[]{true,false,false,false};
    static {
        dfltUIBehaviorVals.setIsUsedByWindow(true);
        dfltUIBehaviorVals.setIsReadOnly(false);
        dfltUIBehaviorVals.setIsValueRange(false);
    }
    /**
     * Boolean array of default behavior boolean values for label/read-only constructs
     *         idx 0: value is sent to owning window,  
     *         idx 1: value is sent on any modifications (while being modified, not just on release), 
     *         idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         idx 3: object is read only
     *         idx 4: object should display a value range and not a value
     */    
    private static final GUIObjConfig_Flags dfltUIReadOnlyBehaviorVals = new GUIObjConfig_Flags();//new boolean[] {false,false,false,true};
    static {
        dfltUIReadOnlyBehaviorVals.setIsUsedByWindow(false);
        dfltUIReadOnlyBehaviorVals.setIsReadOnly(true);
        dfltUIReadOnlyBehaviorVals.setIsValueRange(false);
    }
    /**
     * Boolean array of default behavior boolean values for label/read-only constructs displaying a range
     *         idx 0: value is sent to owning window,  
     *         idx 1: value is sent on any modifications (while being modified, not just on release), 
     *         idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *         idx 3: object is read only
     *         idx 4: object should display a value range and not a value
     */    
    private static final GUIObjConfig_Flags dfltUIReadOnlyRangeBehaviorVals = new GUIObjConfig_Flags();//new boolean[] {false,false,false,true};
    static {
        dfltUIReadOnlyRangeBehaviorVals.setIsUsedByWindow(false);
        dfltUIReadOnlyRangeBehaviorVals.setIsReadOnly(true);
        dfltUIReadOnlyRangeBehaviorVals.setIsValueRange(true);
    }    
        
    public GUIObjConfig_Flags() {  super(numConfigFlags);}
    
    /**
     * Builds GUIObjConfig_Flags object with common default values set. 
     * TODO : Support for non-read-only range object (i.e. to be used in sampling perhaps) 
     * needs to be added. Until then, specifying isRanged forces isReadOnly to be true.
     * @param isReadOnly whether the object is read only or supports user interaction
     * @param isRangeObject whether the object is a range (min/max) object or a direct value object
     */
    public GUIObjConfig_Flags(boolean isReadOnly, boolean isRangeObject) {
        super(numConfigFlags);        
        setIsUsedByWindow(!isReadOnly && !isRangeObject);
        setIsReadOnly(isReadOnly || isRangeObject);
        setIsValueRange(isRangeObject);    
    }
    
    public GUIObjConfig_Flags(boolean[] vals) {
        super(numConfigFlags);
        // Initialize values from an array
        for(int i=0;i<vals.length;++i) {
            setFlag(i+_numBaseFlags, vals[i]);
        }        
    }
    
    public GUIObjConfig_Flags(GUIObjConfig_Flags _otr) {super(_otr);}
    
    @Override
    protected void handleSettingDebug(boolean val) {    }
       
    
    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
        switch (idx) {//special actions for each flag
        case usedByWinsIDX            :{break;}
        case updateWhileModIDX        :{break;}
        case explicitUIDataUpdateIDX  :{break;}    
        case objectIsReadOnlyIDX      :{break;}
        case isValueRangeIDX          :{break;}
        }
    }//handleFlagSet_Indiv
   
    /**
     * Set whether this value is actually used by the owning window
     * @return
     */
    public void setIsUsedByWindow(boolean val) {setFlag(usedByWinsIDX, val);}
    /**
     * Get whether this value is actually used by the owning window
     * @return
     */
    public boolean isUsedByWindow() {return getFlag(usedByWinsIDX);}
    /**
     * Set whether this value is sent to window on any change, not just release
     * @return
     */
    public void setUpdateWindowWhileMod(boolean val) {setFlag(updateWhileModIDX, val);}
    /**
     * Get whether this value is sent to window on any change, not just release
     * @return
     */
    public boolean isUpdateWindowWhileMod() {return getFlag(updateWhileModIDX);}
    /**
     * Set whether this value, if true, does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
     * @return
     */
    public void setDontUpdateOwner(boolean val) {setFlag(explicitUIDataUpdateIDX, val);}
    /**
     * Get whether this value, if true, does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
     * @return
     */
    public boolean getDontUpdateOwner() {return getFlag(explicitUIDataUpdateIDX);}
    /**
     * Set whether this object is read only
     * @return
     */
    public void setIsReadOnly(boolean val) {setFlag(objectIsReadOnlyIDX, val);}
    /**
     * Get whether this object is read only
     * @return
     */
    public boolean isReadOnly() {return getFlag(objectIsReadOnlyIDX);}
    
    /**
     * Set whether this object is value-range instead of just a single value (uses min and max instead of val)
     * @return
     */
    public void setIsValueRange(boolean val) {setFlag(isValueRangeIDX, val);}
    /**
     * Get whether this object is value-range instead of just a single value (uses min and max instead of val)
     * @return
     */
    public boolean isValueRange() {return getFlag(isValueRangeIDX);}
    
    
}//GUIObjConfig_Flags
