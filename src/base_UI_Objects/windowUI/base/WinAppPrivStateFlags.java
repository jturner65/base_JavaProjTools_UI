package base_UI_Objects.windowUI.base;

import base_UI_Objects.windowUI.UIObjectManager;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

public class WinAppPrivStateFlags extends Base_BoolFlags {
    /**
     * Manager of all UI objects in this window
     */
    protected UIObjectManager uiMgr;
    
    
    public WinAppPrivStateFlags(UIObjectManager _uiMgr, int _numFlags) {
        super(_numFlags);
        uiMgr = _uiMgr;
    }
    
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
