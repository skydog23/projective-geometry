package charlesgunn.jreality.tools;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JColorChooser;

import charlesgunn.jreality.geometry.FrontWindow;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;

public class BackgroundColorsTool  extends AbstractTool {
		int faceNumber = -1;
		boolean activated = false;
		SceneGraphComponent frontwindow;
		static IndexedFaceSet cornerIFS;
		IndexedFaceSetFactory ifsf;
		boolean changed = false;
		double a = .2;
		double[] oldcolor;
		double[][] corners3 = {{1-a,1,0},{1,1,1},{1,1-a,0}};
		double[][] corners = new double[12][3];
		int[][] faces = {{0,1,2},{3,4,5},{6,7,8},{9,10,11}};
		double[][] faceColors = {{1,1,1,1},{1,0,0,1},{0,1,0,1},{0,0,1,1}};
		Color[] backgroundColors = new Color[4];
		{
			for (int i = 0; i<4; ++i)	{
				double angle = Math.PI*i/(2.0);
				double[] m = P3.makeRotationMatrixZ(null, angle);
				for (int j = 0; j<3; ++j)	{
					Rn.matrixTimesVector(corners[3*i+j], m, corners3[j]);
				}
				backgroundColors[i] = doubleToColor(faceColors[i]);
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
		public BackgroundColorsTool() {
			super(InputSlot.POINTER_HIT);
		}
		@Override
		public void activate(ToolContext tc) {
			SceneGraphComponent cmp = tc.getRootToLocal().getLastComponent();
			PickResult hit = tc.getCurrentPick();
			if (hit == null || hit.getPickType() != PickResult.PICK_TYPE_FACE) return;
			activated = true;
			addCurrentSlot(InputSlot.LEFT_BUTTON, "choose a new color");
			faceNumber = hit.getIndex();
			System.err.println("face number = "+faceNumber);
			extractBackgroundColors(v);
			oldcolor = faceColors[faceNumber];
			faceColors[faceNumber] = complementaryColor(null, faceColors[faceNumber]); //[3] = 1;
			faceColors[faceNumber][3] = 1.0;
			ifsf.setFaceColors(faceColors);
			ifsf.update();
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.0);
			System.err.println("activating");
			changed = false;
		}
		
		@Override
		public void perform(ToolContext tc) {
			if (!tc.getAxisState(InputSlot.LEFT_BUTTON).isPressed()) return;
			Color color = JColorChooser.showDialog((Component) v.getViewingComponent(), "Select color ",  backgroundColors[faceNumber]);
			if (color != null) {
				backgroundColors[faceNumber] = color;
				faceColors[faceNumber] = colorToDouble(faceColors[faceNumber], color);
				ifsf.setFaceColors(backgroundColors);
				ifsf.update();
				changed = true;
				v.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, backgroundColors);
				v.renderAsync();
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			if (!activated || faceNumber < 0) return;
			activated = false;
			removeCurrentSlot(InputSlot.LEFT_BUTTON);
			if (!changed) faceColors[faceNumber] = oldcolor;
			for (int i =0; i<4; ++i) faceColors[i][3] = 0.0;
			ifsf.setFaceColors(backgroundColors);
			ifsf.update();
			SceneGraphComponent cmp = tc.getRootToLocal().getLastComponent();
			cmp.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 1.0);
			System.err.println("de-activating");
		}
	
	protected void attachToViewer(Viewer v) {
		attachToViewer(v, null);
	}
	protected void attachToViewer(Viewer v, SceneGraphPath camPath) {
		this.v = v;
		FrontWindow fw = new FrontWindow(v, camPath);
		frontwindow = fw.getWindow();
		frontwindow.setGeometry(cornerIFS);
		frontwindow.addTool(this);
		frontwindow.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 1.0);
		frontwindow.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		extractBackgroundColors(v);
	}
	protected void extractBackgroundColors(Viewer v) {
		Object bgo = v.getSceneRoot().getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS);
		if (bgo != null && bgo instanceof Color[]) {
			backgroundColors = (Color[]) bgo;
			for (int i = 0; i<4; ++i)	{
				faceColors[i] = colorToDouble(null, backgroundColors[i]);
				faceColors[i][3] = 0.0;
			}		
			ifsf.setFaceColors(faceColors);
			ifsf.update();
		}
	}

	public static void addBackgroundColorsTool(Viewer v, SceneGraphPath cp)	{
		BackgroundColorsTool tnr = new BackgroundColorsTool();
		tnr.attachToViewer(v, cp);
	}

	private double[] complementaryColor(double[] dst, double[] c)	{
		if (dst == null) dst = new double[4];
		for (int i = 0; i<4; ++i)	{ dst[i] = 1.0 - c[i];}
		return dst;
	}

	private double[] colorToDouble(double[] dst, Color c)	{
		float[] cc = new float[4];
		c.getRGBComponents(cc);
		if (dst == null) dst = new double[4];
		for (int i = 0; i<4; ++i)  dst[i] = (double) cc[i];
		return dst;
	}

	private Color doubleToColor(double[] src)	{
		if (src.length >= 4) return new Color((float)src[0], (float)src[1], (float) src[2], (float) src[3]);
		else return new Color((float)src[0], (float)src[1], (float) src[2]);
	}
	public Color[] getBackgroundColors() {
		return backgroundColors;
	}
	public void setBackgroundColors(Color[] backgroundColors) {
		this.backgroundColors = backgroundColors;
	}
}
