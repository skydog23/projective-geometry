/*
 * Created on Dec 19, 2012
 *
 */
package charlesgunn.jreality;
import java.awt.Color;
import java.awt.Font;

import de.jreality.geometry.CoordinateSystemFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;

public class TestLabels {

public void createViewer3(SceneGraphComponent root) {
Viewer viewer = JRViewer.display(root);
JRViewer view = JRViewer.getLastJRViewer();
// encompass sets near, far planes and moves camera as needed to see
// whole world
CameraUtility.encompass(view.getViewer(), root, false, Pn.EUCLIDEAN);
}

public SceneGraphComponent drawPoints() {
double[][] vertices1 = new double[][] { { 0, 0, 0 }, { 1, 0, 0 },
{ 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 }, { 2, 0, 0 },
{ 2, 2, 0 }, { 0, 2, 0 }, { 0, 0, 0 }, { 0, 3, 0 },
{ 3, 0, 0 }, { 3, 3, 0 }, { 0, 0, 0 }, { 0, 4, 0 },
{ 4, 0, 0 }, { 4, 4, 0 }, { 0, 0, 0 }, { 0, 5000, 0 },
{ 5000, 0, 0 }, { 5000, 5000, 0 }, { 0, 0, 4000 },
{ 0, 4000, 0 }, { 4000, 0, 0 }, { 4000, 4000, 0 } };

PointSetFactory psf = new PointSetFactory();
psf.setVertexCount(vertices1.length);
psf.setVertexCoordinates(vertices1);
psf.update();
SceneGraphComponent sgc = new SceneGraphComponent();
sgc.setGeometry(psf.getPointSet());
Appearance ap = new Appearance();
ap.setAttribute(CommonAttributes.SPHERES_DRAW, true); // false also
// works here,
ap.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE, false); // but
// then
// this
// must
// be
// false
ap.setAttribute(CommonAttributes.POINT_RADIUS, 50.0); // for spheres
ap.setAttribute(CommonAttributes.POINT_SIZE, 10.0); // for "sprites"
sgc.setAppearance(ap);
setUpCoordinateSystem(new double[]{6000.0, 6000.0, 5000.0}, sgc);
return sgc;
}

public void setUpCoordinateSystem(double[] xyz_extent, SceneGraphComponent root) {
double axisScale = calculateAxisScale(xyz_extent);
// create coordinate system
final CoordinateSystemFactory coords = new CoordinateSystemFactory(xyz_extent,
axisScale);
// SET PROPERTIES:
System.out.println("AxisScale: " + axisScale);
coords.setAxisScale(axisScale);
coords.setLabelScale(2.0);
//coords.setLabelScale(1/(axisScale * 0.0005));
// coords.showBoxArrows(true);
coords.showAxesArrows(true);
coords.showLabels(true);
coords.setColor(Color.BLACK);
coords.setGridColor(Color.darkGray);
coords.setLabelColor(Color.BLACK);
coords.setLabelFont(new Font("Times New Roman", Font.BOLD, 200));
//Font f = new Font("Arial Bold", Font.ITALIC, 48);
// display axes/box/grid
// coords.showAxes(false);
coords.showBox(true);
coords.showGrid(true);

// beautify box
coords.beautify(true);
root.addChild(coords.getCoordinateSystem());

}

private double calculateAxisScale(double[] xyz_extent) {
//To display 10 ticks per axis
double max = Math.max(Math.max(xyz_extent[0], xyz_extent[1]), xyz_extent[2]);
return (2.0 * max)/10.0;
}

public static void main(String[] argv) {
TestLabels tk = new TestLabels();
tk.createViewer3(tk.drawPoints());
}
}
