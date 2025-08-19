package base_UI_Objects.windowUI.sidebar;

import java.util.Map;

import base_UI_Objects.windowUI.uiData.UIDataUpdater;

public class SidebarMenuData extends UIDataUpdater {
    /**
     * 
     * @param _owner
     */
    public SidebarMenuData(SidebarMenu _owner) {
        super(_owner);
    }
    /**
     * 
     * @param _owner
     * @param _iVals
     * @param _fVals
     * @param _bVals
     */
    public SidebarMenuData(SidebarMenu _owner, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals,
            Map<Integer, Boolean> _bVals) {
        super(_owner, _iVals, _fVals, _bVals);
        // TODO Auto-generated constructor stub
    }
    /**
     * 
     * @param _otr
     */
    public SidebarMenuData(SidebarMenuData _otr) {
        super(_otr);
    }

}// class SidebarMenuData 
