package base_UI_Objects.colorRamp;

import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;

public class ColorPointf extends myPointf {
	public String name;
	public float rad;

	public ColorPointf(String _nm, float _x, float _y, float _z, float r) {
		super(_x, _y, _z);
		name = _nm;
		rad = r;
	}
	
	public ColorPointf(String _nm, myPointf p, float r) {
		super(p);
		name = _nm;
		rad = r;
	}

	public ColorPointf(ColorPointf p) {
		super(p);
		name = p.name;
		rad = p.rad;
	}
	
	public void show(IRenderInterface pa, myPointf P, float r, String txt) {
		pa.pushMatState();  
		pa.drawSphere(P, r, 5); 
		pa.setColorValFill(IRenderInterface.gui_Black, 255);pa.setColorValStroke(IRenderInterface.gui_Black,255);
		float d = 1.1f * r;
		pa.showTextAtPt(myPointf.ZEROPT, txt, new myVectorf(d,d,d));
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

}
