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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Quaternion;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class HopfFibration extends Assignment {
		SceneGraphComponent icokit;
		SceneGraphComponent[][] levels = new SceneGraphComponent[5][2];
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		protected boolean showTubes = true,
				showThree = true;
		final protected boolean 
				showLevel[] = {true, true, false, false, false};
		double[] radii = {.02, .01, .005, .0025,.00125};
		double globalRadius = .02;
		private SceneGraphComponent theUniverse;
		
		public SceneGraphComponent getContent()	{
			
			theWorld.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
			initWorld();
			Color[] threec = {Color.red, Color.blue, Color.yellow};
			theUniverse = SceneGraphUtility.createFullSceneGraphComponent("universe");
			for (int i =0; i<3;++i)	{
				SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
				sgc.getAppearance().setAttribute("polygonShader.diffuseColor", threec[i]);
				sgc.addChild(theWorld);
				MatrixBuilder.euclidean().rotate(i*Math.PI*2/3.0,new double[]{1,1,1}).assignTo(sgc);				
				theUniverse.addChild(sgc);
			}
			//SceneGraphComponent icokit = SceneGraphUtility.createFullSceneGraphComponent("ico");
			//icokit.setGeometry(ico);
			//icokit.getTransformation().setStretch(.2);
			//theWorld.addChild(icokit);
			updateVisibility();
			return theUniverse;
		}

		private void updateVisibility() {
			theUniverse.getChildComponent(1).setVisible(showThree);
			theUniverse.getChildComponent(2).setVisible(showThree);
		}

		private void initWorld() {
			int numSegs = 11;
			SceneGraphUtility.removeChildren(theWorld);
			for (int i =0 ; i< levels.length; ++i)	{
				SceneGraphComponent tmp = hopfFibration(i, numSegs, radii[i]);
				theWorld.addChild(tmp);
				levels[i][0] = tmp.getChildComponent(0);
				System.out.println("Adding "+tmp.getName()+levels[i][0].getName());
				levels[i][1] = tmp.getChildComponent(1);				
				System.out.println("Adding "+tmp.getName()+levels[i][1].getName());
				if (showLevel[i]) 	{
					levels[i][0].setVisible(true);
					if (showTubes)  levels[i][1].setVisible(true);
					else levels[i][1].setVisible(false);
				} else	{
					levels[i][0].setVisible(false);
					levels[i][1].setVisible(false);
				}
			}
		}

		/**
		 * 
		 */
		protected void updateSceneGraph() {
			for (int i = 0; i < levels.length; ++i)	{
				if (showLevel[i]) 	{
					levels[i][0].setVisible(true);
					if (showTubes)  levels[i][1].setVisible(true);
					else levels[i][1].setVisible(false);
				} else	{
					levels[i][0].setVisible(false);
					levels[i][1].setVisible(false);
				}
			}
		}

		@Override
		public Component getInspector(V) {
			super.getInspector();
			Box inspectionPanel =  Box.createVerticalBox();
			final JCheckBox jcperp = new JCheckBox("Show perpendiculars");
			jcperp.setSelected(showThree);
			inspectionPanel.add(jcperp);
			jcperp.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					showThree = ((JCheckBoxMenuItem) e.getSource()).isSelected();
					System.out.println("Show three is "+showThree);
					updateVisibility();
					initWorld();
				}
			});
			TextSlider theSlider = new TextSlider.Double("tube radius",SwingConstants.HORIZONTAL,0.0, .1, globalRadius);
			theSlider.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)	{
					globalRadius = ((TextSlider) e.getSource()).getValue().doubleValue();
					radii[0] = globalRadius;
					for (int i = 1; i<radii.length; ++i){
						radii[i] =  radii[i-1]*.5;
					}
					initWorld();
				}
			});
			inspectionPanel.add(theSlider);
			JMenu testM = new JMenu("View");
			final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show tubes");
			jcm.setSelected(showTubes);
			testM.add(jcm);
			jcm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					showTubes = ((JCheckBoxMenuItem) e.getSource()).isSelected();
					System.out.println("Show tubes is "+showTubes);
					updateSceneGraph();
					viewer.renderAsync();
				}
			});
			for (int i = 0; i<showLevel.length; ++i)	{
				final JCheckBoxMenuItem jcx = new JCheckBoxMenuItem("Show level "+i);
				jcx.setSelected(showLevel[i]);
				testM.add(jcx);
				final int k = i;
				jcx.addActionListener( new ActionListener() {
					final int j = k;
					public void actionPerformed(ActionEvent e)	{
						showLevel[j] = ((JCheckBoxMenuItem) e.getSource()).isSelected();
						updateSceneGraph();
						viewer.renderAsync();
					}
				});				
			}
			inspector.add(testM);
			return inspector;

			return inspectionPanel;
		}

		@Override
		public void display()	{
			//theMenuBar = super.createMenuBar(viewer);
			//theMenuBar = new JMenuBar(); //super.createMenuBar();
			super.display();
			viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, 
					Color.black);
//			theMenuBar.add(testM);
		}
		

		/**
		 * @param theWorld
		 * @return
		 */
		private SceneGraphComponent hopfFibration(int level, int numSegs, double radius) {
			if (level < 0) level = 0; 
			if (level > 4) level = 4;
			IndexedFaceSet ico = SphereUtility.tessellatedIcosahedronSphere(level, true);
			int profileSize = 7;
			double[][] verts = ico.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			int numCurves = verts.length;
			Quaternion[] baseCircle = new Quaternion[numSegs];
			Quaternion[][] baseTube = new Quaternion[numSegs][profileSize];
			double totalAngle = Math.PI * 2;
			double da = totalAngle/(numSegs-1.0);
			double scale = radius;
			for (int j = 0; j<numSegs; ++j)	{
				double angle = (j) * da;
				baseCircle[j] = new Quaternion(  Math.cos(angle), 0.0, 0.0,  Math.sin(angle));
				for (int k = 0; k < profileSize; ++k)	{
					double a = k * Math.PI/3.0;
					Quaternion smallCircleYZ = new Quaternion(Math.sqrt(1-scale*scale), Math.cos(a)*scale, Math.sin(a)*scale, 0.0);
					baseTube[j][k] = Quaternion.times(null, smallCircleYZ, baseCircle[j]);
				}
			}
			SceneGraphComponent node = new SceneGraphComponent();
			node.setName("Hopf fibration level "+level);
			SceneGraphComponent curves = new SceneGraphComponent();
			curves.setName("Curves");
			SceneGraphComponent tubes = new SceneGraphComponent();
			tubes.setName("Tubes");
			node.addChild(curves);
			node.addChild(tubes);
			Appearance ap = new Appearance();
			node.setAppearance( ap);
			ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 0.5d);
			
			for (int i = 0; i<numCurves; ++i)  	{
				///if (verts[i][2] > 0.0) continue;
				double[][] oneCircle = new double[numSegs][4];
				double[][] oneTube = new double[numSegs*profileSize][4];
				// find phi and then halve it
				double xy = Math.sqrt(verts[i][0]*verts[i][0] + verts[i][1]*verts[i][1]);
				double phi = Math.atan2(verts[i][2], xy);
				// bring phi up into the northern hemisphere
				phi = Math.PI/2.0 - (Math.PI/2.0 - phi)/2.0;
				// convert to a point on this hemisphere ...
				double x = Math.cos(phi) * verts[i][0]/xy;
				double y = Math.cos(phi) * verts[i][1]/xy;
				double z = Math.sin(phi);
				// convert to an imaginary quaternion
				Quaternion q = new Quaternion(0.0,x,y,z);
				//System.out.println("Vertex "+i+" is "+q.toString());
				// calculate a color based on this position
				Color c = new Color((float) (.5 + .5 *verts[i][0]), (float) (.5 + .5 *verts[i][1]), (float) (.5 + .5 *verts[i][2]),1.0f);
				for (int j = 0; j<numSegs; ++j)	{
					Quaternion product = Quaternion.times(null, q, baseCircle[j]);
					product.asDouble(oneCircle[j]);
					for (int k = 0; k<profileSize; ++k)	{
						product = Quaternion.times(null, q, baseTube[j][k]);
						product.asDouble(oneTube[j*profileSize+k]);
					}
				}
				IndexedLineSet dsc = IndexedLineSetUtility.createCurveFromPoints(oneCircle, false);
				SceneGraphComponent coreSGC = new SceneGraphComponent();
				coreSGC.setName("core curve"+i);
				coreSGC.setAppearance(new Appearance());
				coreSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c );
				coreSGC.setGeometry(dsc);
				curves.addChild(coreSGC);
				QuadMeshFactory qmf = new QuadMeshFactory( );
				qmf.setMetric(Pn.ELLIPTIC);
				qmf.setULineCount(profileSize);
				qmf.setVLineCount(numSegs);
				qmf.setClosedInUDirection(false);
				qmf.setClosedInVDirection(false);
				qmf.setVertexCoordinates(oneTube);
				qmf.setGenerateEdgesFromFaces(true);
				qmf.setGenerateFaceNormals(true);
				qmf.setGenerateVertexNormals(true);
				qmf.update();
				IndexedFaceSet qms = qmf.getIndexedFaceSet();
//				qms.setGeometryAttributes(GeometryUtility.METRIC, new Integer(Pn.ELLIPTIC));
//				qms.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(oneTube));
//				GeometryUtility.calculateAndSetNormals(qms);
				SceneGraphComponent tubeSGC = new SceneGraphComponent();
				tubeSGC.setName("tube"+i);
				tubeSGC.setAppearance(new Appearance());
				if (!showThree) tubeSGC.getAppearance().setAttribute(
						CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c );
				tubeSGC.setGeometry(qms);
				tubes.addChild(tubeSGC);
				//theWorld.addChild(tubeSGC);
			}
			return node;
		}


	}
