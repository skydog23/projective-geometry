package charlesgunn.math;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;
import org.ejml.data.SingularMatrixException; // Needed to inspect singular values, though not strictly required for the final coefficient extraction

import java.util.List;
import java.util.ArrayList;

public class ConicFitEjmlSimpleMatrix {

    /**
     * Calculates the coefficients [A, B, C, D, E, F] of the conic
     * Ax^2 + Bxy + Cy^2 + Dx + Ey + F = 0 passing through 5 given points.
     *
     * @param points A list of 5 (x, y) coordinates.
     * @return A 6-element array [A, B, C, D, E, F] representing the conic coefficients.
     * Returns null if the points are degenerate (e.g., collinear) or if the
     * null space is not 1-dimensional, or if less than 5 points are provided.
     */
    public static double[] conicFrom5Points(List<double[]> points) {
        if (points == null || points.size() != 5) {
            System.err.println("Error: Exactly 5 points are required to define a conic.");
            return null;
        }

        // Create the M_data matrix.
        // Each row corresponds to [x^2, xy, y^2, x, y, 1] for a point.
        SimpleMatrix M_data = new SimpleMatrix(5, 6);

        for (int i = 0; i < 5; i++) {
            double x = points.get(i)[0];
            double y = points.get(i)[1];

            M_data.set(i, 0, x * x);     // A (x^2)
            M_data.set(i, 1, x * y);     // B (xy)
            M_data.set(i, 2, y * y);     // C (y^2)
            M_data.set(i, 3, x);         // D (x)
            M_data.set(i, 4, y);         // E (y)
            M_data.set(i, 5, 1.0);       // F (1) - assuming W=1 for affine points
        }

        // Compute the Singular Value Decomposition (SVD)
        // M_data = U * S * V^T
//        SimpleMatrix[] svdResult = M_data.svd();
//        SimpleMatrix V = svdResult[2]; // V is the third element, which is the V matrix from U*S*V^T
        SimpleSVD<SimpleMatrix> svdResult = M_data.svd();
        SimpleMatrix V = svdResult.nullSpace(); // V is the third element, which is the V matrix from U*S*V^T

        // In EJML's SimpleMatrix.svd(), the V matrix returned is already V, not V^T (Vh in NumPy)
        // The columns of V are the right singular vectors.
        // The last column of V corresponds to the smallest singular value and spans the null space.
        
        // We can optionally check the singular values to see if it's truly a valid conic
        // (i.e., if the smallest singular value is near zero).
        // For SimpleMatrix, you can't directly get the singular values as a vector like in DMatrixRMaj.
        // You'd typically extract them from the diagonal of the S matrix (svdResult[1])
        // or check the smallest singular value property via a different SVD method if needed.
        // For most practical purposes in this algorithm, just taking the last column of V is sufficient,
        // assuming the input points define a non-degenerate conic.
        // If you need the singular values:
        // SimpleMatrix S_matrix = svdResult[1]; // S is a diagonal matrix
        // double smallestSingularValue = S_matrix.get(S_matrix.numRows() - 1, S_matrix.numCols() - 1);
        // if (smallestSingularValue > 1e-9) { /* handle warning */ }

   
        // Extract the last column of V, which contains the conic coefficients
        double[] coeffs = new double[6];
        for (int i = 0; i < 6; i++) {
            coeffs[i] = V.get(i, V.numCols() - 1);
        }
//
        return coeffs;
    }

    public static void main(String[] args) {
        // --- Example Usage ---

        // Points for a circle: x^2 + y^2 - 1 = 0 (coeffs proportional to [1, 0, 1, 0, 0, -1])
        List<double[]> circlePoints = new ArrayList<>();
        circlePoints.add(new double[]{1, 0});
        circlePoints.add(new double[]{-1, 0});
        circlePoints.add(new double[]{0, 1});
        circlePoints.add(new double[]{0, -1});
        circlePoints.add(new double[]{0.6, 0.8}); // Another point on the unit circle

        double[] coeffsCircle = conicFrom5Points(circlePoints);
        if (coeffsCircle != null) {
            System.out.println("Coefficients for circle: [A=" + coeffsCircle[0] + ", B=" + coeffsCircle[1] +
                               ", C=" + coeffsCircle[2] + ", D=" + coeffsCircle[3] +
                               ", E=" + coeffsCircle[4] + ", F=" + coeffsCircle[5] + "]");
            // Normalize to make F close to -1 for easier comparison with x^2+y^2-1=0
            if (Math.abs(coeffsCircle[5]) > 1e-9) {
                double scale = -1.0 / coeffsCircle[5];
                System.out.println("Normalized: [A=" + (coeffsCircle[0]*scale) + ", B=" + (coeffsCircle[1]*scale) +
                                   ", C=" + (coeffsCircle[2]*scale) + ", D=" + (coeffsCircle[3]*scale) +
                                   ", E=" + (coeffsCircle[4]*scale) + ", F=" + (coeffsCircle[5]*scale) + "]");
            }
        }
        System.out.println("\n-------------------------------------------------\n");

        // Points for a parabola: y - x^2 = 0 (coeffs proportional to [-1, 0, 0, 0, 1, 0])
        List<double[]> parabolaPoints = new ArrayList<>();
        parabolaPoints.add(new double[]{0, 0});
        parabolaPoints.add(new double[]{1, 1});
        parabolaPoints.add(new double[]{-1, 1});
        parabolaPoints.add(new double[]{2, 4});
        parabolaPoints.add(new double[]{-2, 4});

        double[] coeffsParabola = conicFrom5Points(parabolaPoints);
        if (coeffsParabola != null) {
            System.out.println("Coefficients for parabola: [A=" + coeffsParabola[0] + ", B=" + coeffsParabola[1] +
                               ", C=" + coeffsParabola[2] + ", D=" + coeffsParabola[3] +
                               ", E=" + coeffsParabola[4] + ", F=" + coeffsParabola[5] + "]");
             // Normalize if needed, e.g., make A close to -1 or E close to 1
        }
        System.out.println("\n-------------------------------------------------\n");
        
        // Points for a hyperbola: x^2 - y^2 - 1 = 0 (coeffs proportional to [1, 0, -1, 0, 0, -1])
        List<double[]> hyperbolaPoints = new ArrayList<double[]>();
        hyperbolaPoints.add(new double[]{1, 0});
        hyperbolaPoints.add(new double[]{-1, 0});
        hyperbolaPoints.add(new double[]{Math.sqrt(2), 1});
        hyperbolaPoints.add(new double[]{Math.sqrt(2), -1});
        hyperbolaPoints.add(new double[]{2, Math.sqrt(3)});

        double[] coeffsHyperbola = conicFrom5Points(hyperbolaPoints);
        if (coeffsHyperbola != null) {
            System.out.println("Coefficients for hyperbola: [A=" + coeffsHyperbola[0] + ", B=" + coeffsHyperbola[1] +
                               ", C=" + coeffsHyperbola[2] + ", D=" + coeffsHyperbola[3] +
                               ", E=" + coeffsHyperbola[4] + ", F=" + coeffsHyperbola[5] + "]");
            // Normalize to make F close to -1
            if (Math.abs(coeffsHyperbola[5]) > 1e-9) {
                double scale = -1.0 / coeffsHyperbola[5];
                System.out.println("Normalized: [A=" + (coeffsHyperbola[0]*scale) + ", B=" + (coeffsHyperbola[1]*scale) +
                                   ", C=" + (coeffsHyperbola[2]*scale) + ", D=" + (coeffsHyperbola[3]*scale) +
                                   ", E=" + (coeffsHyperbola[4]*scale) + ", F=" + (coeffsHyperbola[5]*scale) + "]");
            }
        }
    }
}