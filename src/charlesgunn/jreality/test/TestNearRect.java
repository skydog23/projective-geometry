package charlesgunn.jreality.test;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JColorChooser;
import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.FrontWindow;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.jreality.viewer.PluginSceneLoader;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class TestNearRect extends LoadableScene {

	public class MouseOverTool extends AbstractTool {
		int faceNumber = -1;
		Color hcl;
		boolean activated = false;
		public MouseOverTool(Color highLightColor) {
			super(InputSlot.POINTER_HIT);
			hcl=highLightColor;
		}
		@Override
		public void activate(ToolContext tc) {
			SceneGraphComponent cmp = tc.getRootToLocal().getLastComponent();
			PickResult hit = tc.getCurrentPick();
			if (hit == null || hit.getPickType() != PickResult.PICK_TYPE_FACE) return;
			activated = true;
			addCurrentSlot(InputSlot.LEFT_BUTTON, "choose a new color");
			faceNumber = hit.getIndex();
			faceColors[faceNumber][3] = 1;
			ifsf.setFaceColors(faceColors);
			ifsf.update();
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, hcl);
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, .25);
			System.err.println("activating");
		}
		
		@Override
		public void perform(ToolContext tc) {
			if (!tc.getAxisState(InputSlot.LEFT_BUTTON).isPressed()) return;
			Color color = JColorChooser.showDialog((Component) v.getViewingComponent(), "Select color ",  bcolors[faceNumber]);
			if (color != null) {
				bcolors[faceNumber] = color;
				faceColors[faceNumber] = colorToDouble(faceColors[faceNumber], color);
				ifsf.setFaceColors(faceColors);
				ifsf.update();
				v.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, bcolors);
				v.renderAsync();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			if (!activated || faceNumber < 0) return;
			activated = false;
			removeCurrentSlot(InputSlot.LEFT_BUTTON);
			faceColors[faceNumber][3] = 0.0;
			ifsf.setFaceColors(faceColors);
			ifsf.update();
			SceneGraphComponent cmp = tc.getRootToLocal().getLastComponent();
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Appearance.INHERITED);
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 1.0);
			System.err.println("de-activating");
		}
	};
	@Override
	public SceneGraphComponent makeWorld() {
		theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.setGeometry(Primitives.coloredCube());
		return theWorld;
	}
	SceneGraphComponent frontwindow;
	private SceneGraphComponent theWorld;
	static IndexedFaceSet cornerIFS;
	IndexedFaceSetFactory ifsf;
	double a = .2;
	double[][] corners3 = {{1-a,1,0},{1,1,1},{1,1-a,0}};
	double[][] corners = new double[12][3];
	int[][] faces = {{0,1,2},{3,4,5},{6,7,8},{9,10,11}};
	double[][] faceColors = {{1,1,1,1},{1,0,0,1},{0,1,0,1},{0,0,1,1}};
	Color[] bcolors = new Color[4];
	{
		for (int i = 0; i<4; ++i)	{
			double angle = Math.PI*i/(2.0);
			double[] m = P3.makeRotationMatrixZ(null, angle);
			for (int j = 0; j<3; ++j)	{
				Rn.matrixTimesVector(corners[3*i+j], m, corners3[j]);
			}
			bcolors[i] = doubleToColor(faceColors[i]);
			faceColors[i][3] = 1.0;
		}
		ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(corners.length);
		ifsf.setVertexCoordinates(corners);
		ifsf.setFaceCount(4);
		ifsf.setFaceIndices(faces);
		ifsf.setFaceColors(faceColors);
		ifsf.update();
		cornerIFS = ifsf.getIndexedFaceSet();
	}
	Viewer v;
	@Override
	public void customize(JMenuBar menuBar, PluginSceneLoader psl ) {
		v = psl.getViewer();
		attachToViewer(v);
//		v.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, bcolors);
	}

	protected void attachToViewer(Viewer v) {
		this.v = v;
		FrontWindow fw = new FrontWindow(v);
		frontwindow = fw.getWindow();
		frontwindow.setGeometry(cornerIFS);
		frontwindow.addTool(new MouseOverTool(Color.magenta));
		Object bgo = v.getSceneRoot().getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS);
		if (bgo != null && bgo instanceof Color[]) {
			bcolors = (Color[]) bgo;
			for (int i = 0; i<4; ++i)	{
				faceColors[i] = colorToDouble(faceColors[i], bcolors[i]);
				faceColors[i][3] = 0.0;
				ifsf.setFaceColors(faceColors);
				ifsf.update();
			}		
		}
	}

	public static void addBackgroundColorsTool(Viewer v)	{
		TestNearRect tnr = new TestNearRect();
		tnr.attachToViewer(v);
	}
	
	private double[] colorToDouble(double[] dst, Color c)	{
		float[] cc = new float[4];
		c.getRGBComponents(cc);
		for (int i = 0; i<4; ++i)  dst[i] = (double) cc[i];
		return dst;
	}

	private Color doubleToColor(double[] src)	{
		if (src.length >= 4) return new Color((float)src[0], (float)src[1], (float) src[2], (float) src[3]);
		else return new Color((float)src[0], (float)src[1], (float) src[2]);
	}
}
