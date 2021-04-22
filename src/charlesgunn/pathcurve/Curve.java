package charlesgunn.pathcurve;

public interface Curve {
	public boolean isImmutable();
	public int getDimensionOfAmbientSpace();
	public void evaluate(double u, double[] xyz, int index);
}

