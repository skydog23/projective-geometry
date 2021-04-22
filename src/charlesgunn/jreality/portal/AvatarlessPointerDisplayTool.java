package charlesgunn.jreality.portal;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;

public class AvatarlessPointerDisplayTool implements Tool {

  List currentSlots = new LinkedList();
  InputSlot pointer = InputSlot.getDevice("PointerShipTransformation");
  SceneGraphComponent c = new SceneGraphComponent();
  
  public AvatarlessPointerDisplayTool(double radius) {
    currentSlots.add(pointer);
    SceneGraphComponent stick=new SceneGraphComponent();
    MatrixBuilder.euclidean().translate(0,0,-1).scale(radius, radius, 1).assignTo(stick);
    IndexedFaceSet cube = new IndexedFaceSet();
    IndexedFaceSetUtility.calculateAndSetFaceNormals(cube);
	stick.setGeometry(cube);
    c.setAppearance(new Appearance());
    c.getAppearance().setAttribute("diffuseColor", Color.yellow);
    c.setTransformation(new Transformation());
    c.addChild(stick);
  }
  public AvatarlessPointerDisplayTool() {
    this(0.05);
  }
  
  public List getActivationSlots() {
	    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return currentSlots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  public void activate(ToolContext tc) {
  }

  boolean isAssigned;
  
  Matrix m = new Matrix();
  public void perform(ToolContext tc) {
	if (!isAssigned) {
		tc.getRootToToolComponent().getLastComponent().addChild(c);
		isAssigned=true;
	}
    m.assignFrom(tc.getTransformationMatrix(pointer));
    m.assignTo(c.getTransformation());
  }

  public void deactivate(ToolContext tc) {
  }
public String getDescription(InputSlot slot) {
	// TODO Auto-generated method stub
	return null;
}
public String getDescription() {
	// TODO Auto-generated method stub
	return null;
}

}
