package base_UI_Objects.windowUI.uiData;

import java.util.Map;

import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.dataAdapter.Base_DataAdapter;

/**
 * structure holding UI-derived/modified data used to update execution code
 * @author john
 */

public class UIDataUpdater extends Base_DataAdapter {
	/**
	 * Owning UI Window
	 */
	protected Base_DispWindow win;
	
	public UIDataUpdater(Base_DispWindow _win) {super(); win=_win;}
	public UIDataUpdater(Base_DispWindow _win, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals, Map<Integer, Boolean> _bVals) {
		super(_iVals, _fVals, _bVals);
		win=_win;
	}
	
	public UIDataUpdater(UIDataUpdater _otr) {
		super(_otr);
		win=_otr.win;
	}
	
	/**
	 * Boolean value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateBoolValue_Indiv(int idx, boolean value) {
		win.updateBoolValFromExecCode(idx, value);		
	}
	
	/**
	 * Integer value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateIntValue_Indiv(int idx, Integer value) {
		win.updateIntValFromExecCode(idx, value);		
	}
	
	/**
	 * Float value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateFloatValue_Indiv(int idx, Float value) {
		win.updateFloatValFromExecCode(idx, value);		
	}
	
	@Override
	public String getName() {		return win.name;	}
}//class base_UpdateFromUIData
