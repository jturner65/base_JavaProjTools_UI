package testProject.uiData;

import java.util.Map;

import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;

public class UITestDataUpdater_2D extends UIDataUpdater {

    public UITestDataUpdater_2D(Base_DispWindow _win) {
        super(_win);
        // TODO Auto-generated constructor stub
    }

    public UITestDataUpdater_2D(Base_DispWindow _win, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals,
            Map<Integer, Boolean> _bVals) {
        super(_win, _iVals, _fVals, _bVals);
        // TODO Auto-generated constructor stub
    }

    public UITestDataUpdater_2D(UIDataUpdater _otr) {
        super(_otr);
        // TODO Auto-generated constructor stub
    }

}//class UITestDataUpdater_2D
