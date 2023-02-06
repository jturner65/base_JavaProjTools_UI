package base_UI_Objects.windowUI.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

public class WinAppPrivStateFlags extends Base_BoolFlags {
	/**
	 * Owning display window
	 */
	private final Base_DispWindow owner;
	
	
	public WinAppPrivStateFlags(Base_DispWindow _owner, int _numFlags) {
		super(_numFlags);
		owner = _owner;
	}
	
	/**
	 * Toggle the button represented by passed idx
	 * @param idx
	 */
	public final void toggleButton(int idx) {
		setFlag(idx, !getFlag(idx));
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
		owner.handlePrivFlags_Indiv(idx, val, oldVal);
	}

}//class WinAppPrivStateFlags
