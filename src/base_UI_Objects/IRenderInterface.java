/**
 * 
 */
package base_UI_Objects;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * These are the functions that are expected to be found in a rendering class for proper rendering 
 * This interface is very much a work in progress - ultimately, everything that can be expected from
 * whatever rendering mechanism is being used should be referrenced here so all consuming code can be
 * implementation agnostic
 * @author john
 *
 */
public interface IRenderInterface {
	//vars in interface are automatically static/final
	public double 
		halfPi = .5*Math.PI,
		twoPi = 2.0*Math.PI,
		threeQtrPI = .75 * Math.PI,
		sqrt2 = Math.sqrt(2.0),
		invSqrt2 = .5 * sqrt2,
		sqrt3 = Math.sqrt(3.0),
		invSqrt3 = 1.0/sqrt3;

	public float 
		halfPi_f = (float) halfPi,
		twoPi_f = (float) twoPi,
		threeQtrPI_f = (float) threeQtrPI,
		sqrt2_f = (float) sqrt2,
		invSqrt2_f = (float) invSqrt2,
		sqrt3_f = (float) sqrt3,
		invSqrt3_f = (float) invSqrt3;	

	//added to support old color constant defs from old projects - should be an enum
	public int gui_rnd = -1;
	public int gui_Black 	= 0;
	public int gui_White 	= 1;	
	public int gui_Gray 	= 2;
	
	public int gui_Red 	= 3;
	public int gui_Blue 	= 4;
	public int gui_Green 	= 5;
	public int gui_Yellow 	= 6;
	public int gui_Cyan 	= 7;
	public int gui_Magenta = 8;
	
	public int gui_LightRed = 9;
	public int gui_LightBlue = 10;
	public int gui_LightGreen = 11;
	public int gui_LightYellow = 12;
	public int gui_LightCyan = 13;
	public int gui_LightMagenta = 14;
	public int gui_LightGray = 15;
	
	public int gui_DarkCyan = 16;
	public int gui_DarkYellow = 17;
	public int gui_DarkGreen = 18;
	public int gui_DarkBlue = 19;
	public int gui_DarkRed = 20;
	public int gui_DarkGray = 21;
	public int gui_DarkMagenta = 22;
	
	public int gui_FaintGray = 23;
	public int gui_FaintRed = 24;
	public int gui_FaintBlue = 25;
	public int gui_FaintGreen = 26;
	public int gui_FaintYellow = 27;
	public int gui_FaintCyan = 28;
	public int gui_FaintMagenta = 29;
	
	public int gui_TransBlack = 30;
	public int gui_TransGray = 31;
	public int gui_TransMagenta = 32;	
	public int gui_TransLtGray = 33;
	public int gui_TransRed = 34;
	public int gui_TransBlue = 35;
	public int gui_TransGreen = 36;
	public int gui_TransYellow = 37;
	public int gui_TransCyan = 38;	
	public int gui_TransWhite = 39;	
	public int gui_OffWhite = 40;
	/**
	 * index of color definition next after static IRenderInterface defs
	 */
	public int gui_nextColorIDX = 41;
	
	//max ratio of width to height to use for windows
	public float maxWinRatio =  1.77777778f;

	
	///////////////////////
	// required methods
	
	/////////////////////
	// transformation stack and transformations
	
	/**
	 * push gl transformation matrix onto trans stack
	 */
	public void pushMatrix();
	/**
	 * pop gl transformation matrix off of trans stack
	 */
	public void popMatrix();
	/**
	 * push current style params onto "style stack" (save current settings)
	 */	
	public void pushStyle();
	/**
	 * pop current style params from "style stack" (restore/overwrite with last saved settings)
	 */		
	public void popStyle();
	
	/**
	 * translate by the given values
	 * @param x,y,z
	 */
	public void translate(float x, float y, float z);
	public void translate(double x, double y, double z);	
	public void translate(myPoint p);
	public void translate(myPointf p);
	
	/**
	 * rotate by given theta around specified axis 
	 * @param thet
	 * @param axis must be normalized
	 */
	public void rotate(float thet, myPoint axis);
	/**
	 * rotate by given theta around specified axis 
	 * @param thet
	 * @param axis must be normalized
	 */
	public void rotate(float thet, myPointf axis);
	/**
	 * rotate by given theta around specified axis 
	 * @param thet
	 * @param x,y,z must be normalized
	 */
	public void rotate(float thet, double x, double y, double z);
	
	/**
	 * set fill color
	 * @param clr 1st 3 values denot integer color vals
	 * @param alpha 
	 */
	public void setFill(int[] clr, int alpha);
	/**
	 * set stroke color
	 * @param clr 1st 3 values denot integer color vals
	 * @param alpha 
	 */
	public void setStroke(int[] clr, int alpha);
	
	/**
	 * tell current drawing cycle that there should be no stroke 
	 */
	public void noStroke();
	
	/**
	 * set stroke weight
	 */
	public void setStrokeWt(float stW);

	 /**
	  * changes normal for smooth shading
	  * @param V
	  */
	public void gl_normal(myVector V);          
	/**
	 * vertex for shading or drawing
	 * @param P
	 */
	public void gl_vertex(myPoint P) ;   
	/**
	 * changes normal for smooth shading - float
	 * @param V
	 */
	public void gl_normal(myVectorf V) ;   
	/**
	 * vertex for shading or drawing - float
	 * @param P
	 */
	public void gl_vertex(myPointf P);
	/**
	 * set the detail of drawn spheres using passed detail value
	 * @param det
	 */
	public void setSphereDetail(int det);
	/**
	 * draw a sphere of specified radius
	 * @param rad 
	 */
	public void drawSphere(float rad);
	
	/**
	 * draw a 2 d ellipse 
	 * @param a 4 element array : x,y,x rad, y rad
	 */
	public void drawEllipse(float[] a);
	/**
	 * draw a circle centered at P with specified radius r in plane proscribed by passed axes using n number of points
	 * @param P center
	 * @param r radius
	 * @param I x axis
	 * @param J y axis
	 * @param n # of points to use
	 */
	public void drawCircle(myPoint P, float r, myVector I, myVector J, int n);
	
	/**
	 * draw a circle centered at P with specified radius r in plane proscribed by passed axes using n number of points
	 * @param P center
	 * @param r radius
	 * @param I x axis
	 * @param J y axis
	 * @param n # of points to use
	 */
	public void drawCircle(myPointf P, float r, myVectorf I, myVectorf J, int n);
	
	/**
	 * draw a rectangle in 2D using the passed values as x,y,w,h
	 * @param a 4 element array : x,y,w,h
	 */
	public void drawRect(float[] a);
	
	/**
	 * this will translate the passed box dimensions to keep them on the screen
	 * using p as start point and rectDims[2] and rectDims[3] as width and height
	 * @param P starting point
	 * @param rectDims box dimensions 
	 */
	public void transToStayOnScreen(myPointf P, float[] rectDims);
	
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param alpha alpha value for how dark/faint axes should be
	 * @param centered whether axis should be centered at ctr or just in positive direction at ctr
	 */
	public void drawAxes(double len, float stW, myPoint ctr, int alpha, boolean centered);
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param _axis : orientation
	 * @param alpha
	 * @param drawVerts
	 */
	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int alpha, boolean drawVerts);//RGB -> XYZ axes
	
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param _axis : orientation
	 * @param clr
	 * @param drawVerts
	 */
	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts);
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param _axis : orientation
	 * @param alpha
	 */
	public void drawAxes(double len, double stW, myPoint ctr, myVectorf[] _axis, int alpha);
	
	/////////////////////////
	// display objects
	
	////////////////////////
	// lines
	/**
	 * draw line with given end points in 3d, represented as float coords
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void line(float x1, float y1, float z1, float x2, float y2, float z2);
	/**
	 * draw line with given end points in 3d, represented as float coords
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void line(double x1, double y1, double z1, double x2, double y2, double z2);
	/**
	 * draw line with given end points in 3d, represented as 2 points
	 * @param p1
	 * @param p2
	 */
	public void line(myPoint p1, myPoint p2);
	/**
	 * draw line with given end points in 3d, represented as 2 points with floating point coords
	 * @param p1
	 * @param p2
	 */
	public void line(myPointf p1, myPointf p2);
	/**
	 * draw line with given end points in 3d, represented as 2 points with floating point coords, with start and end color represented as integer keys to color array
	 * @param a
	 * @param b
	 * @param stClr
	 * @param endClr
	 */
	public void line(myPointf a, myPointf b, int stClr, int endClr);
	/**
	 * draw line with given end points in 3d, represented as 2 points with floating point coords, with start and end color represented as integer arrays
	 * @param a
	 * @param b
	 * @param stClr
	 * @param endClr
	 */
	public void line(myPointf a, myPointf b, int[] stClr, int[] endClr);

	
	////////////////////
	// points - show functions need to be rethought TODO
	/**
	 * show a point, either as flat circle or as a sphere
	 * @param P
	 * @param r
	 * @param fclr
	 * @param sclr
	 * @param flat
	 */
	public void show(myPoint P, double r,int fclr, int sclr, boolean flat);
	/**
	 * show a point, either as flat circle or as a sphere
	 * @param P
	 * @param r
	 * @param fclr
	 * @param sclr
	 * @param flat
	 */
	public void show(myPointf P, double r,int fclr, int sclr, boolean flat);
	/**
	 * show a point, either as flat circle or as a sphere
	 * @param P
	 * @param r
	 * @param fclr
	 * @param sclr
	 * @param flat
	 */
	public void show(myPoint P, double r,int[] fclr, int[] sclr, boolean flat);
	/**
	 * show a point, either as flat circle or as a sphere
	 * @param P
	 * @param r
	 * @param fclr
	 * @param sclr
	 * @param flat
	 */
	public void show(myPointf P, double r,int[] fclr, int[] sclr, boolean flat);
	/**
	 * render this point as a black sphere in 3d
	 * @param pa : render interface capable of drawing this point
	 * @param r : radius of resultant sphere
	 */
	public void showPtAsSphere(myPoint p, float r) ;
	/**
	 * render this point as a black sphere in 3d
	 * @param pa : render interface capable of drawing this point
	 * @param r : radius of resultant sphere
	 */
	public void showPtAsSphere(myPointf p, float r) ;

	
	//////////////////////
	// display text
	/**
	 * print out multiple-line text to screen
	 * @param str text to show
	 * @param x x displacement of text
	 * @param y y displacement of text
	 */
	public void ml_text(String str, float x, float y);	
	
	/**
	 * print out a string ara to screen with perLine # of strings per line
	 * @param sAra array of strings
	 * @param perLine # of strings per line to display to screen
	 */
	public void outStr2ScrAra(String[] sAra, int perLine);
	/**
	 * print out a string to screen
	 * @param str string to display
	 */	
	public void outStr2Scr(String str);
	/**
	 * print informational string data to console, and to screen
	 * @param str
	 * @param showDraw whether to show in graphical window as well as console
	 */
	public void outStr2Scr(String str, boolean showDraw);
	
	//TODO put all functions commonly used from myDispWindow and its inheritors in here to support different rendering engines

}//IRenderInterface
