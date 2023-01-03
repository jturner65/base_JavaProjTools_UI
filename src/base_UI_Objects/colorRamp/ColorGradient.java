package base_UI_Objects.colorRamp;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Render_Interface.IRenderInterface;

public class ColorGradient{
	public IRenderInterface pa;
	public ColorRamp rmp;
	public String name;
	public float x,y, w,h;
	public ColorPoint[] clrs;				//all colors in this gradient - r = x, g = z, b = y

	public ColorGradient(IRenderInterface _pa, ColorRamp _rmp, float _x, float _y, String _name){
		pa = _pa;
		rmp = _rmp;
		name = _name;
		w = pa.getWidth() * .13f; 
		h = pa.getHeight() * .07f;

		x = _x;
		y = _y;
		clrs = new ColorPoint[255];
		for(int i =0; i < 255; ++i){clrs[i] = new ColorPoint(pa, name+"pt:"+i, 255-i,i,0,rmp.clrPtRad);}
	}
	//take an array of values and map it to this clrs array
	public void rebuildClrs(ColorPoint[] _clrs){
		clrs = _clrs;
//		clrs = new myClrPoint[_clrs.length];
//		System.arraycopy(_clrs, 0, clrs, 0, _clrs.length);
	}
	
	public void fillAndShowLineByRBGPt(myPoint p, float x,  float y, float w, float h){
		pa.setFill((int)p.x,(int)p.y,(int)p.z, 255);
		pa.setStroke((int)p.x,(int)p.y,(int)p.z, 255);
		pa.drawRect(x,y,w,h);
	}	
	
	public void drawMe(){
		pa.pushMatState(); 
		pa.setFill(0,0,0,255);
		pa.showText(name, x, y-10);
		pa.drawRect(x, y, w, h);
		float offset = w/(1.0f * clrs.length);			//how much to move laterally in ramp for each color
		float st = x;
		for(int i = 0; i< clrs.length;++i){
			fillAndShowLineByRBGPt(clrs[i],st, y, offset, h);//(myPoint p, float x,  float y, float w, float h)
			st += offset;
		}
		pa.setFill(0,0,0,255);
		pa.showText("3rd Color",x-100, y-10);
		fillAndShowLineByRBGPt(clrs[clrs.length/2],x-100, y, h, h);
		pa.drawRect(x-100, y, h, h);
		pa.popMatState();
	}//drawGradientRectangle	
	
	public void drawSpline(){
		pa.pushMatState(); 
		for(int i = 0; i< clrs.length;++i){	rmp.showColor(clrs[i],clrs[i].rad);}		
		pa.popMatState();
	}
}//class myClrGradient