package base_UI_Objects.windowUI;

import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.base.myDispWindow;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myVector;

public class myScrollBars{
	public my_procApplet pa;
	public myDispWindow win;
	public static int scrBarCnt = 0;
	public int ID;
	//displacement for scrolling display - x/y location of window, x/y zoom - use scrollbars&zoomVals - displacement is translate, zoom is scale
	public float[] scrollZoomDisp,
	//start x,y, width, height of scroll bar region 
	 	hScrlDims,	
	//start x,y, width, height of scroll bar region 
	 	vScrlDims;
	private final float thk = 20, thmult = 1.5f;				//scrollbar dim is 25 pxls wide	
	
	public float[][] arrowDims = new float[][]{{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	//hthumb x,y width,height,vthumb x,y width,height
	public float[][] thmbs = new float[][]{{0,0},{0,0}};
	//hthumb min/max, vthumb min/max bounds
	public float[][] thmbBnds = new float[][]{{0,0},{0,0}};
	
	public static final int
		upIDX = 0,
		dnIDX = 1,
		ltIDX = 2,
		rtIDX = 3,
		hThmbIDX = 0,
		vThmbIDX = 1;
	
	public int[][] clrs;			//colors for thumb and up/down/left/right arrows
	
	public myScrollBars(my_procApplet _pa,myDispWindow _win){
		pa = _pa;
		win = _win;
		ID = scrBarCnt++;
		setSize();
		clrs = new int[][]{ pa.getRndClr(),pa.getRndClr(),pa.getRndClr(),pa.getRndClr(),pa.getRndClr(),pa.getRndClr()};
	}//myScrollBars
	
	public void setSize(){
		float rectWidth = win.rectDim[0]+win.rectDim[2],vScrlStartY = win.rectDim[1]+(win.closeBox[3]),
				rectHeight = win.rectDim[3]-(vScrlStartY);
		vScrlDims = new float[]{rectWidth - thk,vScrlStartY, thk,  rectHeight-thk};
		hScrlDims = new float[]{win.rectDim[0], win.rectDim[1]+win.rectDim[3] - thk,win.rectDim[2]-thk,thk};

		thmbs[hThmbIDX] = new float[]{hScrlDims[0]+thk,hScrlDims[1],thmult*thk,thk};			//location/dims of thumb
		thmbBnds[hThmbIDX] = new float[]{thmbs[hThmbIDX][0],hScrlDims[2]-thmbs[hThmbIDX][2]-thk};		//min/max x val of horiz thumb
		
		thmbs[vThmbIDX] = new float[]{vScrlDims[0],(vScrlDims[1]+thk),thk,thmult*thk};			//location/dims of thumb
		thmbBnds[vThmbIDX] = new float[]{thmbs[vThmbIDX][1],vScrlDims[3]-thmbs[vThmbIDX][3]-thk};		//min/max of y val of vert thumb
		
		//arrow boxes - x,y,widht,height
		arrowDims = new float[][]{
			{vScrlDims[0],vScrlDims[1],thk,thk},			//up
			{vScrlDims[0],vScrlDims[1]+vScrlDims[3]-thk,thk,thk},		//down
			{hScrlDims[0],					hScrlDims[1],thk,thk},			//left
			{hScrlDims[0]+hScrlDims[2]-thk,hScrlDims[1],thk,thk}};		//right

	}
	public void drawMe(){
		pa.pushMatrix(); pa.pushStyle();
		pa.setColorValFill(IRenderInterface.gui_LightGray,255);
		pa.setColorValStroke(IRenderInterface.gui_Black,255);
		pa.strokeWeight(1.0f);
		pa.drawRect(vScrlDims);
		pa.drawRect(hScrlDims);
		for(int i =0; i<arrowDims.length;++i){
			pa.setFill(clrs[i],clrs[i][3]);
			pa.drawRect(arrowDims[i]);
		}
		pa.popStyle();pa.popMatrix();
		pa.pushMatrix(); pa.pushStyle();
		for(int i =0; i<thmbs.length;++i){
			pa.setFill(clrs[i + 4],clrs[i + 4][3]);
			pa.drawRect(thmbs[i]);
		}
		
		pa.popStyle();pa.popMatrix();
	}//drawMe
	
	
	public boolean msePtInRect(int x, int y, float[] r){return ((x > r[0])&&(x <= r[0]+r[2])&&(y > r[1])&&(y <= r[1]+r[3]));}		
	public boolean handleMouseClick(int mouseX, int mouseY, myPoint mouseClickIn3D){
		
		return false;	
	}//handleMouseClick
	public boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld){	
		
		return false;
	}//handleMouseDrag
	public void handleMouseRelease(){
		
		
	}//handleMouseRelease
	
}//myScrollBars