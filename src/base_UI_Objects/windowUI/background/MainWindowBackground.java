package base_UI_Objects.windowUI.background;

/**
 * Class to manage main display window background for an entire application.
 * Each display window should have 1+ options for background display, either an image projected onto a sphere or a color.
 */
public class MainWindowBackground {
    // Whether or not a particular window's background should be cleared on every draw.
    private boolean[] _clearBackground;
    
    public MainWindowBackground(int numDispWins) {
        _clearBackground = new boolean[numDispWins];
        // init setup
        for (int i =0;i<numDispWins;++i) {
            // all backgrounds should init to clearing every draw
            _clearBackground[i]=true;
        }
        
    }//ctor
    
    void setClearBkgOnDraw(int idx, boolean val) {_clearBackground[idx] = val;}
    boolean getClearBkgOnDraw(int idx) {return _clearBackground[idx];}

}// MainWindowBackground
