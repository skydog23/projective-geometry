package charlesgunn.jreality;

import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;

public class GeometryCollector extends SceneGraphComponent {

	int poolSize = 100;
	int currentIndex = 0;
	int currentLength = 0;
	public GeometryCollector(int n)	{
		poolSize = n;
		init();
	}
	
	private void init()	{
//		Scene.executeWriter(this, new Runnable() {
//
//			public void run() {
				int n = getChildComponentCount();
				for (int i = n; i<poolSize; ++i)	{
					SceneGraphComponent child = new SceneGraphComponent("pool "+i);
					addChild(child);
				}
				for (int i = 0; i<n; ++i)	{
					getChildComponent(i).setVisible(false);
				}
				for (int i = 0; i<currentLength; ++i)	{
					int which = (i+currentIndex)%poolSize;
					getChildComponent(which).setVisible(true);
				}
//			}
//			
//		});
	}
	
	public void reset()	{
		currentIndex = currentLength = 0;
		init();
	}
	
	public void addGeometry(Geometry g)	{
		SceneGraphComponent child = getChildComponent(currentIndex);
		child.setGeometry(g);
		child.setVisible(true);
		currentIndex = (currentIndex+1)%poolSize;
		if (currentLength < poolSize) currentLength++;
	}

	public int getCount() {
		return currentIndex;
	}
}
