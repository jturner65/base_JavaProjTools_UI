package base_UI_Objects.windowUI.base;

import base_UI_Objects.windowUI.UIObjectManager;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * This class manages user-configured UI-driven private flags 
 * owned by a UIObjectManager and the IUIManagerOwner that owns it.
 */
public class WinAppPrivStateFlags extends Base_BoolFlags {
    /**
     * Manager of all UI objects these flags belong to
     */
    protected UIObjectManager uiMgr;
    
    /**
     * Constructor
     * @param _uiMgr
     * @param _numFlags application-specific count of managed booleans.
     */
    public WinAppPrivStateFlags(UIObjectManager _uiMgr, int _numFlags) {
        super(_numFlags);
        uiMgr = _uiMgr;
    }
    /**
     * Copy constructor
     * @param _otr
     */
    public WinAppPrivStateFlags(WinAppPrivStateFlags _otr) {
        super(_otr);
        uiMgr = _otr.uiMgr;
    }
    
    /**
     * Win-Application-specific debug execution
     */
    @Override
    protected void handleSettingDebug(boolean val) {
        uiMgr.handlePrivFlagsDebugMode(val);
    }

    /**
     * Build Win-Application-specific switch statement
     */
    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
        //update consumers of UI struct
        uiMgr.checkSetBoolAndUpdate(idx, val);
        uiMgr.handlePrivFlags(idx, val, oldVal);
    }

}//class WinAppPrivStateFlags
