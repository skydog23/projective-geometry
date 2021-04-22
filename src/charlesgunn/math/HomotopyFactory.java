package charlesgunn.math;

import java.util.LinkedList;

import de.jreality.math.Rn;

public class HomotopyFactory {
	
	double[][][] curves = null;
	double[] times = null;
	double[][] interpolatedCurve = null;
	double[] radia;
	double interpolatedRadius;
	double currentTime = 0.0;
	boolean hasChanged = true;
	
	
	public HomotopyFactory()	{
		super();
	}

	
	/**
	 * requires: curves[i].length = constant
	 */
	public void setCurves(double[][][] curves) {
		//validate input
		if (curves != null) {
			final int vertexCount = curves[0].length;
			for (int i=0; i<curves.length; i++)
				if (curves[i].length != vertexCount)
					throw new IllegalArgumentException("All curves must have same length!");
		}
		
		this.curves = curves;
		hasChanged = true;
	}

	public double[][][] getCurves() {
		return curves;
	}
	
	public void setRadia(double[] radia){
		if(radia.length!=curves.length){
			throw new IllegalArgumentException("Numbers of curves and associated radia mustbe the same!");
		}
		this.radia=radia;
		interpolatedRadius=radia[0];
	}
	
	public double[] getRadia(){
		return radia;
	}

	
	/**
	 * requires: times.length = getCurves().length, 
	 * times[0] = 0.0 and times[times.length-1] = 1.0
	 */
	public void setTimes(double[] times) {
		//validate input
		if (times != null) {
			if (curves != null && times.length != curves.length)
				throw new IllegalArgumentException("Length of double[] is incorrect!");
			if (times[0] != 0.0 || times[times.length-1] != 1.0)
				throw new IllegalArgumentException("times[0] != 0.0 or times[times.length-1] != 1.0");
		}
		this.times = times;
		hasChanged = true;
	}
	
	public double[] getTimes() {
		return times;
	}

	
	/**
	 * requires: 0.0 <= currentTime <= 1.0
	 */
	public void setCurrentTime(double currentTime) {
		//validate input
		if (currentTime < 0.0 || currentTime > 1.0)
			throw new IllegalArgumentException("Time has to be in [0,1]!");
		
		this.currentTime = currentTime;
		hasChanged = true;
	}

	public double getCurrentTime() {
		return currentTime;
	}

	
	
	public void update() {
		if (hasChanged)	{
			if (curves==null) 
				throw new NullPointerException("No curves specified!");
			if (times==null) {  //linear distribution
				final int n = curves.length;
				times = new double[n];
				for (int i=0; i<n; i++) {
					times[i] = i*1.0/(n-1);
				}
			}
			hasChanged = false;
		}	
		//assume times[0] = 0.0 and times[times.length-1] = 1.0
		//determine limiting time values
		int upper;
		for (upper=0; upper<times.length; upper++)
			if ( times[upper] >= currentTime ) break;
		if (times[upper] == currentTime) {  //don't need to interpolate
			interpolatedCurve =  curves[upper];
			interpolatedRadius=radia[upper];
			return;
		}

		//linear interpolation
		final int lower = upper-1;  //upper>=1
		interpolatedCurve = new double[curves[0].length][3];
		
		final double factor = (currentTime-times[lower])/(times[upper]-times[lower]);
		
		for (int i=0; i<interpolatedCurve.length; i++) {  //for all points
			double[] tmp = (double[])curves[lower][i].clone();
			Rn.add(tmp, curves[upper][i], Rn.negate(tmp, tmp));			
			Rn.times(tmp, factor, tmp);
			Rn.add(tmp, curves[lower][i], tmp);
			interpolatedCurve[i] = tmp;
		}
		if(normalize) normalizeLength(interpolatedCurve); 
		if(killDoubleVertices) killDoubleVertices();
		
		interpolatedRadius=radia[lower]+factor*(radia[upper]-radia[lower]);		
	}

	public double[][] getInterpolatedCurve() {
		update();  //update the curve
		return interpolatedCurve;
	}
	
	public double getInterpolatedRadius(){
		update();
		return interpolatedRadius;
	}
	
	boolean normalize=false;
	double curveLength=0;
	boolean isClosed=false;
	
	public void setCurveClosed(boolean curveIsClosed){
		isClosed=curveIsClosed;
	}	
	public boolean isClosed()	{
		return isClosed;
	}
	public void setNormalizedLength(double curveLength, boolean curveIsClosed){
		normalize=true;
		this.curveLength=curveLength;
		this.isClosed=curveIsClosed;
		if(curves.length>1){
			for(int i=1; i<curves.length;i++){	
				curves[i]=normalizeLength(curves[i],i);
			}
		}
		System.err.println("Curve length is "+curveLength);
	}
	public void setNormalizedLength(boolean curveIsClosed){
		this.isClosed=curveIsClosed;
		setNormalizedLength(getLength(curves[0], isClosed), isClosed);		
	}
	public void setNormalizedLength(){
		setNormalizedLength(getLength(curves[0], isClosed), isClosed);		
	}	
	public double[][] normalizeLength(double[][] curve){
		double oldLength=getLength(curve, isClosed);
		for(int i=0;i<curve.length;i++){
			Rn.times(curve[i],curveLength/oldLength,curve[i]);
		}
		return curve;
	}
	private double[][] normalizeLength(double[][] curve, int curveNumber){
		radia[curveNumber]=normalizeRadius(radia[curveNumber],curves[curveNumber]);
		return normalizeLength(curve);
	}	
	public double normalizeRadius(double radius, double[][] curve){
		return curveLength/getLength(curve, isClosed)*radius;
	}
	
	public static double getLength(double[][] curve, boolean isClosed){
		double length=0;
		for(int i=0;i<curve.length-1;i++){
			length=length+Rn.euclideanDistance(curve[i],curve[i+1]);
		}
		if(isClosed) length=length+Rn.euclideanDistance(curve[0],curve[curve.length-1]);
		return length;	
	}	
	public double getCurveLength(){
		return curveLength;
	}
	
	
	boolean killDoubleVertices=false;
	public void killDoubleVertices(){		
		killDoubleVertices=true;		
		LinkedList doubleIndicesList=new LinkedList();	
		int countIFrom=0;
		if(isClosed) countIFrom++;
		for(int n=0;n<curves.length;n++){
			for(int i=countIFrom;i<curves[0].length-1;i++){
				for(int j=i+1;j<curves[0].length;j++){
					if(Rn.euclideanDistanceSquared(curves[n][i],curves[n][j])<1E-10){
						if(!doubleIndicesList.contains(new Integer(i))){
							doubleIndicesList.add(new Integer(i));
						}
					}					
				}				
			}			
		}		
		double[][][] newCurves=new double[curves.length][curves[0].length-doubleIndicesList.size()][curves[0][0].length];
		int i=0;
		int j=0;
		for(int n=0;n<curves.length;n++){
			while(i<curves[0].length){
				if(!doubleIndicesList.contains(new Integer(i))){
					newCurves[n][j]=(double[])curves[n][i].clone();
					j++;					
				}//else{if(n==0) System.out.println("killed vertex number "+i+" in all curves");}		
				i++;				
			}	
			i=0;
			j=0;
		}
		curves=newCurves;	
		
		//System.out.println("updated curves[0].length="+curves[0].length);
	}

}
