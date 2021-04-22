/*
 * Created on Jan 29, 2004
 *
 */
package charlesgunn.jreality.worlds;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;


/**
 * @author gunn
 *
 */
public class Icosahedra extends LoadableScene {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;
	
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent theRow;
		//Cube ico = new Cube();
		
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theRow = new SceneGraphComponent();
		theRow.setName("theRow");
		theRow.setTransformation(new Transformation());
		SceneGraphComponent newRow;
		newRow = new SceneGraphComponent();
		newRow.setName("newRow");
		newRow.setTransformation(new Transformation());
		MatrixBuilder.euclidean().translate(0.0, 0.0, 1.0).rotateZ(Math.PI/2).assignTo(newRow.getTransformation());
		newRow.addChild(theRow);
		newRow.setAppearance(new Appearance());
		Appearance ap = newRow.getAppearance();
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.SMOOTH_LINE_SHADING, true);

		IndexedFaceSet[] spheres = new IndexedFaceSet[7];
		for (int i = 0; i<6; ++i)	{
			spheres[i] = SphereUtility.tessellatedIcosahedronSphere(i, false);
			DataList vv = spheres[i].getVertexAttributes(Attribute.COORDINATES);
			int ll = spheres[i].getNumPoints();
			double[][] vc = new double[ll][4];
			for (int j=0; j<ll; ++j)	{
				DoubleArray v = vv.item(j).toDoubleArray();
				vc[j][0] = .3+.7*v.getValueAt(0);
				vc[j][1] = .3+.7*v.getValueAt(1);
				vc[j][2] = .3+.7*v.getValueAt(2);
				vc[j][3] = 0.5d;		// alpha component
			}
			spheres[i].setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vc));

			icokit = SceneGraphUtility.createFullSceneGraphComponent("sphere"+i);
			icokit.setTransformation(new Transformation());
			icokit.setGeometry(spheres[i]);
			MatrixBuilder.euclidean().translate(-2.5 + i, 0, 0).scale(.5).assignTo(icokit.getTransformation());
			if (i == 0) icokit.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			theRow.addChild(icokit);

		}
			
		theWorld.addChild(theRow);
		theWorld.addChild(newRow);
		
		theWorld.setAppearance(new Appearance());
		theWorld.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		return theWorld;
	}

	public boolean isEncompass() {
		return true;
	}
			
	
}
