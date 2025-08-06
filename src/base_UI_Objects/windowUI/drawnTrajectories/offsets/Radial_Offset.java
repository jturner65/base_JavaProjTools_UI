package base_UI_Objects.windowUI.drawnTrajectories.offsets;

import java.util.ArrayList;

import base_Math_Objects.vectorObjs.doubles.myCntlPt;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.drawnTrajectories.offsets.base.Base_Offset;

public class Radial_Offset extends Base_Offset {

    public Radial_Offset(boolean _ec) {
        super(_ec);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ArrayList<myPoint> calcOffset(myCntlPt[] cntlPts, myVector[] nAra, myVector[] tAra) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void drawCntlPts(IGraphicsAppInterface ri, myCntlPt[] myPoints, myVector[] nAra, myVector[] tAra,
            boolean derived) {
        // TODO Auto-generated method stub

    }

}//class Radial_Offset
