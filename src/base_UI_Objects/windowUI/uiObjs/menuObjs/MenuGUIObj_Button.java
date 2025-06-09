package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public class MenuGUIObj_Button extends MenuGUIObj_List {
	
	/**
	 * First index of stateLabels array should be considered 'off'/'false' for this button
	 */
	protected final int _offStateIDX = 0;

	/**
	 * Flags structure to specify internal button behavior
	 */
	private int[] btnTypeFlags;
	private static final int 
		//Whether this button should stay enabled until next draw frame
		isMomentaryIDX = 0,
		//Whether this button waits for some external process to complete before returning to _offStateIDX State
		waitOnProcIDX = 1;	
	private static final int numBtnTypeFlags = 2;
	
	/**
	 * Current state of the button
	 */
	private int[] btnStateFlags;
	private static final int 
		// Whether this button is currently waiting on the next frame to reset (if is momentary)
		waitingOnDrawIDX = 0,
		//Whether this button is currently waiting on processing (if has been specified to wait on processing)
		waitingOnProcIDX = 1;
	private static final int numBtnStateFlags = 2;	

	
	/**
	 * Build a boolean/multi-state button as a variant of a listbox
	 * This button will have no label - it's label and its valueAsString are the same
	 *  # values this object uses in GUIObj_Params
	 * @param objID the index of the object in the managing container
	 * @param objParams GUIObj_Params construct that will provide : 
	 *       name the name/display label of the object
	 *       initVal the initial value
	 *       configFlags any preset configuration flags
	 *       stateLabels list of labels for each state
	 *       btnFlags flags that specify the type/behavior of this button (i.e. this button's behavior or configuration)
	 */
	public MenuGUIObj_Button(int _objID, GUIObj_Params objParams) {
		super(_objID, objParams);
		//super(_objID, _name, new double[] {0, _stateLabels.length-1, 1}, _initialState, GUIObj_Type.Button, _flags, _stateLabels);		
		//Initialize structure to manage button behavior
		initBtnTypeFlags();	
		//provide init values for type flags
		int numToInit = (objParams.buttonFlags.length < numBtnTypeFlags ? objParams.buttonFlags.length : numBtnTypeFlags);
		for(int i =0; i<numToInit;++i){ 	setBtnTypeFlags(i,objParams.buttonFlags[i]);	}
		//initialize the button state
		initBtnStateFlags();
	}//ctor	

	private void initBtnTypeFlags(){			btnTypeFlags = new int[1 + numBtnTypeFlags/32]; for(int i = 0; i<numBtnTypeFlags; ++i){setBtnTypeFlags(i,false);}	}
	protected boolean getBtnTypeFlags(int idx){	int bitLoc = 1<<(idx%32);return (btnTypeFlags[idx/32] & bitLoc) == bitLoc;}	
	protected void setBtnTypeFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		btnTypeFlags[flIDX] = (val ?  btnTypeFlags[flIDX] | mask : btnTypeFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case isMomentaryIDX 			:{break;}
		case waitOnProcIDX				:{break;}
		}
	}//setBtnTypeFlags	

	private void initBtnStateFlags(){			btnStateFlags = new int[1 + numBtnStateFlags/32]; for(int i = 0; i<numBtnStateFlags; ++i){setBtnStateFlags(i,false);}	}
	protected boolean getBtnStateFlags(int idx){	int bitLoc = 1<<(idx%32);return (btnStateFlags[idx/32] & bitLoc) == bitLoc;}	
	protected void setBtnStateFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		//val is going from true to false
		boolean isTrueToFalseToggle = (!val && (val != getBtnStateFlags(idx)));
		btnStateFlags[flIDX] = (val ?  btnStateFlags[flIDX] | mask : btnStateFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case waitingOnDrawIDX 			:{
			if(isTrueToFalseToggle) {returnToInitState();}
			break;}
		case waitingOnProcIDX			:{
			if(isTrueToFalseToggle) {returnToInitState();}
			break;}
		}
	}//setBtnStateFlags
	
	/**
	 * Set the appropriate flags for this button based on its configuration and status when it has changed
	 */
	protected void setBtnHasChanged() {
		// btn data has changed. Set waiting flags, if appropriate, for rendering
		setStartWaitingOnDraw();
		setStartWaitingOnProc();
	}
	
	/**
	 * Get whether or not this button is momentary
	 * @return
	 */
	public final boolean getIsMomentary() {					return getBtnTypeFlags(isMomentaryIDX);}
	/**
	 * Get whether or not this button should stay in on state until soome processing has finished
	 * @return
	 */
	public final boolean getShouldWaitOnProc() {			return getBtnTypeFlags(waitOnProcIDX);}
	
	/**
	 * Set that this button is finished waiting on draw cycle
	 */
	public final void setDoneWaitingOnDraw() {				setBtnTypeFlags(waitingOnDrawIDX, false);}
	/**
	 * Set that this button is starting to wait on the draw cycle
	 */
	public final void setStartWaitingOnDraw() {				if(getIsMomentary()) { setBtnTypeFlags(waitingOnDrawIDX, true);}}
	/**
	 * Get whether this button is waiting on the draw cycle
	 * @return
	 */
	public final boolean getIsWaitingOnDraw() {				return getIsMomentary() && getBtnTypeFlags(waitingOnDrawIDX);}
	
	/**
	 * Set that this button is finished waiting on a process to complete
	 */
	public final void setDoneWaitingOnProc() { 				setBtnTypeFlags(waitingOnProcIDX, false);}
	/**
	 * Set that this button is now waiting on a process to complete
	 */
	public final void setStartWaitingOnProc() { 			if (getShouldWaitOnProc()) { setBtnTypeFlags(waitingOnProcIDX, true);}}
	/**
	 * Get whether or not this button is waiting on a process to complete
	 * @return
	 */
	public final boolean getIsWaitingOnProc() {				return getShouldWaitOnProc() && getBtnTypeFlags(waitingOnProcIDX);}
	
	/**
	 * Return this button to its initial state
	 */
	public final void returnToInitState() {returnToInitVal(); }
	
	/**
	 * Return this button's state
	 * @return
	 */
	public final int getButtonState() {return this.getValueAsInt();}
	
	/**
	 * Return button's state as a boolean - either off (set to false/off state idx) or not
	 * @return
	 */
	public final boolean getValueAsBoolean() {
		return getButtonState() == _offStateIDX ? false : true;
	}
	
	/**
	 * Make sure val adheres to specified bounds, looping around at boundaries (i.e. torroid)
	 * @param _val
	 * @return
	 */
	@Override
	protected double forceBounds(double _val) {		return forceBoundsTorroidal(_val);	}
	
	/**
	 * Return how many states this button supports
	 */
	public int getNumStates() {return getNumEntries();}
	
	/**
	 * Get all the state labels for this button
	 * @return
	 */
	public final String[] getStateLabels() {return getListValues();}
	
	/**
	 * Set the state by the passed state label.
	 * @param _lbl
	 * @return
	 */
	public final int[] setStateByLabel(String _lbl) {return setValInList(_lbl);}
	
	/**
	 * Set this button object's list of possible states
	 * @param stateLbls The new list of state labels to set for this object
	 * @param setAsDefault Whether these values should be set as the default values (i.e. reloaded on reset)
	 * @return returns current val cast to int as idx (i.e. which state this button is currently in)
	 */
	public final int setStateLabels(String[] stateLbls, boolean setAsDefault) {		return setListVals(stateLbls, setAsDefault);	}
	
	/**
	 * Return the string representation of this button's state
	 * @param idx
	 * @return
	 */
	public final String getStateLabel(int idx) {return getValueAsString(idx);}
	
	
	@Override
	public final String getLabel() {return getValueAsString();}
	
	/**
	 * Dragging is disabled on buttons
	 */
	@Override
	public final double dragModVal(double _notUsed) {return getVal();}
	
	/**
	 * Whether this button is in the "off" state.
	 * @return
	 */
	public final boolean isOff() {return (getValueAsInt() == _offStateIDX);}
	
	/**
	 * Button mod should only ever be +/-1
	 * @param mod
	 * @param scale
	 * @return
	 */
	@Override
	public final double clickModVal(double mod, double scale) {
		double modVal = (mod *scale*modMult);
		double retVal = setVal(modValAssign(getVal() + (modVal > 0 ? 1 : -1)));
		if(getIsDirty()) {			setBtnHasChanged();	}		
		return retVal;
	}

}//class Base_ButtonGUIObj