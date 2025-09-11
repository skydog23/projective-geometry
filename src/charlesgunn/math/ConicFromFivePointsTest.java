package charlesgunn.math;

import java.util.Arrays;

/**
 * Java translation of the conic section solver from conics.js
 * Solves for conic coefficients from 5 points using SVD approach
 */
public class ConicFromFivePointsTest {
    
    /**
     * Point class for homogeneous coordinates
     */
    public static class Point {
        public double x, y, w;
        
        public Point(double x, double y, double w) {
            this.x = x;
            this.y = y;
            this.w = w;
        }
        
        @Override
        public String toString() {
            return String.format("[%.4f:%.4f:%.4f]", x, y, w);
        }
    }
    
    /**
     * Conic coefficients result
     */
    public static class ConicCoefficients {
        public double a, h, b, g, f, c;
        
        public ConicCoefficients(double a, double h, double b, double g, double f, double c) {
            this.a = a;
            this.h = h;
            this.b = b;
            this.g = g;
            this.f = f;
            this.c = c;
        }
        
        @Override
        public String toString() {
            return String.format("a=%.6f, h=%.6f, b=%.6f, g=%.6f, f=%.6f, c=%.6f", 
                               a, h, b, g, f, c);
        }
    }
    
    /**
     * Solve for conic coefficients from 5 points using SVD
     * @param points Array of exactly 5 points in homogeneous coordinates
     * @return ConicCoefficients representing the conic section
     */
    public static ConicCoefficients solveConicFromPoints(Point[] points) {
        if (points.length != 5) {
            throw new IllegalArgumentException("Exactly 5 points required");
        }
        
        // Build the coefficient matrix A (5x6) directly from homogeneous coordinates
        double[][] A = new double[5][6];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            double x = point.x;
            double y = point.y;
            double w = point.w;
            
            System.out.printf("Using point %s%n", point.toString());
            
            // Row corresponds to Ax² + Bxy + Cy² + Dxw + Eyw + Fw² = 0
            A[i][0] = x * x;  // x² term
            A[i][1] = x * y;  // xy term
            A[i][2] = y * y;  // y² term
            A[i][3] = x * w;  // xw term
            A[i][4] = y * w;  // yw term
            A[i][5] = w * w;  // w² term
        }
        
        // Find the null space of A using SVD
        // For now, we'll use a simple approach: find the eigenvector with smallest eigenvalue
        // of A^T * A, which approximates the null space vector
        
        // Compute A^T * A (6x6 matrix)
        double[][] ATA = new double[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                double sum = 0;
                for (int k = 0; k < 5; k++) {
                    sum += A[k][i] * A[k][j];
                }
                ATA[i][j] = sum;
            }
        }
        
        // Use inverse power iteration to find eigenvector with smallest eigenvalue
        double[] v = {1, 1, 1, 1, 1, 1};  // Initial guess
        final int numIterations = 50;
        
        for (int iter = 0; iter < numIterations; iter++) {
            // Normalize v
            double norm = 0;
            for (double val : v) {
                norm += val * val;
            }
            norm = Math.sqrt(norm);
            
            for (int i = 0; i < v.length; i++) {
                v[i] /= norm;
            }
            
            // Solve (A^T * A) * w = v
            double[][] augmented = new double[6][7];
            for (int i = 0; i < 6; i++) {
                System.arraycopy(ATA[i], 0, augmented[i], 0, 6);
                augmented[i][6] = v[i];
            }
            
            double[] w = solveLinearSystem(augmented);
            
            // Update v
            v = w;
            
            // Check convergence
            if (iter > 0 && Math.abs(norm - 1) < 1e-10) {
                break;
            }
        }
        
        // Normalize the final vector
        double norm = 0;
        for (double val : v) {
            norm = Math.max(val, norm);
        }
        //norm = Math.sqrt(norm);
        
        for (int i = 0; i < v.length; i++) {
            v[i] /= norm;
        }
        
        System.out.println("Computed coefficients: " + Arrays.toString(v));
        
        // Return coefficients
        return new ConicCoefficients(
            v[0],  // a
            v[1],  // h
            v[2],  // b
            v[3],  // g
            v[4],  // f
            v[5]   // c
        );
    }
    
    /**
     * Solve linear system using Gaussian elimination
     * @param matrix Augmented matrix [A|b] where Ax = b
     * @return Solution vector x
     */
    public static double[] solveLinearSystem(double[][] matrix) {
        int n = matrix.length;
        
        // Create a copy of the matrix to avoid modifying the original
        double[][] augmented = new double[n][];
        for (int i = 0; i < n; i++) {
            augmented[i] = matrix[i].clone();
        }
        
        // Forward elimination
        for (int i = 0; i < n; i++) {
            // Find pivot
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) {
                    maxRow = k;
                }
            }
            
            // Swap rows
            double[] temp = augmented[i];
            augmented[i] = augmented[maxRow];
            augmented[maxRow] = temp;
            
            // Eliminate column
            for (int k = i + 1; k < n; k++) {
                if (augmented[i][i] != 0) {  // Avoid division by zero
                    double factor = augmented[k][i] / augmented[i][i];
                    for (int j = i; j < n + 1; j++) {  // n+1 because of augmented column
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }
        
        // Back substitution
        double[] solution = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += augmented[i][j] * solution[j];
            }
            if (augmented[i][i] != 0) {  // Avoid division by zero
                solution[i] = (augmented[i][n] - sum) / augmented[i][i];
            }
        }
        
        return solution;
    }
    
    /**
     * Example usage and test method
     */
    public static void main(String[] args) {
        // Example: Create 5 test points
        Point[] testPoints = {
            new Point(1.0, 0.0, 1.0),   // (1,0)
            new Point(0.0, 1.0, 1.0),   // (0,1)
            new Point(-1.0, 0.0, 1.0),  // (-1,0)
            new Point(0.0, -1.0, 1.0),  // (0,-1)
            new Point(0.6, 0.8, 1.0)    // (0.5,0.5)
        };
        
        try {
            ConicCoefficients coefficients = solveConicFromPoints(testPoints);
            System.out.println("Result: " + coefficients);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
