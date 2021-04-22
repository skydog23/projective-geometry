/*
 * Created on Jan 6, 2010
 *
 */
package de.jreality.geometry;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.SwingConstants;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;

public class ToolContextTest1 {

	   private ArrayList<Point3D> points;
	   private DefaultGeometryShader defaultGeometryShader;
	   private String label;
	   private double[][] controlPoints;
	   // IndexedLineSetFactory lsf = new IndexedLineSetFactory();

	   double offset = 5;
	   private SceneGraphComponent curveSGC;
	   private SceneGraphComponent controlSGC;
	   private double[][] curvePoints;
	   private boolean mouseClicked = false;
	   private ArrayList<Point3D> points2;
	   private double[] startPoint;
	   private double[] endPoint;

	   public static void main(String[] args) {
	      ToolContextTest1 example = new ToolContextTest1();
	   }

	   public ToolContextTest1() {

	      SceneGraphComponent world = SceneGraphUtility
	            .createFullSceneGraphComponent();
	      // add a transparent background rectangle for picking
		  SceneGraphComponent backgroundSGC = 
			   SceneGraphUtility.createFullSceneGraphComponent("Background");
		  double width = 40, height = 20;
		  double[] corners = {0,0,0,  width,0,0, width,height,0, 0,height,0};
	      Appearance ap = backgroundSGC.getAppearance();
	      ap.setAttribute("transparency", 1.0);
	      MatrixBuilder.euclidean().translate(0,0,-.001).assignTo(backgroundSGC);
	      backgroundSGC.setGeometry(Primitives.texturedQuadrilateral(corners));
	      world.addChild(backgroundSGC);
	      world.addTool(new AbstractTool() {
	         {
	            addCurrentSlot(InputSlot.SHIFT_LEFT_BUTTON, "add a new point");
	         }

	         public void perform(ToolContext tc) {
	            System.out.println("Entering perform method");
	            if (!tc.getAxisState(InputSlot.SHIFT_LEFT_BUTTON).isPressed())
	               return;
	            // only show interest in real picks
	            PickResult pr = tc.getCurrentPick();
	            // could also reject all picks that don't end at the backgroundSGC node
	            if (pr == null) return;
	            if (!mouseClicked) {
	               startPoint = pr.getObjectCoordinates();
	               points.add(new Point3D(startPoint));
	               System.out
	                     .println("Start Point is: [" + startPoint[0] + ", "
	                           + startPoint[1] + ", " + startPoint[2]
	                           + "]");
	               mouseClicked = true;
	            } else {
	               endPoint = pr.getObjectCoordinates();
	               points2.add(new Point3D(endPoint));
	               System.out.println("End Point is: [" + endPoint[0] + ", "
	                     + endPoint[1] + ", " + endPoint[2] + "]");
	               attachGeometry();
	            }
	         }
	      });
	      points = generatePoints();
	      curvePoints = convertToArray(generatePoints());
	      points2 = generateControlPoints();
	      controlPoints = convertToArray(generateControlPoints());
	      createCCP_SGC(world);
	      setUpCoordinateSystem(world);
		      JRViewer.display(world);
	   }

	   public void createCCP_SGC(SceneGraphComponent sgc) {
	      curveSGC = SceneGraphUtility.createFullSceneGraphComponent("Curve");
	      controlSGC = SceneGraphUtility.createFullSceneGraphComponent("Control");
	      sgc.addChild(curveSGC);
	      curveSGC.addChild(controlSGC);
	      attachGeometry();
	   }

	   public void attachGeometry() {
	      createCurveGeometry(curveSGC, Color.BLUE, convertToArray(points));
	      createCurveGeometry(controlSGC, Color.GRAY, convertToArray(points2));
	   }

	   public void createCurveGeometry(SceneGraphComponent sgc, Color color,
	         double[][] points) {
	      
	      IndexedLineSetFactory factory = new IndexedLineSetFactory();
	      double[][] vertices = points;
	      int labelPoint = points.length / 2;
	      setUpLabelForCurve(sgc, color, vertices, labelPoint, 0);
	   
	      int[][] edgeIndices = createEdgeIndices(vertices);

	      factory.setVertexCount(vertices.length);
	      factory.setVertexCoordinates(vertices);
	      factory.setEdgeCount(edgeIndices.length);
	      factory.setEdgeIndices(edgeIndices);

	      factory.update();
	      
	      sgc.setGeometry(factory.getGeometry());
	      setUpCurveAppearance(sgc, color);
	   }

	   public void setUpCurveAppearance(SceneGraphComponent sgc, Color color) {
	      // set up the line shader
	      Appearance ap = sgc.getAppearance();
	      defaultGeometryShader = ShaderUtility.createDefaultGeometryShader(ap,
	            true);
	      defaultGeometryShader.setShowPoints(false);
	      defaultGeometryShader.setShowLines(true);
	      RenderingHintsShader rhs = ShaderUtility
	            .createDefaultRenderingHintsShader(ap, true);
	      DefaultLineShader dls = (DefaultLineShader) defaultGeometryShader
	            .createLineShader("default");
	      dls.setTubeDraw(false);
	      dls.setLineWidth(3.0);
	      dls.setDiffuseColor(color);
	   }

	   public int[][] createEdgeIndices(double[][] points) {
	      int[][] edges = new int[points.length - 1][2];

	      for (int i = 0; i < points.length - 1; i++) {
	         edges[i][0] = i;
	         edges[i][1] = i + 1;
	      }
	      return edges;
	   }

	   public double[][] convertToArray(ArrayList<Point3D> points) {
	      double[][] newPoints = new double[points.size()][3];
	      for (int i = 0; i < points.size(); i++) {
	         double[] pt = points.get(i).getPoint();
	         newPoints[i] = new double[] { pt[0], pt[1], pt[2] };
	         System.out.println("Point: (" + newPoints[i][0] + ", "
	               + newPoints[i][1] + ", " + newPoints[i][2] + ")");
	      }
	      return newPoints;
	   }

	   public double[][] normalizePoints(double[][] points) {
	      double largestValue = findLargestValue(points);
	      for (int i = 0; i < points.length; i++) {
	         for (int j = 0; j < points[i].length; j++) {
	            points[i][j] = points[i][j] / largestValue;
	         }
	      }
	      return points;
	   }

	   public double findLargestValue(double[][] points) {
	      double value = -1000.0;
	      for (int i = 0; i < points.length; i++) {
	         for (int j = 0; j < points[i].length; j++) {
	            if (value < points[i][j])
	               value = points[i][j];
	         }
	      }
	      return value;
	   }

	   public ArrayList<Point3D> generatePoints() {
	      ArrayList<Point3D> points = new ArrayList<Point3D>();
	      Point3D point1 = new Point3D();
	      point1.setPoint(new double[] { 0.0f, 0.0f, 0.0f });
	      points.add(point1);
	      Point3D point2 = new Point3D();
	      point2.setPoint(new double[] { 2.0f, 2.0f, 0.0f });
	      points.add(point2);
	      Point3D point3 = new Point3D();
	      point3.setPoint(new double[] { 5.0f, 9.0f, 0.0f });
	      points.add(point3);
	      Point3D point4 = new Point3D();
	      point4.setPoint(new double[] { 10.0f, 10.0f, 0.0f });
	      points.add(point4);
	      Point3D point5 = new Point3D();
	      point5.setPoint(new double[] { 12.0f, 8.0f, 0.0f });
	      points.add(point5);
	      Point3D point6 = new Point3D();
	      point6.setPoint(new double[] { 18.0f, 2.0f, 0.0f });
	      points.add(point6);
	      Point3D point7 = new Point3D();
	      point7.setPoint(new double[] { 20.0f, 0.0f, 0.0f });
	      points.add(point7);
	      Point3D point8 = new Point3D();
	      point8.setPoint(new double[] { 22.0f, 2.0f, 0.0f });
	      points.add(point8);
	      Point3D point9 = new Point3D();
	      point9.setPoint(new double[] { 25.0f, 9.0f, 0.0f });
	      points.add(point9);
	      Point3D point10 = new Point3D();
	      point10.setPoint(new double[] { 30.0f, 10.0f, 0.0f });
	      points.add(point10);
	      Point3D point11 = new Point3D();
	      point11.setPoint(new double[] { 32.0f, 8.0f, 0.0f });
	      points.add(point11);
	      Point3D point12 = new Point3D();
	      point12.setPoint(new double[] { 38.0f, 2.0f, 0.0f });
	      points.add(point12);
	      Point3D point13 = new Point3D();
	      point13.setPoint(new double[] { 40.0f, 0.0f, 0.0f });
	      points.add(point13);
	      return points;
	   }

	   public ArrayList<Point3D> generateControlPoints() {
	      ArrayList<Point3D> points = new ArrayList<Point3D>();
	      Point3D point1 = new Point3D();
	      point1.setPoint(new double[] { 0.0, 0.0, 0.0 });
	      points.add(point1);
	      Point3D point2 = new Point3D();
	      point2.setPoint(new double[] { 2.0, 5.0, 0.0 });
	      points.add(point2);
	      Point3D point3 = new Point3D();
	      point3.setPoint(new double[] { 5.0, 14.0, 0.0 });
	      points.add(point3);
	      Point3D point4 = new Point3D();
	      point4.setPoint(new double[] { 10.0, 17.0, 0.0 });
	      points.add(point4);
	      Point3D point5 = new Point3D();
	      point5.setPoint(new double[] { 12.0, 15.0, 0.0 });
	      points.add(point5);
	      Point3D point6 = new Point3D();
	      point6.setPoint(new double[] { 18.0, 7.0, 0.0 });
	      points.add(point6);
	      Point3D point7 = new Point3D();
	      point7.setPoint(new double[] { 20.0, 5.0, 0.0 });
	      points.add(point7);
	      Point3D point8 = new Point3D();
	      point8.setPoint(new double[] { 22.0, 8.0, 0.0 });
	      points.add(point8);
	      Point3D point9 = new Point3D();
	      point9.setPoint(new double[] { 25.0, 16.0, 0.0 });
	      points.add(point9);
	      Point3D point10 = new Point3D();
	      point10.setPoint(new double[] { 30.0, 15.0, 0.0 });
	      points.add(point10);
	      Point3D point11 = new Point3D();
	      point11.setPoint(new double[] { 32.0, 14.0, 0.0 });
	      points.add(point11);
	      Point3D point12 = new Point3D();
	      point12.setPoint(new double[] { 38.0, 7.0, 0.0 });
	      points.add(point12);
	      Point3D point13 = new Point3D();
	      point13.setPoint(new double[] { 40.0, 0.0, 0.0 });
	      points.add(point13);
	      return points;
	   }

	   class Point3D {
	      public double[] point;

	      public Point3D(double[] point) {
	         this.point = point;
	      }

	      public Point3D() {
	         // ToDo
	      }

	      public double[] getPoint() {
	         return point;
	      }

	      public void setPoint(double[] point) {
	         this.point = point;
	      }

	   }

	   public void setUpPlots(double[][] points, double[][] controlPoints,
	         String label) {
	      // this.label = label;
	      // curvePoints = points;
	      // this.controlPoints = controlPoints;
	      // colors = createColorList(numberCurves);
	      // curveType = 3;
	      SceneGraphComponent world = SceneGraphUtility
	            .createFullSceneGraphComponent("world");
	      world.getAppearance().setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.SMOOTH_SHADING,
	            true);
	      world.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING, false);
	      // setUpCurves(world);
	      // createCoordinateSystem(world);
	      setUpCoordinateSystem(world);
	      JRViewer.display(world);
	   }

	   public void setUpCoordinateSystem(SceneGraphComponent component) {
	      // create coordinate system
	      final CoordinateSystemFactory coords = new CoordinateSystemFactory(
	            component);
	      // SET PROPERTIES:
	      double axisScale = 2.0;
	      Font font = new Font("TimesRoman", Font.PLAIN, 80);
	      coords.setAxisScale(axisScale);
	      coords.setLabelScale(0.01);
	      // coords.showBoxArrows(true);
	      coords.showAxesArrows(true);
	      coords.showLabels(true);
	      // coords.setColor(Color.RED);
	      // coords.setGridColor(Color.GRAY);
	      coords.setLabelColor(Color.DARK_GRAY);
	      coords.setLabelFont(font);
	      coords.setColor(Color.BLUE);

	      // display axes/box/grid
	      coords.showAxes(true);
	      // coords.showBox(true);
	      coords.showGrid(false);

	      // beautify box
	      coords.beautify(true);
	   }

	   public void drawCurveWithLabel(double[][] verts, double[][] contPts,
	         int index, SceneGraphComponent sgc, Color color) {
	      SceneGraphComponent child1 = SceneGraphUtility
	            .createFullSceneGraphComponent("child" + index);
	      sgc.addChild(child1);
	      int numPoints = verts.length;
	      int[][] edges = createEdgeIndices(verts);
	      // double[][] edgeColors = createEdgeColors(edges.length, index);
	      int labelPoint = numPoints / 2;

	      setUpLabelForCurve(child1, color, verts, labelPoint, index);
	      // set up the indexed line set for this example
	      IndexedLineSetFactory lsf = new IndexedLineSetFactory();
	      lsf.setVertexCount(numPoints);
	      lsf.setVertexCoordinates(verts);
	      lsf.setVertexNormals(verts); // outward pointing normals for lighting
	      // enabled
	      lsf.setEdgeCount(edges.length);
	      lsf.setEdgeIndices(edges);

	      lsf.update();
	      IndexedLineSet ils = lsf.getIndexedLineSet();
	      child1.setGeometry(ils);

	      // set up the line shader
	      Appearance ap = child1.getAppearance();
	      defaultGeometryShader = ShaderUtility.createDefaultGeometryShader(ap,
	            true);
	      defaultGeometryShader.setShowPoints(false);
	      defaultGeometryShader.setShowLines(true);
	      RenderingHintsShader rhs = ShaderUtility
	            .createDefaultRenderingHintsShader(ap, true);
	      DefaultLineShader dls = (DefaultLineShader) defaultGeometryShader
	            .createLineShader("default");
	      dls.setTubeDraw(false);
	      dls.setLineWidth(3.0);
	      dls.setDiffuseColor(color);

	      // Set up curve for control points
	      SceneGraphComponent child2 = SceneGraphUtility
	            .createFullSceneGraphComponent("grandchild" + index);
	      child1.addChild(child2);
	      numPoints = contPts.length;
	      edges = createEdgeIndices(contPts);
	      // double[][] edgeColors = createEdgeColors(edges.length, index);
	      labelPoint = numPoints / 2;

	      setUpLabelForCurve(child2, color, contPts, labelPoint, index);
	      // set up the indexed line set for this example
	      lsf = new IndexedLineSetFactory();
	      lsf.setVertexCount(numPoints);
	      lsf.setVertexCoordinates(contPts);
	      lsf.setVertexNormals(contPts); // outward pointing normals for lighting
	      // enabled
	      lsf.setEdgeCount(edges.length);
	      lsf.setEdgeIndices(edges);

	      lsf.update();
	      ils = lsf.getIndexedLineSet();
	      child2.setGeometry(ils);

	      // set up the line shader
	      ap = child2.getAppearance();
	      defaultGeometryShader = ShaderUtility.createDefaultGeometryShader(ap,
	            true);
	      defaultGeometryShader.setShowPoints(false);
	      defaultGeometryShader.setShowLines(true);
	      rhs = ShaderUtility.createDefaultRenderingHintsShader(ap, true);
	      dls = (DefaultLineShader) defaultGeometryShader
	            .createLineShader("default");
	      dls.setTubeDraw(false);
	      dls.setLineWidth(2.0);
	      dls.setDiffuseColor(Color.CYAN);

	   }

	   public void setUpLabelForCurve(SceneGraphComponent child, Color color,
	         double[][] verts, int labelPoint, int index) {
	      // create the label at the center of the example
	      Appearance app = setUpLabels(color, new double[] {
	            verts[labelPoint][0], verts[labelPoint][1],
	            verts[labelPoint][2] });
	      SceneGraphComponent labelSGC = SceneGraphUtility
	            .createFullSceneGraphComponent(label + Integer.toString(index));
	      PointSetFactory labelFac = new PointSetFactory();
	      labelFac.setVertexCount(1);
	      labelFac.setVertexCoordinates(new double[] { verts[labelPoint][0],
	            verts[labelPoint][1], verts[labelPoint][2] });
	      labelFac.setVertexLabels(new String[] { label
	            + Integer.toString(index + 1) });
	      labelFac.update();
	      labelSGC.setGeometry(labelFac.getPointSet());
	      labelSGC.setAppearance(app);
	      child.addChild(labelSGC);
	   }

	   public Appearance setUpLabels(Color color, double[] offset) {
	      // each sample gets a numeric label at its geometric center point.
	      // Prepare the appearance.
	      Appearance labelAp = new Appearance();
	      defaultGeometryShader = ShaderUtility.createDefaultGeometryShader(
	            labelAp, false);
	      defaultGeometryShader.setShowPoints(true);
	      DefaultPointShader pointShader = (DefaultPointShader) defaultGeometryShader
	            .getPointShader();
	      pointShader.setPointRadius(.0001);
	      DefaultTextShader pts = (DefaultTextShader) (pointShader)
	            .getTextShader();
	      pts.setScale(.01);
	      pts.setOffset(offset);
	      pts.setAlignment(SwingConstants.CENTER);
	      pts.setDiffuseColor(color);
	      Font font = new Font("TimesRoman", Font.PLAIN, 60);
	      pts.setFont(font);
	      return labelAp;
	   }

	}
