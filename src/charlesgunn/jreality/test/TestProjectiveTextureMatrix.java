package charlesgunn.jreality.test;

import java.awt.Color;

import javax.swing.JMenuBar;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.util.SceneGraphUtility;

public class TestProjectiveTextureMatrix extends LoadableScene {

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
	}

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
		double[][][] verts = {{{-1,-1,0},{1,-1,0}},{{-1,1,0},{1,1,0}}};
		BezierPatchMesh thing = new BezierPatchMesh(1, 1, verts);
		thing.refine();
		thing.refine();
		thing.refine();
		IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(thing);
		sgc.setGeometry(ifs);
//		sgc.setGeometry(BigMesh.bigMesh(50, 50, 1000));
		sgc.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		Appearance ap1 = sgc.getAppearance();
		   Texture2D tex2d = null;
		   tex2d = (Texture2D) AttributeEntityUtility
		       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d",ap1, true);		
		    SimpleTextureFactory stf  = new SimpleTextureFactory();
		    stf.setType(SimpleTextureFactory.TextureType.GRAPH_PAPER);//GRADIENT);//
		    stf.setColor(0, Color.blue); //new Color(30,30,50,255));
		    stf.setColor(1, Color.red);
		    stf.update();
		    tex2d.setImage(stf.getImageData());
		   Matrix m = new Matrix();
		   MatrixBuilder.euclidean().scale(2,2,1).translate(-.5,-.5,0).assignTo(m);
		   m.setEntry(3,1, -1);
		   tex2d.setTextureMatrix(m);
		return sgc;
	}

	@Override
	public boolean isEncompass() {
		return true;
	}


}
