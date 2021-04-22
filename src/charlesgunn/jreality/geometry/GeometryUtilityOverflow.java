package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.logging.Level;

import charlesgunn.anim.util.AnimationUtility;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class GeometryUtilityOverflow {
	static double[][] viewerVerts = {{1d,0d,-1.5d},{1d,1d,-1.5d},{-1d,1d,-1.5d},{-1d, 0d, -1.5d}};
	static double[] tip = {0,.5d,-1d};
	
	private static SceneGraphComponent _camIcon = null;
	public static IndexedFaceSet sphereAsTriangStrip = null;
	static {
		_camIcon = SceneGraphUtility.createFullSceneGraphComponent("cameraIcon");
		_camIcon.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		_camIcon.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		_camIcon.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		_camIcon.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().scale(.3,1.0,1.0).assignTo(_camIcon.getTransformation());

		SceneGraphComponent viewer = new SceneGraphComponent();
		viewer.setGeometry(Primitives.pyramid(viewerVerts, tip));
		_camIcon.addChild(viewer);
	}
	public static double[] anglesFromSides(double[] sides, int metric)	{
		double[] angles = new double[3];
		double[] squares = {sides[0]*sides[0],sides[1]*sides[1], sides[2]*sides[2]};
		for (int i =0; i<3; ++i)	{
			if (sides[i] > sides[(i+1)%3]+sides[(i+2)%3]) 
				throw new IllegalArgumentException("Side is too long");
			angles[i] = Math.acos((squares[i]-squares[(i+1)%3]-squares[(i+2)%3])/(-2 * sides[(i+1)%3]*sides[(i+2)%3]));
		}
		
		return angles;
	}
	public static IndexedLineSet attachVectorField(PointSet ps, double[][] vectors)	{
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		int n = ps.getNumPoints();
		ilsf.setVertexCount(2*n);
		double[][] vv = new double[n*2][];
		double[][] base = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int[][] indices = new int[n][2];
		for (int i = 0; i<n; ++i) {
			vv[i] = base[i];
			vv[n+i] = Rn.add(null, base[i], vectors[i]);
			indices[i][0] = i;
			indices[i][1] = n+i;
		}
		ilsf.setVertexCoordinates(vv);
		ilsf.setEdgeCount(n);
		ilsf.setEdgeIndices(indices);
		ilsf.update();
		
		return ilsf.getIndexedLineSet();
	}
	
	public static SceneGraphComponent boxedSignFromString(String s, double boxHeight, double textHeight, Font font)	{
		if (font == null)  font = new Font("Serif",Font.PLAIN,32);
		BufferedImage i = LabelUtility.createImageFromString(s,font, Color.white, Color.black );
		return boxedTerrainFromImage(i, boxHeight, textHeight, true);
    }
	
	public static SceneGraphComponent boxedTerrainFromImage(BufferedImage i, double boxHeight, double textHeight, boolean zflipped)	{
    	SceneGraphComponent all = SceneGraphUtility.createFullSceneGraphComponent();
    	all.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
    	
		SceneGraphComponent theGrid = SceneGraphUtility.createFullSceneGraphComponent();
//		Appearance ap = theGrid.getAppearance();
//		Texture2D tex2d = (Texture2D) AttributeEntityUtility
//	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);	
//	  	ImageData it = new ImageData(i);
//	  	tex2d.setImage(it);
//		tex2d.setApplyMode(Texture2D.GL_REPLACE);
//		tex2d.setRepeatS(Texture2D.GL_CLAMP);
//		tex2d.setRepeatT(Texture2D.GL_CLAMP);
//		Matrix m = new Matrix();
//		MatrixBuilder.euclidean().translate(0,1,0).scale(1,-1,1).assignTo(m);
//		tex2d.setTextureMatrix(m);
		double w = i.getWidth(),
			h = i.getHeight();
		Rectangle3D domain = new Rectangle3D(new double[][]{{-.5*w, -.5*h, 0},
			{.5*w, .5*h, (h*textHeight)}
		});
    	theGrid.setGeometry(heightFieldFromImage(i, domain, zflipped));
//    	theGrid.addChild(GeometryUtilityOverflow.displayFaceNormals((IndexedFaceSet)theGrid.getGeometry(),1));
    	all.addChild(theGrid);
//    	if (zflipped) MatrixBuilder.euclidean().translate(0,0,h*boxHeight).scale(1,-1,1).translate(0,0,h*textHeight).scale(1,1,-1).translate(0,0,-h*textHeight).assignTo(theGrid);
//    	else 
    	MatrixBuilder.euclidean().translate(0,0,h*(boxHeight+textHeight)).rotateX(Math.PI).assignTo(theGrid);
   	
    	SceneGraphComponent base = new SceneGraphComponent();
    	base.setAppearance(new Appearance());
    	all.addChild(base);
    	base.setGeometry(Primitives.openCube());
		domain = new Rectangle3D(new double[][]{{-.5*w, -.5*h, 0},
				{.5*w, .5*h, (h*boxHeight)}
		});
    	double[] extent = domain.getExtent();
    	System.err.println("extent: "+Rn.toString(extent));
    	MatrixBuilder.euclidean().translate(domain.getMinX(), domain.getMinY(), domain.getMinZ()).
    		scale(extent).scale(.5).translate(1,1,1).assignTo(base);
    	MatrixBuilder.euclidean().scale(1.0/w).assignTo(all);
    	return all;
    }
	
	public static double[] calculateVertexCurvature(IndexedFaceSet ifs)	{
		int n = ifs.getNumPoints();
		double[] curvature = new double[n];
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int m = indices.length;
		for (int i = 0; i<m; ++i)		{
			int k = indices[i].length;
			for (int j = 0; j<k; ++j)	{
				int v0 = indices[i][j];
				int vp = indices[i][(j+k-1)%k];
				int vn = indices[i][(j+1)%k];
				double angle = Rn.euclideanAngle(Rn.subtract(null,verts[vn], verts[v0]), Rn.subtract(null, verts[vp],verts[v0]));
				curvature[v0] += angle;
			}
		}
		for (int i = 0; i<n; ++i)		curvature[i] = Math.PI - curvature[i];
		return curvature;
	}
	
	public static SceneGraphComponent cameraIcon(double scale)		{
		SceneGraphComponent ci = SceneGraphUtility.createFullSceneGraphComponent("cameraIcon");
		MatrixBuilder.euclidean().scale(scale).assignTo(ci.getTransformation());
		ci.addChild(_camIcon);
		return ci;
	}
	
	public static PointSet pointSet(double[][] verts, Color[] clrs)	{
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(verts.length);
		psf.setVertexCoordinates(verts);
		if (clrs != null) psf.setVertexColors(clrs);
		psf.update();
		return psf.getPointSet();
	}
	
	public static IndexedFaceSet texturedDisk(int rays, int circles)	{
		return texturedDisk(rays, circles, false, true);
	}
	public static IndexedFaceSet texturedDisk(int rays, int circles, final boolean old, final boolean polar)	{
		return texturedDiskFactory(rays, circles, old, polar).getIndexedFaceSet();
	}
	public static ParametricSurfaceFactory texturedDiskFactory(int rays, int circles, final boolean old, final boolean polar)	{
	      ParametricSurfaceFactory factory = new ParametricSurfaceFactory(new ParametricSurfaceFactory.DefaultImmersion() {
		        public void evaluate(double u, double v) {
		        	// the idea seems to be to 
		        	//  1) leave a little hole in the middle of the disk
		        	//  2) make the polygons get bigger as they move further out
		        	double foo = v;
		          if (old)  foo = Math.exp(v) - Math.exp(-2.9);
		          x=foo*Math.cos(u);
		          y=foo*Math.sin(u);
		          z=0;
		        }
		      });
		      
		      factory.setULineCount(rays);
		      factory.setVLineCount(circles);
		      
		      factory.setClosedInUDirection(false); //true);
		      factory.setClosedInVDirection(false);
		      
		      factory.setUMin(0);
		      factory.setUMax(Math.PI*2);
		      factory.setVMin(old ? -3.0 : 0);
		      factory.setVMax(old ? 0.0 : 1.0);
		      
		      factory.setGenerateFaceNormals(true);
		      factory.setGenerateVertexNormals(false); // ??
		      factory.setGenerateTextureCoordinates(polar);
		      factory.setGenerateEdgesFromFaces(true);
		      factory.setEdgeFromQuadMesh(true);
		      factory.update();
		      if (!polar) {
			      DataList verts = factory.getIndexedFaceSet().getVertexAttributes(Attribute.COORDINATES);
			      factory.getIndexedFaceSet().setVertexAttributes(Attribute.TEXTURE_COORDINATES, verts);		    	  
		      }
		      return factory;      
	}
	public static SceneGraphComponent closedCylinder(int n,   double r, double R, double zmin, double zmax, double thetamax) {
		if (Math.abs(thetamax - 2*Math.PI) > 10E-4)
			throw new IllegalArgumentException("Can only do full cylinders");
		SceneGraphComponent result = new SceneGraphComponent("closedCylinder");
		SceneGraphComponent d1 = new SceneGraphComponent("disk1"), d2 = new SceneGraphComponent("disk2");
		IndexedFaceSet cyl = Primitives.cylinder(n, r, R, zmin, zmax, thetamax);
		result.setGeometry(cyl);
		IndexedFaceSet disk = texturedDisk(n+1, 2, false, false);
		d1.setGeometry(disk);
		d2.setGeometry(disk);
		result.addChild(d1);
		result.addChild(d2);
		MatrixBuilder.euclidean().translate(0,0,zmin).scale(r,R,1).assignTo(d1);
		MatrixBuilder.euclidean().translate(0,0,zmax).rotateX(Math.PI).scale(r,R,1).assignTo(d2);
		return result;
	}

	public static IndexedFaceSet triangulateQuadMesh(final IndexedFaceSet ifs)	{
//    	if (ifs.getEdgeAttributes(Attribute.INDICES) == null)
//    		throw new IllegalArgumentException("Must have edges");
    	Object obj = ifs.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
    	if (obj == null || !(obj instanceof Dimension)) 
    		throw new IllegalArgumentException("Must be a quad mesh");
    	int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
    	final int[][] newindices = new int[indices.length * 2][];
    	for (int i = 0; i<indices.length; ++i)	{
    		int[] j = indices[i];
    		newindices[2*i] = new int[]{j[0], j[1], j[2]};
    		newindices[2*i+1]=new int[]{j[2], j[3], j[0]};
    	}
    	Scene.executeWriter(ifs, new Runnable() {
			
			@Override
			public void run() {
				ifs.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(newindices));
				IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
				IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
			}
		});
    	return ifs;
	} 
	public static IndexedFaceSet diamondize(IndexedFaceSet ifs)	{
		return diamondize(ifs, .5);
	}

	public static IndexedFaceSet diamondize(IndexedFaceSet ifs, double weight)	{
//    	if (ifs.getEdgeAttributes(Attribute.INDICES) == null)
//    		throw new IllegalArgumentException("Must have edges");
    	Object obj = ifs.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
    	if (obj == null || !(obj instanceof Dimension)) 
    		throw new IllegalArgumentException("Must be a quad mesh");
    	boolean uclosed = isVClosed(ifs);
    	boolean vclosed = isUClosed(ifs);
    	Dimension dim = (Dimension) obj;
    	int w = dim.width;
    	int h = dim.height;
    	int numOldFaces = ifs.getNumFaces();
    	if ( (w-1)*(h-1) != numOldFaces) 
    		throw new IllegalStateException("Bad face count");
    	int numv = h*(w-1) + w*(h-1);
    	double[][] newv = new double[numv][];
    	System.err.println(numv+" new vertices");
    	double[][] oldv = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    	int horizontalEdges = h*(w-1);
    	int verticalEdges = w*(h-1);
    	for (int i = 0; i<h; ++i)	{
    		// midpoints of horizontal edges
    		for (int j = 0; j<w-1; ++j)	{
    			double s = ((i+j)%2==0)? weight : 1-weight;
    			double t = 1-s;
    			newv[i*(w-1)+j] = Rn.linearCombination(null, s, oldv[i*w+j], t, oldv[i*w+j+1]);
    		}
    		// midpoints of vertical edges
        	if (i != (h-1))
        		for (int j = 0; j<w; ++j)	{
        			double s = ((i+j)%2==1)? weight : 1-weight;
        			double t = 1-s;
        			newv[horizontalEdges+i*w+j] = Rn.linearCombination(null, s, oldv[i*w+j], t, oldv[(i+1)*w+j]);
    		}
    	}
		int numf = numOldFaces + (w-2)*(h-2); // faces are centered on old faces and old vertices
    	if (uclosed) numf += h-2;
    	if (vclosed) numf += w-2;
    	if (uclosed && vclosed) numf++;
    	int[][] newf = new int[numf][];
    	System.err.println(numf+" new faces");
    	int foffset = numOldFaces;
    	// create the faces which are centered on the old faces
       	int imod = vclosed ? (h-1) : h;
    	int jmod = uclosed ? (w-1) : w;
    	for (int i = 0; i<h-1; ++i)	{
    		for (int j = 0; j<(w-1); ++j)	{
    			int[] pp = newf[i*(w-1)+j] = new int[4];
    			pp[0] = i*(w-1)+j;
    			pp[1] = horizontalEdges + i*(w) + j;
    			pp[2] = ((i+1) % imod) *(w-1)+j;
    			pp[3] = horizontalEdges + i*w + (j+1)%jmod;
    		}
    	}
    	// now create the faces centered on the old vertices
    	int ilim = vclosed ? (h-1) : (h-2);
    	int jlim = uclosed ? (w-1) : (w-2);
     	for (int i = 0; i<ilim; ++i)	{
    		for (int j = 0; j<jlim; ++j)	{
    			int[] pp = newf[foffset+i*(jlim)+j] = new int[4];
    			pp[0] = horizontalEdges + i*(w)+ (j+1) % (jmod);
    			pp[1] = ((i+1)%imod)*(w-1) + j;
    			pp[2] = horizontalEdges + (((i+1)%imod)*(w)+ (j+1)%jmod)%verticalEdges;
    			pp[3] = ((i+1)%imod)*(w-1) + (j + 1)%jmod;
    		}
    	}
    	if (uclosed && vclosed)	{
			int[] pp = newf[newf.length - 1] = new int[4];
			pp[0] = 0;
			pp[1] = numv - w;
			pp[2] = w-2;
			pp[3] = horizontalEdges;
    	}
    	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
    	ifsf.setVertexCount(newv.length);
    	ifsf.setVertexCoordinates(newv);
    	ifsf.setFaceCount(newf.length);
    	ifsf.setFaceIndices(newf);
    	ifsf.setGenerateEdgesFromFaces(true);
    	ifsf.setGenerateFaceNormals(true);
    	ifsf.update();
    	
    	return ifsf.getIndexedFaceSet();
    }

	public static SceneGraphComponent displayFaceNormals(SceneGraphComponent sgc, double scale)	{
		SceneGraphComponent ret = SceneGraphUtility.createFullSceneGraphComponent();
		return ret;
	}
	public static SceneGraphComponent displayFaceNormals(IndexedFaceSet ifs, double scale)	{
    	return displayFaceNormals(ifs, scale, Pn.EUCLIDEAN);
    }
	public static SceneGraphComponent displayFaceNormals(IndexedFaceSet ifs, double scale, int metric)	{
    	SceneGraphComponent sgc = new SceneGraphComponent("displayFaceNormals()");
    	System.err.println("display face normals metric = "+metric);
    	Appearance ap  = new Appearance();
    	ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
       	ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
    	ap.setAttribute(CommonAttributes.FACE_DRAW, false);
    	ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
    	sgc.setAppearance(ap);
    	int[][] faces = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
   
    	int n = faces.length;
		int[][] edges = new int[n][2];
		double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    	int fiberlength = verts[0].length;
		double[][] normals = null,
    		nvectors = new double[2*n][fiberlength];
    	if (ifs.getFaceAttributes(Attribute.NORMALS) != null)
    		normals = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
     	else normals = IndexedFaceSetUtility.calculateFaceNormals(ifs);
//    	System.err.println("Vertex fiber is "+fiberlength);
//    	System.err.println("Normal fiber is "+normals[0].length);
     	for (int i = 0; i<n; ++i)	{
     		// find the center of the face
    		for (int k = 0; k<faces[i].length; ++k)	{
    			Rn.add(nvectors[i], verts[faces[i][k]], nvectors[i]);
    		}
    		double xx = 1.0/faces[i].length;
    		if (metric == Pn.EUCLIDEAN) {
    			Rn.times(nvectors[i], xx, nvectors[i]);
        		Rn.add(nvectors[i+n], nvectors[i], Rn.times(null, scale, normals[i]));
    		}
    		else {
//    			System.err.println("Dot product is "+Pn.innerProduct(nvectors[i], normals[i], metric));
    			Pn.dragTowards(nvectors[i+n], nvectors[i], normals[i], scale, metric);
    		}
    		if (fiberlength == 4) {
    			if (metric == Pn.EUCLIDEAN) nvectors[i+n][3] = 1.0;
    			else Pn.dehomogenize(nvectors[i+n], nvectors[i+n]);
    		}
 //   		System.err.println("Normal length is "+Rn.euclideanNorm(normals[i]));
    		edges[i][0] = i;
    		edges[i][1] = i+n;
    	}
    	IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
    	ilsf.setVertexCount(2*n);
    	ilsf.setVertexCoordinates(nvectors);
    	ilsf.setEdgeCount(n);
    	ilsf.setEdgeIndices(edges);
    	ilsf.update();
    	sgc.setGeometry(ilsf.getIndexedLineSet());
    	return sgc;
    }

	public static void flipNormals(IndexedFaceSet ifs)	{
		DataList vn = ifs.getVertexAttributes(Attribute.NORMALS);
		if (vn != null)	{
			double[][] vnn = vn.toDoubleArrayArray(null);
			Rn.times(vnn, -1, vnn);
			ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vnn[0].length).createReadOnly(vnn));
		}
		DataList fn = ifs.getFaceAttributes(Attribute.NORMALS);
		if (fn != null)	{
			double[][] fnn = fn.toDoubleArrayArray(null);
			Rn.times(fnn, -1, fnn);
			ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(fnn[0].length).createReadOnly(fnn));
		}
	}
    public static IndexedFaceSet heightFieldFromImage(BufferedImage img, Rectangle3D domain, boolean zflipped)	{
     	IndexedFaceSet ifs = null;
 	   Raster raster = img.getRaster();
 	   int height = raster.getHeight();
 	   int width = raster.getWidth();
  	   if (raster.getDataBuffer().getClass() != DataBufferInt.class) 
 		   throw new IllegalStateException("Can only handle DataBufferInt's");
 	   int[] byteArray = ((DataBufferInt) raster.getDataBuffer()).getData();
	   double[][] heights = new double[height*width][3];
	   double xmin = domain.getMinX(), xmax = domain.getMaxX(),
	   		ymin = domain.getMinY(), ymax = domain.getMaxY(), 
	   		dx = (xmax - xmin)/(width-1), dy = (ymax - ymin)/(height-1),
	   		zmin = domain.getMinZ(), dz = domain.getMaxZ() - zmin;
	   // unfortunately the image string is flipped in the y-direction
	   for (int y = 0, ptr = 0; y < height; y++) {       	 
        	 for (int x = 0; x < width; x++, ptr ++) {             
        		 int entry  = byteArray[ptr];
        		 int sum = (entry & 255) + ((entry >> 8)&255) + ((entry>>16)&255); // + ((entry>>24)&255);
         		 heights[y*width+x][0] = xmin + x*dx;
         		 heights[y*width+x][1] = ymin + y*dy;
         		 double value = sum/782.0;
         		 if (zflipped) value = 1.0 - value;
         		 heights[y*width+x][2] = (zmin + dz *value);
          }
        }
		QuadMeshFactory hff = new QuadMeshFactory();
		hff.setULineCount(width);
		hff.setVLineCount(height);
		hff.setVertexCoordinates(heights);
		hff.setEdgeFromQuadMesh(true);
		hff.setGenerateVertexNormals(true);
		hff.setGenerateFaceNormals(true);
		hff.setGenerateTextureCoordinates(true);  
		hff.update();
		ifs = hff.getIndexedFaceSet();
		return ifs;
    }
    public static boolean isUClosed(IndexedFaceSet ifs)	{
	   	Object obj = ifs.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
    	if (obj == null) return false;
    	Dimension dim = (Dimension) obj;
    	int n = dim.width;
    	int m = dim.height;
    	double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
     	// check first and last row
    	for (int i = 0; i<n; ++i)	{
    		int j = (m-1)*n+i;
    		double d = Rn.euclideanDistance(verts[i], verts[j]);
    		if ( d > 10E-8) return false;
    	}
		return true;
	}
    public static boolean isVClosed(IndexedFaceSet ifs)	{
	   	Object obj = ifs.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
    	if (obj == null) return false;
    	Dimension dim = (Dimension) obj;
    	int n = dim.width;
    	int m = dim.height;
    	double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
     	// check first and last row
    	for (int i = 0; i<m; ++i)	{
    		int j = i*n;
    		int k = (i+1)*n-1;
    		double d = Rn.euclideanDistance(verts[j], verts[k]);
     		if ( d > 10E-8) return false;
    	}
		return true;
	}
    public static IndexedFaceSet plainQuadMesh(final double[][] verts, int xDetail, int yDetail)	{
       ParametricSurfaceFactory factory = new ParametricSurfaceFactory(new ParametricSurfaceFactory.DefaultImmersion() {
            public void evaluate(double u, double v) {
            	double[] pt = Rn.linearCombination(null, (1-v), 
            		Rn.linearCombination(null, 1-u, verts[0], u, verts[1])	, 
            		v,
            		Rn.linearCombination(null, 1-u, verts[2], u, verts[3]));
              x=pt[0];
              y=pt[1];
              z=pt[2];
            }
          });
          
          factory.setULineCount(xDetail+1);
          factory.setVLineCount(yDetail+1);
          
          factory.setClosedInUDirection(false);
          factory.setClosedInVDirection(false);
          
          factory.setUMin(0);
          factory.setUMax(1);
          factory.setVMin(0);
          factory.setVMax(1);
          
          factory.setGenerateFaceNormals(true);
          factory.setGenerateVertexNormals(false); // ??
          factory.setGenerateTextureCoordinates(true);
          
          factory.update();
          
          return factory.getIndexedFaceSet();      
    	
    }
    /**
     * Generate a rectangular quad mesh centered at the origin with a mesh of dimension
     * <i>(xDetail,yDetail)</i>.
     * 
     * @param xStep
     * @param yStep
     * @param xDetail
     * @param yDetail
     * @return
     */
	public static IndexedFaceSet plainQuadMesh(double xStep, double yStep, int xDetail, int yDetail) {
		double[][] verts = {{-xStep/2,-yStep/2,0},{xStep/2,-yStep/2,0},{-xStep/2,yStep/2,0},{xStep/2,yStep/2,0}};
		return plainQuadMesh(verts, xDetail, yDetail);
	}
	
    public static void removeBoundaryDuplicates(IndexedFaceSet qm)	{
    	Object obj = qm.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
    	if (obj == null) return;
    	Dimension dim = (Dimension) obj;
    	int n = dim.width;
    	int m = dim.height;
    	double[][] verts = qm.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    	int[][] ind = qm.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
    	boolean uclosed = true;
    	// check first and last row
    	for (int i = 0; i<n; ++i)	{
    		int j = (m-1)*n+i;
    		double d = Rn.euclideanDistance(verts[i], verts[j]);
     		if ( d > 10E-8) uclosed = false;
    	}
    	boolean vclosed = true;
    	// check first and last column
    	for (int i = 0; i<m; ++i)	{
    		int j = i*n;
    		int k = (i+1)*n-1;
    		double d = Rn.euclideanDistance(verts[j], verts[k]);
    		if ( d > 10E-8) vclosed = false;
    	}
    	if (uclosed)
    	  for (int i = 0; i<n-1; ++i)	{
    		int j = (m-2)*(n-1)+i;
    		ind[j][1] = i;
    		ind[j][2] = i+1;
    	  }
    	if (vclosed) 
    		for (int i = 0; i<m-1; ++i)	{
    		int j = (i+1)*(n-1)-1;
    		ind[j][3] = i*n;
    		ind[j][2] = (i+1)*n;
     	}
    	if (uclosed && vclosed) ind[(n-1)*(m-1)-1][2] = 0;
    	qm.setFaceAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(ind));
    	IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(qm);
   	System.err.println("Uclosed = "+uclosed+" VClosed = "+vclosed);
    }
    /*
	static	{
		sphereAsTriangStrip = new IndexedFaceSet(102, 10);
		double[][] verts = new double[102][3];
		int[][] indices = new int[10][];
		
		int count = 0, count2;
		double theta = 0, phi = 0;
		for (int i = 0; i<5; ++i)		{
			phi = (Math.PI/2.0 ) * (i/5.0);
			double cp = Math.cos(phi);
			double sp = Math.sin(phi);
			int lim = 4 * (5-i);
			indices[i] = new int[lim];
			indices[9-i] = new int[lim];
			for (int j = 0; j < lim; ++j)	{
				theta = (Math.PI * 2.0  * j)/lim;
				double ct = Math.cos(theta);
				double st = Math.sin(theta);
				verts[count] = new double[] {cp * ct, cp * st, sp};
				
				count++;
			}
		}
		verts[count++] = new double[] {0,0,1};
		System.err.println("vertex in n hemisphere: "+count);
		for (int i = 20; i<count; ++i)		{
			Rn.copy(verts[i+count-20], verts[i]);
			verts[i+count][2] *= -1.0;
		}
		for (int i = 0; i<5; ++i)	{
		}
	}
	*/
	public static IndexedFaceSet representBezierPatchMeshAsQuadMesh(BezierPatchMesh bpm)	{
	return representBezierPatchMeshAsQuadMesh(null, bpm);
}
    public static IndexedFaceSet representBezierPatchMeshAsQuadMesh(IndexedFaceSet existing, BezierPatchMesh bpm)	{
		double[][][] thePoints = bpm.getControlPoints();
		//if (qmpatch == null) 
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setMetric(Pn.EUCLIDEAN);
		qmf.setULineCount(thePoints[0].length);
		qmf.setVLineCount( thePoints.length);
	    double[] verts1d = Rn.convertArray3DToArray1D(thePoints);
	    qmf.setVertexCoordinates(verts1d);
	    qmf.setGenerateFaceNormals(true);
	    qmf.setGenerateVertexNormals(true);
	    qmf.update();
		existing = qmf.getIndexedFaceSet();
		return existing;
	}

    /**
	 * Create a surface of revolution surface by rotating the profile curve around the X-axis.
	 * The resulting array with have the original curve twice, once at the beginning and also
	 * at the end.  
	 * @param profile	a 3- or 4-d array of points (generally of form (x,y,0) or (x,y,0,1))
	 * @param num		number of copies of the curve to make
	 * @return
	 * @see 
	 */
	public static double[][] surfaceOfRevolution(double[][] profile, int num, double angle) {
		if (num <= 1 || profile[0].length < 3) {
			throw new IllegalArgumentException("Bad parameters");
		}
		double[][] vals = new double[num * profile.length][profile[0].length];
		for (int i = 0 ; i < num; ++i)	{
			double a = i * angle/(num-1);
			double[] rot = P3.makeRotationMatrixX(null, a);
			for (int j = 0; j<profile.length; ++j)
				Rn.matrixTimesVector(vals[i*profile.length+j], rot, profile[j]);
		}
		return vals;
	}
    /*
	 * @deprecated
	 */
	public static IndexedFaceSet surfaceOfRevolutionAsIFS(double[][] profile, int num, double angle)	{
		QuadMeshFactory qmf = new QuadMeshFactory();//Pn.EUCLIDEAN, profile.length, num, false, false);
		qmf.setULineCount(profile.length);
		qmf.setVLineCount(num);
		double[][] vals = surfaceOfRevolution(profile, num, angle);
		qmf.setVertexCoordinates(vals);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		return qmf.getIndexedFaceSet();
	}

	public static void transformTextureCoordinates(IndexedFaceSet ifs, double[] tm)	{
    	if (ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES) == null) return;
    	double[][] texc = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
    	for (int i = 0; i<texc.length; ++i)		{
    		double[] t4 = {texc[i][0], texc[i][1], 0.0, 1.0};
    		double[] t4t = Rn.matrixTimesVector(null, tm, t4);
    		Pn.dehomogenize(t4t, t4t);
    		texc[i][0] = t4t[0]; texc[i][1] = t4t[1];
    	}
    	ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
    }
	
	/**
	 * Rough clipping: retains all faces one of whose vertices lies within the clipping box <i>box</i>.
	 * Currently not all attributes are retained.
	 * @param ifs
	 * @param box
	 * @return
	 */
	public static IndexedFaceSet clipToBox(IndexedFaceSet ifs, Rectangle3D box)	{
//		IndexedFaceSet clipped = new IndexedFaceSet();
		int n = ifs.getNumFaces();
//		clipped.setNumPoints(ifs.getNumPoints());
//		clipped.setNumEdges(ifs.getNumEdges());
//		clipped.setNumFaces(ifs.getNumFaces());
//		clipped.setVertexAttributes(ifs.getVertexAttributes());
//		clipped.setFaceAttributes(ifs.getFaceAttributes());
//		clipped.setEdgeAttributes(ifs.getEdgeAttributes());
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
		ArrayList inBounds = new ArrayList();
		double[][] bnds = box.getBounds();
		int outcount = 0;
		for (int i = 0; i<n; ++i)	{
			int[] tf = ifs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray(null);
			boolean outside = false;
			for (int j = 0; (j<tf.length); ++j)	{
				int k = tf[j];
				double[] vec = verts.item(k).toDoubleArray(null);
				if (vec.length == 4) Pn.dehomogenize(vec,vec);
//				System.err.println("checking "+Rn.toString(vec));
				// every vertex must be in the bounding box
				if (bnds[0][0] > vec[0] || bnds[1][0] < vec[0] 
						|| bnds[0][1] > vec[1] || bnds[1][1] < vec[1] 
						|| bnds[0][2] > vec[2] || bnds[1][2] < vec[2]	 ) {	
					outside = true;
					outcount++;
					break;
				}
			}
			if (!outside)	{
				inBounds.add(tf);
			}
		}
		int m = inBounds.size();
		int[][] newIndices = new int[m][];
		for (int i =0; i<m; ++i)	{
			newIndices[i] = (int[] ) inBounds.get(i);
		}
		System.err.println("surviving faces "+m);
		LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINE,"In, out face count: "+n+"  "+m);
		//TODO rescue the other face attributes
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(ifs.getNumPoints());
		ifsf.setVertexAttributes(ifs.getVertexAttributes());
		ifsf.setFaceCount(m);
		ifsf.setFaceIndices(newIndices);		
		ifsf.setGenerateEdgesFromFaces(true);

		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	/*  From here to end, various deprecated methods   from IndexedFaceSetUtility  ********************/
	/**
	 * Returns the total nubmer of faces in array of indexed face sets.
	 * @param ifs array of indexed faces sets
	 * @return total number of faces in array of indexed face sets.
	 */
	public static int getTotalNumFaces( IndexedFaceSet [] ifs ) {
		int N =0;
		for( int i=0; i<ifs.length; i++ ) {
			N += ifs[i].getNumFaces();
		}
		return N;
	}
	/**
	 * Returns the total nubmer of lines in array of indexed line sets.
	 * @param ifs array of indexed line sets
	 * @return total number of lines in array of indexed line sets.
	 */
	public static int getTotalNumLines( IndexedLineSet [] ils ) {
		int N =0;
		for( int i=0; i<ils.length; i++ ) {
			N += ils[i].getNumEdges();
		}
		return N;
	}
	
	/**
	 * Returns the total number of points in array of point sets.
	 * @param ps array of point sets
	 * @return total number of points in array of point set.
	 */
	public static int getTotalNumPoints( PointSet [] ps ) {
		int N =0;
		for( int i=0; i<ps.length; i++ ) {
			N += ps[i].getNumPoints();
		}
		return N;
	}

	/** Generate a "unit disk" in the requested metric.  The resulting topological "point set" is identical in all
	 * cases, but the meshing respects the metric.  That is, a polar subdivision is carried out in which the radial
	 * steps are equal (in the metric).  
	 * @param metric
	 * @return  A quad mesh representation of the unit disk
	 */
	public static IndexedFaceSet getDisk(int phisteps, int rsteps, final int metric) {
		ParametricSurfaceFactory psf = new ParametricSurfaceFactory();
		psf.setMetric(metric);
		psf.setULineCount(phisteps);
		psf.setVLineCount(rsteps);
	    psf.setUMin(0.0);
	    psf.setUMax(Math.PI * 2);
	    psf.setVMin(0.00);
	    psf.setVMax(1.0);
	    psf.setGenerateFaceNormals(true);
	    psf.setGenerateEdgesFromFaces(true);
	    psf.setGenerateVertexNormals(true);
		psf.setImmersion( new Immersion()		{
			double[] scales = {10,1,Math.PI/4};
			double[] foo = {1,0,0,0};
			public boolean isImmutable() {
				return false;
			}

			public int getDimensionOfAmbientSpace() {
				return 4;
			}

			public void evaluate(double u, double v, double[] xyz, int index) {
				double[] p = Pn.dragTowards(null, P3.originP3, foo, v * scales[metric+1], metric);
				
				xyz[0] = Math.cos(u) * p[0];
				xyz[1] = Math.sin(u) * p[0];
				xyz[2] = 0.0;
				xyz[3] = p[3];
			}
			
		});
		psf.update();
		IndexedFaceSet ifs = psf.getIndexedFaceSet();
		ifs.setName("disk"+(metric+1));
		return ifs;
	}
	
	public static IndexedFaceSet fanFromSegment(double[] p0, double[] p1, int numsteps, double r)	 {
//		QuadMeshFactory qmf = new QuadMeshFactory();
		Pn.normalize(p0, p0, Pn.ELLIPTIC);
		Pn.normalize(p1, p1, Pn.ELLIPTIC);
		double angle = Pn.angleBetween(p0, p1, Pn.ELLIPTIC);
//		double[][][] verts = new double[2][(numsteps+1)][3];
		double[][] verts = new double[(numsteps+3)][3];
		
		for ( int i = 0; i<= numsteps; ++i)	{
			double da = (i*angle)/numsteps;
			Pn.dragTowards(verts[i], p0, p1, da, Pn.ELLIPTIC);
		}
		Rn.times(verts[numsteps+1], r, verts[numsteps]);
		Rn.times(verts[numsteps+2], r, verts[0]);
		return IndexedFaceSetUtility.constructPolygon(verts);
//		qmf.setULineCount(numsteps+1);
//		qmf.setVLineCount(2);
//		qmf.setVertexCoordinates(verts);
//		qmf.setClosedInUDirection(false);
//		qmf.setClosedInVDirection(false);
//		qmf.setGenerateEdgesFromFaces(true);
//		qmf.setGenerateFaceNormals(true);
//		qmf.update();
//		return qmf.getIndexedFaceSet();
		
	}
	public static IndexedLineSet starPoint(int n, double r1, double r2)	{
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		int[][] inds = new int[n][2];
		double[][] v = new double[2*n][3];
		
		for (int i = 0; i<n; ++i)	{
			double d = n;
			double angle = 2*Math.PI*i/d;
			inds[i][0] = 2*i;
			inds[i][1] = 2*i+1;
			double c = Math.cos(angle);
			double s = Math.sin(angle);
			double f = 1.0;
			if (i%2 == 0) f *= 1.25;
			if (i%4 == 0) f *= 1.25;
			if (i%8 == 4) f *= 1.25;
			v[2*i] = new double[]{f*r1*c, f*r1*s, 0};
			v[2*i+1] = new double[]{f*r2*c, f*r2*s, 0};
		}
		ilsf.setVertexCount(2*n);
		ilsf.setVertexCoordinates(v);
		ilsf.setEdgeCount(n);
		ilsf.setEdgeIndices(inds);
		ilsf.update();
		return ilsf.getIndexedLineSet();
		
	}
	
	public static IndexedFaceSet texturedAnnulus(int xdim, int ydim, final double R, final double r)	{
		ParametricSurfaceFactory psf = new ParametricSurfaceFactory();
		psf.setImmersion(new Immersion() {
			double k1 = r, k2 = Math.log(R/r);
			public boolean isImmutable() {
				// TODO Auto-generated method stub
				return false;
			}
			
			public int getDimensionOfAmbientSpace() {
				return 3;
			}
			
			public void evaluate(double u, double v, double[] xyz, int index) {
				double angle = Math.PI*2*u;
				double vt = k1 *Math.exp(k2 * v); //r*R/(R*(1-v)+r*v);
				xyz[0] = Math.cos(angle)*vt;
				xyz[1] = Math.sin(angle)*vt;
				xyz[2] = 0.0;
			}
		});
		psf.setUMin(0);psf.setUMax(1);psf.setVMin(0);psf.setVMax(1);
		//subdivisions of th domain
		psf.setULineCount(xdim);psf.setVLineCount(ydim);
		psf.setClosedInUDirection(true);
		psf.setClosedInVDirection(false);
		//generate edges and normals
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		//generate the IndexFaceSet
		psf.update();

		return psf.getIndexedFaceSet();
	}
	
	public static QuadMeshFactory sphericalCapFactory( double alpha, int xDetail, int yDetail, double radius)	{
		//Globe qms = new Globe(n, m, false, false, factor*(cU-uH), factor*(cU+uH), factor*(cV-vH), factor*(cV+vH), r);
		//Globe qms = new Globe(n, m, false, false, 
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setClosedInUDirection(true);
		qmf.setClosedInVDirection(false);
		qmf.setULineCount(xDetail);
		qmf.setVLineCount(yDetail);
		//xDetail, yDetail, false, false);
		double du = Math.PI*2;
		double dv = alpha;
		du = du/(xDetail-1.0);
		dv = dv/(yDetail-1.0);
		double[] points = new double[xDetail*yDetail*3];
		double x,y, cu, cv, su, sv;
		int index;
		for (int i = 0; i< yDetail; ++i)	{
			y =  Math.PI/2 - i*dv;
			for (int j = 0; j<xDetail; ++j)	{
				index  = 3*(i*xDetail + j);
				x = j*du;
				cu = Math.cos(x);
				su = Math.sin(x);
				cv = Math.cos(-y);
				sv = Math.sin(-y);
				points[index] = radius * cu * cv;
				points[index+1] = radius * su*cv;
				points[index+2] = radius * sv;
			}
		}
		qmf.setVertexCoordinates(points);
		qmf.setVertexNormals(points);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		return qmf;
	}

	public static double[] convert44To33(double[] d) {
		double[] d33 = new double[9];
		d33[0] = d[0];
		d33[1] = d[1];
		d33[2] = d[3];
		d33[3] = d[4];
		d33[4] = d[5];
		d33[5] = d[7];
		d33[6] = d[12];
		d33[7] = d[13];
		d33[8] = d[15];
		return d33;
	}
	
	public static IndexedFaceSet sphericalPatch(double cU, double cV, double uSize, double vSize, int xDetail, int yDetail, double radius)	{
		return sphericalPatchFactory(cU,cV,uSize,vSize,xDetail,yDetail,radius, true).getIndexedFaceSet();
	}

	/**
	 * Generate a spherical patch. <i>(cU, cV)</i> specify the center of the patch in
	 * spherical angles (longitude, latitude) in radians. 
	 * @param cU
	 * @param cV
	 * @param uSize		wdith of the patch	(longitude)
	 * @param vSize		height of the patch (latitude)
	 * @param n			number of sample points in u
	 * @param m			number of sample points in v
	 * @param r			radius of the sphere
	 * @return
	 */
	public static QuadMeshFactory sphericalPatchFactory( double cU, double cV, double uSize, double vSize, int xDetail, int yDetail, double radius, boolean conform)	{
		double factor = Math.PI/180.0;
		double uH = uSize/2.0; double vH = vSize/2.0;
		//Globe qms = new Globe(n, m, false, false, factor*(cU-uH), factor*(cU+uH), factor*(cV-vH), factor*(cV+vH), r);
		//Globe qms = new Globe(n, m, false, false, 
		double umin = factor*(cU-uH), 
			umax = factor*(cU+uH), 
			vmin = factor*(cV-vH), 
			vmax= factor*(cV+vH);
		// stereographic project the v coordinate
		// and base the v-interpolation on equal steps in the log of this projection
		double r = (1+Math.sin(-vmax))/Math.cos(vmax),
			R = (1+Math.sin(-vmin))/Math.cos(vmin),
			k1 = r,
			k2 = Math.log(R/r);
		System.err.println("R : r ="+R+" "+r);
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setClosedInUDirection(false);
		qmf.setClosedInVDirection(false);
		qmf.setULineCount(xDetail);
		qmf.setVLineCount(yDetail);
		//xDetail, yDetail, false, false);
		double du = umax - umin;
		double dv = vmax - vmin;
		du = du/(xDetail-1.0);
		dv = dv/(yDetail-1.0);
		double[] points = new double[xDetail*yDetail*3];
		double[][] texcoord2 = new double[xDetail * yDetail][3];
		double x,y, cu, cv, su, sv;
		int index;
		for (int i = 0; i< yDetail; ++i)	{
			y = vmin + i*dv;
			cv = Math.cos(-y);
			sv = Math.sin(-y);
			// calculate the interpolation based on the log
			// when y = 
			double vv = (yDetail-1-i)/(yDetail-1.0);
			
			double t = k1 * Math.exp(k2 * vv);
			double nsv = (t*t-1)/(t*t+1),
				ncv = 2*t/(t*t+1);
			double RR = (1+sv)/cv;
			double tv = Math.log(RR/k1)/k2;
//			if (conform)	{
//				y = AnimationUtility.linearInterpolation(tv, 0, 1, vmin, vmax);
//				cv = Math.cos(-y);
//				sv = Math.sin(-y);			
//			}
			for (int j = 0; j<xDetail; ++j)	{
				index  = 3*(i*xDetail + j);
				x = umin+j*du;
				cu = Math.cos(x);
				su = Math.sin(x);
				points[index] = radius * cu * cv;
				points[index+1] = radius * su* cv;
				points[index+2] = radius * sv;
				texcoord2[i*xDetail+j][0] = (j/(xDetail-1.0));
				texcoord2[i*xDetail+j][1] = 1-tv;
				texcoord2[i*xDetail+j][2] = 0;
			}
		}
		qmf.setVertexCoordinates(points);
//		qmf.setVertexTextureCoordinates(texcoord2);
		qmf.setVertexNormals(points);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateTextureCoordinates(false);
		qmf.update();
		DataList texCoordDL = StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(texcoord2);
		qmf.getIndexedFaceSet().setVertexAttributes(Attribute.TEXTURE_COORDINATES1,
//				qmf.getIndexedFaceSet().getVertexAttributes(Attribute.TEXTURE_COORDINATES));
				texCoordDL);
		qmf.getIndexedFaceSet().setVertexAttributes(Attribute.TEXTURE_COORDINATES,
				qmf.getIndexedFaceSet().getVertexAttributes(Attribute.TEXTURE_COORDINATES1));
		return qmf;
	}

}
