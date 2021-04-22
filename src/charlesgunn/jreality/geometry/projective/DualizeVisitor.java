/*
 * Created on Nov 23, 2011
 *
 */
package charlesgunn.jreality.geometry.projective;

import java.awt.Color;

import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.util.CopyVisitor;

public class DualizeVisitor extends SceneGraphVisitor {

	CopyVisitor copier = new CopyVisitor();
	SceneGraphComponent root, dualRoot, 
		currentSGC, currentDualSGC,
		currentParentSGC, currentParentDualSGC;
	boolean finiteLines = true;
	
	public DualizeVisitor(SceneGraphComponent root)	{
		this.root = root;
	}
	
	public SceneGraphComponent visit()	{
		dualRoot = new SceneGraphComponent();
		currentSGC = currentDualSGC = null;
		visit(root);
		return dualRoot;
	}

	@Override
	public void visit(IndexedFaceSet f) {
		visit((IndexedLineSet) f);
	}

	@Override
	public void visit(IndexedLineSet g) {
		visit((PointSet) g);
	}

	@Override
	public void visit(PointSet p) {
		double[][] verts = p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] colors = null;
		if (p.getVertexAttributes(Attribute.COLORS) != null)
			colors = p.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		int numVerts = verts.length;
		double[][] dualLines = new double[numVerts][];
		for (int i = 0; i<numVerts; ++i)	{
			dualLines[i] = dualizePoint2Line(dualLines[i], verts[i]); //new double[]{ theVerts[i][3], 0, -theVerts[i][1], 0, -theVerts[i][0], 0};
		}
		for (int i = 0; i<numVerts; ++i)	{
			PointRangeFactory prf = new PointRangeFactory();
			prf.setPluckerLine(dualLines[i]);
			prf.setFiniteSphere(finiteLines);
			prf.setSphereRadius(500.0);
			prf.update();
			prf.getLine().setName(p.getName()+" dual line "+i);
			SceneGraphComponent child = new SceneGraphComponent(p.getName()+" dual line "+i);
			child.setGeometry(prf.getLine());
			if (colors != null)	{
				child.setAppearance(new Appearance());
				Color foo = new Color((float) colors[i][0], (float) colors[i][1], (float) colors[i][2]);
				child.getAppearance().setAttribute("lineShader.diffuseColor", foo);
				child.getAppearance().setAttribute("lineShader.diffuseColor", foo);
			}
			currentDualSGC.addChild(child);
		}
	}

	@Override
	public void visit(SceneGraphComponent c) {
		copier.visit(c);
		SceneGraphComponent dualSGC = (SceneGraphComponent) copier.getCopy();
		dualSGC.setName( c.getName()+" dual");
		currentSGC = c;
		currentDualSGC = dualSGC;
		if (currentParentSGC == null) {
			dualRoot = currentDualSGC;
		}
		else if (currentParentSGC.isDirectAncestor(currentSGC)) {
			currentParentDualSGC.addChild(currentDualSGC);
		}
		SceneGraphComponent oldCPSGC = currentParentSGC,
			oldCPDSGC = currentParentDualSGC;
		currentParentSGC = currentSGC;
		currentParentDualSGC = currentDualSGC;
		c.childrenAccept(this);
		currentParentSGC = oldCPSGC;
		currentParentDualSGC = oldCPDSGC;
	}

	@Override
	public void visit(Transformation t) {
		double[] mat = t.getMatrix();
		mat = Rn.transpose(null, Rn.inverse(null, mat));
		currentDualSGC.setTransformation(new Transformation(mat));
	}

	@Override
	public void visit(Appearance a) {
		copier.visit(a);
		Appearance copied = (Appearance) copier.getCopy();
		Object foo = copied.getAttribute("pointShader.diffuseColor");
		Object bar = copied.getAttribute("lineShader.diffuseColor");
		if (foo instanceof Color){
			copied.setAttribute("lineShader.diffuseColor", foo);
			copied.setAttribute("pointShader.diffuseColor", Appearance.INHERITED);
		}
		if (bar instanceof Color){
			copied.setAttribute("pointShader.diffuseColor", bar);
			copied.setAttribute("lineShader.diffuseColor", Appearance.INHERITED);
		}
		currentDualSGC.setAppearance(copied);
	}

	@Override
	public void visit(Camera c) {
		copier.visit(c);
		Camera copied = (Camera) copier.getCopy();
		currentDualSGC.setCamera(copied);
	}

	@Override
	public void visit(DirectionalLight l) {
		copier.visit(l);
		DirectionalLight copied = (DirectionalLight) copier.getCopy();
		currentDualSGC.setLight(copied);
	}

	@Override
	public void visit(PointLight l) {
		copier.visit(l);
		PointLight copied = (PointLight) copier.getCopy();
		currentDualSGC.setLight(copied);
	}

	@Override
	public void visit(SpotLight l) {
		copier.visit(l);
		SpotLight copied = (SpotLight) copier.getCopy();
		currentDualSGC.setLight(copied);
	}

	public static double[] dualizeLine2Point(double[] pt, double[] line)	{
		if (pt == null) return new double[]{-line[4], -line[2],0, line[0]};
		pt[0] = -line[4]; pt[1] = -line[2]; pt[2] = 0; pt[3] = line[0];
		return pt;
	}

	public static double[] dualizePoint2Line(double[] line, double[] pt)	{
		if (line == null) {
			if (pt.length == 4)
				return new double[]{pt[3], 0, -pt[1], 0, -pt[0], 0};
			return new double[]{1,0,-pt[1], 0, pt[0], 0};
		}
		System.arraycopy(new double[]{pt[3], 0, -pt[1], 0, -pt[0], 0}, 0, line, 0, 6);
		return line;
	}

	public boolean isFiniteLines() {
		return finiteLines;
	}

	public void setFiniteLines(boolean finiteLines) {
		this.finiteLines = finiteLines;
	}

}
