/*
 * Created on May 30, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package charlesgunn.jreality.portal;

import java.util.Collections;
import java.util.List;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author weissman
 *
 **/
public class PortalFancyHeadMoveTool implements Tool {
  
  final transient InputSlot headSlot = InputSlot.getDevice("AvatarShipTransformation");
  final transient List used = Collections.singletonList(headSlot);
  transient EffectiveAppearance eap;
  // the portalScale values determines how large the Portal is,
  // where a value of 1.0 corresponds to the physical dimensions in meters
  // of the actual installation.  Larger values simulate larger portals, etc.
  // in order to use non-euclidean geometries easily, it's preferable to
  // avoid using "scaling" matrices in the scene graph to make the world
  // larger or smaller, since "scaling" is an asymmetric operation WRT the 
  // w coordinate.  Hence, the portal scale factor can be used to avoid explicit
  // use of such scaling matrices in the scene graph.  The portal scale value
  // is used in this head move tool, in order to transform the coordinates of the
  // raw head movement into the "scaled" portal coordinates.
  // Similar adjustments take place in the wand tool, and finally in the
  // HeadTrackedViewer class, the corresponding scaling matrix is applied to 
  // the camera (that is, is applied as the final transformation on the world to
  // camera path, where it IS a valid transformation since the camera occupies
  // the point (0,0,0,1) ).
  transient double portalScale = 1.0;
  transient int metric = Pn.EUCLIDEAN;
  
  public List getActivationSlots() {
    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return used;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  transient double[] tmp = new double[16];
  transient Viewer viewer;
  
  public void perform(ToolContext tc) {
    if (viewer == null) {
      viewer = tc.getViewer();
    }
	if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
        eap = EffectiveAppearance.create(tc.getRootToToolComponent());
}
    metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
    portalScale = eap.getAttribute("portalScale", 1.0);
    SceneGraphComponent head = tc.getRootToToolComponent().getLastComponent();
    if (head.getTransformation() == null) head.setTransformation(new Transformation());
    FactoredMatrix physicalMatrix = new FactoredMatrix(Pn.EUCLIDEAN, tc.getTransformationMatrix(headSlot).toDoubleArray(tmp));
    
    double[] physicalTranslation = physicalMatrix.getTranslation();
    physicalTranslation[3] /= portalScale;
    //double[] virtualTranslation = Rn.matrixTimesVector(null, physicalToVirtual, physicalTranslation);
    physicalMatrix.setColumn(3, P3.originP3);
    FactoredMatrix virtualMatrix = new FactoredMatrix(metric, physicalMatrix.getArray());
    virtualMatrix.setTranslation(physicalTranslation);
    // remove the translation from the head matrix, leaving (hopefully) an orientation matrix
    virtualMatrix.assignTo(head);
    //head.getTransformation().setMatrix(tc.getTransformationMatrix(headSlot).toDoubleArray(tmp));
    viewer.renderAsync();
  }

  public void activate(ToolContext tc) {
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
