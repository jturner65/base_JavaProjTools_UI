package base_UI_Objects.colorRamp;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;

public class ColorPoint extends myPoint{
	public String name;
	public float rad;
	
	public ColorPoint(String _nm, double _red, double _green, double _blue, double r){
		super(_red, _green, _blue);
		name = _nm;
		rad = (float)r;
	}	
	public ColorPoint(String _nm, myPoint pt, double r){
		super(pt);
		name = _nm;
		rad = (float)r;
	}	
	public void show(IRenderInterface pa, myPoint P, double r, String txt) {
		pa.pushMatState(); 
		pa.setSphereDetail(5);
		pa.translate((float)P.x,(float)P.y,(float)P.z); 
		pa.drawSphere((float)r); 
		pa.setColorValFill(IRenderInterface.gui_Black, 255);pa.setColorValStroke(IRenderInterface.gui_Black,255);
		double d = 1.1 * r;
		pa.showTextAtPt(myPoint.ZEROPT, txt, new myVector(d,d,d));
		pa.popMatState();
	} // render sphere of radius r and center P)

	public void showColor(IRenderInterface pa ){
		pa.setFill((int)x, (int)y, (int)z, 255); 
		show(pa, this,rad, toString());
	}
	public String toString(){
		String res = ""+ name + " R :"+x+" G : " + y  + " B : " + z;
		return res;
	}
}//myClrPoint