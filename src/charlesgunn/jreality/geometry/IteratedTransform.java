package charlesgunn.jreality.geometry;

import java.awt.Color;

import charlesgunn.anim.util.AnimationUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;

public class IteratedTransform extends SceneGraphComponent implements TransformationListener {
	int iterationCount = 0, oldIterationCount = 0, highWaterMark = 0;
	boolean countChanged = true;
	Color[] colors = {new Color(.8f, .8f, 0f), new Color(.2f,.9f,0f)};
	Transformation theT;
	SceneGraphComponent geometry;
	SceneGraphPath pathToEnd = null;
	/**
	 * 
	 */
	public IteratedTransform(Transformation t, int itcount, SceneGraphComponent g) {
		super("itT0");
		theT = t;
		theT.addTransformationListener(this);
		setTransformation(new Transformation());
 		geometry = g;
		setAppearance(new Appearance());
		addChild((SceneGraphComponent) geometry);
		setIterationCount(itcount);
	}
	
	public int getIterationCount() {
		return iterationCount;
	}
	public void setIterationCount(int i) {
		if ( i == iterationCount) return;
		if (i<=1) i = 2;
		oldIterationCount = iterationCount;
		iterationCount = i;
		countChanged = true;
		update();
	}
	
	public void setColors(Color[] cc)	{
		colors = cc;
		countChanged = true;
	}
	public void update()	{
		if (countChanged) {
			SceneGraphComponent parent = this, child = null;
			pathToEnd = new SceneGraphPath();
			pathToEnd.push(parent);
			for (int i = 0; i<iterationCount; ++i)	{
				double factor = i/((double)iterationCount);
				if (colors != null)	{
					Color cl = AnimationUtility.linearInterpolation(colors[0], colors[1], factor);
					parent.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, cl);					
				} else
					parent.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+
							CommonAttributes.DIFFUSE_COLOR, Appearance.INHERITED);					
					
				if (parent.getChildComponentCount() == 1)	{		// have to add a child
					child = new SceneGraphComponent("itT"+i);
					child.setAppearance(new Appearance());
					child.setTransformation(theT);
					child.addChild( geometry);
					parent.addChild(child);
					parent = child;
				}
				else {
					parent = parent.getChildComponent(1);
					parent.setVisible(true);
				}
				pathToEnd.push(parent);
			}
			for (int i = iterationCount; i<oldIterationCount; ++i) {
				parent.setVisible(false);
				parent = parent.getChildComponent(1);
			}
			countChanged = false;
			if (iterationCount > highWaterMark) highWaterMark = iterationCount;
		} 
	}

		

	/* (non-Javadoc)
	 * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(de.jreality.scene.event.TransformationEvent)
	 */
	public void transformationMatrixChanged(TransformationEvent ev) {
		update();

	}

	public SceneGraphPath getPathToEnd() {
		return pathToEnd;
	}

}
