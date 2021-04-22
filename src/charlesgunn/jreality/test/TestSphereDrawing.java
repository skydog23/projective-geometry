/*
 * Created on May 12, 2004
 *
 */
package charlesgunn.jreality.test;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Pn;
import de.jreality.plugin.experimental.ViewerKeyListener;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.util.SceneGraphUtility;

/**
 * @author Charles Gunn
 *
 */
public class TestSphereDrawing extends LoadableScene {

	SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
	public SceneGraphComponent makeWorld() {
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"name","glsl");
//		try {
//			ap1.setAttribute("polygonShader::glsl-source", new GlslSource(Input.getInput("de/jreality/jogl/shader/resources/standard3dlabs.vert"),
//					null)); //Input.getInput("de/jreality/jogl/shader/resources/standard3dlabs.frag")));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"name", "twoSide");
		TwoSidePolygonShader tsps = 
			(TwoSidePolygonShader) AttributeEntityUtility.createAttributeEntity(
				TwoSidePolygonShader.class, CommonAttributes.POLYGON_SHADER, ap1, true);
//		ap1.setAttribute(CommonAttributes.POLYGON_SHADER, new TwoSidePolygonShader());
		ImplodePolygonShader ips = 
			(ImplodePolygonShader) AttributeEntityUtility.createAttributeEntity(
				ImplodePolygonShader.class, CommonAttributes.POLYGON_SHADER+".front", ap1, true);
//		tsps.createFront("implode");
//		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".front"+"name", "implode");
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".implodeFactor", -.6);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "simple");
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(210, 150, 0));
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .012);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, false);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPECULAR_COLOR, new Color(0,255, 255));
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(250, 0, 100));
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.LIGHT_DIRECTION, new double[]{1,-1,2});
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 10.0);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPECULAR_EXPONENT, 10.0);
		for (int i = 0; i< 1; ++i)	{
			//Torus torus= new Torus(0.5, 0.3, 20, 30);
			ParametricSurfaceFactory foo = new ParametricSurfaceFactory();
			foo.setImmersion(new ParametricSurfaceFactory.Immersion() {
					     double R = 0.5;
					     double r = 0.3;
					     public int getDimensionOfAmbientSpace() { return  3; }
					     public void evaluate( double u, double v, double [] xyz, int offset ) {   
					        xyz[1]=Math.cos(u)*(R+r*Math.cos(v));
					        xyz[0]=Math.sin(u)*(R+r*Math.cos(v));
					        xyz[2]=r*Math.sin(v);
					     }
						public boolean isImmutable() {
							return true;
						}
					   }

			);

			foo.setUMax( 2*Math.PI);
			foo.setVMax( 2*Math.PI);
			foo.setULineCount(20);
			foo.setVLineCount(20);
			foo.setGenerateVertexNormals( true );
			foo.setGenerateTextureCoordinates( true );
			foo.setGenerateEdgesFromFaces(true);
			foo.setClosedInUDirection( true );
			foo.setClosedInVDirection( true );
			foo.update();
			IndexedFaceSet bar =  foo.getIndexedFaceSet();

			double[][] verts = new double[21][3];
			double angle = 0, delta = Math.PI * 2 / (20);
			for (int j = 0; j<21; ++j) {
				angle = j * delta;
				verts[j][0] = Math.cos(angle);
				verts[j][1] = Math.sin(angle);
			}
			   double[][] mysection = verts;
			   IndexedLineSet torus1 = Primitives.discreteTorusKnot(1,.5, 2, 3, 250);//		   double[][] verts = new double[250][3];
			   PolygonalTubeFactory ptf = new PolygonalTubeFactory(torus1, 0);
			   ptf.setClosed(true);
//			   ptf.setVertexColorsEnabled(true);
			   ptf.setRadius(.4);
			   ptf.setGenerateEdges(true);
			   ptf.setCrossSection(mysection);
			   ptf.setMatchClosedTwist(true);
//			   ptf.setTwists(6);
//			   double[][] vcolors = ils.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
//			   ptf.setVertexColors(vcolors);
			   ptf.update();
			   IndexedFaceSet torus1Tubes = ptf.getTube();
			SceneGraphComponent globeNode = new SceneGraphComponent();
			globeNode.setName("comp"+i);
			Transformation gt= new Transformation();
			//gt.setTranslation(-5.0 + 2.0* i, 0, 0.0);
			globeNode.setTransformation(gt);
			//if (i!=0) globeNode.setGeometry(GeometryUtility.implode(torus, -.9 + .4 * i));
			//else globeNode.setGeometry(GeometryUtility.truncate(torus));
 			globeNode.setGeometry(torus1Tubes); //bar);
			root.addChild(globeNode);
		}
		//CameraUtility.getCameraNode(viewer).getTransformation().setTranslation(0.0d, 0.0d, 4.0d);
		return root;
	}
 
	public boolean addBackPlane()	{return false;}

	public int getMetric() {
		return Pn.EUCLIDEAN;
	}
	public boolean isEncompass() {
		return true;
	}
	public void customize(JMenuBar menuBar, Viewer v) {
		final Viewer viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,80, 60));
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter()	{
			
		    public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	7:  increase/decrease implode factor increment");
					System.out.println("shift-7:  decrease plane movement increment");
					break;

				case KeyEvent.VK_7:
					ViewerKeyListener.modulateValueAdditive(root.getAppearance(), CommonAttributes.POLYGON_SHADER+".implodeFactor", 0.5, .1, -1.0, 1.0, !e.isShiftDown());
				    viewer.render();
					break;
					}
			}
		});
	}

	@Override
	public Component getInspector(Viewer v) {
		return new JPanel();
	}

	@Override
	public boolean hasInspector() {
		return true;
	}
	
}

