package base_UI_Objects.windowUI.base;

import java.util.Arrays;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;

/**
 * Struct holding per-window object dims and colors, consumed by Base_DispWindow to create 
 * the window and also to determine its capabilities.
 * @author John Turner
 *
 */
public class GUI_AppWinVals {
	/**
	 * Window index to be created
	 */
	public final int winIdx;
	/**
	 * Window name
	 */
	public final String winName;
	/**
	 * Window description
	 */
	public final String winDescr;
	
	/**
	 * Window dimension when open - upper left x,y; width, height
	 */
	public float[] rectDim;
	/**
	 * Window dimension when closed - upper left x,y; width, height
	 */
	public float[] rectDimClosed;	
	/**
	 * Initial Camera values for window
	 */
	public final float[] initCameraVals;

	/**
	 * Window fill color
	 */
	public final int[] fillClr;
	/**
	 * Window stroke color
	 */
	public final int[] strkClr;
	
	/**
	 * Trajectory fill color for window
	 */
	public final int[] trajFillClr;
	/**
	 * Trajectory stroke color for window
	 */
	public final int[] trajStrkClr;	
	
	/**
	 * Fill colors for right side menu
	 */
	public final int[] rtSideFillClr;
	/**
	 * Stroke color for right side menu
	 */
	public final int[] rtSideStrkClr;

	/**
	 * Scene center value, for drawing
	 */
	public final myPointf sceneOriginVal;
	/**
	 * Initial focus point for camera
	 */
	public final myVectorf initSceneFocusVal;
	/**
	 * Background color for this window
	 */
	public int[] bGroundColor;
	/**
	 * Inverted background color for this window to be used as a canvas color
	 */	
	public int[] canvasColor;
	
	/**
	 * Flags describing the window's capabilities
	 */
	protected boolean[] dispWinFlags;
	/**
	 * idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 */
	protected static final int
				dispWinIs3dIDX 			= 0,
				dispCanDrawInWinIDX 	= 1,
				dispCanShow3dboxIDX 	= 2,
				dispCanMoveViewIDX 		= 3;
	protected static final int numDispWinBoolFlags = 4;
	
	/**
	 * Creates a struct holding a display window's necessary initialization values
	 * @param _winIdx the window's idx
	 * @param _strVals an array holding the window title(idx 0) and the window description(idx 1)
	 * @param _flags an array holding boolean values for idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 * @param _floatVals an array holding float arrays for 
	 * 				rectDimOpen(idx 0),
	 * 				rectDimClosed(idx 1),
	 * 				initCameraVals(idx 2)
	 * @param _intVals and array holding int arrays for
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4),
	 * 				rtSideStrkClr(idx 5)
	 * @param _sceneCenterVal center of scene, for drawing objects
	 * @param _initSceneFocusVal initial focus target for camera
	 */
	public GUI_AppWinVals(int _winIdx, String[] _strVals, boolean[] _flags, float[][] _floatVals, int[][] _intVals, myPointf _sceneCenterVal, myVectorf _initSceneFocusVal) {
		winIdx = _winIdx;
		winName = _strVals[0];
		winDescr = _strVals[1];
		
		dispWinFlags = new boolean[numDispWinBoolFlags];
		System.arraycopy(_flags, 0, dispWinFlags, 0, dispWinFlags.length);

		rectDim = new float[_floatVals[0].length];
		System.arraycopy(_floatVals[0], 0, rectDim, 0, rectDim.length);
		rectDimClosed = new float[_floatVals[1].length];
		System.arraycopy(_floatVals[1], 0, rectDimClosed, 0, rectDimClosed.length);
		initCameraVals = new float[_floatVals[2].length];
		System.arraycopy(_floatVals[2], 0, initCameraVals, 0, initCameraVals.length);
		
		fillClr = new int[_intVals[0].length];
		System.arraycopy(_intVals[0], 0, fillClr, 0, fillClr.length);
		strkClr = new int[_intVals[1].length];
		System.arraycopy(_intVals[1], 0, strkClr, 0, strkClr.length);
		trajFillClr = new int[_intVals[2].length];
		System.arraycopy(_intVals[2], 0, trajFillClr, 0, trajFillClr.length);
		trajStrkClr = new int[_intVals[3].length];		
		System.arraycopy(_intVals[3], 0, trajStrkClr, 0, trajStrkClr.length);
		if (_intVals.length> 4) {
			rtSideFillClr = new int[_intVals[4].length];
			System.arraycopy(_intVals[4], 0, rtSideFillClr, 0, rtSideFillClr.length);
			rtSideStrkClr = new int[_intVals[5].length];
			System.arraycopy(_intVals[5], 0, rtSideStrkClr, 0, rtSideStrkClr.length);
		} else {
			rtSideFillClr = new int[]{0,0,0,200};
			rtSideStrkClr = new int[]{255,255,255,255};
		}		
		sceneOriginVal = new myPointf(_sceneCenterVal);
		initSceneFocusVal = new myVectorf(_initSceneFocusVal);
		//background and canvas colors
		bGroundColor = new int[4];
		canvasColor = new int[4];
	}//ctor
		
	public final void setBackgrndColor(int[] _clr) {
		for (int i=0;i<_clr.length;++i) {
			bGroundColor[i] = _clr[i];
			canvasColor[i] = 255-_clr[i];
		}
		canvasColor[3] = 80;
	}//setBackgrndColor
			
	/**
	 * Window can be drawn in
	 * @return
	 */
	public final boolean canDrawInWin() {return dispWinFlags[dispCanDrawInWinIDX];}
	/**
	 * Window is 3D
	 * @return
	 */
	public final boolean dispWinIs3D() {return dispWinFlags[dispWinIs3dIDX];}
	/**
	 * Window should display wireframe box (supported for 3D only)
	 * @return
	 */
	public final boolean canShow3dbox() {return dispWinFlags[dispCanShow3dboxIDX];}
	/**
	 * Window accepts camera movement (supported for 3D only)
	 * @return
	 */
	public final boolean canMoveView() {return dispWinFlags[dispCanMoveViewIDX];}
	
	
	/**
	 * Set fill for the owning window
	 * @param ri
	 */
	public final void setWinFill(IRenderInterface ri) {ri.setFill(fillClr, fillClr[3]);}
	
	/**
	 * Set stroke for owning window
	 * @param ri
	 */
	public final void setWinStroke(IRenderInterface ri) {ri.setStroke(strkClr, strkClr[3]);}
	
	
	/**
	 * Set fill for the owning window using specified stroke color
	 * @param ri
	 */
	public final void setWinFillWithStroke(IRenderInterface ri) {ri.setFill(strkClr, strkClr[3]);}	
	
	/**
	 * Set fill and stroke for owning window
	 * @param ri
	 */
	public final void setWinFillAndStroke(IRenderInterface ri) {
		ri.setFill(fillClr, fillClr[3]);
		ri.setStroke(strkClr, strkClr[3]);
	}

	/**
	 * Check if passed x-y point is in rectDims
	 * @param x
	 * @param y
	 * @return
	 */
	public final boolean pointInRectDim(int x, int y){
		return ((x >= rectDim[0]) && (x <= rectDim[0] + rectDim[2])
				&&(y >= rectDim[1]) && (y <= rectDim[1] + rectDim[3]));	}
	
	/**
	 * Check if passed myPoint (x,y) is in rectDims
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean pointInRectDim(myPoint pt){
		return ((pt.x >= rectDim[0]) && (pt.x <= rectDim[0]+rectDim[2]) 
				&& (pt.y >= rectDim[1]) && (pt.y <=  rectDim[1]+rectDim[3]));}
	
	/**
	 * Check if passed myPointf (x,y) is in rectDims
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean pointInRectDim(myPointf pt){
		return ((pt.x >= rectDim[0]) && (pt.x <= rectDim[0]+rectDim[2]) 
				&& (pt.y >= rectDim[1]) && (pt.y <=  rectDim[1]+rectDim[3]));}
	
	
	/**
	 * Draw a rectangle using the specified rectDim values for this window
	 * @param ri
	 */
	public final void drawRectDim(IRenderInterface ri) {ri.drawRect(rectDim);}
	
	
	/**
	 * Draw a rectangle using the specified rectDimClosed values for this window
	 * @param ri
	 */
	public final void drawRectDimClosed(IRenderInterface ri) {ri.drawRect(rectDimClosed);}
	
	@Override
	public String toString() {
		String res = "IDX :"+winIdx+" | Name : `"+ winName + "` | Descr : `"+ winDescr + "`\n";
		res += "\tFill Color :"+Arrays.toString(fillClr) + " | Stroke Color :"+Arrays.toString(strkClr)+ "\n";
		res += "\tTraj Fill Color :"+Arrays.toString(trajFillClr) + " | Traj Stroke Color :"+Arrays.toString(trajStrkClr)+ "\n";
		res += "\tRt Side Fill Color :"+Arrays.toString(rtSideFillClr) + " | Rt Side Stroke Color :"+Arrays.toString(rtSideStrkClr)+ "\n";
		res += "\tIs 3D window :"+dispWinIs3D() + " | Can draw in window :"+canDrawInWin()+" | Shows 3D box :"+canShow3dbox() +" | Can Modify view :"+ canMoveView()+ "\n";
		res += "\t| Rect :("+String.format("%.2f",rectDim[0])+","+String.format("%.2f",rectDim[1])+","+String.format("%.2f",rectDim[2])+","+String.format("%.2f",rectDim[3])+")\n";	
		return res;	
	}
	
}//class GUI_AppWinVals
