package base_UI_Objects.windowUI.simulation.simExec;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.simExec.Base_SimExec;

public abstract class Base_UISimExec extends Base_SimExec {
	/**
	 * Owning window, or null if console application
	 */
	protected final Base_DispWindow win;
	
	/**
	 * ref to render interface, if window-based, or null if console
	 */
	protected final IRenderInterface ri;	
	
	public Base_UISimExec(Base_DispWindow _win, String _name, int _maxSimLayouts) {
		super(_name, _maxSimLayouts);
		if(_win != null) {	win = _win;		ri = Base_DispWindow.ri;} 
		else {				win = null;		ri = null;}
	}
	
	@Override
	public final boolean hasRenderInterface() {return ri!=null;}
	
	/**
	 * Returns this application's render interface. Will be null if a console application 
	 * @return
	 */
	public final IRenderInterface getRenderInterface() {return ri;}	
	
	
	/**
	 * Update owning window with changes to Sim exec/simulation Data Updater values, if appropriate
	 */
	@Override
	protected final void updateSimDataUpdaterFromSim_Indiv() {
		if(win==null) {return;}
		//copy current state of masterDataUpdate to win.uiUpdateData, 
		//appropriately mapping the fields so that the UI can change to reflect simulation values 
		
		
		
	}//updateSimDataUpdaterFromSim_Indiv
	
}//Base_UISimExec
