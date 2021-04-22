/*
 * Created on Sep 5, 2010
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class MetricNeutralTubeDemo extends LoadableScene {
	static double a = .5;
	static private double[][] cubeVerts3 =  
	{{a,a,a},{a,a,-a},{a,-a,a},{a,-a,-a},{-a,a,a},{-a,a,-a},{-a,-a,a},{-a,-a,-a}};
	int metric = 2;
	private SceneGraphComponent cube, cubeV;
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");

		cube = SceneGraphUtility.createFullSceneGraphComponent("cube");
		cubeV = SceneGraphUtility.createFullSceneGraphComponent("cube verts");
		updateCube();
		world.addChildren(cube, cubeV);
		cube.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		cube.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		cubeV.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		cubeV.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		cubeV.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		world.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .04);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR, new Color(200, 150, 0));
		world.getAppearance().setAttribute("pointShader."+CommonAttributes.DIFFUSE_COLOR, new Color(200, 150, 0));
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, .04);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
		world.getAppearance().setAttribute("lineShader."+CommonAttributes.TUBE_STYLE, FrameFieldType.FRENET);
		world.getAppearance().setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0);
		world.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
		world.getAppearance().setAttribute("useGLSL", true);
		return world;
	}

	private void updateCube() {
		IndexedFaceSet cubex = cube4();
		cubeV.setGeometry(cubex);
		cube.setGeometry(IndexedLineSetUtility.refine(cubex,6));
	}
	Viewer viewer;
	@Override
	public void customize(JMenuBar menuBar, Viewer v) {
		viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, metric - 1);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(200,200,200));
		((Component) v.getViewingComponent()).addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_0:
					metric = (metric + 1) % 3;
					System.err.println("Setting metric to "+metric);
					updateCube();
					viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, metric-1);
					break;
				}
			}
			
		});
	}
	static private int[][] cubeIndices = {
		{0,2,3,1},
		{1,5,4,0},
		{0,4,6,2},
		{5,7,6,4},
		{2,6,7,3},
		{3,7,5,1}};
	public static IndexedFaceSet cube4()	{

		IndexedFaceSet cube = new IndexedFaceSet(8, 6);

		cube.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(cubeIndices));
		cube.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cubeVerts3));
		IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(cube);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(cube);		
		return cube;
	}

}
