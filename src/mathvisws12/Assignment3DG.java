package mathvisws12;

import charlesgunn.math.Biquaternion.Metric;
import de.jreality.math.MatrixBuilder;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.FriezeGroup;
import de.jtem.discretegroup.util.TranslateTool;

/**
 * This class demonstrates the difference between the three classical planar geometries
 * (euclidean, elliptic, and hyperbolic).  A translation in the x-direction is calculated
 * and repeated in both directions. The length of the translation is controlled by a slider.
 * To simplify the example, the results are rendered without lighting. The user can click
 * and drag the pattern.  The dragging respects the metric of the pattern.  
 * 
 * @author Charles Gunn
 *
 */
public class Assignment3DG extends Assignment3Complete {
	FriezeGroup friezeGroup;
	
	public static void main(String[] args)		{
		Assignment3DG theProgram = new Assignment3DG();
		theProgram.display();
	}

	@Override
	protected void initCopies() {
		for (int i = 0; i<3; ++i)	{
			fundamentalDomain[i] = SceneGraphUtility.createFullSceneGraphComponent(names[i]+"fd");
			fundamentalDomain[i].addChild(elSGC);			
		}
	}


	@Override
	protected void updateCopies() {
		replaceGroup(whichGroup);
	}


	private void replaceGroup(int num)	{
		int tmetric = getMetric().metric;
		friezeGroup = FriezeGroup.instanceOfGroup(num, translate, getMetric().metric);
		friezeGroup.setConstraint(new DiscreteGroupSimpleConstraint(2*count-1));
		friezeGroup.update();
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(friezeGroup);
		MatrixBuilder.euclidean().translate(.2, friezeGroup.isHorizontalMirror() ? 1.06 : 0,0).assignTo(elSGC);
		MatrixBuilder.euclidean().scale(.7*friezeGroup.getUnitWidth()).assignTo(fundamentalDomain[tmetric+1]);
		dgsgr.setWorldNode(fundamentalDomain[tmetric+1]);
		dgsgr.update();
		worldSGC.removeAllChildren();
		dgsgr.getRepresentationRoot().addTool(new TranslateTool());
		worldSGC.addChildren(circleSGC,dgsgr.getRepresentationRoot());
		circleSGC.setVisible(getMetric() == Metric.hyperbolic);
	}
	
	@Override
	public String getDocumentationFile() {
		return "html/Assignment3.html";
	}


}
