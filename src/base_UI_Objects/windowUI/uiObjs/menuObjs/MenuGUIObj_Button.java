package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;

public class MenuGUIObj_Button extends MenuGUIObj_List {
	
	/**
	 * Flags structure to specify internal button behavior
	 */
	private int[] btnTypeFlags;
	private static final int 
		//Whether this button only stays enabled until next draw frame
		isMomentaryIDX = 0,
		//Whether this button waits for some external process to complete before returning to _initial State
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
	 * This button will have no label and instead should 
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _initialState the initial state setting for this object
	 * @param _flags any preset configuration flags
	 * @param _stateLabels list of labels for each state
	 * @param _btnFlags flags that govern this buttons behavior.
	 */
	public MenuGUIObj_Button(int _objID, String _name, int _initialState, boolean[] _flags, String[] _stateLabels, boolean[] _btnFlags) {		
		super(_objID, _name, new double[] {0, _stateLabels.length-1, 1}, _initialState, GUIObj_Type.Button, _flags, _stateLabels);		
		//Initialize structure to manage button behavior
		initBtnTypeFlags();		
		int numToInit = (_flags.length < numBtnTypeFlags ? _flags.length : numBtnTypeFlags);
		for(int i =0; i<numToInit;++i){ 	setBtnTypeFlags(i,_flags[i]);	}
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
	
	@Override
	public final String getLabel() {return getValueAsString();}
	
	/**
	 * Set the appropriate flags for this button based on its configuration and status when it has changed
	 */
	protected void setBtnHasChanged() {
		// btn data has changed. Set waiting flags, if appropriate, for rendering
		setStartWaitingOnDraw();
		setStartWaitingOnProc();
	}
	
	public final boolean getIsMomentary() {					return getBtnTypeFlags(isMomentaryIDX);}
	public final boolean getShouldWaitOnProc() {			return getBtnTypeFlags(waitOnProcIDX);}
	
	public final void setDoneWaitingOnDraw() {				setBtnTypeFlags(waitingOnDrawIDX, false);}
	public final void setStartWaitingOnDraw() {				if(getIsMomentary()) { setBtnTypeFlags(waitingOnDrawIDX, true);}}
	public final boolean getIsWaitingOnDraw() {				return getIsMomentary() && getBtnTypeFlags(waitingOnDrawIDX);}
	
	public final void setDoneWaitingOnProc() { 				setBtnTypeFlags(waitingOnProcIDX, false);}
	public final void setStartWaitingOnProc() { 			if (getShouldWaitOnProc()) { setBtnTypeFlags(waitingOnProcIDX, true);}}
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
	 * Make sure val adheres to specified bounds, looping around if necessary
	 * @param _val
	 * @return
	 */
	@Override
	protected double forceBounds(double _val) {
		int numStates = getNumStates();
		while(_val < 0) { _val += numStates;}
		_val %= numStates;
		return _val;
	}
	
	/**
	 * Return how many states this button supports
	 */
	public final int getNumStates() {return getNumEntries();}
	
	/**
	 * Get all the state labels for this button
	 * @return
	 */
	public final String[] getStateLabels() {return getListValues();}
	
	/**
	 * Dragging is disabled on buttons
	 */
	@Override
	public final double dragModVal(double _notUsed) {return getVal();}
	
	/**
	 * Modify this object by passed mod value, multiplied by scale. This is for a single click
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