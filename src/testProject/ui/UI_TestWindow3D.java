package testProject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import testProject.uiData.UITestDataUpdater_3D;

public class UI_TestWindow3D extends Base_DispWindow {

	public UI_TestWindow3D(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx, int _flagIdx) {
		super(_p, _AppMgr, _winIdx, _flagIdx);
		super.initThisWin(false);
	}

	@Override
	protected UIDataUpdater buildUIDataUpdateObject() {
		return new UITestDataUpdater_3D(this);
	}

	@Override
	protected void updateCalcObjUIVals() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleDispFlagsDebugMode_Indiv(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handlePrivFlagsDebugMode_Indiv(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int[] getFlagIDXsToInitToTrue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setUI_IntValsCustom(int UIidx, int ival, int oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setUI_FloatValsCustom(int UIidx, float val, float oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawCustMenuObjs(float animTimeMod) {
		// TODO Auto-generated method stub

	}

	@Override
	public int initAllPrivBtns(ArrayList<Object[]> tmpBtnNamesArray) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleSideMenuMseOvrDispSel(int btn, boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleSideMenuDebugSelEnable(int btn) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleSideMenuDebugSelDisable(int btn) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void launchMenuBtnHndlr(int funcRow, int btn, String label) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String[] getSaveFileDirNamesPriv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initDrwnTrajIndiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addSScrToWinIndiv(int newWinKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addTrajToScrIndiv(int subScrKey, String newTrajKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void delSScrToWinIndiv(int idx) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void delTrajToScrIndiv(int subScrKey, String newTrajKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected myPoint getMsePtAs3DPt(myPoint mseLoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setVisScreenDimsPriv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean hndlMouseDragIndiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D,
			myVector mseDragInWorld, int mseBtn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void hndlMouseRelIndiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endShiftKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endAltKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endCntlKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setCustMenuBtnLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray,
			TreeMap<Integer, String[]> tmpListObjVals) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTrajIndiv(DrawnSimpleTraj drawnTraj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hndlFileLoad(File file, String[] vals, int[] stIdx) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<String> hndlFileSave(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initDispFlags() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void resizeMe(float scale) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void showMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void closeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean simMe(float modAmtSec) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void stopMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setCameraIndiv(float[] camVals) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawMe(float animTimeMod) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawRightSideInfoBarPriv(float modAmtMillis) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawOnScreenStuffPriv(float modAmtMillis) {
		// TODO Auto-generated method stub

	}

}
