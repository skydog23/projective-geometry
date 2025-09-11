package charlesgunn.math;

import java.util.Arrays;

/**
 * Java translation of the SVD-based conic section solver from conics.js
 * Uses true Singular Value Decomposition via Jacobi eigenvalue method
 */
public class ConicFromFivePointsSVDTest {
    
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
     * SVD decomposition result
     */
    public static class SVDResult {
        public double[][] U;
        public double[] S;
        public double[][] V;
        
        public SVDResult(double[][] U, double[] S, double[][] V) {
            this.U = U;
            this.S = S;
            this.V = V;
        }
    }
    
    /**
     * Eigenvalue decomposition result
     */
    public static class EigenResult {
        public double[] eigenValues;
        public double[][] eigenVectors;
        
        public EigenResult(double[] eigenValues, double[][] eigenVectors) {
            this.eigenValues = eigenValues;
            this.eigenVectors = eigenVectors;
        }
    }
    
    /**
     * Solve for conic coefficients from 5 points using true SVD
     * @param points Array of exactly 5 points in homogeneous coordinates
     * @return ConicCoefficients representing the conic section
     */
    public static ConicCoefficients solveConicFromPointsSVD(Point[] points) {
        if (points.length != 5) {
            throw new IllegalArgumentException("Exactly 5 points required");
        }
        
        System.out.println("Using SVD method for conic fitting");
        
        // Build the coefficient matrix A (5x6) directly from homogeneous coordinates
        double[][] A = new double[5][6];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            double x = point.x;
            double y = point.y;
            double w = point.w;
            
            System.out.printf("Using point %s%n", point.toString());
            
            // Row corresponds to ax² + hxy + by² + gxw + fyw + cw² = 0
            A[i][0] = x * x;  // x² term (a)
            A[i][1] = x * y;  // xy term (h)
            A[i][2] = y * y;  // y² term (b)
            A[i][3] = x * w;  // xw term (g)
            A[i][4] = y * w;  // yw term (f)
            A[i][5] = w * w;  // w² term (c)
        }
        
        System.out.println("Coefficient matrix A:");
        printMatrix(A);
        
        // Perform SVD decomposition
        SVDResult svd = svdDecomposition(A);
        System.out.println("SVD singular values: " + Arrays.toString(svd.S));
        
        // The null space vector is the column of V corresponding to the smallest singular value
        int minIndex = findMinIndex(svd.S);
        double[] nullVector = new double[6];
        for (int i = 0; i < 6; i++) {
            nullVector[i] = svd.V[i][minIndex];
        }
        
        System.out.println("Null space vector from SVD: " + Arrays.toString(nullVector));
        
        // Normalize the vector to unit length
        double norm = 0;
        for (double val : nullVector) {
            norm += val * val;
        }
        norm = Math.sqrt(norm);
        
        double[] normalizedVector = new double[6];
        for (int i = 0; i < 6; i++) {
            normalizedVector[i] = nullVector[i] / norm;
        }
        
        System.out.println("Normalized coefficients (SVD): " + Arrays.toString(normalizedVector));
        
        // Return coefficients
        return new ConicCoefficients(
            normalizedVector[0],  // a
            normalizedVector[1],  // h
            normalizedVector[2],  // b
            normalizedVector[3],  // g
            normalizedVector[4],  // f
            normalizedVector[5]   // c
        );
    }
    
    /**
     * SVD decomposition using Jacobi eigenvalue method
     * Returns {U, S, V} where A = U * S * V^T
     */
    public static SVDResult svdDecomposition(double[][] A) {
        int m = A.length;      // number of rows
        int n = A[0].length;   // number of columns
        
        // Create copies to avoid modifying original
        double[][] U = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, U[i], 0, n);
        }
        
        double[][] V = new double[n][n];
        double[] S = new double[Math.min(m, n)];
        
        // Initialize V as identity matrix
        for (int i = 0; i < n; i++) {
            V[i][i] = 1.0;
        }
        
        final double eps = 1e-15;
        final int maxIter = 50;
        
        // Form A^T * A
        double[][] ATA = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    ATA[i][j] += U[k][i] * U[k][j];
                }
            }
        }
        
        // Find eigenvalues and eigenvectors of A^T * A using Jacobi method
        EigenResult result = jacobiEigendecomposition(ATA, maxIter, eps);
        
        // The singular values are square roots of eigenvalues of A^T * A
        for (int i = 0; i < result.eigenValues.length; i++) {
            S[i] = Math.sqrt(Math.max(0, result.eigenValues[i]));
        }
        
        // V matrix columns are the eigenvectors of A^T * A
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                V[j][i] = result.eigenVectors[i][j];
            }
        }
        
        // Compute U = A * V * S^(-1) for non-zero singular values
        double[][] UResult = new double[m][Math.min(m, n)];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < Math.min(m, n); j++) {
                if (S[j] > eps) {
                    for (int k = 0; k < n; k++) {
                        UResult[i][j] += U[i][k] * V[k][j] / S[j];
                    }
                }
            }
        }
        
        // Copy back to U
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < Math.min(m, n); j++) {
                U[i][j] = UResult[i][j];
            }
        }
        
        return new SVDResult(U, S, V);
    }
    
    /**
     * Jacobi eigenvalue decomposition for symmetric matrices
     */
    public static EigenResult jacobiEigendecomposition(double[][] matrix, int maxIter, double eps) {
        int size = matrix.length;
        double[][] eigenVecs = new double[size][size];
        double[] eigenVals = new double[size];
        
        // Initialize eigenvector matrix as identity
        for (int i = 0; i < size; i++) {
            eigenVecs[i][i] = 1.0;
        }
        
        // Copy matrix
        double[][] A = new double[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[i], 0, A[i], 0, size);
        }
        
        for (int iter = 0; iter < maxIter; iter++) {
            // Find largest off-diagonal element
            double maxVal = 0;
            int p = 0, q = 1;
            
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (Math.abs(A[i][j]) > maxVal) {
                        maxVal = Math.abs(A[i][j]);
                        p = i;
                        q = j;
                    }
                }
            }
            
            if (maxVal < eps) break;
            
            // Calculate rotation angle
            double theta = 0.5 * Math.atan2(2 * A[p][q], A[q][q] - A[p][p]);
            double c = Math.cos(theta);
            double s = Math.sin(theta);
            
            // Apply Jacobi rotation
            double App = A[p][p];
            double Aqq = A[q][q];
            double Apq = A[p][q];
            
            A[p][p] = c * c * App + s * s * Aqq - 2 * s * c * Apq;
            A[q][q] = s * s * App + c * c * Aqq + 2 * s * c * Apq;
            A[p][q] = A[q][p] = 0;
            
            // Update other elements
            for (int i = 0; i < size; i++) {
                if (i != p && i != q) {
                    double Aip = A[i][p];
                    double Aiq = A[i][q];
                    A[i][p] = A[p][i] = c * Aip - s * Aiq;
                    A[i][q] = A[q][i] = s * Aip + c * Aiq;
                }
            }
            
            // Update eigenvectors
            for (int i = 0; i < size; i++) {
                double Vip = eigenVecs[i][p];
                double Viq = eigenVecs[i][q];
                eigenVecs[i][p] = c * Vip - s * Viq;
                eigenVecs[i][q] = s * Vip + c * Viq;
            }
        }
        
        // Extract eigenvalues
        for (int i = 0; i < size; i++) {
            eigenVals[i] = A[i][i];
        }
        
        // Sort eigenvalues and eigenvectors in descending order
        Integer[] indices = new Integer[size];
        for (int i = 0; i < size; i++) {
            indices[i] = i;
        }
        
        Arrays.sort(indices, (a, b) -> Double.compare(Math.abs(eigenVals[b]), Math.abs(eigenVals[a])));
        
        double[] sortedVals = new double[size];
        double[][] sortedVecs = new double[size][size];
        
        for (int i = 0; i < size; i++) {
            sortedVals[i] = eigenVals[indices[i]];
            for (int j = 0; j < size; j++) {
                sortedVecs[i][j] = eigenVecs[j][indices[i]];
            }
        }
        
        return new EigenResult(sortedVals, sortedVecs);
    }
    
    /**
     * Find index of minimum value in array
     */
    private static int findMinIndex(double[] array) {
        int minIndex = 0;
        double minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
                minIndex = i;
            }
        }
        return minIndex;
    }
    
    /**
     * Print matrix for debugging
     */
    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
    
    /**
     * Example usage and test method
     */
    public static void main(String[] args) {
        // Example: Create 5 test points on a unit circle
        Point[] testPoints = {
            new Point(1.0, 0.0, 1.0),       // (1,0)
            new Point(0.0, 1.0, 1.0),       // (0,1)
            new Point(-1.0, 0.0, 1.0),      // (-1,0)
            new Point(0.0, -1.0, 1.0),      // (0,-1)
            new Point(0.707, 0.707, 1.0)    // (√2/2, √2/2)
        };
        
        try {
            System.out.println("=== SVD-based Conic Solver Test ===");
            ConicCoefficients coefficients = solveConicFromPointsSVD(testPoints);
            System.out.println("\nFinal Result: " + coefficients);
            
            // Verify by substituting points back into conic equation
            System.out.println("\n=== Verification ===");
            for (Point p : testPoints) {
                double value = coefficients.a * p.x * p.x + 
                              coefficients.h * p.x * p.y + 
                              coefficients.b * p.y * p.y + 
                              coefficients.g * p.x * p.w + 
                              coefficients.f * p.y * p.w + 
                              coefficients.c * p.w * p.w;
                System.out.printf("Point %s: conic value = %.2e%n", p, value);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}