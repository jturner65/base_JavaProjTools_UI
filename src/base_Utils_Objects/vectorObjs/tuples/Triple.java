package base_Utils_Objects.vectorObjs.tuples;

//only works for comparables
public class Triple<X,Y,Z> implements Comparable<Triple<X,Y,Z>> { 
	//private static final int[] seeds = new int[] {7919, 1699, 5659};
	public final X x;    public final Y y; public final Z z; private final Float sqmag;
	public Triple(X x, Y y, Z z) {    this.x = x;   this.y = y; this.z = z; sqmag = calcSqMag(); }
	public Triple(Triple<X,Y,Z> _t) {    this( _t.x,_t.y, _t.z);  }
	public String toString() {      return "(" + x + "," + y +"," + z + ")";  }
	@SuppressWarnings("unchecked")
	public boolean equals(Object _o) {  if (_o == null) {return false;} if (_o == this) { return true; } if (!(_o instanceof Triple)){ return false; } Triple<X,Y,Z> o = (Triple<X,Y,Z>) _o;  return o.x.equals(this.x) && o.y.equals(this.y)&& o.z.equals(this.z);  }
	public int hashCode() { int result = 97 + ((x == null) ? 0 : x.hashCode()); result = 97 * result + ((y == null) ? 0 : y.hashCode()); return 97 * result + ((z == null) ? 0 : z.hashCode()); }
	public Float getSqMag() {return sqmag;}
	private Float calcSqMag(){if((x != null) && (y != null) && (z!= null)) { return 1.0f*((x.hashCode()*x.hashCode()) + (y.hashCode()*y.hashCode()) + (z.hashCode()*z.hashCode()));} else {return null;}}
	@Override
	public int compareTo(Triple<X,Y,Z> arg0) {
		return (this.hashCode() > arg0.hashCode() ? 1 : (this.hashCode() < arg0.hashCode() ? -1 : (this.equals(arg0) ? 0 : 1)));
	}
}//Triple<X,Y,Z>