package base_UI_Objects.colorRamp;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;

public class ColorPoint extends myPoint{
	public IRenderInterface pa;	
	public String name;
	public float rad;
	
	public ColorPoint(IRenderInterface _pa, String _nm, double _red, double _green, double _blue, double r){
		super(_red, _green, _blue);
		pa = _pa;
		name = _nm;
		rad = (float)r;
	}	
	public ColorPoint(IRenderInterface _pa, String _nm, myPoint pt, double r){
		super(pt);
		pa = _pa;
		name = _nm;
		rad = (float)r;
	}	
	public void show(myPoint P, double r, String txt) {
		pa.pushMatState(); 
		pa.setSphereDetail(5);
		pa.translate((float)P.x,(float)P.y,(float)P.z); 
		pa.drawSphere((float)r); 
		pa.setColorValFill(IRenderInterface.gui_Black, 255);pa.setColorValStroke(IRenderInterface.gui_Black,255);
		double d = 1.1 * r;
		pa.showTextAtPt(myPoint.ZEROPT, txt, new myVector(d,d,d));
		pa.popMatState();
	} // render sphere of radius r and center P)

	public void showColor(){
		pa.setFill((int)x, (int)y, (int)z, 255); 
		show(this,rad, toString());
	}
	public String toString(){
		String res = ""+ name + " R :"+x+" G : " + y  + " B : " + z;
		return res;
	}
}//myClrPoint