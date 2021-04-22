package charlesgunn.jreality.geometry;

import java.awt.geom.GeneralPath;

import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class BilliardsTable {
	double globalSpeed = 1.0, curvature = .05;
	double[] initialPosition = {0,0,1};
	double[] initialVelocity = {1,1, 0};
	double[] initialAcceleration = {0,0,0}; //.01,-.01,0};
	double[] currentPosition, currentVelocity, currentAcceleration = initialAcceleration;
	double[][] lines;
	double[][] reflectionMatrices = new double[4][];
	double initialPoint[];
	GeneralPath ingr0;
	private double[][] corners;
	
	public BilliardsTable(double[] ip, double[] iv, double[][] polygon)	{
		currentPosition = ip;
		currentVelocity = iv;
		System.arraycopy(ip, 0, initialPosition, 0, 3);
		System.arraycopy(iv, 0, initialVelocity, 0, 3);
		setTable(polygon);
	}
	
	public void updateLines(){
	    // first update the corner array
		if (corners == null) throw new IllegalStateException("No table");
	    int n = corners.length;
	    lines = new double[n][3];
		int i = 0;
	    for (i = 0; i<n; ++i)	{
	        Rn.crossProduct(lines[i], corners[i], corners[(i+1)%n]);
	        reflectionMatrices[i] = reflectionInLine(lines[i]);
	    }
	}
	double t = 0, dt = .01;
	public void update(){
		t+=dt;
		double x = currentPosition[0], y = currentPosition[1];
	    double[] pt0 = new double[3]; 
	    pt0[0] = x;  pt0[1] = y; pt0[2] = 1.0;
	    currentVelocity[0] += globalSpeed*currentAcceleration[0];
	    currentVelocity[1] += globalSpeed*currentAcceleration[1];
	    x += globalSpeed*currentVelocity[0];
	    y += globalSpeed*currentVelocity[1]; 
	    double s = Math.sin(t) * curvature;
	    double f = Rn.euclideanNorm(currentVelocity);
	    currentVelocity[0] += -s * currentVelocity[1];
	    currentVelocity[1] += s * currentVelocity[0];
	    Rn.setToLength(currentVelocity, currentVelocity, f);
	    double[] pt1 = {x,y,1};
	    int n = lines.length;
	    boolean foundSegment = false;
	    do {
	     foundSegment = false;
	     for (int i = 0; i<n; ++i){
	        double dot = Rn.innerProduct(lines[i], pt1); 
//	        System.err.println("Dot is "+dot);
	        // the moving point crossed the ith line
	        if (dot < 0) {   
	           double[] line = P2.lineFromPoints(null, pt0, pt1);
	           // see if it crosses the ith line SEGMENT   
	           double dot0 = Rn.innerProduct(corners[i], line);   
	           double dot1 = Rn.innerProduct(corners[(i+1)%n], line);
	           if (dot0 * dot1 <= 0.0)  {   
	               double[] cept = P2.pointFromLines(null, lines[i], line);  
	               Pn.dehomogenize(cept, cept);
	               Rn.matrixTimesVector(currentAcceleration, reflectionMatrices[i], currentAcceleration);
	               Rn.matrixTimesVector(currentVelocity, reflectionMatrices[i], currentVelocity);
	               Rn.matrixTimesVector(pt1, reflectionMatrices[i], pt1);
//	               ingr0.lineTo( (float)cept[0], (float)cept[1]); 
	               foundSegment=true;
	          }
	        }
	       }
	     } while (foundSegment);
	    System.arraycopy(pt1, 0, currentPosition, 0, 3);
	}
	 
	public double[] reflectionInLine(double[] line){ 
	     double[] matrix = new double[9]; 
	     double[] direction = new double[3]; 
	     direction[0] = line[0]; 
	     direction[1] = line[1];   
	     direction[2] = 0.0; 
	     double f = 1.0/Rn.innerProduct(direction,direction); 
	     for (int i = 0; i<3; ++i)  {    
	         for (int j = 0; j<3; ++j) {
	              matrix[3*i+j] = (i==j? 1 : 0) - 2 * f * direction[i] * line[j];
	         }
	     }
	     return matrix;
	 
	}

	public void setTable(double[][] t) {
		corners = t;
		updateLines();
		
	}

	public double[] getInitialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(double[] initialPosition) {
		this.initialPosition = initialPosition;
	}

	public double[] getInitialVelocity() {
		return initialVelocity;
	}

	public void setInitialVelocity(double[] initialVelocity) {
		this.initialVelocity = initialVelocity;
	}

	public double getGlobalSpeed() {
		return globalSpeed;
	}

	public void setGlobalSpeed(double globalSpeed) {
		this.globalSpeed = globalSpeed;
	}

	public double getCurvature() {
		return curvature;
	}

	public void setCurvature(double curvature) {
		this.curvature = curvature;
	}
}
