/*
 * Created on 30 Aug 2024
 *
 */
package charlesgunn.jreality.worlds.misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class PenrosePlanePairs3D extends Assignment {

	double a = 1.0;
	double[][] cubeVerts4 =  
	{{a,a,a,a},{a,a,-a,a},{a,-a,a,a},{a,-a,-a,a},{-a,a,a,a},{-a,a,-a,a},{-a,-a,a,a},{-a,-a,-a,a}};

	double stretch = 1.5, stretchf = 1.0;
	
	int[][] edgeIndices = 
		{ {0,4},{1,5},{2,6},{3,7},
				{0,2}, {1,3}, {4,6}, {5,7},
				{0,1},{2,3},{4,5},{6,7},
				{0,7},{1,6},{2,5},{3,4}
		};
	int[][] edgeIndicesStretched = new int[16][2];
	
	int[][] faceIndices = {
			{0,1,7,6}, {2,3,5,4},
			{8,9,15,14}, {10,11,13,12},
			{16,17,23,22},{18,19,21,20}
	};

	Color[] cls = {Color.red, new Color(100,150,255), Color.green,Color.darkGray};
	
	Color[] edgeColors = {
			cls[0], cls[0], cls[0], cls[0],
			cls[1], cls[1], cls[1], cls[1],
			cls[2], cls[2], cls[2], cls[2],
			cls[3], cls[3], cls[3], cls[3],
	};
	Color[] faceColors = {cls[0], cls[0], cls[1], cls[1], cls[2], cls[2]};
	
	IndexedLineSetFactory ilsf = new IndexedLineSetFactory(),
			ilsfStretched = new IndexedLineSetFactory();
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	SceneGraphComponent edgesSGC = SceneGraphUtility.createFullSceneGraphComponent("edges");
	SceneGraphComponent facesSGC = SceneGraphUtility.createFullSceneGraphComponent("faces");
	
	double[] fvis = {1,1,1,1,1,1}, evis= {1,1,1,1};
	@Override
	public SceneGraphComponent getContent() {
		ilsf.setVertexCount(cubeVerts4.length);
		ilsf.setVertexCoordinates(cubeVerts4);
		ilsf.setEdgeCount(edgeIndices.length);
		ilsf.setEdgeIndices(edgeIndices);
		ilsf.setEdgeColors(edgeColors);
		ilsf.update();
		
		for (int i = 0; i<16; ++i)	{
			edgeIndicesStretched[i][0] = 2*i;
			edgeIndicesStretched[i][1] = 2*i+1;
		}
		ilsfStretched.setVertexCount(32);
		ilsfStretched.setEdgeCount(16);
		ilsfStretched.setEdgeIndices(edgeIndicesStretched);
		ilsfStretched.setEdgeColors(edgeColors);
		
		ifsf.setVertexCount(32);
		ifsf.setFaceCount(6);
		ifsf.setFaceIndices(faceIndices);
		ifsf.setFaceColors(faceColors);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(false);
		update();
		edgesSGC.setGeometry(ilsfStretched.getGeometry());
		facesSGC.setGeometry(ifsf.getGeometry());
		world.addChildren(edgesSGC,facesSGC);
		MatrixBuilder.euclidean().translate(0,0,-4).assignTo(world);
		return world;
	}

	
	@Override
	public void display() {
		super.display();
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.white);
		Camera c = CameraUtility.getCamera(viewer);
		c.setFieldOfView(80.0);
	    CameraUtility.getCameraNode(viewer).addTool(new FlyTool());
	    addCameraLight(.6);
	    
		Component comp = ((Component) viewer.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle camera path");
						break;
		
					case KeyEvent.VK_1:
						edgesSGC.setVisible(!edgesSGC.isVisible());
						break;
					case KeyEvent.VK_2:  // print lengths
						facesSGC.setVisible(!facesSGC.isVisible());
						break;
					case KeyEvent.VK_3:  // print lengths
						if (e.isShiftDown())
							stretch /= 1.1;
						else stretch *= 1.1;
						update();
						break;
					case KeyEvent.VK_4:  // print lengths
						if (e.isShiftDown())
							stretchf /= 1.1;
						else stretchf *= 1.1;
						update();
						break;
					case KeyEvent.VK_5:  // print lengths
						if (e.isShiftDown()) toggleFace(1);
						else toggleFace(0);
						break;
					case KeyEvent.VK_6:  // print lengths
						if (e.isShiftDown()) toggleFace(3);
						else toggleFace(2);
						break;
					case KeyEvent.VK_7:  // print lengths
						if (e.isShiftDown()) toggleFace(5);
						else toggleFace(4);
						break;
					case KeyEvent.VK_8:  // print lengths
						toggleEdges(0);
						break;
					case KeyEvent.VK_9:  // print lengths
						toggleEdges(1);
						break;
					case KeyEvent.VK_0:  // print lengths
						if (e.isShiftDown()) toggleEdges(3);
						else toggleEdges(2);
						break;
					}
				}
		});
	}

	protected void toggleFace(int i)	{
		fvis[i] = 1 - fvis[i];
		update();
	}
	protected void toggleEdges(int i)	{
		evis[i] = 1 - evis[i];
		update();
	}
	protected void update()	{
		double[][] vv = new double[32][],
				vvf = new double[32][];
		for (int i = 0; i<16; ++i)	{
			int j = edgeIndices[i][0],
					k = edgeIndices[i][1];
			double[] v1 = cubeVerts4[j],
					v2 = cubeVerts4[k];
			double realS = i < 12 ? stretch : 1 + (stretch-1)/1.732;
			vv[2*i] = AnimationUtility.linearInterpolation(null, realS, 1, 0, v1, v2);
			vv[2*i+1] = AnimationUtility.linearInterpolation(null, realS, 1, 0, v2, v1);
			realS = i < 12 ? stretchf : 1 + (stretchf-1)/1.732;
			vvf[2*i] = AnimationUtility.linearInterpolation(null, realS, 1, 0, v1, v2);
			vvf[2*i+1] = AnimationUtility.linearInterpolation(null, realS, 1, 0, v2, v1);
		}
		ilsfStretched.setVertexCoordinates(vv);
		System.err.println("evis = "+Rn.toString(evis));
		int sum = 0;
		for (int i = 0; i<4; ++i)	{
			if (evis[i] == 1.0) sum++;
		}
		int[][] eind = new int[4*sum][];
		Color[] ecol = new Color[4*sum];
		sum = 0;
		for (int i = 0; i<4; ++i)	{
			if (evis[i] == 1.0) {
				for (int j = 0; j<4; ++j)	{
					eind[4*sum+j] = edgeIndicesStretched[4*i+j];
					ecol[4*sum+j] = edgeColors[4*i+j];
				}
				sum++;
			}
		}

		ilsfStretched.setEdgeCount(eind.length);
		ilsfStretched.setEdgeIndices(eind);
		ilsfStretched.setEdgeColors(ecol);
		ilsfStretched.update();
		System.err.println("fvis = "+Rn.toString(fvis));
		sum = 0;
		for (int i = 0; i<6; ++i)	{
			if (fvis[i] == 1.0) sum++;
		}
		int[][] find = new int[sum][];
		Color[] fcol = new Color[sum];
		sum = 0;
		for (int i = 0; i<6; ++i)	{
			if (fvis[i] == 1.0) {
				find[sum] = faceIndices[i];
				fcol[sum] = faceColors[i];
				sum++;
			}
		}

		ifsf.setVertexCoordinates(vvf);
		ifsf.setFaceCount(find.length);
		ifsf.setFaceIndices(find);
		ifsf.setFaceColors(fcol);
		ifsf.update();
	}

	public static void main(String[] args) {
		new PenrosePlanePairs3D().display();

	}

}
