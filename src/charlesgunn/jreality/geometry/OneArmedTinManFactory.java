package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class OneArmedTinManFactory {

	double phi = Math.sqrt(5.0)/2.0 - .5;
	double globalScale = 1;
	double armScale = 3.5, 
		legScale = 6, 
		headScale = 2.2, 
		torsoWidth = 1.2,
		k1 = .3; 
	double[] 
		head = {phi, phi, .8},
		arm = {1,.9,.9},
		leg = {1.0, .25, 1.0};
	double[] armLengths = {.9,.9,.6};
	double[] legLengths = {1,1,.5};
	double[] armRadii = {.12,.1,.1};
	double[] legRadii = {.3, .25, .2};
	double eyeLevelHeadFraction = .6;		// eyes are 3/5 of the way up the head
	SceneGraphComponent tinMan = SceneGraphUtility.createFullSceneGraphComponent("tinMan"),
		headSGC,
		legSGC,
		armSGC,
		armProper,
		jointSGC,
		headOrientation,
		armOrientation,
		herbert,
		localToWorld,
		planeWandRepn,
		elbowPlaneRepn,
		schnittLine,
		hack;
	HeadFactory headFactory;
	ArmFactory armFactory;
	LegFactory legFactory;
	int resolution = 8;
	PartFactory[] factories = new PartFactory[3];
	int metric = Pn.EUCLIDEAN;
	double height = 1,
		standingEyeLevel = 1.8,		// default: meters, height to eye
		legLengthLocal = legScale*(legLengths[0]+legLengths[1]),
		armLengthLocal = armScale*(armLengths[0]+armLengths[1]),
		otherLengthsLocal = k1+2*eyeLevelHeadFraction*headScale,
		standingEyeLevelLocal = legLengthLocal+otherLengthsLocal;
	private double[] headOrientationM = Rn.identityMatrix(4);
	double[] untransformedEye = {0, -1.1*headScale*head[1], (2*eyeLevelHeadFraction)*headScale};
	private double[] transformedEye = untransformedEye;
	SceneGraphPath worldToHand, worldToShoulder, shoulderToHand, worldToStick;
	Matrix worldToShoulderRotation;
	IndexedLineSetFactory armILSF = new IndexedLineSetFactory();
	boolean debug = false, printDebug = false;
	boolean doTexturedFace = true,
		flatten = false;
	public OneArmedTinManFactory()	{
		tinMan.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255,200,0));
		tinMan.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(100,255,0));
		tinMan.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		tinMan.getAppearance().setAttribute("metric", Pn.EUCLIDEAN);
		factories[0] = headFactory = new HeadFactory();
		factories[1] = armFactory = new ArmFactory();
		// the joint object for the torso is not a sphere (default) but a horizontal cylinder
		SceneGraphComponent cyl = Primitives.closedCylinder(resolution, k1, k1, -torsoWidth, torsoWidth, Math.PI*2);
		MatrixBuilder.euclidean().rotateY(Math.PI/2).scale(1.0/globalScale).assignTo(cyl);
		gmf = new GeometryMergeFactory();
		IndexedFaceSet merged = gmf.mergeGeometrySets(cyl);
		factories[2] = legFactory = new LegFactory(merged);
		headSGC = headFactory.getSceneGraphComponent();
		armSGC = armFactory.getSceneGraphComponent();
		legSGC = legFactory.getSceneGraphComponent();
		legSGC.setName("leg");
		localToWorld = new SceneGraphComponent("scale");
		localToWorld.setTransformation(new Transformation());
		tinMan.addChild(localToWorld);
		headOrientation = new SceneGraphComponent("head orientation");
		headOrientation.setTransformation(new Transformation());
		headOrientation.addChild(headSGC);
		armOrientation = new SceneGraphComponent("arm orientation");
		armOrientation.setTransformation(new Transformation());
		armOrientation.addChild(armSGC);
		localToWorld.addChildren(headOrientation, armOrientation, legSGC);		
		headFactory.setScale(Rn.times(null, headScale, head));
		legFactory.setScale(leg);
		armFactory.setLengths(Rn.times(null, armScale, armLengths));
		legFactory.setLengths(Rn.times(null, legScale, legLengths));		
		armFactory.setRadii(Rn.times(null, armScale, armRadii));
		legFactory.setRadii(Rn.times(null, armScale, legRadii));		
		for (PartFactory pf : factories) {
			pf.update();
		}
		MatrixBuilder.init(new Matrix(headSGC.getTransformation().getMatrix()), metric).translate(0, 0, k1).assignTo(headSGC);
		MatrixBuilder.init(null, metric).rotateY(Math.PI).assignTo(legSGC);
		MatrixBuilder.init(null, metric).translate(-leg[0]*torsoWidth, 0.0, 0).rotateY(2*Math.PI).assignTo(armOrientation);
		//try a new approach to the arm
		armILSF.setEdgeCount(1); //(2);
		armILSF.setEdgeIndices(new int[][]{{0,1}}); //{{0,1},{1,2}});
		armILSF.setVertexCount(2); //(3);
		//armSGC.setGeometry(armILSF.getIndexedLineSet());
		SceneGraphUtility.removeChildren(armSGC);
		armSGC.addChild(armFactory.getHand());
		armSGC.addChild(armProper = new SceneGraphComponent("arm proper"));
		SceneGraphComponent debugStuff = new SceneGraphComponent("debug stuff");
		armSGC.addChild(debugStuff);
		armSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		armSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		armSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.yellow);
//		armSGC.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
//		armSGC.getAppearance().setAttribute("lineShader.tubeRadius", 0.4);
//		armSGC.getAppearance().setAttribute("pointShader.pointRadius", 0.6);
		armProper.setAppearance(new Appearance());
		armProper.getAppearance().setAttribute("polygonShader.diffuseColor", Color.green);
		// try a new approach to the leg
		legFactory.upperPart.removeChild(legFactory.lowerPart);
		legFactory.lowerPart.removeChild(legFactory.endPart);
		legSGC.addChild(legFactory.lowerPart);
		legSGC.addChild(legFactory.endPart);
		
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(armSGC);
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(legSGC);
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(headSGC);
		planeWandRepn = new SceneGraphComponent("wand plane repn");
		debugStuff.addChild(planeWandRepn);
		elbowPlaneRepn = new SceneGraphComponent("elbow plane repn");
		debugStuff.addChild(elbowPlaneRepn);
		schnittLine = new SceneGraphComponent("schnittline repn");
		debugStuff.addChild(schnittLine);
		hack = new SceneGraphComponent("hack repn");
		debugStuff.addChild(hack);
		debugStuff.setVisible(debug);
		setStandingEyeLevel(standingEyeLevel);
		worldToHand = SceneGraphUtility.getPathsBetween(localToWorld, armFactory.getHand()).get(0);
		worldToHand.pop();
		double[] m = worldToHand.getMatrix(null);
		worldToShoulder = SceneGraphUtility.getPathsBetween(localToWorld, armOrientation).get(0);
		m = worldToShoulder.getInverseMatrix(null);
		FactoredMatrix fm = new FactoredMatrix(m);
		// this remains constant even though the rest of the matrix might change
		worldToShoulderRotation = fm.getRotation();
		shoulderToHand = SceneGraphUtility.getPathsBetween(armOrientation, armFactory.getHand()).get(0);
		worldToStick = SceneGraphUtility.getPathsToNamedNodes(localToWorld, "stick").get(0);
		final double[] fd = Rn.normalize(null, new double[]{0,-.7,-.7});
		Matrix mm = new Matrix();
		MatrixBuilder.euclidean().translate(0,1.7,0).rotate(-Math.PI/4, 1,0,0).assignTo(mm);
		setHeadTransformation(mm.getArray());
		double x = fd[0]+.34; 
		double y = fd[1]-.3; 
		double z = fd[2]+.17;
		MatrixBuilder.euclidean().translate(x,y,z).rotate(-Math.PI/4, 0,0,1).assignTo(mm);
		setHandTransformation(mm.getArray());
	}
	
	public boolean isDoTexturedFace() {
		return doTexturedFace;
	}

	public void setDoTexturedFace(boolean doTexturedFace) {
		this.doTexturedFace = doTexturedFace;
	}

	public SceneGraphComponent getTinMan()	{
		return tinMan;
	}
	
	public void setStandingEyeLevel(double d)	{
		standingEyeLevel = d;
		globalScale = standingEyeLevel/standingEyeLevelLocal;
		setTopLevelTform();
	}
	
	public void setHeadTransformation(double[] m)	{
		headOrientationM = P3.extractOrientationMatrix(null, m, P3.originP3, metric);
		headOrientationM = Rn.conjugateByMatrix(null, headOrientationM, 
				P3.extractOrientationMatrix(null, localToWorld.getTransformation().getMatrix(),P3.originP3, metric));
		headOrientation.getTransformation().setMatrix(headOrientationM);
		transformedEye = Rn.matrixTimesVector(null, headOrientationM, untransformedEye);
		double[] imageOfOrigin = new Matrix(m).getColumn(3);		// image of origin under all metrics
		Pn.dehomogenize(imageOfOrigin, imageOfOrigin);
		// the y-coordinate is the height of the eye above the ground
		// use that to perform a "kneel"
		double kneelingEyeLevel = imageOfOrigin[1];
		//System.err.println("eye height is "+kneelingEyeLevel);
		double kneelingEyeLevelLocal = kneelingEyeLevel/globalScale;
		otherLengthsLocal = k1 + (transformedEye[2]);
		double d = kneelingEyeLevelLocal - otherLengthsLocal;
		if (d<0) d = 0;
		if (d>legLengthLocal) {
			//setStandingEyeLevel(kneelingEyeLevel);
			d = legLengthLocal;
		}
		double cosa = d/legLengthLocal;
		if (printDebug) System.err.println("cosine of a = "+cosa);
		setHeight(cosa);
		setTopLevelTform();
	}
	
	public void setHandTransformation(double[] m)	{
		// find the position of the handjoint in shoulder space
		double[] imageOfOrigin = new Matrix(m).getColumn(3);		// image of origin in world coords
		Pn.dehomogenize(imageOfOrigin, imageOfOrigin);
		double[] worldToShoulderM = worldToShoulder.getInverseMatrix(null);  // coordinates in shoulder space
		double[] positionInShoulderSpace = Rn.matrixTimesVector(null, worldToShoulderM, imageOfOrigin);
//		System.err.println("position in shoulder space is "+Rn.toString(positionInShoulderSpace));
		double length = Pn.norm(positionInShoulderSpace, metric);
//		if (length > armLengthLocal) {
//			//System.err.println("hand is too far from shoulder");
//			Pn.setToLength(positionInShoulderSpace, positionInShoulderSpace, armLengthLocal-.001, metric);
//			length=armLengthLocal-.001;
//		}
		// assign a rotation which rotates the shoulder joint to the correct orientation
//		double[] rot = P3.makeRotationMatrix(null, new double[]{0,0,1}, positionInShoulderSpace);
		// figure out a correction so that the hand orientation remains constant wrt world coordinates
		double[] handOrientationM = P3.extractOrientationMatrix(null, m, P3.originP3, metric);
		handOrientationM = Rn.times(null,  
				P3.extractOrientationMatrix(null, worldToShoulderRotation.getArray(), P3.originP3, metric),
				handOrientationM);
		double[] trans = P3.makeTranslationMatrix(null, positionInShoulderSpace, metric);
		new Matrix(Rn.times(null, trans, handOrientationM)).assignTo(armFactory.getHand());
		// try to bend the elbow!
//		double[] sides = {length, armScale*armLengths[0], armScale*armLengths[1]};
//		Matrix h2s = new Matrix(shoulderToHand.getMatrix(null));
//		double[] planePerpToWand = h2s.getColumn(2);		// the wand points in the z-direction  hand coordinates
//		planePerpToWand[3] = -Rn.innerProduct(h2s.getColumn(2), positionInShoulderSpace, 3);
//		if (printDebug) System.err.println("Plane perp to wand is "+Rn.toString(planePerpToWand));
//		double[] p = positionInShoulderSpace;
//		double r1 = sides[1];
//		double r2 = sides[2];
//		double r3 = length;
//		double[] elbowPlane = {p[0], p[1], p[2], -.5*(r1*r1-r2*r2+r3*r3)};
//		double[] elbowPoint = P3.lineIntersectPlane(null, P3.lineFromPoints(null, positionInShoulderSpace, Pn.originP3), elbowPlane);
//		double[] line = P3.lineFromPlanes(null, planePerpToWand, elbowPlane);
//		double x = (r2*r2+r3*r3-r1*r1)/(2*r3);
//		double d = Math.sqrt(r1*r1-(r3-x)*(r3-x));
//		double[] cc = Rn.setToLength(null, new double[]{p[0], p[1], p[2]}, r3-x);
//		double[][] schnitte = LineUtility.lineIntersectSphere(hack,line, cc, d);
//		//System.err.println("d1, d2: "+d1+" "+d2);
//		double[] pp = (schnitte[0][0] > schnitte[1][0]) ? schnitte[0] : schnitte[1];
//		SceneGraphComponent elbow = Primitives.sphere(0.1, pp);
//		armSGC.addChild(elbow);
		double[][] verts = {{0,0,0},positionInShoulderSpace}; //pp,positionInShoulderSpace};
		armILSF.setVertexCoordinates(verts);
		armILSF.update();
		TubeUtility.tubeOneEdge(armProper, verts[0], verts[1], .4, null, metric);
//		boolean bad = schnitte[0] == schnitte[1];
////		armSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", bad ? Color.red : Color.blue);
//		if (debug)	{
//			LineUtility.sceneGraphForPlane(planeWandRepn, planePerpToWand, positionInShoulderSpace,2.0);
//			LineUtility.sceneGraphForPlane(elbowPlaneRepn, elbowPlane, elbowPoint,2.0);
//			LineUtility.sceneGraphForLine(schnittLine, line, null, 10.0);			
//		}
	}
	protected void setTopLevelTform()	{
		// position the figure so that the origin of the coordinate system is 
		// a bit in front of the "middle eye"
		MatrixBuilder.euclidean().scale(globalScale)
		.rotateY(Math.PI)
		.rotateX(-Math.PI/2).translate(Rn.times(null,-1,transformedEye)).assignTo(localToWorld);
		//DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(localToWorld);
	}
	
	public void setHeight(double height) {
		this.height = height;
		double angle = Math.acos(height); //(1-height)*Math.PI/2;
//		legFactory.setHeight(angle);
		Matrix rm = new Matrix();
		MatrixBuilder.euclidean().rotateX(angle).assignTo(rm);
		double[] t1 = Rn.matrixTimesVector(null, rm.getArray(),new double[]{0,0,legFactory.lengths[0]} );
		MatrixBuilder.euclidean().rotateX(angle).assignTo(legFactory.upperPart);
		MatrixBuilder.euclidean().translate(t1).rotateX(-angle).assignTo(legFactory.lowerPart);
		MatrixBuilder.euclidean().translate(0,0,height*(legFactory.lengths[1]+legFactory.lengths[0])).assignTo(legFactory.endPart);
	}
	double[] worldToStickMat = new double[16];
	double[] stickTipObject = {0,0,-.7};
	public double[] getStickTipWorldPosition()	{
		double[] mat = worldToStick.getMatrix(worldToStickMat);
		return Rn.matrixTimesVector(null, mat, stickTipObject);
	}
	public void update() {
		if (flatten)	{
			GeometryMergeFactory gmf = new GeometryMergeFactory();
			IndexedFaceSet flat = gmf.mergeIndexedFaceSets(tinMan);
			localToWorld.setVisible(false);
			tinMan.setGeometry(flat);
		} else {
			if (tinMan.getGeometry() != null) tinMan.setGeometry(null);
			localToWorld.setVisible(true);
		}
	}
	protected abstract class PartFactory {
		SceneGraphComponent contentSGC = SceneGraphUtility.createFullSceneGraphComponent(),
			attachmentSGC =  SceneGraphUtility.createFullSceneGraphComponent();
		double[] scale = {1,1,1};
		PartFactory()	{
			super();
			attachmentSGC.addChild(contentSGC);
			contentSGC.getTransformation().setReadOnly(true);
		}
		
		public double[] getScale() {
			return scale;
		}
		public void setScale(double[] dims) {
			this.scale = dims;
		}
		public  void update()	{
		}
		
		public SceneGraphComponent getSceneGraphComponent()	{
			return attachmentSGC;
		}
		
	}
	
	protected class JointFactory extends PartFactory {
		JointFactory()	{
			this(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_COARSE));
		}
		JointFactory(Geometry g) {
			contentSGC.setGeometry(g);
			attachmentSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		}
		JointFactory(SceneGraphComponent g) {
			if (g!=null) contentSGC.addChild(g);
			else contentSGC.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_COARSE));
			attachmentSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		}
		public void update()	{
			super.update();
			contentSGC.getTransformation().setReadOnly(false);
			MatrixBuilder.init(null, Pn.EUCLIDEAN).scale(scale).assignTo(contentSGC);
			contentSGC.getTransformation().setReadOnly(true);
		}
	}
	protected class HeadFactory	extends PartFactory{
		SceneGraphComponent headsgc = new SceneGraphComponent("head");
		private IndexedFaceSet sphere;
		HeadFactory()	{
			sphere = SphereUtility.sphericalPatch(0.0,0.0, 360.0, 180.0, 15, 10, 1.0);
			if (doTexturedFace)	{
				attachmentSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.yellow);
				Texture2D tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
						Texture2D.class, "polygonShader.texture2d", attachmentSGC.getAppearance(), true);
				try {
					InputStream is = this.getClass().getResourceAsStream("smileyFace-smudged.jpg");
					ImageData id = ImageData.load(Input.getInput("test", is));
					tex2d2.setImage(id);
				} catch (IOException e) {
					e.printStackTrace();
				}
				tex2d2.setApplyMode(Texture2D.GL_MODULATE);
				Matrix foo = new Matrix();
				MatrixBuilder.init(foo, metric).scale(2).translate(0.0,-.25,0).assignTo(foo);
				tex2d2.setTextureMatrix(foo);
				tex2d2.setRepeatS(Texture2D.GL_CLAMP);
				tex2d2.setRepeatT(Texture2D.GL_CLAMP);
				
			}
			headsgc.setGeometry(sphere);
			headsgc.setTransformation(new Transformation());
			contentSGC.addChild(headsgc);
		}
		public void update()	{
			super.update();
			headsgc.getTransformation().setReadOnly(false);
			MatrixBuilder.init(null, Pn.EUCLIDEAN).translate(0,0,scale[2]).scale(scale).assignTo(headsgc);
			headsgc.getTransformation().setReadOnly(true);
			SceneGraphPath sgp = SceneGraphUtility.getPathsBetween(attachmentSGC, headsgc).get(0);
			double[] tform = sgp.getMatrix(null);
			attachmentSGC.getTransformation().setMatrix(tform);
			attachmentSGC.removeChild(contentSGC);
			attachmentSGC.setGeometry(sphere);
		}
	}
	
	protected class NeckFactory	extends PartFactory{
		public void update()	{
			super.update();
			attachmentSGC.removeChild(contentSGC);
			contentSGC = Primitives.closedCylinder(resolution, scale[0], scale[1], 0.0, 2*scale[2], Math.PI*2);
			attachmentSGC.addChild(contentSGC);
		}
	}
	
	protected class TorsoFactory	extends PartFactory{
		double proportion = .5;
		public void update()	{
			SceneGraphUtility.removeChildren(contentSGC);
			SceneGraphComponent cyl = Primitives.closedCylinder(resolution, scale[0], scale[1], 0.0, proportion * scale[2], Math.PI*2);
			SceneGraphComponent belly = new SceneGraphComponent("belly");
			belly.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_COARSE));
			contentSGC.addChildren(cyl, belly);
			MatrixBuilder.init(null, metric).translate(0,0,(1-proportion)*scale[2]).assignTo(cyl);
			MatrixBuilder.init(null, metric).translate(0,0,.8*scale[0]).scale(1.1).scale(scale[0],phi*scale[0],scale[0]).assignTo(belly);
		}
	}
	double jf = 1.2;
	protected class LimbFactory	extends PartFactory{
		double[] lengths = {1, phi, phi*phi};
		double[] radii = {1,phi,phi*phi};
		SceneGraphComponent upperPart = SceneGraphUtility.createFullSceneGraphComponent("upper"),
			lowerPart = SceneGraphUtility.createFullSceneGraphComponent("middle"),
			endPart = SceneGraphUtility.createFullSceneGraphComponent("lower");
		JointFactory joints[] = new JointFactory[3];
		LimbFactory()	{
			this(null);
		}
		LimbFactory(Geometry joint)	{
			contentSGC.addChild(upperPart);
			upperPart.addChild(lowerPart);
			lowerPart.addChild(endPart);
			for (int i = 0; i<2; ++i) joints[i] = new JointFactory(joint);
			upperPart.addChild(joints[0].getSceneGraphComponent());
			lowerPart.addChild(joints[1].getSceneGraphComponent());
//			endPart.addChild(joints[2].getSceneGraphComponent());
		}
		public void update()	{
			SceneGraphComponent sgc1, sgc2;
			sgc1 = new SceneGraphComponent();
			sgc1.setGeometry(Primitives.cylinder(resolution, 
					radii[0], 0.0, lengths[0], Math.PI*2));
			upperPart.addChild(sgc1);
			sgc2 = new SceneGraphComponent();
			sgc2.setGeometry(Primitives.cylinder(resolution, 
					radii[1], 0.0, lengths[1], Math.PI*2));
			lowerPart.addChild(sgc2);
			for (int i = 0; i<2; ++i)	{
				joints[i].setScale(new double[]{radii[i]*jf, radii[i]*jf, radii[i]*jf});	
				joints[i].update();
			}
			MatrixBuilder.init(null, metric).translate(0,0,lengths[0]).assignTo(lowerPart);
			if (sgc1.getTransformation()!=null) sgc1.getTransformation().setReadOnly(false);
			MatrixBuilder.init(null, Pn.EUCLIDEAN).scale(scale).assignTo(sgc1);
			sgc1.getTransformation().setReadOnly(true);
//			IndexedFaceSet foo = gmf.mergeGeometrySets(sgc1);
//			upperPart.setGeometry(foo);
//			upperPart.removeChild(sgc1);
			if (sgc2.getTransformation()!=null) sgc2.getTransformation().setReadOnly(false);
			MatrixBuilder.init(null, Pn.EUCLIDEAN).scale(scale).assignTo(sgc2);
			sgc2.getTransformation().setReadOnly(true);
//			foo = gmf.mergeGeometrySets(sgc2);
//			lowerPart.setGeometry(foo);
//			lowerPart.removeChild(sgc2);
		}
		public double[] getLengths() {
			return lengths;
		}
		public void setLengths(double[] lengths) {
			this.lengths = lengths;
		}
		public double[] getRadii() {
			return radii;
		}
		public void setRadii(double[] radii) {
			this.radii = radii;
		}
	}
	
	protected class ArmFactory extends LimbFactory {
		SceneGraphComponent hand1 = new SceneGraphComponent("hand"), hand=new SceneGraphComponent();
		ArmFactory()	{
			this(null);
		}
		public void setAngles(double[] angles) {
			DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(attachmentSGC, false);
			MatrixBuilder.euclidean(upperPart).rotateX(-angles[2]).assignTo(upperPart);
			MatrixBuilder.euclidean(lowerPart).rotateX(-angles[0]+Math.PI).assignTo(lowerPart);
			MatrixBuilder.euclidean(endPart).rotateX(-angles[1]).assignTo(endPart);
			
		}
		ArmFactory(Geometry joint)	{
			super(joint);
			endPart.addChild(hand);
			hand.addChild(hand1);
		}
		public void update()	{
			super.update();
//			hand.setGeometry(Primitives.cone(20, lengths[2]/radii[2]));					
			double[] tmp = Rn.times(null, radii[2], handShape);
			//hand1.addChild(Primitives.closedCylinder(resolution, tmp[0], tmp[1], -tmp[2], tmp[2], Math.PI*2));
			SceneGraphComponent cyl = Primitives.closedCylinder(resolution, tmp[0], tmp[1], -tmp[2], tmp[2], Math.PI*2);
			gmf = new GeometryMergeFactory();
			IndexedFaceSet merged = gmf.mergeGeometrySets(cyl);
			hand1.setGeometry(merged);
			hand1.setAppearance(new Appearance());
//			hand1.getAppearance().setAttribute("polygonShader.diffuseColor", Color.green);
			hand1.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			hand1.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			SceneGraphComponent stick = new SceneGraphComponent("stick");
			stick.setAppearance(new Appearance());
			stick.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255,120,40));
			SceneGraphComponent foo = Primitives.closedCylinder(8,1.0,1.0, stickTipObject[2], 0.5, 2*Math.PI); //pyramid(Primitives.regularPolygonVertices(4, 0.0), new double[]{0,0,-1}));
			foo.setAppearance(stick.getAppearance());
			gmf = new GeometryMergeFactory();
			merged = gmf.mergeGeometrySets(foo);
			merged.setVertexAttributes(Attribute.COLORS, null);
			stick.setGeometry(merged);
//			stick.addChild(foo);
			MatrixBuilder.init(null, metric).scale(tmp[0]*.15, tmp[1]*.15, 10).assignTo(stick);
			hand1.addChild(stick);
			//MatrixBuilder.init(null, metric).assignTo(hand1);
			MatrixBuilder.init(null, metric).translate(0,0,tmp[1]+lengths[1]).assignTo(endPart);
		}
		public SceneGraphComponent getHand() {
			return hand;
		}
	}
	double[] footShape = {4, 4, .6};
	double[] handShape = {1,1,2.5};
	private GeometryMergeFactory gmf;
	protected class LegFactory extends LimbFactory {
		SceneGraphComponent foot = new SceneGraphComponent("foot");
		LegFactory()	{
			this(null);
		}
		LegFactory(Geometry joint)	{
			super(joint);
			endPart.addChild(foot);
			foot.setAppearance(new Appearance());
			foot.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		}
		public void update()	{
			super.update();
			double[] tmp = Rn.times(null, radii[2], footShape);
			//foot.addChild(Primitives.closedCylinder(resolution, tmp[0], tmp[1], 0.0, tmp[2], Math.PI*2));
			SceneGraphComponent cyl = Primitives.closedCylinder(resolution, tmp[0], tmp[1], 0.0, tmp[2], Math.PI*2);
			gmf = new GeometryMergeFactory();
			IndexedFaceSet merged = gmf.mergeGeometrySets(cyl);
			foot.setGeometry(merged);
//			foot.setGeometry(Primitives.box(tmp[0], tmp[1], tmp[2], false));					
			MatrixBuilder.init(null, metric).translate(0,0,lengths[1]).assignTo(endPart);
		}
		
		public void setHeight(double angle)	{
			DefaultMatrixSupport.getSharedInstance().restoreDefaultMatrices(attachmentSGC, false);
			MatrixBuilder.euclidean(upperPart).rotateX(angle).assignTo(upperPart);
			MatrixBuilder.euclidean(lowerPart).rotateX(-2*angle).assignTo(lowerPart);
			MatrixBuilder.euclidean(endPart).rotateX(angle).assignTo(endPart);
		}
	}
	public void setFlatten(boolean b) {
		flatten = b;
	}
}
