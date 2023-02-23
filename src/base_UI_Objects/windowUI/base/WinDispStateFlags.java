package base_UI_Objects.windowUI.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

public class WinDispStateFlags extends Base_BoolFlags {
	/**
	 * Owning display window
	 */
	private final Base_DispWindow owner;
	
	public static final int 
		showIDX 			= _numBaseFlags,				//whether or not to show this window
		is3DWin 			= _numBaseFlags + 1,
		canChgView			= _numBaseFlags + 2,			//view can change
		isRunnable 			= _numBaseFlags + 3,			//runs a simulation
		isCloseable			= _numBaseFlags + 4,			//window is able to be closed
		hasScrollBars 		= _numBaseFlags + 5,			//this window has scroll bars (both vert and horizontal)
		uiObjMod			= _numBaseFlags + 6,			//a ui object in this window has been modified
		useRndBtnClrs		= _numBaseFlags + 7,	
		useCustCam			= _numBaseFlags + 8,			//whether or not to use a custom camera for this window
		drawMseEdge			= _numBaseFlags + 9,			//whether or not to draw the mouse location/edge from eye/projection onto box
		hasRightSideMenu	= _numBaseFlags + 10,			//whether this window has a right-side info menu overlay
		showRightSideMenu	= _numBaseFlags + 11,			//whether this window is currently showing right side info menu, or if it is minimized
		clearPrivBtns		= _numBaseFlags + 12;			//momentary priv buttons have been set, need to be cleared next frame
				
	private static final int _numStateFlags = _numBaseFlags + 13;
	
	public WinDispStateFlags(Base_DispWindow _owner) {
		super(_numStateFlags);
		owner = _owner;		
	}

	/**
	 * Whether or not to show this window
	 * @return
	 */
	public final boolean getShowWin() {return getFlag(showIDX);}	
	/**
	 * Set whether or not to show this window
	 * @param _val
	 */
	public final void setShowWin(boolean _val) {setFlag(showIDX, _val);}	
	/**
	 * Flip the current window state
	 * @param _owner
	 */
	public final void toggleShowWin() {setFlag(showIDX,!getFlag(showIDX));}
	
	/**
	 * Whether or not this window is 3d
	 * @return
	 */
	public final boolean getIs3DWin() {return getFlag(is3DWin);}
	
	/**
	 * Set Whether or not this window is 3d
	 * @param _val
	 */
	public final void setIs3DWin(boolean _val) {setFlag(is3DWin, _val);}

	/**
	 * Whether or not this window can change view
	 * @return
	 */
	public final boolean getCanChgView() {return getFlag(canChgView);}
	
	/**
	 * Set whether or not this window can change view
	 * @param _val
	 */
	public final void setCanChgView(boolean _val) {setFlag(canChgView, _val);}

	/**
	 * Whether or not this window is runnable (simulation)
	 * @return
	 */
	public final boolean getIsRunnable() {return getFlag(isRunnable);}
	
	/**
	 * Set Whether or not this window is runnable (simulation)
	 * @param _val
	 */
	public final void setIsRunnable(boolean _val) {setFlag(isRunnable, _val);}
	
	/**
	 * Whether or not this window can be closed
	 * @return
	 */
	public final boolean getIsCloseable() {return getFlag(isCloseable);}
	/**
	 * Set whether or not this window can be closed
	 * @param _val
	 */
	public final void setIsCloseable(boolean _val) {setFlag(isCloseable, _val);}	
	
	/**
	 * Whether or not this window has scrollbars
	 * @return
	 */
	public final boolean getHasScrollBars() {return getFlag(hasScrollBars);}	
	/**
	 * Set whether or not this window has scrollbars
	 * @param _val
	 */
	public final void setHasScrollBars(boolean _val) {setFlag(hasScrollBars, _val);}
	
	/**
	 * Whether or not a UI object has been modified in owning window
	 * @return
	 */
	public final boolean getUIObjMod() {return getFlag(uiObjMod);}
	
	/**
	 * Set whether or not a UI object has been modified in owning window
	 * @param _val
	 */
	public final void setUIObjMod(boolean _val) {setFlag(uiObjMod, _val);}
	
	/**
	 * Whether or not to use random colors for buttons
	 * @return
	 */
	public final boolean getUseRndBtnClrs() {return getFlag(useRndBtnClrs);}
	
	/**
	 * Set whether or not to use random colors for buttons
	 * @param _val
	 */
	public final void setUseRndBtnClrs(boolean _val) {setFlag(useRndBtnClrs, _val);}

	/**
	 * Whether or not to use a custom camera for this window
	 * @return
	 */
	public final boolean getUseCustCam() {return getFlag(useCustCam);}	
	/**
	 * Set whether or not to use a custom camera for this window
	 * @param _val
	 */
	public final void setUseCustCam(boolean _val) {setFlag(useCustCam, _val);}
	
	/**
	 * Whether or not to draw lines to mouse reticle
	 * @return
	 */
	public final boolean getDrawMseEdge() {return getFlag(drawMseEdge);}
	/**
	 * Set whether or not to draw lines to mouse reticle
	 * @param _val
	 */
	public final void setDrawMseEdge(boolean _val) {setFlag(drawMseEdge, _val);}
	
	
	/**
	 * Set State for right-side info window
	 * @param visible
	 */
	public final void setRtSideInfoWinSt(boolean visible) {
		if(getFlag(hasRightSideMenu)) {setFlag(showRightSideMenu,visible);}
	}
	
	/**
	 * Whether able to draw right side menu
	 * @return
	 */
	public final boolean getHasRtSideMenu() {return getFlag(hasRightSideMenu);}
	
	/**
	 * Whether able to draw right side menu
	 * @return
	 */
	public final void setHasRtSideMenu(boolean val) {setFlag(hasRightSideMenu, val);}	
	
	/**
	 * Whether to show right side menu
	 * @return
	 */
	public final boolean getShowRtSideMenu() {return getFlag(showRightSideMenu);}	
	/**
	 * Whether to show right side menu
	 * @return
	 */
	public final void setShowRtSideMenu(boolean val) {setFlag(showRightSideMenu, val);}	
	
	/**
	 * Whether to show right side menu
	 * @return
	 */
	public final boolean getClearPrivBtns() {return getFlag(clearPrivBtns);}	
	/**
	 * Whether to show right side menu
	 * @return
	 */
	public final void setClearPrivBtns(boolean val) {setFlag(clearPrivBtns, val);}	
	
	/**
	 * Set or clear debug functionality for flag owner
	 */
	@Override
	protected void handleSettingDebug(boolean val) {owner.handleDispFlagsDebugMode(val);		}

	@Override
	protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
		switch(idx){
		case showIDX 			: {	
			owner.handleShowWinFromFlags(val);
			break;}	
		case is3DWin 			: {	break;}	
		case isCloseable		: {	break;}	
		case hasScrollBars 		: {	break;}	
		case uiObjMod			: {	break;}			
		case useRndBtnClrs		: { break;}
		case useCustCam			: { break;}
		case drawMseEdge		: { break;}
		case clearPrivBtns		: { break;}
		case hasRightSideMenu  : { break;}	//can drawn right side menu
		case showRightSideMenu  : { 		
			//modify the dimensions of the visible window based on whether the side bar menu is shown
			if(getFlag(hasRightSideMenu)) {
				owner.handleShowRtSideMenu(val);
			}
			break;}		
		}
	}//handleFlagSet_Indiv

}
