/*
 * Created on 11.04.2017
 *
 */
package charlesgunn.pathcurve;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.CurveCollector;
import charlesgunn.math.Complex;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.tool.InputSlot;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.TranslateTool;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class WateryVortex {

	double 		
			wvZtlate = 0.27,
			wateryVortexLambda = 1.43,
			wvInit = .65,
			wvTmin = -1,
			wvTmax = 2;
	int dimu = 50, dimv = 50;
	double[][][] planeForm, pointForm, transformedPlaneForm;

	transient private TextSlider<Double>  
		bSlider,
		wvlSlider,
		wviSlider,
		wvTminSlider, wvTmaxSlider;
	transient SceneGraphComponent wateryVortexSGC;
	transient private PathCurveFactory wateryVortexPCF;
	transient private QuadMeshFactory wateryVortexFac;

	double rad = 0.5,
			xmin = .05, xmax = 5.0;
	static double[] ydir = {0,1,0,0},
			wvCoord = {
					1,0,0,0,
					0,1,0,0,
					0,0,-1,0,
					0,0,1,1
			};
	PathCurveDemo pcd = null;
	public WateryVortex(PathCurveDemo pcd)	{
		this.pcd = pcd;
		getSGC();
	}
	public SceneGraphComponent getSGC()	{
		if (wateryVortexSGC != null) return wateryVortexSGC;
		wateryVortexSGC = SceneGraphUtility.createFullSceneGraphComponent("wateryVortex");
		wateryVortexSGC.setVisible(false);
		wateryVortexSGC.addTool(new RotateTool());
		DraggingTool tool = new DraggingTool();
//		List<InputSlot> activationSlots = tool.getActivationSlots();
//		activationSlots.removeAll(activationSlots);
//		activationSlots.add(InputSlot.MIDDLE_BUTTON);
		wateryVortexSGC.addTool(tool);
		wateryVortexPCF = new PathCurveFactory();
		planeForm = new double[dimv][dimu][];
		pointForm = new double[dimv][dimu][];
		wateryVortexSGC.getTransformation().addTransformationListener(new TransformationListener() {
			
			@Override
			public void transformationMatrixChanged(TransformationEvent ev) {
				// get form for acting on planes
				System.err.println("In trans listener");
				updatePlanes();
				pcd.update();
			}
		});
		Appearance ap = wateryVortexSGC.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		initializePlanes();
		return wateryVortexSGC;
	}
	public void initializePlanes() {
		
		Complex[] eigen = {
				new Complex(0,0),
				new Complex(0,0),
				new Complex(wateryVortexLambda,0),
				new Complex(1,0)
		};
		wateryVortexPCF.setEigenvalues(eigen);
		wateryVortexPCF.setTmin(wvTmin);
		wateryVortexPCF.setTmax(wvTmax);
		wateryVortexPCF.setNumberSteps(dimu+1);
		wvCoord[11] = wvZtlate;
		wateryVortexPCF.setCoordinateSystem(wvCoord);
		wateryVortexPCF.setInitialPoint(new double[]{wvInit,0,-wvInit+wvZtlate,1});
		wateryVortexPCF.update();
		double[][] oneCurve = wateryVortexPCF.getCurvePoints();
		double diff = xmax - xmin;
		for (int i = 0; i<dimv; ++i)	{
			double x = xmin + i * (diff/(dimv-1.0));
			double[] pointy = oneCurve[i],
					planey = P3.planeFromPoints(null, oneCurve[i], oneCurve[i+1], ydir); //{1,0, -x*x,-2*x+b*x*x};
			for (int j = 0; j<dimu; ++j)	{
				double angle = j*Math.PI * 2.0/(dimu-1.0);
				double[] m = MatrixBuilder.euclidean().rotateZ(angle).getArray();
				planeForm[i][j] = Rn.matrixTimesVector(null, m, planey);
				pointForm[i][j] = Rn.matrixTimesVector(null, m, pointy);
			}
		}
//		System.err.println("one curve = \n"+Rn.toString(oneCurve));
//		System.err.println("planes = \n"+Rn.toString(planeForm));
		wateryVortexFac = new QuadMeshFactory();
		wateryVortexFac.setULineCount(dimu);
		wateryVortexFac.setVLineCount(dimv);
		wateryVortexFac.setClosedInUDirection(true);
		wateryVortexFac.setClosedInVDirection(false);
		wateryVortexFac.setGenerateEdgesFromFaces(true);
		wateryVortexFac.setGenerateFaceNormals(true);
		wateryVortexFac.setVertexCoordinates(pointForm);
		wateryVortexFac.update();
		
		wateryVortexSGC.setGeometry(wateryVortexFac.getGeometry());
		wateryVortexFac.getGeometry().setGeometryAttributes(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		
		updatePlanes();

	}
	private void updatePlanes() {
		double[] tform = wateryVortexSGC.getTransformation().getMatrix();
		tform = Rn.inverse(null, Rn.transpose(null, tform));
		transformedPlaneForm = new double[dimv][][];
		for (int i =0; i<planeForm.length; ++i)	{
			transformedPlaneForm[i] = Rn.matrixTimesVector(null, tform, planeForm[i]);				
		}
	}
	
	public Component getInspector()	{
		JPanel inspectionJPanel = new JPanel();
		inspectionJPanel.setBorder(BorderFactory.createTitledBorder(
                null, "watery vortex"));
		Box inspectionPanel = Box.createVerticalBox();
		inspectionJPanel.add(inspectionPanel);

		bSlider = new TextSlider.Double("z-tlate",SwingConstants.HORIZONTAL,-1.0, 1.0, wvZtlate);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				wvZtlate = bSlider.getValue().doubleValue();
				initializePlanes();
				pcd.update();
			}
		});
		inspectionPanel.add(bSlider);
		wvlSlider = new TextSlider.Double("wv lambda",SwingConstants.HORIZONTAL,-2.0, 2.0, wateryVortexLambda);
		wvlSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				wateryVortexLambda = wvlSlider.getValue().doubleValue();
				initializePlanes();
				pcd.update();
			}
		});
		inspectionPanel.add(wvlSlider);
		
		wviSlider = new TextSlider.Double("wv init",SwingConstants.HORIZONTAL,-1.0, 1.0, wvInit);
		wviSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				wvInit = wviSlider.getValue().doubleValue();
				System.err.println("init value = "+wvInit);
				initializePlanes();
				pcd.update();
			}
		});
		inspectionPanel.add(wviSlider);
		wvTminSlider = new TextSlider.Double("wv tmin",SwingConstants.HORIZONTAL,-3.0, 3.0, wvTmin);
		wvTminSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				wvTmin = wvTminSlider.getValue().doubleValue();
				initializePlanes();
				pcd.update();
			}
		});
		inspectionPanel.add(wvTminSlider);
		wvTmaxSlider = new TextSlider.Double("wv tmax",SwingConstants.HORIZONTAL,-3.0, 3.0, wvTmax);
		wvTmaxSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				wvTmax = wvTmaxSlider.getValue().doubleValue();
				initializePlanes();
				pcd.update();
			}
		});
		inspectionPanel.add(wvTmaxSlider);
		return inspectionJPanel;
	}
	public double[][][] getPlaneForm() {
		return transformedPlaneForm != null ? transformedPlaneForm : planeForm;
	}
	public int getDimu() {
		return dimu;
	}
	public void setDimu(int dimu) {
		this.dimu = dimu;
	}
	public int getDimv() {
		return dimv;
	}
	public void setDimv(int dimv) {
		this.dimv = dimv;
	}

}
