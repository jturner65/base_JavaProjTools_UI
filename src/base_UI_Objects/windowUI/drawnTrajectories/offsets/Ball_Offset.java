package base_UI_Objects.windowUI.drawnTrajectories.offsets;

import java.util.ArrayList;

import base_Math_Objects.vectorObjs.doubles.myCntlPt;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.drawnTrajectories.offsets.base.Base_Offset;

public class Ball_Offset extends Base_Offset {
    
    /**
     * Radius of resultant offset for each cntl point
     */
    public double[] _radii;
    /**
     * "top" (idx 0) and "bottom" (idx 1) ball centers for each point
     */
    public myPoint[][] _centers; 

    public Ball_Offset(boolean _ec) {    super(_ec);name = "Ball offset";}
    
    @Override
    public ArrayList<myPoint> calcOffset(myCntlPt[] cntlPts, myVector[] nAra, myVector[] tAra) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void drawCntlPts(IGraphicsAppInterface ri, myCntlPt[] myPoints, myVector[] nAra, myVector[] tAra,
            boolean derived) {
        for(int i=0;i<myPoints.length;++i) {
            
        }
    }

}//class Ball_Offset
