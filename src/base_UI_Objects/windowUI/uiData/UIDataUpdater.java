package base_UI_Objects.windowUI.uiData;

import java.util.Map;

import base_UI_Objects.windowUI.base.IUIManagerOwner;
import base_Utils_Objects.dataAdapter.Base_DataAdapter;

/**
 * structure holding UI-derived/modified data used to update execution code
 * @author john
 */

public class UIDataUpdater extends Base_DataAdapter {
	/**
	 * Owning UI Window/UI construct
	 */
	protected IUIManagerOwner owner;
	
	public UIDataUpdater(IUIManagerOwner _owner) {super(); owner=_owner;}
	public UIDataUpdater(IUIManagerOwner _owner, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals, Map<Integer, Boolean> _bVals) {
		super(_iVals, _fVals, _bVals);
		owner=_owner;
	}
	
	public UIDataUpdater(UIDataUpdater _otr) {
		super(_otr);
		owner=_otr.owner;
	}
	
	/**
	 * Boolean value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateBoolValue_Indiv(int idx, boolean value) {
	    owner.updateBoolValFromExecCode(idx, value);		
	}
	
	/**
	 * Integer value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateIntValue_Indiv(int idx, Integer value) {
	    owner.updateIntValFromExecCode(idx, value);		
	}
	
	/**
	 * Float value updater - this will update the owning window's corresponding data values as well
	 */	
	@Override
	protected final void updateFloatValue_Indiv(int idx, Float value) {
	    owner.updateFloatValFromExecCode(idx, value);		
	}
	
	@Override
	public String getName() {		return owner.getName();	}
}//class base_UpdateFromUIData
