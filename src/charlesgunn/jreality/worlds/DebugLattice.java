
/*
 * Created on Jul 14, 2004
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;

/**
 * @author weissman
 *
 */
public class DebugLattice extends LoadableScene {

	public boolean isEncompass() { return false; }
	public  SceneGraphComponent makeWorld()	{
		SceneGraphComponent theRow;
		//Cube ico = new Cube();
		
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theWorld.setName("navComp");
//		theRow = new SceneGraphComponent();
//		theRow.setTransformation(new Transformation());
//		SceneGraphComponent theRowI = new SceneGraphComponent();
//		theRowI.setTransformation(new Transformation());
//		SceneGraphComponent newRow;
//		newRow = new SceneGraphComponent();
//		newRow.setTransformation(new Transformation());
//		MatrixBuilder.euclidean().translate(0.0, 0.0, 1.0).rotateZ(Math.PI/2.0).assignTo(newRow.getTransformation());
//		newRow.addChild(theRow);
		//theWorld.addChild(newRow);
		
//		IndexedFaceSet[] spheres = new IndexedFaceSet[5];
//		for (int i = 0; i<4; ++i)	{
//			if (i == 0)		spheres[0] = Primitives.icosahedron();
//			else {
//				spheres[i] = IndexedFaceSetUtility.binaryRefine(spheres[i-1]);
//				double[][] verts = spheres[i].getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//				int vlength = GeometryUtility.getVectorLength(spheres[i]);
//				Rn.normalize(verts, verts);
//				spheres[i].setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
//			}
//			spheres[i].setVertexAttributes(Attribute.NORMALS, spheres[i].getVertexAttributes(Attribute.COORDINATES)); 
//			GeometryUtility.calculateAndSetFaceNormals(spheres[i]);
//			//spheres[i].buildEdgesFromFaces();
//			
//			DataList vv = spheres[i].getVertexAttributes(Attribute.COORDINATES);
//			int ll = spheres[i].getNumPoints();
//			double[][] vc = new double[ll][4];
//			for (int j=0; j<ll; ++j)	{
//				DoubleArray v = vv.item(j).toDoubleArray();
//				vc[j][0] = .3+.7*v.getValueAt(0);
//				vc[j][1] = .3+.7*v.getValueAt(1);
//				vc[j][2] = .3+.7*v.getValueAt(2);
//				vc[j][3] = 0.5d;		// alpha component
//			}
//			spheres[i].setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vc));
//
//			SceneGraphComponent icokit = SceneGraphUtility.createFullSceneGraphComponent();
//			icokit.setTransformation(new Transformation());
//			icokit.setGeometry(spheres[i]);
//			MatrixBuilder.euclidean().translate(-1.5 + i, 0, 0).scale(.5d).assignTo(icokit.getTransformation());
//			if (i == 0) icokit.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
//			theRow.addChild(icokit);
//
//		}
			
		//theWorld.addChild(theRow);
//		MatrixBuilder.euclidean().translate(0., 3.28, 0.).assignTo(theWorld.getTransformation());
		// create a 5x5x5 lattice which fills the Portal space
		int dim = 5;
		double scale = 4.068*0.3048;  // 2.034; //2*4.068/((dim-1));
		double yscale = 6.561*0.3048; // 1.64; //6.561/(dim-1);
		double yoffset = 1.365*0.3048;
		double[][] bnds = {{-scale, -yscale/2, -scale},{scale, yscale/2, scale}};
//		double[][] bnds = {{-5, -5, -5}, {4,4,4}};
		Rectangle3D portalBox = new Rectangle3D(bnds);
		SceneGraphComponent lattice = makeLattice(portalBox, 5); //-scale*(dim-1)/2., -yscale*(dim-1)/2., -scale*(dim-1)/2., scale, yscale, scale, dim, dim, dim, 0.05, 0.01);
//		lattice.setAppearance(new Appearance());
		theWorld.setAppearance(new Appearance());
		theWorld.addChild(lattice);
		return theWorld;
	}
	
    public static SceneGraphComponent makeLattice(Rectangle3D box, int segments) {
        SceneGraphComponent latticeComp = new SceneGraphComponent();
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
        ap.setAttribute(CommonAttributes.POINT_RADIUS, .04);
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
        ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+".diffuseColor", new Color(.8f, .4f, 0f));
        ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+".diffuseColor", new Color(.3f, .8f, 0f));
        latticeComp.setAppearance(ap);
        int total = segments*segments*segments;  //segments*segments; //
        int n = segments;
        int n2 = segments*segments;
        double factor = segments - 1.0;
        double[][] bnds = box.getBounds();
        double[] extent = box.getExtent();
        double[][] verts = new double[total][3];
        int[][] edges = new int[3*n2][2]; //2*n][2]; //
        for (int i = 0; i<segments; ++i) { //segments; ++i)	{		// z loop
        	for (int j = 0; j<segments; ++j)	{	// y loop
        		for (int k = 0; k<segments; ++k)	{	// x loop
        			int[] lookup = {k,j,i};
        			for (int m = 0; m<3; ++m)	{
            			verts[i*n2+j*n+k][m] = bnds[0][m] + (lookup[m]/factor)*extent[m];	
        			}
        		}
        	}
        }
        for (int i = 0; i<segments; ++i) { //segments; ++i)	{
        	for (int j = 0; j<segments; ++j)	{	
//        		edges[j][0] = j*(n);
//        		edges[j][1] = j*n+ (n-1);
//        		edges[j+n][0] = j;
//        		edges[j+n][1] = n*(n-1)+ j;;
        		edges[i*n+j][0] = i*n+j;
        		edges[i*n+j][1] = i*n+j+n2*(n-1);
        		edges[i*n+j+n2][0] = i*n2+j;
        		edges[i*n+j+n2][1] = i*n2+j+n*(n-1);
        		edges[i*n+j+2*n2][0] = i*n2+j*n;
        		edges[i*n+j+2*n2][1] = i*n2+j*n+(n-1);
        	}
        }
        IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
        ifsf.setVertexCount(total);
        ifsf.setVertexCoordinates(verts);
        ifsf.setEdgeCount(edges.length);
        ifsf.setEdgeIndices(edges);
        ifsf.update();
        IndexedLineSet ils = ifsf.getIndexedLineSet();
        latticeComp.setGeometry(ils);
        return latticeComp;
    }
    
    public static void remoteMain(String[] args)	{
//		ViewerAppLoader val = TestViewerApp.makeViewerAppLoader();
//		val.loadScene(new DebugLattice());
//		ViewerApp va = new ViewerApp(new DebugLattice().makeWorld());
//		MatrixBuilder.euclidean().assignTo(CameraUtility.getCameraNode(va.getViewer()));
//		MatrixBuilder.euclidean().assignTo(va.getJrScene().getPath("avatarPath").getLastComponent());
//		va.getJrScene().getPath("avatarPath").getLastComponent().addTool(new PointerDisplayTool());
//		va.update();
//		va.display();
		JRViewer v = new JRViewer();
//		v.display(new DebugLattice().makeWorld());
		v.addBasicUI();
//		v.addVRSupport();
		v.addContentSupport(ContentType.Raw);
		v.setContent(new DebugLattice().makeWorld());
//		v.registerPlugin(new ContentAppearance());
//		v.registerPlugin(new ContentLoader());
//		v.registerPlugin(new ContentTools());
		Scene scene = v.getPlugin(Scene.class);
		SceneGraphPath apath = scene.getAvatarPath();
		MatrixBuilder.euclidean().translate(0,-1.4,0).assignTo(apath.getLastComponent());
		v.startup();
    }
    
    public static void main(String[] args)	{
    	remoteMain(args);
    }

}
