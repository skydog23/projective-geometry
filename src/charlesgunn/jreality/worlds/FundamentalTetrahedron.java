/*
 * Created on Jun 2, 2010
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class FundamentalTetrahedron extends LoadableScene {

	Color[] edgeC = {
			Color.blue,
			Color.red,
			Color.green,
			Color.green,
			Color.red,
			Color.blue,
			Color.cyan,
			Color.pink,
			Color.red,
			Color.orange,
			Color.yellow,
			Color.magenta,
			Color.gray,
			Color.black
	};
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
		SceneGraphComponent tetra = SceneGraphUtility.createFullSceneGraphComponent("tetra");
		world.addChild(tetra);
		ap = tetra.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("lineShader.textShader.offset", new double[]{0,0,.1});
		ap.setAttribute("lineShader.textShader.scale", .01);
		double[][] verts = {{1,1,1,1}, {-1,-1,1,1}, {-1,1,-1,1}, {1,-1,-1,1}};
		int[][] inds = {{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
		int[][] finds = {{0,1,2}, {1,2,3},{2,3,0},{3,0,1}};
		String[] vlabels = {"e0", "e1", "e2", "e3"};
		String[] elabels = {"e01","e02","e03","e12","e31","e23"};
		IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setVertexCoordinates(verts);
		ifsf.setEdgeCount(6);
		ifsf.setEdgeIndices(inds);
		ifsf.setVertexLabels(vlabels);
		ifsf.setEdgeLabels(elabels);
		ifsf.update();
//		tetra.setGeometry(ifsf.getGeometry());
		
		for (int i = 0; i<inds.length; ++i)	{
			PointRangeFactory prf = new PointRangeFactory();
			prf.setElement0(verts[inds[i][0]]);
			prf.setElement1(verts[inds[i][1]]);
			prf.setFiniteSphere(true);
			prf.setSphereRadius(20);
			prf.setNumberOfSamples(2);
			prf.update();
			IndexedLineSet ils = prf.getLine();
			SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			ap = child.getAppearance();
			ap.setAttribute("lineShader.polygonShader.diffuseColor", edgeC[i]);
			child.setGeometry(ils);
			world.addChild(child);
		}
		MatrixBuilder.euclidean().translate(0, 0, -4).assignTo(world);
		return world;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
	}

}
