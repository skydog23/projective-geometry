package charlesgunn.jreality.worlds;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;

public class LabelsOnCube extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {

	      IndexedFaceSet ifs = Primitives.cube();
	      IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
	      IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
	      //ifs = new CatenoidHelicoid(7);
	  		label(ifs);
	      
	      SceneGraphComponent cmp = new SceneGraphComponent();
	      Appearance a = new Appearance();
	      cmp.setAppearance(a);
	      cmp.setGeometry(ifs);
	      
	      DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(a, false);
	      DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
	      DefaultTextShader ets = (DefaultTextShader) ((DefaultLineShader)dgs.getLineShader()).getTextShader();
	      DefaultTextShader fts = (DefaultTextShader) ((DefaultPolygonShader)dgs.getPolygonShader()).getTextShader();
	      dgs.setShowPoints(true);
	      pts.setDiffuseColor(Color.blue);
	      ets.setDiffuseColor(Color.orange);
	      fts.setDiffuseColor(Color.green);
	      
	      Double scale = new Double(0.01);
	      pts.setScale(scale);
	      ets.setScale(scale);
	      fts.setScale(scale);
	      
	      double[] offset = new double[]{-.1,0,-0.3};
	      pts.setOffset(offset);
	      ets.setOffset(offset);
	      fts.setOffset(offset);
	      
	      dgs.setShowPoints(Boolean.TRUE);
	      return cmp;
	      
	}
    public static void label(IndexedFaceSet ps) {
        int n=ps.getNumPoints();
        String[] labels=new String[n];
        for (int i = 0; i<n; i++) labels[i] = "Point "+i;
        ps.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));

        n=ps.getNumEdges();
        labels=new String[n];
        for (int i = 0; i<n; i++) labels[i] = "Edge "+i;
        ps.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));

        n=ps.getNumFaces();
        labels=new String[n];
        for (int i = 0; i<n; i++) labels[i] = "Face "+i;
        ps.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
  }

 
}
