package base_UI_Objects.windowUI;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;

/**
 * This class will manage all aspects of UI object creation, placement, rendering and interaction.
 * TODO
 * @author John Turner
 *
 */
public class Base_uiObjectManager {
	/**
	 * Used to render objects
	 */
	public static IRenderInterface pa;
	/**
	 * Owning window
	 */
	public final Base_DispWindow win;

	public Base_uiObjectManager(Base_DispWindow _win) {
		win = _win; pa = Base_DispWindow.ri;
	}

}//class uiObjectManager
