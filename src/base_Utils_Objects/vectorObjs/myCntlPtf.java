package base_Utils_Objects.vectorObjs;

import base_UI_Objects.my_procApplet;

public class myCntlPtf extends myPointf {
	public int ID;
	public static int IDincr = 0;
	public static final float maxR = 75, 
			minR = 1,
			baseRad = 20;			//default radius for control points
	public float r, w;				//weight is calculated based on the distance to neighboring cntl myPoints when cntl myPoints are drawn
	public static int[][] clr = new int[][]{{0,0,255,255}, {111,111,111,255}};
	private static int[] blackClr = new int[] {0,0,0,0};
	
	public myCntlPtf(myPointf _p, float _r, float _w){ super(_p.x,_p.y, _p.z);ID=IDincr++;r=_r; w=_w; }
	public myCntlPtf(myPointf _p, float _w){this( _p, baseRad, _w);}
	public myCntlPtf(myPointf _p){this( _p, baseRad, baseRad);}
	public myCntlPtf(){this(new myPointf(),1);}
	public myCntlPtf(myCntlPtf _p){this(new myPointf(_p),_p.w); r = _p.r; w = _p.w;ID = _p.ID;}		
	public static myCntlPt L(myCntlPt A, float s, myCntlPt B){	return new myCntlPt(new myPoint(A, s, B), capInterpR(A.r, s, B.r), (1-s)*A.w + (s)*B.w);}//(1-s)*A.r + (s)*B.r,
	public static myCntlPt P(myCntlPt A, myCntlPt B){	float s = .5f;return L(A, s, B);}
	public myPointf set(myPointf P){super.set(P); return (myPointf)this;}
	private static float capInterpR(float a, float s, float b){ float res = (1-s)*a + (s)*b; res = (res < minR ? minR : res > maxR ? maxR : res); return res;}
	public void drawMe(my_procApplet pa, int cIdx, boolean flat){	pa.setFill(clr[cIdx],clr[cIdx][3]);  pa.setStroke(clr[cIdx],clr[cIdx][3]);		((my_procApplet) pa).show(this,2,-1,-1, flat);}		
	public void drawRad(my_procApplet pa, int cIdx,myVectorf I, myVectorf J){
        pa.setFill(clr[cIdx],clr[cIdx][3]);  
        pa.setStroke(clr[cIdx],clr[cIdx][3]); 
        pa.drawCircle(this, r, I,J,20);
    }
	public void drawRad(my_procApplet pa, myVectorf I, myVectorf J){
        pa.drawCircle(this, r, I,J,20);
    }
	public void drawBall(my_procApplet pa, int cIdx,myVectorf I, myVectorf J) {
	    float rhalf = this.r*0.5f;
	    myPointf center1 = new myPointf(this);center1._add(myVectorf._mult(I,rhalf));
	    myPointf center2 = new myPointf(this);center2._add(myVectorf._mult(I,-rhalf));
	    pa.setFill(clr[cIdx],clr[cIdx][3]);  
	    pa.setStroke(blackClr,255); 
        pa.drawCircle(center1, rhalf, I,J,20);
        pa.drawCircle(center2, rhalf, I,J,20);
        center1.showMeSphere(pa, 1.0f);
        center2.showMeSphere(pa, 1.0f);
    }
	public void drawNorm(my_procApplet pa, int cIdx,myVectorf I, myVectorf J) {
	    myPointf p1 = new myPointf(this);p1._add(myVectorf._mult(I,r));
        myPointf p2 = new myPointf(this);p2._add(myVectorf._mult(I,-r));
        pa.setStroke(blackClr,255);
        pa.line(p1, p2); 
	}
	public void calcRadFromWeight(my_procApplet pa, float lenRatio, boolean inv){r = Math.min(maxR, Math.max(minR, baseRad * (inv ? (lenRatio/w) : (pa.wScale*w/(lenRatio*lenRatio)))));  }
	public void modRad(float modAmt){float oldR = r; r += modAmt; r = (r < minR ? minR : r > maxR ? maxR : r); w *= oldR/r; }
	public String toString(){String res = "Cntl Pt_f ID:"+ID+" p:"+super.toString()+" r:"+r+" w:"+w;return res;}
}
