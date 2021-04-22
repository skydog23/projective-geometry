/*
 * Created on Nov 11, 2004
 *
 */
package charlesgunn.jreality.worlds.projective;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.texture.SimpleTextureFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class EllipticCongruence extends LoadableScene {
	SceneGraphComponent[][] levels = new SceneGraphComponent[1][2];
	protected boolean showTubes = true, showLevel[] = {true, true, false, false};
	SceneGraphComponent leftLines = new SceneGraphComponent("curves");
	SceneGraphComponent rightLines = new SceneGraphComponent("tubes");
	Appearance noTex1 = new Appearance(), noTex2 = new Appearance();
	//double[] c1 = {1,.8, 0,1}, c2 = {.4,1,0, 1};
	public SceneGraphComponent makeWorld()	{
		
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		theWorld.addChild(leftLines);
		theWorld.addChild(rightLines);
		theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		theWorld.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		update();
//		if (false)	{
//			DefaultGeometryShader dgs = (DefaultGeometryShader) 
//			ShaderUtility.createDefaultGeometryShader(theWorld.getAppearance(), true);
//			dgs.setShowLines(false);
//			dgs.setShowPoints(false);
//			TwoSidePolygonShader tsps = (TwoSidePolygonShader) dgs.createPolygonShader("twoSide");
//			DefaultPolygonShader dps = (DefaultPolygonShader) tsps.createFront("default");
//			DefaultPolygonShader dps2 = (DefaultPolygonShader) tsps.createBack("default");
//			dps.setDiffuseColor(new Color(0,204,204));
//			dps2.setDiffuseColor(new Color(204,204,0));
//			DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
//			tsps = (TwoSidePolygonShader) dls.createPolygonShader("twoSide");
//			dps = (DefaultPolygonShader) tsps.createFront("default");
//			dps2 = (DefaultPolygonShader) tsps.createBack("default");
//			dps.setDiffuseColor(new Color(0,204,204));
//			dps2.setDiffuseColor(new Color(204,204,0));
//			DefaultPointShader dvs = (DefaultPointShader) dgs.createPointShader("default");
//			tsps = (TwoSidePolygonShader) dvs.createPolygonShader("twoSide");
//			dps = (DefaultPolygonShader) tsps.createFront("default");
//			dps2 = (DefaultPolygonShader) tsps.createBack("default");
//			dps.setDiffuseColor(new Color(0,204,204));
//			dps2.setDiffuseColor(new Color(204,204,0));
//		}
		
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(theWorld);
		noTex1.setAttribute("polygonShader.texture2d", Appearance.DEFAULT);
		Color c1 = new Color(1f, .8f, 0f),  c2 = new Color(.4f, 1f, 0f);
		noTex1.setAttribute("polygonShader.diffuseColor", c1);
		noTex2.setAttribute("polygonShader.diffuseColor", c2);
		for (int i = 0; i<2; ++i)	{
			SimpleTextureFactory sft = new SimpleTextureFactory();
			sft.setType(SimpleTextureFactory.TextureType.GRADIENT);
			sft.setColor(0, c1);
			sft.setColor(1, c2);
			sft.update();
			Appearance ap = new Appearance();
			SceneGraphComponent sgc = (i==0)? leftLines : rightLines;
			sgc.setAppearance(ap);
			Texture2D tex2d = (Texture2D) AttributeEntityUtility
		       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);	
		  	tex2d.setImage(sft.getImageData());
		  	Matrix foo = new Matrix();
		  	if (i==0) MatrixBuilder.euclidean().scale(1 ,1,1).translate(.5,.5,0).rotateZ(Math.PI/2).translate(-.5,-.5,0).assignTo(foo);
		  	else MatrixBuilder.euclidean().translate(0,1,0).scale(1,-1,1).translate(.5,.5,0).rotateZ(Math.PI/2).translate(-.5,-.5,0).assignTo(foo);

		  	tex2d.setTextureMatrix(foo);
		  	tex2d.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		  	tex2d.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);			
		}
		return theWorld;
	}

	public void customize(JMenuBar theMenuBar, final Viewer viewer)	{
		//theMenuBar = super.createMenuBar(viewer);
		//theMenuBar = new JMenuBar(); //super.createMenuBar();
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.black);
	}
	
	int[] channels = {3,0,1,2};		// map quaternions onto coordinates
	private void update() {
		int numLevels = endLevel - beginLevel + 1;
		double tubeRadiusScale = .5*globalRadius/(numLevels+beginLevel+2);
		Quaternion[] baseCircle = new Quaternion[numSegs];
		Quaternion[][] baseTube = new Quaternion[numSegs][profileSize];
		double[][] oneCircle = new double[numSegs][4];
		double[][] oneTube = new double[numSegs*profileSize][4];
		SceneGraphUtility.removeChildren(rightLines);
		SceneGraphUtility.removeChildren(leftLines);
		Color[] clrs = {Color.white, Color.white}; //{new Color(255,50,50), new Color(50,50,255)};
		for (int m= 0; m<numLevels; ++m)	{
			SceneGraphComponent thislevel = new SceneGraphComponent("level"+m);
			thislevel.setAppearance(new Appearance());
			thislevel.getAppearance().setAttribute("polygonShader.diffuseColor", clrs[m%2]);
			double da = length/(numSegs-1.0);
			double scale = overThickenFactor*tubeRadiusScale; //radius;
			for (int j = 0; j<numSegs; ++j)	{
				double angle = -length/2.0 + (j) * da;
				baseCircle[j] = new Quaternion(  Math.cos(angle), 0.0, 0.0,  Math.sin(angle));
				for (int k = 0; k < profileSize; ++k)	{
					double a = k * 2*Math.PI/(profileSize-1.0);
					Quaternion smallCircleYZ = new Quaternion(Math.sqrt(1-scale*scale), Math.cos(a)*scale, Math.sin(a)*scale, 0.0);
					baseTube[j][k] = Quaternion.times(null, smallCircleYZ, baseCircle[j]);
				}
			}
			boolean reversed = (m%2 == 1);
			if (reversed) rightLines.addChild(thislevel);
			else leftLines.addChild(thislevel);
			int numCurves = (int) (density*(4 +(constantSamples ? 0 : m)+ beginLevel)); //(int) (r * densityFactor);
			double radius = (m+beginLevel)*2*tubeRadiusScale;
			System.err.println("Radius is "+radius);
			double cr = Math.cos(radius); //Math.sqrt(1.0-r*r);
			double sr = Math.sin(radius);
			for (int i = 0; i<numCurves; ++i)  	{
				///if (verts[i][2] > 0.0) continue;
				double phi =(i + (reversed ? .5 : 0))* Math.PI*2.0/(numCurves);
				double x = sr* Math.cos(phi);
				double y = sr* Math.sin(phi);
				// convert to an imaginary quaternion
				Quaternion q = new Quaternion(cr,x,y,0);
				// calculate a color based on this position
				Color c = new Color((float) (.5 + .5 *x), (float) (.5 + .5 *y), 0.0f, 1.0f);
				for (int j = 0; j<numSegs; ++j)	{
					Quaternion product = null;
					if (reversed) product = Quaternion.times(null, baseCircle[j], q);
					else product = Quaternion.times(null, q, baseCircle[j]);
					product.asDouble(oneCircle[j], channels);
					for (int k = 0; k<profileSize; ++k)	{
						product = reversed ?Quaternion.times(null, baseTube[j][k], q) : Quaternion.times(null, q, baseTube[j][k]);
						product.asDouble(oneTube[j+numSegs*k], channels);
					}
				}
//				IndexedLineSet dsc = IndexedLineSetUtility.createCurveFromPoints(oneCircle, false);
//				SceneGraphComponent coreSGC = new SceneGraphComponent();
//				coreSGC.setName("core curve"+i);
//				coreSGC.setAppearance(new Appearance());
//				coreSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c );
//				coreSGC.setGeometry(dsc);
//					thislevel.addChild(coreSGC);
				QuadMeshFactory qmf = new QuadMeshFactory( );
//				qmf.setMetric(Pn.ELLIPTIC);
				qmf.setULineCount(numSegs);
				qmf.setVLineCount(profileSize);
				qmf.setClosedInUDirection(false);
				qmf.setClosedInVDirection(true);
				double[][] oneTube3 = new double[oneTube.length][3];
				Pn.dehomogenize(oneTube3, oneTube);
				qmf.setVertexCoordinates(oneTube3);
				qmf.setGenerateEdgesFromFaces(true);
				qmf.setGenerateFaceNormals(true);
				qmf.setGenerateVertexNormals(true);
				qmf.setGenerateTextureCoordinates(true);
				qmf.update();
				IndexedFaceSet qms = qmf.getIndexedFaceSet();
				double[][] pts = QuadMeshUtility.extractUParameterCurve(null, qms, 0);
				int nx = pts.length;
				double[][] ptsReversed = new double[nx][];
				for (int k=0;k<nx;++k)  ptsReversed[k] = pts[nx-k-1];
				IndexedFaceSet cap1 = IndexedFaceSetUtility.constructPolygon(ptsReversed);
				pts = QuadMeshUtility.extractUParameterCurve(null, qms, numSegs-1);
				IndexedFaceSet cap2 = IndexedFaceSetUtility.constructPolygon(pts);
				SceneGraphComponent tubeSGC = new SceneGraphComponent();
				tubeSGC.setName("tube"+i);
				tubeSGC.setGeometry(qms);
				tubeSGC.setAppearance(new Appearance());
				SceneGraphComponent sgc = new SceneGraphComponent();
				sgc.setAppearance(reversed ? noTex1 : noTex2);
				sgc.setGeometry(cap1);
				tubeSGC.addChild(sgc);
				sgc = new SceneGraphComponent();
				sgc.setAppearance(!reversed ? noTex1 : noTex2);
				sgc.setGeometry(cap2);
				tubeSGC.addChild(sgc);
//				tubeSGC.addChild(GeometryUtilityOverflow.displayFaceNormals(cap1, .02));
//				tubeSGC.addChild(GeometryUtilityOverflow.displayFaceNormals(cap2, .02));
//				tubeSGC.addChild(GeometryUtilityOverflow.displayFaceNormals(qms, .02));
				thislevel.addChild(tubeSGC);
			}
		}
	}

		public int getMetric()	{
			return Pn.EUCLIDEAN;
		}
		public boolean addBackPlane()	{
			return false;
		}
		public boolean isEncompass()	{
			return true;
		}
		public boolean hasInspector() {return true; }
		int profileSize = 13;
		int numSegs = 20;
		int beginLevel = 7, endLevel = 16;
		double length = 2.3;
		double globalRadius = 1.0; //Math.PI/4;
		double density =.7,
			overThickenFactor = 1.2;
		boolean constantSamples = true;
		public Component getInspector(final Viewer viewer) {	
			Box inspectionPanel =  Box.createVerticalBox();
			final JCheckBox constantSB = new JCheckBox("constant samples");
			constantSB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					constantSamples = constantSB.isSelected();
					update();
				}
				
			});
			constantSB.setSelected(constantSamples);
			inspectionPanel.add(constantSB);
			TextSlider theSlider = new TextSlider.Integer("profile size",SwingConstants.HORIZONTAL,4,24,profileSize);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					profileSize = ((TextSlider) e.getSource()).getValue().intValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Integer("begin level",SwingConstants.HORIZONTAL,1,20, beginLevel);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					beginLevel = ((TextSlider) e.getSource()).getValue().intValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Integer("end level",SwingConstants.HORIZONTAL,1,40,endLevel);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					endLevel = ((TextSlider) e.getSource()).getValue().intValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Integer("num segments",SwingConstants.HORIZONTAL,1,40,numSegs);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					numSegs = ((TextSlider) e.getSource()).getValue().intValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			 theSlider = new TextSlider.Double("length",SwingConstants.HORIZONTAL,0.0,5.0,length);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					length = ((TextSlider) e.getSource()).getValue().doubleValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Double("global radius",SwingConstants.HORIZONTAL,0.0,Math.PI,globalRadius);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					globalRadius = ((TextSlider) e.getSource()).getValue().doubleValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Double("overthicken",SwingConstants.HORIZONTAL,1.0,2.0,overThickenFactor);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					overThickenFactor = ((TextSlider) e.getSource()).getValue().doubleValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			theSlider = new TextSlider.Double("density",SwingConstants.HORIZONTAL,0.0,1.0,density);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					density = ((TextSlider) e.getSource()).getValue().doubleValue();
					update();
				}
			});
			inspectionPanel.add(theSlider);
			inspectionPanel.setName("Parameters");
			return inspectionPanel;
		}
		
	}
