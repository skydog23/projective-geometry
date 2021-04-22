/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.projective.NullPlaneFactory;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class NullLines extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		Appearance ap = world.getAppearance();
//		ap.setAttribute("polygonShader.diffuseColor", Color.yellow);

		// create a representation of the line as point range
		double[][] verts = {{1,0,0,1}, {-1,0,0,1}};
		double extent = 5;
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(verts[0]);
		prf.setElement1(verts[1]);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(extent);
		prf.setNumberOfSamples(40);
		prf.update();
		IndexedLineSet ils = prf.getLine();
		SceneGraphComponent line = SceneGraphUtility.createFullSceneGraphComponent("line");
		ap = line.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.tubeRadius", .005);
		line.setGeometry(ils);
		world.addChild(line);

		double width = 1;
		double[][] fverts = {{1,-width,0,1}, {-1,-width,0,1}, {-1, width,0,1}, {1,width,0,1}};
		int[][] finds = {{0,1,2,3}};
		
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setVertexCoordinates(fverts);
		ifsf.setFaceCount(1);
		ifsf.setFaceIndices(finds);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		IndexedFaceSet ifs = ifsf.getIndexedFaceSet();
		if (false)	{
			NullPlaneFactory npf = new NullPlaneFactory(prf);
			npf.setNumPlanes(40);
			npf.update();
			SceneGraphComponent npfSGC = npf.getNullPlanes();
			world.addChild(npfSGC);
			return world;
		}
		SceneGraphComponent oneNP = NullPlaneFactory.nullPlaneRepresentation(20);

		SceneGraphComponent firstInstance = SceneGraphUtility.createFullSceneGraphComponent("plane1");
		SceneGraphComponent secondInstance = SceneGraphUtility.createFullSceneGraphComponent("plane2");
		SceneGraphComponent thirdInstance = SceneGraphUtility.createFullSceneGraphComponent("plane3");
		firstInstance.addChild(oneNP);
		secondInstance.addChild(oneNP);
		thirdInstance.addChild(oneNP);
		firstInstance.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(220,220,255));
		secondInstance.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(220,255,220));
		thirdInstance.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255,220,220));
		world.addChildren(firstInstance, secondInstance, thirdInstance);
		MatrixBuilder.euclidean().translate(-1.5,0,0).assignTo(firstInstance);
		MatrixBuilder.euclidean().rotateX(Math.PI/8).assignTo(secondInstance);
		MatrixBuilder.euclidean().rotateX(2*Math.PI/8).translate(1.5,0,0).assignTo(thirdInstance);
		
		double[][] vvec =  {{0,0,1,1}, {0,0,0,1}};			
		IndexedLineSetFactory vec = new IndexedLineSetFactory();
		vec.setVertexCount(2);
		vec.setVertexCoordinates(vvec);
		vec.setEdgeCount(1);
		vec.setEdgeIndices(new int[][]{{1,0}});
		vec.update();
		BallAndStickFactory basf = new BallAndStickFactory(vec.getIndexedLineSet());
		basf.setArrowColor(Color.red);
		basf.setStickColor(Color.red);
		basf.setShowArrows(true);
		basf.setShowBalls(false);
		basf.setArrowPosition(1.0);
		basf.setStickRadius(.02);
		basf.setArrowSlope(2.0);
		basf.setArrowScale(.07);
		basf.update();
		oneNP.addChild(basf.getSceneGraphComponent());
		
		return world;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
	}

}
