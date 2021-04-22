package charlesgunn.jreality.geometry;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;

public class ClipBox {

	SceneGraphComponent box = SceneGraphUtility.createFullSceneGraphComponent("clipBox"),
			clips[] = new SceneGraphComponent[6];
	double[] dim = {1,1,1}, axis = {1,1,1};
	public double[] getDim() {
		return dim;
	}
	public void setDim(double[] dim) {
		this.dim = dim;
		updateBox();
	}
	public ClipBox()	{
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<2; ++j)	{
				SceneGraphComponent child = new SceneGraphComponent("clip"+i+j);
				box.addChild(child);
				SceneGraphComponent clipSGC = new SceneGraphComponent("clipplane");
				clips[2*i+j] = clipSGC;
				clipSGC.setGeometry(new ClippingPlane());
				child.addChild(clipSGC);
				MatrixBuilder.euclidean().rotate(2*i*Math.PI/3, axis).rotateX(Math.PI * j).assignTo(child);
				child.getTransformation().setReadOnly(true);
			}
		}
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(box);
		updateBox();
	}
	private void updateBox() {
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<2; ++j)	{
				MatrixBuilder.euclidean().translate(0,0,dim[i]).assignTo(clips[2*i+j]);
			}
		}
	}
	
	public SceneGraphComponent getBox() {
		return box;
	}
}
