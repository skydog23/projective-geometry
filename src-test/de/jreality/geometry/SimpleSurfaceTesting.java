package de.jreality.geometry;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;


public class SimpleSurfaceTesting {

private double[][][] points;
private int uCount = 0;
private int vCount = 0;
private IndexedFaceSet theSurface;

public static void main(String[] args) {
SimpleSurfaceTesting testing = new SimpleSurfaceTesting();
}

public SimpleSurfaceTesting() {
SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("world");
sgc.setGeometry(create3DSurface(50));
Appearance ap = sgc.getAppearance();
ap.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, Color.CYAN);
ap.setAttribute(CommonAttributes.TUBES_DRAW, false);
ap.setAttribute(CommonAttributes.LINE_WIDTH, 1.0);

double axisScale = 5.0;
Font font = new Font("TimesRoman", Font.PLAIN, 50);
int[][] edges = new int[][]{{-1, 1}, {-1, 1}, {-1, -1}};
Color green = new Color(0, 255, 0);
Color blue = new Color(0, 0, 255);
Color red = new Color(255, 0, 0);
Color gray = new Color(131, 138, 131);
Color darkviolet = new Color(148, 0, 211);
CoordinateSystemFactory factory2 = new CoordinateSystemFactory(sgc, axisScale);
factory2.setLabelBoxEdges(edges);
factory2.setLabelScale(.02);
factory2.setLabelFont(font);
factory2.showAxes(true);
factory2.setLabelColor(blue);
factory2.showLabels(true);
factory2.showAxesArrows(true);
factory2.setColor(darkviolet);
//factory2.showGrid(false);
factory2.setGridColor(red);
factory2.setBoxColor(gray);

factory2.showBox(true);
factory2.beautify(true);


//ViewerApp va = ViewerApp.display(sgc);
Viewer v = JRViewer.display(sgc);
//va.setAttachNavigator(true);
//va.setExternalNavigator(false);
//va.update();
//va.setBackgroundColor(Color.LIGHT_GRAY);
CameraUtility.encompass(v);
//ViewerApp va = ViewerApp.display(create3DSurface( 50 ));
//CameraUtility.encompass(va.getCurrentViewer());

}

public IndexedFaceSet create3DSurface(int N) {

if (N < 2)
throw new IllegalArgumentException("n neets to be greater then 1");

QuadMeshFactory factory = new QuadMeshFactory();
uCount = 20;
vCount = 20;

double[][][] coords = generate3DPoints(20, 20);
factory.setVLineCount(vCount);
factory.setULineCount(uCount);


factory.setVertexCoordinates(coords);

factory.setGenerateFaceNormals(true);
factory.setGenerateTextureCoordinates(true);

factory.update();

return factory.getIndexedFaceSet();
}


public double roundTwoDecimals(double d) {
DecimalFormat twoDForm = new DecimalFormat("#.##");
return Double.valueOf(twoDForm.format(d));
}

public double[][][] generate3DPoints(int uValues, int vValues) {
//float qrtX = (lrgX - smX) / 4.0f;
//float threeQrtX = 3.0f * qrtX;
//float qrtY = (lrgY - smY) / 4.0f;
//float threeQrtY = 3.0f * qrtY;
//float zVal = 0.0f;
int quarterU = uValues / 4;
int quarterV = vValues / 4;
int threeQuartU = uValues * 3 / 4;
int threeQuartV = vValues * 3 / 4;
int midPointU = uValues / 2;
int midPointV = vValues / 2;

double[][][] points = new double[vValues][uValues][3];

for (int v = 0; v < vValues; v++) {
for (int u = 0; u < uValues; u++) {
double[] point = new double[3];
//Point3D p3D = new Point3D();
if ((u > threeQuartU) || (v > threeQuartV) || (u < quarterU) || (v < quarterV)) {
point[0] = u;
point[1] = v;
point[2] = 0.0f;
points[u][v] = point;
} else if (u < midPointU) {
point[0] = u;
point[1] = v;
point[2] = u;
points[u][v] = point;
} else if (v < midPointV) {
point[0] = u;
point[1] = v;
point[2] = v;
points[u][v] = point;
} else if (u > midPointU) {
point[0] = u;
point[1] = v;
point[2] = uValues - u;
points[u][v] = point;
} else if (v > midPointV) {
point[0] = u;
point[1] = v;
point[2] = vValues - v;
points[u][v] = point;
} else if ((u == midPointU) || (v == midPointV)) {
point[0] = u;
point[1] = v;
point[2] = u;
points[u][v] = point;
}
}
}
return points;
}
}
