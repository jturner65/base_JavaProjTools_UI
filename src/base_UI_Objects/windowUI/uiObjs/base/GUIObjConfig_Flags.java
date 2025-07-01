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
    
    public GUIObjConfig_Flags() {  super(numConfigFlags);}
    
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
    public void setIsValueRange(boolean val) {setFlag(objectIsReadOnlyIDX, val);}
    /**
     * Get whether this object is value-range instead of just a single value (uses min and max instead of val)
     * @return
     */
    public boolean isValueRange() {return getFlag(objectIsReadOnlyIDX);}

    
}//GUIObjConfig_Flags
