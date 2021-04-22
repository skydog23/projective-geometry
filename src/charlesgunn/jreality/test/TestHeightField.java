/*
 * Author	gunn
 * Created on May 4, 2005
 *
 */
package charlesgunn.jreality.test;

import java.awt.geom.Rectangle2D;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.HeightFieldFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class TestHeightField extends LoadableScene {
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent();
			double[][] verts = new double[200][1];
			for (int  i =0; i<20; ++i)	{
				for (int  j =0; j<10; ++j)	{
					verts[10*i+j][0] = 1.0 - (.25*(i-9.5)*(i-9.5)+(j-4.5)*(j-4.5))/50;
				}
			}
			HeightFieldFactory hff = new HeightFieldFactory();
			hff. setULineCount(10);
			hff.setVLineCount(20);
			hff.setClosedInUDirection(false);
			hff.setClosedInVDirection(false);
			hff.setVertexCoordinates(verts);
			hff.setGenerateVertexNormals(true);
			hff.setGenerateFaceNormals(true);
			Rectangle2D.Double domain = new Rectangle2D.Double(-2, -2, 4, 4);
			hff.setRegularDomain(domain);
			hff.update();
			IndexedFaceSet ifs = hff.getIndexedFaceSet();
			theWorld.setGeometry( ifs);
			theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}
			

}
