/*
 * Created on Mar 13, 2005
 *
 */
package charlesgunn.jreality;

import de.jreality.scene.SceneGraphComponent;

/**
 * @author gunn
 *
 */
public class SelectionComponent extends SceneGraphComponent {
	int selectedChild = 0;

	/**
	 * @return Returns the selectedChild.
	 */
	public int getSelectedChild() {
		return selectedChild;
	}
	public SceneGraphComponent getSelectedChildAsSGC() {
		return getChildComponent(selectedChild);
	}
	/**
	 * @param selectedChild The selectedChild to set.
	 */
	public void setSelectedChild(int sc) {
		//if (sc == selectedChild) return;
		int n = getChildComponentCount();
		selectedChild = sc %n;
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent sgc = getChildComponent(i);
			if (i == selectedChild) sgc.setVisible(true);
			else  sgc.setVisible(false);
		}
	}
}
