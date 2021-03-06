package base_UI_Objects.windowUI.drawnObjs.offsets;

import java.util.ArrayList;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myCntlPt;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_UI_Objects.windowUI.drawnObjs.offsets.base.baseOffset;

/**
* calculates normal offset - distance r, normal from stroke line
* @author john
*/
//make other classes to use different offset mechanism
public class myNormOffset extends baseOffset{
	public myNormOffset(){super(); name = "Normal offset";}

	@Override
	public  ArrayList<myPoint> calcOffset(myCntlPt[] cntlPts, myVector[] nAra, myVector[] tAra) {
		if(nAra.length != cntlPts.length){return  new ArrayList<myPoint>();}	
		ArrayList<myPoint> tmp = new ArrayList<myPoint>();
		int numCmyPointsM1 = cntlPts.length-1;		
		//start at first point and build endcap
		if(endCaps){tmp.addAll(buildCapPts(cntlPts[0], nAra[0], tAra[0], 1));}
		for(int i = 0; i<cntlPts.length;++i){	tmp.add(new myPoint(cntlPts[i], cntlPts[i].r, nAra[i]));}//add cntl point + rad offset from norm
		//build endcap on last cntlpoint
		if(endCaps){tmp.addAll(buildCapPts(cntlPts[numCmyPointsM1], nAra[numCmyPointsM1], tAra[numCmyPointsM1],-1));}
		for(int i = numCmyPointsM1; i>=0;--i){	tmp.add(new myPoint(cntlPts[i], -cntlPts[i].r, nAra[i]));}//add cntl point + rad offset from norm negated, in backwards order, so all points are added properly
		return tmp;
	}
	
	public String toString(){
		String res = name +super.toString();		
		return res;
	}
	
  @Override
//  public void drawCntlPts(cntlPt[] myPoints, myVector[] nAra, myVector[] tAra, boolean derived) {
//      for(int i = 0; i < myPoints.length; ++i){
//          myPoints[i].drawNorm((derived ? 0 : 1), nAra[i], tAra[i]);
//      }
//  }
  public void drawCntlPts(IRenderInterface pa, myCntlPt[] myPoints, myVector[] nAra, myVector[] tAra, boolean derived) {
	  pa.pushMatState();
	  int clrInt = 0;
      for(int i = 0; i < myPoints.length; ++i){
    	  clrInt = (int)(i/(1.0f * myPoints.length) * 255.0f);
          pa.setFill(clrInt,0,(255 - clrInt),255);  
          pa.setStroke(clrInt,0,(255 - clrInt),255); 
          pa.drawCircle3D( myPoints[i], myPoints[i].r, nAra[i], tAra[i],20);
      }
      pa.popMatState();
  }
      

}//myNormOffset
