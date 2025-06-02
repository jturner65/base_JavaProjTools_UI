package base_UI_Objects.colorRamp;

import java.util.ArrayList;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.MyMathUtils;
import base_UI_Objects.GUI_AppManager;


public class ColorRamp  {
	private static IRenderInterface ri;	
	private static GUI_AppManager AppMgr;
	public int R0=250, G0=245, B0=20, R1=17, G1=60, B1=242, k=30;
	
	public float sIncr = .005f;
	int numGrads = 7;					//rgb, xyz, lab, lch, userDef1 userDef2
	
	final  int USR3 = 6;
	//public String[] gradNames = new String[]{"RGB","XYZ","LAB","LCH","Modified XYZ","Modified LAB","Modified LCH"};
	public ColorGradient grads;			//gradients
	
	
	public float barThick = 3;
	public ArrayList<ColorPoint> usrPts;		//user controlled color points
	public ArrayList<ColorPoint> clrPtsArrays = new ArrayList<ColorPoint>();// only using idx 6 = new ArrayList[numGrads];
	
	public ColorPoint[] midPoints;		//midpoints of line segments of box - use to find knots for spline
	
	
	public float userPtRad = 7, clrPtRad = 2;	//user controlled color point radius and derived color point radius
	//public myPoint ptA, ptB, ptC;			//user controlled colors
	public String mode = "rgb";
	public float[] scaleVals;
	int numClrPts = 3;					//# of points in color box
	public Layer layer;

	
	public ColorRamp(IRenderInterface _ri, GUI_AppManager _AppMgr){
		ri = _ri; 	 AppMgr = _AppMgr;
		layer = new Layer(50, 10);
		float[] gridDims = AppMgr.get3dGridDims();
		scaleVals = new float[]{gridDims[0]/255.0f,gridDims[1]/255.0f,gridDims[2]/255.0f};
		
		float y = ri.getHeight() *.15f;
		grads = new ColorGradient(ri, this, ri.getWidth() *.85f, y, "Modified LCH");//(CAProject3 _pa, float _x, float _y){			

		usrPts = new ArrayList<ColorPoint>();		
		myPoint ptA =new myPoint(R0,G0,B0),
		ptB= new myPoint(R1,G1,B1);
		usrPts.add(new ColorPoint("Point A", ptA.x, ptA.y, ptA.z, userPtRad));
		usrPts.add(new ColorPoint("Point B",  ptB.x, ptB.y, ptB.z, userPtRad));
		//linear interp
		usrPts.add(new ColorPoint("Point C", new myPoint(ptA, .5, ptB), userPtRad));	
		ptsChanged();
	}

	
	//convert to ramp local coords
	public myPoint transToRamp(myPoint p){	return myPoint._mult(p, new myPoint(1.0f/scaleVals[0],1.0f/scaleVals[1],1.0f/scaleVals[2]) );	}
	//raycast from mouse to see if clicking on clickable object
	public int checkClickLoc(myPoint clickLoc, myVector mseDirVec){
		myPoint rmpSpcClkLoc = transToRamp(clickLoc);	
		for(int i = 0; i<2; ++i){			
			if(myPoint._dist(rmpSpcClkLoc, usrPts.get(i)) < AppMgr.msClkEps )			{		return i;	}
		}		
		return -1;
	}
	
	public void boundVals(int idx){
		myPoint pt = usrPts.get(idx);
		if(pt.x < 0){pt.x = 0;} else if(pt.x > 255){pt.x = 255;}
		if(pt.y < 0){pt.y = 0;} else if(pt.y > 255){pt.y = 255;}
		if(pt.z < 0){pt.z = 0;} else if(pt.z > 255){pt.z = 255;}
	}
	
	public void setMe(myPoint newLoc,int idx){
		usrPts.get(idx).set(transToRamp(newLoc));
		boundVals (idx);	
		ptsChanged();		
	}
	
	//all calcs necessary when key points changed
	public void ptsChanged(){
		usrPts.get(2).set(new myPoint(usrPts.get(0), .5, usrPts.get(1)));
		for(int i =0; i<numClrPts;++i){boundVals(i);}
		updateGrads();		
	}
	
	//build new gradient displays when points move around
	public void updateGrads(){
		//{"RGB","XYZ","LAB","LCH","User Defined 1","User Defined 2"};RGBIdx = 0, XYZIdx = 1, LABIdx = 2, LCHIdx = 3, USR1 = 4, USR2 = 5;
		//for each grad rebuild myClrPoint Array
		ColorPoint p1 = this.usrPts.get(0),p2 = this.usrPts.get(1);
		clrPtsArrays.clear();
		
		myVector test23, test33;
		ColorPoint pt0,  pt4;
	
		ColorPoint bs01 = new ColorPoint("kn1",RGBinterpPt(p1, p2, .33f) , userPtRad), 
				bs02 = new ColorPoint("kn1",RGBinterpPt(p1, p2, .66f) , userPtRad);

		pt0 = new ColorPoint("kn1",LCHinterpPt(p1, p2, .33f) , userPtRad);
		test23 = new myVector(bs01,pt0);
	
		pt4 = new ColorPoint("kn2",LCHinterpPt(p1, p2, .66f) , userPtRad);
		test33 = new myVector(bs02,pt4);

		pt0._add(myVector._mult(test23, .5f));
		pt4._add(myVector._mult(test33, .5f));
		
		myPoint	ptu;			
		for(float s = 0; s <= 1.0f; s+=sIncr){		
			float t = (float) Math.pow(Math.sin(s*MyMathUtils.HALF_PI), 2);
			ptu = ri.bezierPoint(new myPoint[] {new myPoint(p1),pt0,pt4,new myPoint(p2)}, t);
			clrPtsArrays.add(new ColorPoint("USR3",ptu, clrPtRad));
		}	

		//for(int i =0; i<numGrads; ++i){
		grads.rebuildClrs((ColorPoint[])(clrPtsArrays).toArray(new ColorPoint[0]));
		//}		
	}//
		
	public void showDebugOutput(){
		double[] lab0 = MyColorUtils.RGBtoLAB(R0,G0,B0), lab1 = MyColorUtils.RGBtoLAB(R1,G1,B1);		 
		AppMgr.getCurFocusDispWindow().getMsgObj().dispInfoMessage("myClrRamp","showDebugOutput",
				String.format("Mode: %s   k: %d   L: %d\nColor0: RGB(%3d,%3d,%3d) Lab(%3.2f,%3.2f,%3.2f)\nColor1: RGB(%3d,%3d,%3d) Lab(%3.2f,%3.2f,%3.2f)",
                mode.toUpperCase(),k,layer.l,R0,G0,B0,lab0[0],lab0[1],lab0[2],R1,G1,B1,lab1[0],lab1[1],lab1[2]));
	}
	
	public ColorPoint XYZtoRGBPoint(double X, double Y, double Z) {int [] C= MyColorUtils.XYZtoRGB(X,Y,Z); return new ColorPoint("XYZ_Pt",C[0],C[1],C[2],clrPtRad); }
	public ColorPoint LCHtoRGBPoint(double L, double c, double h) {int [] C= MyColorUtils.LCHtoRGB(L,c,h); return new ColorPoint("LCH_Pt",C[0],C[1],C[2],clrPtRad); }
	public ColorPoint LABtoRGBPoint(double L, double a, double b) {int [] C= MyColorUtils.LABtoRGB(L,a,b); return new ColorPoint("LAB_Pt",C[0],C[1],C[2],clrPtRad); }
	
	public ColorPoint RGBinterpPt(myPoint p1, myPoint p2, float s) {return RGBinterpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint XYZinterpPt(myPoint p1, myPoint p2, float s) {return XYZinterpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint LABinterpPt(myPoint p1, myPoint p2, float s) {return LABinterpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint LCHinterpPt(myPoint p1, myPoint p2, float s) {return LCHinterpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint USR1interpPt(myPoint p1, myPoint p2, float s) {return USR1interpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint USR2interpPt(myPoint p1, myPoint p2, float s) {return USR2interpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }
	public ColorPoint USR3interpPt(myPoint p1, myPoint p2, float s) {return USR3interpPt((int)p1.x, (int)p1.y, (int)p1.z,(int)p2.x, (int)p2.y, (int)p2.z,s); }

	public ColorPoint RGBinterpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {return new ColorPoint("RGB_Pt",(1.f-s)*R0+s*R1,(1.f-s)*G0+s*G1,(1.f-s)*B0+s*B1,clrPtRad); }
	public ColorPoint USR1interpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] XYZ0 = MyColorUtils.RGBtoXYZ(R0, G0, B0), XYZ1 = MyColorUtils.RGBtoXYZ(R1, G1, B1);
		return XYZtoRGBPoint((1.f-s)*XYZ0[0]+s*XYZ1[0],(1.f-s)*XYZ0[1]+s*XYZ1[1],(1.f-s)*XYZ0[2]+s*XYZ1[2]); 
	}
	public ColorPoint USR2interpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LAB0 = MyColorUtils.RGBtoLAB(R0, G0, B0), LAB1 = MyColorUtils.RGBtoLAB(R1, G1, B1);
		return LABtoRGBPoint((1.f-s)*LAB0[0]+s*LAB1[0],(1.f-s)*LAB0[1]+s*LAB1[1],(1.f-s)*LAB0[2]+s*LAB1[2]); 
	} 

	public ColorPoint USR3interpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LCH0 = MyColorUtils.RGBtoLCH(R0, G0, B0), LCH1 = MyColorUtils.RGBtoLCH(R1, G1, B1);
		double d = LCH1[2] - LCH0[2];
		d = d > 180 ? d - 360 : d;
		return LCHtoRGBPoint((1.f-s)*LCH0[0]+s*LCH1[0],(1.f-s)*LCH0[1]+s*LCH1[1], LCH0[2]+s*d ); 
	} 
	
	public ColorPoint XYZinterpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] XYZ0 = MyColorUtils.RGBtoXYZ(R0, G0, B0), XYZ1 = MyColorUtils.RGBtoXYZ(R1, G1, B1);
		return XYZtoRGBPoint((1.f-s)*XYZ0[0]+s*XYZ1[0],(1.f-s)*XYZ0[1]+s*XYZ1[1],(1.f-s)*XYZ0[2]+s*XYZ1[2]); 
	}
	public ColorPoint LABinterpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LAB0 = MyColorUtils.RGBtoLAB(R0, G0, B0), LAB1 = MyColorUtils.RGBtoLAB(R1, G1, B1);
		return LABtoRGBPoint((1.f-s)*LAB0[0]+s*LAB1[0],(1.f-s)*LAB0[1]+s*LAB1[1],(1.f-s)*LAB0[2]+s*LAB1[2]); 
	} 

	public ColorPoint LCHinterpPt(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LCH0 = MyColorUtils.RGBtoLCH(R0, G0, B0), LCH1 = MyColorUtils.RGBtoLCH(R1, G1, B1);
		double d = LCH1[2] - LCH0[2];
		d = d > 180 ? d - 360 : d;
		return LCHtoRGBPoint((1.f-s)*LCH0[0]+s*LCH1[0],(1.f-s)*LCH0[1]+s*LCH1[1], LCH0[2]+s*d ); 
	  } 

	public int RGBramp(myPoint p1, myPoint p2, float s) {return RGBramp((int)p1.x, (int)p1.z, (int)p1.y,(int)p2.x, (int)p2.z, (int)p2.y,s); }
	public int XYZramp(myPoint p1, myPoint p2, float s) {return XYZramp((int)p1.x, (int)p1.z, (int)p1.y,(int)p2.x, (int)p2.z, (int)p2.y,s); }
	public int LABramp(myPoint p1, myPoint p2, float s) {return LABramp((int)p1.x, (int)p1.z, (int)p1.y,(int)p2.x, (int)p2.z, (int)p2.y,s); }
	public int LCHramp(myPoint p1, myPoint p2, float s) {return LCHramp((int)p1.x, (int)p1.z, (int)p1.y,(int)p2.x, (int)p2.z, (int)p2.y,s); }

	public int RGBramp(int R0, int G0, int B0, int R1, int G1, int B1, float s) {return ri.getClrAsHex((int)((1.f-s)*R0+s*R1),(int)((1.f-s)*G0+s*G1),(int)((1.f-s)*B0+s*B1)); }
	public int XYZramp(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] XYZ0 = MyColorUtils.RGBtoXYZ(R0, G0, B0), XYZ1 = MyColorUtils.RGBtoXYZ(R1, G1, B1);
		return MyColorUtils.XYZtoHexColor((1.f-s)*XYZ0[0]+s*XYZ1[0],(1.f-s)*XYZ0[1]+s*XYZ1[1],(1.f-s)*XYZ0[2]+s*XYZ1[2]); 
	}
	public int LABramp(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LAB0 = MyColorUtils.RGBtoLAB(R0, G0, B0), LAB1 = MyColorUtils.RGBtoLAB(R1, G1, B1);
		return MyColorUtils.LABtoHexColor((1.f-s)*LAB0[0]+s*LAB1[0],(1.f-s)*LAB0[1]+s*LAB1[1],(1.f-s)*LAB0[2]+s*LAB1[2]); 
	} 

	public int LCHramp(int R0, int G0, int B0, int R1, int G1, int B1, float s) {
		double[] LCH0 = MyColorUtils.RGBtoLCH(R0, G0, B0), LCH1 = MyColorUtils.RGBtoLCH(R1, G1, B1);
		double d = LCH1[2] - LCH0[2];
		d = d > 180 ? d - 360 : d;
		return MyColorUtils.LCHtoHexColor((1.f-s)*LCH0[0]+s*LCH1[0],(1.f-s)*LCH0[1]+s*LCH1[1], LCH0[2]+s*d ); 
	} 
	
	public void showColor(int c, float r) {myPointf C = PofColorf(c); ri.setFill(c); ri.drawSphere(C,r,5);}
	public void showColor(myPoint p, float r){int c = ri.getClrAsHex(p); ri.setFill(c); ri.drawSphere(p,r,5);}
	
	public myPoint PofColor(int c) {return new myPoint(ri.getRed(c), ri.getGreen(c), ri.getBlue(c));}
	public myPointf PofColorf(int c) {return new myPointf(ri.getRed(c), ri.getGreen(c), ri.getBlue(c));}

	public void drawLabRamp(int c0, int c1) {
		int rc0 = ri.getRed(c0), rc1 = ri.getRed(c1), bc0=ri.getBlue(c0), bc1=ri.getBlue(c1), gc0=ri.getGreen(c0), gc1=ri.getGreen(c1);
		for(float s=0; s<=1; s+=0.01f) {showColor(LABramp( rc0, gc0, bc0,  rc1, gc1, bc1, s),5);}
	}
	
	public void drawRGBRamp(int c0, int c1) {
		int rc0 = ri.getRed(c0), rc1 = ri.getRed(c1), bc0=ri.getBlue(c0), bc1=ri.getBlue(c1), gc0=ri.getGreen(c0), gc1=ri.getGreen(c1);
		for(float s=0; s<=1; s+=0.01f) {showColor(RGBramp( rc0, gc0, bc0,  rc1, gc1, bc1, s),5);} 
	}
	
	// ***************** Paco's tools for drawing ramps
	public int[] rampColors(int r0, int g0, int b0, int r1, int g1, int b1, int r, String mode) {
		mode = mode.toLowerCase().trim();
		int[] colors = new int[r];
		//myPoint[] points = new myPoint[r];
		float step = 1.f/(r-1);
		if (mode.equals("rgb")) {for (int i=0; i<r; ++i) {colors[i] = RGBramp(r0,g0,b0,r1,g1,b1, i*step);}}
		else if (mode.equals("xyz")) {for (int i=0; i<r; ++i) {colors[i] = XYZramp(r0,g0,b0,r1,g1,b1, i*step);}}
		else if (mode.equals("lab")) {for (int i=0; i<r; ++i) {colors[i] = LABramp(r0,g0,b0,r1,g1,b1, i*step);}}
	  	else if (mode.equals("lch")) {for (int i=0; i<r; ++i) {colors[i] = LCHramp(r0,g0,b0,r1,g1,b1, i*step);}}
		return colors;
	}
	  
	public myPoint[] rampPoints(int[] colors) {
		myPoint[] points = new myPoint[colors.length];
		for (int i=0; i<colors.length; ++i) {points[i] = new myPoint(scaleVals[0]*ri.getRed(colors[i]), scaleVals[1]*ri.getBlue(colors[i]), scaleVals[2]*ri.getGreen(colors[i]));}
		return points;
	}
	
	public void drawRamp(int[] colors, myPoint[] points) {
		for (int i=0; i<colors.length; ++i) {ri.setFill(colors[i]); ri.showPtAsSphere(points[i],5,5, -1, -1);}
	}
	  
	
	// ***************** Paco's tools for showing iso-surface layers of different color spaces 
	public class Layer {
		int l, sample;
		myPoint[][] P;
		//hex color vals ARGB
		int[][] C;
	  
		public Layer(int l, int sample) {
			this.l=l; this.sample=sample;
			P = new myPoint[sample][sample];
			C = new int[sample][sample];
			calculate();
	    }
	    
		public void calculate() {
			int[] rgb = new int[3];
			for(int n=0; n<sample; ++n) {
				for(int m=0; m<sample; ++m) {
					float i=(float)n/sample, j=(float)m/sample;
					if(mode.equals("xyz")) {rgb=MyColorUtils.XYZtoRGB(100*i, l, 100*j);}
					else if(mode.equals("lab")) {rgb=MyColorUtils.LABtoRGB(l, 255*i-128, 255*j-128);}
					else if(mode.equals("lch")) {rgb=MyColorUtils.LCHtoRGB(l, 100*i, 360*j);}
					P[n][m] = new myPoint(300*rgb[0]/255,300*rgb[2]/255,300*rgb[1]/255);
					C[n][m] = ri.getClrAsHex(rgb[0], rgb[1], rgb[2]);
				}
			}
		}
	}//class Layer
	public void show(Layer l) {
		for(int i=0; i<l.sample; ++i) {for(int j=0; j<l.sample; ++j) {ri.setFill(l.C[i][j]); ri.showPtAsSphere(l.P[i][j], 5, 5, -1, -1);}}
		ri.setStrokeWt(1); ri.setStroke(0,0,0,255);
		for(int i=0; i<l.sample; ++i) {for(int j=0; j<l.sample-1; ++j) {ri.drawLine(l.P[i][j].x, l.P[i][j].y, l.P[i][j].z, l.P[i][j+1].x, l.P[i][j+1].y, l.P[i][j+1].z);}}
		for(int i=0; i<l.sample-1; ++i) {for(int j=0; j<l.sample; ++j) {ri.drawLine(l.P[i][j].x, l.P[i][j].y, l.P[i][j].z, l.P[i+1][j].x, l.P[i+1][j].y, l.P[i+1][j].z);}}
	}

}//class myClrRamp
