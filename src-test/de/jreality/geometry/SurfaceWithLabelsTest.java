package de.jreality.geometry;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Random;

import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class SurfaceWithLabelsTest {
    private double[][][] points;
    private int uCount = 0;
    private int vCount = 0;
    private IndexedFaceSet theSurface;

    public static void main(String[] args) {
        SurfaceWithLabelsTest testing = new SurfaceWithLabelsTest();
    }

    public SurfaceWithLabelsTest() {
        SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("world");
        theSurface = create3DSurface(50);
        label(theSurface);
        sgc.setGeometry(theSurface);
        Appearance ap = sgc.getAppearance();
        ap.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, Color.YELLOW);
        ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_WIDTH, 2.0);

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


//        ViewerApp va = ViewerApp.display(sgc);
//        va.setAttachNavigator(true);
//        va.setExternalNavigator(false);
//        va.update();
//        va.setBackgroundColor(Color.LIGHT_GRAY);
//        CameraUtility.encompass(va.getCurrentViewer());

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
         factory.setVLineCount(vCount);
        factory.setULineCount(uCount);
        factory.setClosedInUDirection(false);
      factory.setClosedInVDirection(false);
        factory.setVertexCoordinates(coords);
        factory.setGenerateFaceNormals(true);
        factory.setGenerateTextureCoordinates(true);
        factory.setGenerateEdgesFromFaces(true);
      factory.setEdgeFromQuadMesh(true);   // generate "long" edges: one for each u-, v- parameter curve

        factory.update();

        return factory.getIndexedFaceSet();
    }


    public double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public double[][][] generate3DPoints(int uValues, int vValues) {

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

     public static void label(IndexedFaceSet ps) {
          /*double[][] vc = ps.getVertexAttributes(Attribute.COORDINATES).
         toDoubleArrayArray(null);
      for (double[] c : vc) for (int i = 0; i<3; ++i) c[i] = .5 + .5*c[i];
      ps.setVertexAttributes(Attribute.COLORS,
            StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(vc));*/
        int n = ps.getNumPoints();
        String[] labels = new String[n];
         Random ran = new Random();
         for (int i = 0; i < n; i = i+1) labels[i] = "Point " + i;
        ps.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));


        n = ps.getNumEdges();
        labels = new String[n];
        for (int i = 0; i < n; i = i + 1) labels[i] = "Edge " + i;
        ps.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));

        n = ps.getNumFaces();
        labels = new String[n];
        for (int i = 0; i < n; i = i +1) labels[i] = "Face " + i;
        ps.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
    }
}