package base_UI_Objects.windowUI.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

public class WinAppPrivStateFlags extends Base_BoolFlags {
	/**
	 * Owning display window
	 */
	private final IUIManagerOwner owner;
	
	
	public WinAppPrivStateFlags(IUIManagerOwner _owner, int _numFlags) {
		super(_numFlags);
		owner = _owner;
	}
	
	public WinAppPrivStateFlags(WinAppPrivStateFlags _otr) {
		super(_otr);
		owner = _otr.owner;
	}
	
	/**
	 * Win-Application-specific debug execution
	 */
	@Override
	protected void handleSettingDebug(boolean val) {
		owner.handlePrivFlagsDebugMode(val);
	}

	/**
	 * Build Win-Application-specific switch statement
	 */
	@Override
	protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
		//update consumers of UI struct
		owner.checkSetBoolAndUpdate(idx, val);
		owner.handleOwnerPrivFlags(idx, val, oldVal);
	}

}//class WinAppPrivStateFlags
