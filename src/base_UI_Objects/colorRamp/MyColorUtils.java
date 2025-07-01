package base_UI_Objects.colorRamp;

public class MyColorUtils {

    // FROM: http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
    public static final double[] D65 = {95.0429f, 100.0f, 108.8900f};
    public static final double[] whitePoint = D65;    
    public static final double[][] Mi  = {{ 3.2406f, -1.5372f, -0.4986f},
                             {-0.9689f,  1.8758f,  0.0415f},
                             { 0.0557f, -0.2040f,  1.0570f}};
    public static final double[][] M   = {{0.4124f, 0.3576f,  0.1805f},
                             {0.2126f, 0.7152f,  0.0722f},
                             {0.0193f, 0.1192f,  0.9505f}};
    
    private MyColorUtils() {}
    
    
    /**
     * Returns ARGB hex value of passed color values. Assumes r,g,b are all 0-255 range (forces alpha to 255)
     * Mod 256 is performed on all values, so all rgb values should be [0,255]
     */
    private static int getClrAsHex(int r, int g, int b) {
        return 0xFF000000 + (r & 0xFF)<<16 + (g & 0xFF) << 8 + (b & 0xFF);
    }
    
    /**
     * Converts LAB color to RGB hex value
     * @param L
     * @param a
     * @param b
     * @return
     */
    public static int LABtoHexColor(double L, double a, double b) {int [] C= LABtoRGB(L,a,b); return getClrAsHex(C[0],C[1],C[2]);}  
    
    /**
     * Converts LAB color to RGB by first converting to XYZ
     * @param L
     * @param a
     * @param b
     * @return
     */
    public static int[] LABtoRGB(double L, double a, double b) {return XYZtoRGB(LABtoXYZ(L, a, b));}
    
    /**
     * Converts LAB color to XYZ
     * @param LAB
     * @return
     */
    public static double[] LABtoXYZ(double[] LAB) {return LABtoXYZ(LAB[0], LAB[1], LAB[2]);}
    
    /**
     * Converts LAB color to XYZ
     * @param L
     * @param a
     * @param b
     * @return
     */
    public static double[] LABtoXYZ(double L, double a, double b) {
        double[] result = new double[3];
        double y = (L + 16.0f) / 116.0f, y3 = Math.pow(y, 3.0f);
        double x = (a / 500.0f) + y, x3 = Math.pow(x, 3.0f);
        double z = y - (b / 200.0f), z3 = Math.pow(z, 3.0f);
        if (y3 > 0.008856f) y = y3; else y = (y - (16.0f / 116.0f)) / 7.787f;
        if (x3 > 0.008856f) x = x3; else x = (x - (16.0f / 116.0f)) / 7.787f;
        if (z3 > 0.008856f) z = z3; else z = (z - (16.0f / 116.0f)) / 7.787f;
        result[0] = x * whitePoint[0];
        result[1] = y * whitePoint[1];
        result[2] = z * whitePoint[2];
        return result;
    }
    
    /**
     * Converts LAB to LCH colorspace (from http://www.brucelindbloom.com/index.html?Equations.html)
     * @param LAB
     * @return
     */
    public static double[] LABtoLCH(double[] LAB) {return LABtoLCH(LAB[0], LAB[1], LAB[2]);}
    /**
     * Converts LAB to LCH colorspace (from http://www.brucelindbloom.com/index.html?Equations.html)
     * @param L
     * @param a
     * @param b
     * @return
     */
    public static double[] LABtoLCH(double L, double a, double b) {
        double[] result = new double[3];
        double h = Math.atan2(b, a);
        // convert radians to degrees
        if (h > 0) h = Math.toDegrees(h);
        else if (h < 0) h = 360 - Math.toDegrees(Math.abs(h));
        if (h < 0) h += 360;
        else if (h >= 360) h -= 360;
        result[0] = L;
        result[1] = Math.sqrt(a*a + b*b);
        result[2] = h;
        return result;
    }
    
    /**
     * Converts LCH color to RGB Hex color
     * @param L
     * @param c
     * @param h
     * @return
     */
    public static int LCHtoHexColor(double L, double c, double h) {int [] C= LCHtoRGB(L,c,h); return getClrAsHex(C[0],C[1],C[2]);}
    
    // LCH > RGB = (LCH > LAB) + (LAB > XYZ) + (XYZ > RGB)
    /**
     * Converts LCH color to RGB colorspace by first converting LCH to LAB, then LAB to XYZ and finally XYZ to RGB
     * @param L
     * @param c
     * @param h
     * @return
     */
    public static int[] LCHtoRGB(double L, double c, double h) {return XYZtoRGB(LABtoXYZ(LCHtoLAB(L, c, h)));}
    
    // LCH > LAB 
    /**
     * Converts LCH color to LAB colorspace (from http://www.brucelindbloom.com/index.html?Equations.html)
     * @param L
     * @param c
     * @param h
     * @return
     */
    public static double[] LCHtoLAB(double L, double c, double h) {
        h = Math.toRadians(h);
        return new double[] {L,c * Math.cos(h), c * Math.sin(h)};
    }
    
    // XYZ to color
    /**
     * 
     * @param X
     * @param Y
     * @param Z
     * @return
     */
    public static int XYZtoHexColor(double X, double Y, double Z) {int [] C= XYZtoRGB(X,Y,Z); return getClrAsHex(C[0],C[1],C[2]);}
    
    // XYZ > RGB  
    /**
     * Remap XYZ color coefficient to RGB colorspace
     * @param c
     * @return
     */
    private static int calcXYZtoRGB(double c) {if (c>0.0031308f) {c=((1.055f*Math.pow(c,1.0f/2.4f))-0.055f);} else {c=(c*12.92f);}  return (int)((c < 0) ? 0 : ((c > 1) ? 1 : c));}
    /**
     * Convert XYZ to RGB colorspace
     * @param XYZ
     * @return
     */
    public static int[] XYZtoRGB(double[] XYZ) {return XYZtoRGB(XYZ[0], XYZ[1], XYZ[2]);}
    /**
     * Converts XYZ to RGB colorspace
     * @param X
     * @param Y
     * @param Z
     * @return
     */
    public static int[] XYZtoRGB(double X, double Y, double Z) {
        double x = X / 100.0f;
        double y = Y / 100.0f;
        double z = Z / 100.0f;
        // [r g b] = [X Y Z][Mi]
        double r = (x * Mi[0][0]) + (y * Mi[0][1]) + (z * Mi[0][2]);
        double g = (x * Mi[1][0]) + (y * Mi[1][1]) + (z * Mi[1][2]);
        double b = (x * Mi[2][0]) + (y * Mi[2][1]) + (z * Mi[2][2]);
        return new int[] {calcXYZtoRGB(r),calcXYZtoRGB(g),calcXYZtoRGB(b)};
    }
     
        
    // RGB > LAB = (RGB > XYZ) + (XYZ > LAB)
    /**
     * Convert RGB to LAB colorspace by first converting to XYZ and then converting XYZ to LAB
     * @param R
     * @param G
     * @param B
     * @return
     */
    public static double[] RGBtoLAB(int R, int G, int B) {return XYZtoLAB(RGBtoXYZ(R, G, B));}
    
    // RGB > LCH = (RGB > XYZ) + (XYZ > LAB) + (LAB > LCH)
    /**
     * Convert RGB to LCH colorspace by 
     * @param R
     * @param G
     * @param B
     * @return
     */
    public static double[] RGBtoLCH(int R, int G, int B) {return LABtoLCH(XYZtoLAB(RGBtoXYZ(R, G, B)));}
    
    /**
     * Remap RGB color coefficient to XYZ space (assumes sRGB)
     * @param C
     * @return
     */
    private static double calcRGBtoXYZ(int C) {double c = C/255.0f;if (c <= 0.04045f) {c = c/12.92f;} else {c = Math.pow(((c+0.055f)/1.055f), 2.4f);}return c*100.0;}
    /**
     * Convert RGB to XYZ color (assumes sRGB)
     * @param R
     * @param G
     * @param B
     * @return
     */
    public static double[] RGBtoXYZ(int R, int G, int B) {
        double r = calcRGBtoXYZ(R);
        double g = calcRGBtoXYZ(G);
        double b = calcRGBtoXYZ(B);
//        // assume sRGB
        // [X Y Z] = [r g b][M]
        return new double[] {(r * M[0][0]) + (g * M[0][1]) + (b * M[0][2]),(r * M[1][0]) + (g * M[1][1]) + (b * M[1][2]),(r * M[2][0]) + (g * M[2][1]) + (b * M[2][2]) };
    }
    
    private static final double XYZtoLABScale = (16.0f / 116.0f);
    /**
     * Remap XYZ color coefficient to LAB colorspace
     * @param C
     * @param idx
     * @return
     */
    private static double calcXYZToLAB(double C, int idx) {double c = C/whitePoint[idx]; if (c > 0.008856f) c = Math.pow(c, 1.0f / 3.0f); else c = (7.787f * c) + XYZtoLABScale; return c;}
    
    /**
     * Convert XYZ to LAB color
     * @param XYZ
     * @return
     */
    public static double[] XYZtoLAB(double[] XYZ) {return XYZtoLAB(XYZ[0], XYZ[1], XYZ[2]);}
    
    /**
     * Convert XYZ to LAB color
     * @param X
     * @param Y
     * @param Z
     * @return
     */
    public static double[] XYZtoLAB(double X, double Y, double Z) {        
        double x = calcXYZToLAB(X, 0);
        double y = calcXYZToLAB(Y, 1);
        double z = calcXYZToLAB(Z, 2);        
        return new double[] {(116.0f * y) - 16.0f,500.0f * (x - y),200.0f * (y - z)};
    }
    

}//class ColorUtils
