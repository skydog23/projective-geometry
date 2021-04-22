/*
 * Created on May 1, 2007
 *
 */
package charlesgunn.jreality.worlds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class SudaneseMoebius extends Assignment {
	transient protected int ucount = 40, vcount = 40;
//	@Override
//	public int getMetric() {
//		return Pn.ELLIPTIC;
//	}
	transient private double scaleUFixed = .5, scaleVFixed = 1, lambdaFixed = .5, angleFixed = 2.4, tlateFixed = .72;
	transient private double scaleU = .5, scaleV = 1, lambda = .5, angle = 2.4, tlate = .72;
	transient protected SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			child = SceneGraphUtility.createFullSceneGraphComponent("child");
	transient protected ParametricSurfaceFactory torusFactory = new ParametricSurfaceFactory();
	transient protected Matrix rotater = new Matrix(), tlatet = new Matrix();
	transient protected SceneGraphPath sgp;
	@Override
	public SceneGraphComponent getContent() {
		updateMatrix();
		torusFactory.setClosedInUDirection(true);
		torusFactory.setClosedInVDirection(false);
		torusFactory.setGenerateVertexNormals(true);
		torusFactory.setGenerateFaceNormals(true);
		torusFactory.setGenerateEdgesFromFaces(true);
		update();
		IndexedFaceSet bar = torusFactory.getIndexedFaceSet();
		child.setGeometry(bar);
		world.addChild(child);
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("polygonShader.diffuseColor", new Color(255,153, 153));
		return world;
	}

	transient protected double[] segments = {
			0, .1, 
			1, .25, // rotate x-
			0,  .05,
			0, .25,  // rotate x-
			2, .1,
			3, .4, // change v +
			4, .05,
			5, .4, // change v -
			6, .1,
			7, .4, // change u+
			8, .05,
			9, .4, // change u-
			10, .1,
			11, .5, // rotate y
			12, .1},
			acc = new double[segments.length/2];
	{
		for (int i = 0; i<acc.length; i++) {
			acc[i] = (i==0 ? 0 : acc[i-1])+segments[2*i+1];
		}
	}
	@Override
	public void setValueAtTime(double d) {
		// TODO Auto-generated method stub
		super.setValueAtTime(d);
		double t = acc[acc.length-1]*d;
		Matrix foo = new Matrix();
		int count = 0;
		if (t <= acc[count++]) {
			// pause
		}
		else if (t <= acc[count++]) {
			t = AnimationUtility.hermiteInterpolation(t, acc[count-2], acc[count-1], 0, -Math.PI*.8);
			MatrixBuilder.euclidean().rotateX(t).assignTo(foo);
			rotate(foo);
		} 
		else if (t <= acc[count++]) {
		}
		else if (t <= acc[count++]) {
			t = AnimationUtility.hermiteInterpolation(t, acc[count-2], acc[count-1], -Math.PI*.8, 0);
			MatrixBuilder.euclidean().rotateX(t).assignTo(foo);
			rotate(foo);
		} 
		else if (t <= acc[count++]) {
		}
		else if (t <= acc[count++]){  // v+
			t = AnimationUtility.linearInterpolation(t, acc[count-2], acc[count-1]-.01,1.0,0.0);
			scaleV=t;
			update();
		}
		else if (t <= acc[count++]) {
		}
		else if (t<=acc[count++]) {   // v-
			t = AnimationUtility.linearInterpolation(t, acc[count-2], acc[count-1]-.01,0, 1.0);
			scaleV=t;
			update();
		} 
		else if (t <= acc[count++]) {
		}
		else if (t <= acc[count++]){  // v+
			t = AnimationUtility.linearInterpolation(t, acc[count-2], acc[count-1]-.01,0.5,0.0);
			scaleU=t;
			update();
		}
		else if (t <= acc[count++]) {
		}
		else if (t<=acc[count++]) {   // v-
			t = AnimationUtility.linearInterpolation(t, acc[count-2], acc[count-1]-.01,0, 0.5);
			scaleU=t;
			update();
		} 
		else if (t <= acc[count++]) {
		}
		else if (t<=acc[count++]){
			t = AnimationUtility.hermiteInterpolation(t,acc[count-2], acc[count-1], 0, Math.PI*2);
			
			MatrixBuilder.euclidean().rotateY(t).assignTo(foo);
			rotate(foo);
		} 
		
	}


	private void rotate(Matrix foo) {
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(child); 
		double[] center = bbox.getCenter();
		double[] tmp = new double[16];
		MatrixBuilder.euclidean().translate(center).assignTo(tmp);
		tmp = Rn.conjugateByMatrix(null, foo.getArray(), tmp);
		double[] root2world = sgp.getInverseMatrix(null); 
		tmp = Rn.conjugateByMatrix(null, tmp, root2world);
		world.getTransformation().setMatrix(tmp);
	}


	private void update() {
		updateMatrix();
		torusFactory.setImmersion(new ParametricSurfaceFactory.Immersion() {
					public int getDimensionOfAmbientSpace() {
						return 4;
					}

					public void evaluate(double u, double v, double[] xyz,
							int offset) {
						xyz[0] = Math.sin(u)*Math.cos(v);
						xyz[1] = Math.sin(u)*Math.sin(v);
						xyz[2] = Math.cos(u)*Math.cos(lambda*v);
						xyz[3] = Math.cos(u)*Math.sin(lambda*v);
//						if (xyz[3] < 0) Rn.times(xyz, -1, xyz);
						Rn.matrixTimesVector(xyz, rotater.getArray(), xyz);
						Rn.matrixTimesVector(xyz, tlatet.getArray(), xyz);
						double factor = 1.0/(1-xyz[3]);
						for (int i = 0; i<3; ++i) {xyz[i] *= factor;}
						xyz[3] = 1.0;
					}

					public boolean isImmutable() {
						return false;
					}
				}

		);
		torusFactory.setUMin(2*(scaleUFixed-scaleU)*Math.PI);
		torusFactory.setVMin(0);
		torusFactory.setUMax(2*scaleUFixed*Math.PI);
		torusFactory.setVMax(2*scaleV*Math.PI);
//		int ucount2 = (int) (ucount * (scaleU/scaleUFixed));
//		ucount2 = (ucount2 < 2) ? 2 : ucount2;
//		int vcount2 = (int) (vcount * (scaleV/scaleVFixed));
//		vcount2 = (vcount2 < 2) ? 2 : vcount2;
		torusFactory.setULineCount(ucount);
		torusFactory.setVLineCount(vcount);
		torusFactory.update();
	}

	private void updateMatrix() {
		MatrixBuilder.euclidean().rotate(angle, new double[]{1,1,1}).assignTo(rotater);
		MatrixBuilder.elliptic().translate(0,0,tlate).assignTo(tlatet);
	}
	
	@Override
	public Component getInspector() {
		getContent();
		Box inspectionPanel = inspector;
		final TextSlider alphaSlider = new TextSlider.Double("scale",SwingConstants.HORIZONTAL,0.0,1.0,lambda);
		alphaSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				lambda = alphaSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(alphaSlider);
		final TextSlider uSlider = new TextSlider.Double("scale",SwingConstants.HORIZONTAL,0.0,1.0,scaleU);
		uSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				scaleU = uSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(uSlider);
		final TextSlider timeSlider = new TextSlider.Double("scale",SwingConstants.HORIZONTAL,0.0,1.0,scaleV);
		timeSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				scaleV = timeSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(timeSlider);
		final TextSlider tlateSlider = new TextSlider.Double("tlate",SwingConstants.HORIZONTAL,0.0,Math.PI,tlate);
		tlateSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				tlate = tlateSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(tlateSlider);
		final TextSlider angleSlider = new TextSlider.Double("angle",SwingConstants.HORIZONTAL,0.0,2*Math.PI,angle);
		angleSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				angle = angleSlider.getValue().doubleValue();
				update();
			}
		});
		inspectionPanel.add(angleSlider);
		return inspectionPanel;
	}
	

	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", Color.white);
		sgp = SceneGraphUtility.getPathsToNamedNodes(jrviewer.getViewer().getSceneRoot(), "world").get(0);
		sgp.pop();
		animationPlugin.getAnimationPanel().setResourceDir("src/charlesgunn/jreality/worlds/");
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/gunn_local/Movies/sudaneseMoebius");
//		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
	}

	public static void main(String[] args)		{
		SudaneseMoebius theProgram = new SudaneseMoebius();
		theProgram.display();
	}
}
