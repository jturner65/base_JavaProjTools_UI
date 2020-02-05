package base_Utils_Objects.vectorObjs;
//floating point matrix stack - use float based matrices for potential speedup/footprint
public class myMatStackf {

	public myMatrixf[] s;
	public int top;
	 
	public myMatStackf(int matStackMaxHeight){
		s = new myMatrixf[matStackMaxHeight];
		for (int row = 0; row < 10; ++row){s[row] = new myMatrixf();}  	
		top = 0;        //point top of stack at index of base matrix
	}//stack constructor	
	public void initStackLocation(int idx){  this.initStackLocation(idx, false);}	
	public void initStackLocation(int idx, boolean clear){  s[idx].initMat(clear);}	
	//add the current top of the matrix stack to the matrix stack in a higher position
	public void push(){ ++top; initStackLocation(top);  for (int row = 0; row < 4; ++row){ for (int col = 0; col < 4; ++col){  s[top].m[row][col] = s[top - 1].m[row][col]; }}}//push     	
	//replace the current top of the matrix stack with a new matrix
	public void replaceTop(myMatrixf newTopMatrix){for (int row = 0; row < 4; ++row){ for (int col = 0; col < 4; ++col){ s[top].m[row][col] = newTopMatrix.m[row][col]; }}}//replaceTop	
	//return the top of the matrix stack without popping
	public myMatrixf peek(){ return s[top].clone();	}//peek	
	//remove and return top matrix on stack
	public myMatrixf pop(){
		myMatrixf oldTop = new myMatrixf();
		oldTop = s[top].clone();
		if (top > 0) {	top--;		initStackLocation(top+1,true);    }  //reinitialize stack			
		else {		System.out.println("stack pop error");}
		return oldTop;
	}	
	//returns a string representation of this stack
	public String toString(){
		String result = "";
		for (int si = 0; si < top+1; si++){	result += "Stack[" + si + "] =\n[" + s[si].toString() + "]\n";}//for si
		return result;
	}//to String method
}//myMatStackf
