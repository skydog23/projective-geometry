/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package charlesgunn.jreality.newtools;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;


/**
 *
 * TODO: document this
 *
 * @author brinkman
 *
 */
public class DraggingTool extends AbstractTool {

    private boolean moveChildren;
    transient private boolean dragInViewDirection;
    
    static InputSlot activationSlot = InputSlot.MIDDLE_BUTTON;
    static InputSlot alongPointerSlot = InputSlot.getDevice("DragAlongViewDirection");
    static InputSlot evolutionSlot = InputSlot.getDevice("PointerEvolution");
    
    public DraggingTool() {
        super(activationSlot);
        addCurrentSlot(evolutionSlot);
        addCurrentSlot(alongPointerSlot);
    }
    
    transient protected SceneGraphComponent comp;
    SelectionManager sm = null;
    SceneGraphPath selection;
    
    public void activate(ToolContext tc) {
        sm = SelectionManagerImpl.selectionManagerForViewer(tc.getViewer());
        selection = sm.getSelection().getSGPath();
        comp = selection.getLastComponent();
      if (comp.getTransformation() == null) comp.setTransformation(new Transformation());
      if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
          eap = EffectiveAppearance.create(tc.getRootToToolComponent());
        }
        metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
        System.err.println("metric is "+metric);
    }

    transient EffectiveAppearance eap;
    transient private int metric;
    transient Matrix result = new Matrix();
    transient Matrix root2Object = new Matrix();
    transient Matrix dragFrame;
    transient Matrix pointer = new Matrix();
    
    public void perform(ToolContext tc) {
  
      Matrix evolution = new Matrix(tc.getTransformationMatrix(evolutionSlot));
      selection.getInverseMatrix(root2Object.getArray());
      evolution.conjugateBy(root2Object);
	  if (metric != Pn.EUCLIDEAN) {
		  MatrixBuilder.init(null, metric).translate(evolution.getColumn(3)).assignTo(evolution);		  
	  }
    
      comp.getTransformation().multiplyOnRight(evolution.getArray());
      tc.getViewer().renderAsync();
    }

    public boolean getMoveChildren() {
      return moveChildren;
    }
    public void setMoveChildren(boolean moveChildren) {
      this.moveChildren = moveChildren;
    }

}
