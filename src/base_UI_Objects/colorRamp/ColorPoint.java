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
	
	public ColorPoint(ColorPoint p) {
		super(p);
		name = p.name;
		rad = p.rad;
	}
	
	public void show(IRenderInterface ri, myPoint P, double r, String txt) {
		ri.pushMatState(); 
		ri.drawSphere(P, r, 5); 
		ri.setColorValFill(IRenderInterface.gui_Black, 255);ri.setColorValStroke(IRenderInterface.gui_Black,255);
		double d = 1.1 * r;
		ri.showTextAtPt(myPoint.ZEROPT, txt, new myVector(d,d,d));
		ri.popMatState();
	} // render sphere of radius r and center P)

	public void showColor(IRenderInterface ri ){
		ri.setFill((int)x, (int)y, (int)z, 255); 
		show(ri, this,rad, toString());
	}
	public String toString(){
		String res = ""+ name + " R :"+x+" G : " + y  + " B : " + z;
		return res;
	}
}//myClrPoint