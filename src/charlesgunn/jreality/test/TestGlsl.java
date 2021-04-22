/*
 * Created on Sep 15, 2004
 *
*/
package charlesgunn.jreality.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class TestGlsl extends LoadableScene {

	boolean useLOD = true;
	Viewer viewer = null;
	/* (non-Javadoc)
	 * @see de.jreality.jogl.InteractiveViewerDemo#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Appearance ap = world.getAppearance();
		GlslProgram brickProg = null;
//		SimpleJOGLShader sh = new SimpleJOGLShader("brick.vert", "brick.frag");//null,"SimpleJOGLFragmentShader-01.txt");
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"useGLShader", true);
//		world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"GLShader", sh);
//		world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER, "brick");
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		for (int i = 0; i<6; ++i)	{
			double[] brickSize = new double[2];
			double[] brickPct = new double[2];
			double[] mortarPct = new double[2];
			IndexedFaceSet ifs = SphereUtility.tessellatedIcosahedronSphere(2, true);
			double[] lightPosition = {0,0,4};
			SceneGraphComponent c = SceneGraphUtility.createFullSceneGraphComponent("sphere"+i);
			ap = c.getAppearance();
			c.setGeometry(SphereUtility.tessellatedIcosahedronSphere(3, true)); //new Sphere()); //
			double angle = (2 * Math.PI * i)/6.0;
			MatrixBuilder.euclidean().translate(3 * Math.cos(angle), 3*Math.sin(angle), 0.0).assignTo(c);
			float g = (float) (i/5.0);
			ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(g,g,g));
			float r = (float) (i/6.0);
			float b = 1f - r;
			brickSize[0] = .1+r*.5;
			brickSize[1] = .2+r*.3;
			brickPct[0] = .4 + .1*i;
			brickPct[1] = .7 + .05*i;
			mortarPct[0] = 1.0 - brickPct[0];
			mortarPct[1] = 1.0 - brickPct[1];
			lightPosition[0] = i-2.0;
			try {
//				cylProg = new GlslProgram(sceneRoot.getAppearance(), "polygonShader", Input.getInput(getClass().getResource("cylStereoVertexShader.glsl")), null);
				ap.setAttribute("polygonShadername", "glsl");
				ap.setAttribute("useVertexArrays", false);
//		ap.setAttribute("useGLSL", true);
				brickProg = new GlslProgram(ap, "polygonShader",   Input.getInput("de/jreality/jogl/shader/resources/brick.vert"),
						Input.getInput("de/jreality/jogl/shader/resources/brick.frag")
				    );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			ap.setAttribute("polygonShader","glsl"); 
			brickProg.setUniform("SpecularContribution", (i*.15));
			brickProg.setUniform("DiffuseContribution", 1.0);
			brickProg.setUniform("BrickColor", new double[]{r,0f,b});
			brickProg.setUniform("MortarColor", new double[] {b,1f,r});
			brickProg.setUniform("BrickSize", brickSize);
			System.out.println("Brick size is "+Rn.toString(brickSize));
			brickProg.setUniform("BrickPct", brickPct);
			brickProg.setUniform("MortarPct", mortarPct);
			brickProg.setUniform("LightPosition", lightPosition);			
			world.addChild(c);
		}
		//SceneGraphComponent flatt = GeometryUtility.flatten(world);
		//CopyScene cp = new CopyScene();
		//SceneGraphComponent flatt = (SceneGraphComponent) cp.createProxyScene(world);
//		AbstractDeformation ad = new AbstractDeformation()		{
//			public double[] valueAt(double[] in, double[] out)	{
//				if (out == null || out.length != in.length) out = new double[in.length];
//				for (int i = 0; i<in.length; ++i)	out[i] = in[i];
//				out[1] *= 2;
//				out[2] *= .5;
//				return out;
//			}
//		};
		//AbstractDeformation.deform(flatt, ad);
		return world;
	}
	
	public void customize(JMenuBar menuBar, Viewer viewer) {
		JMenu testM = new JMenu("Actions");
		ButtonGroup bg = new ButtonGroup();
		final JCheckBoxMenuItem jcc = new JCheckBoxMenuItem("Use level of detail");
		jcc.setSelected(useLOD);
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				useLOD = !useLOD;
				setLOD();
			}
		});
		menuBar.add(testM);
		this.viewer = viewer;
	}
	private void setLOD()	{
		Appearance ap = viewer.getSceneRoot().getAppearance();
		if (ap == null) return;
		double lod = 0.0;
		if (useLOD) lod = 1.0;
		ap.setAttribute(CommonAttributes.LEVEL_OF_DETAIL, lod);
	}
	

	public boolean isEncompass() {
		
		return true;
	}

}
