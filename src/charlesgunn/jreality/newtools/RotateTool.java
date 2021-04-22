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

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerImpl;
import de.jreality.util.Rectangle3D;

public class RotateTool extends AbstractTool {

	static InputSlot activationSlot = InputSlot.getDevice("RotateActivation");
	static InputSlot evolutionSlot = InputSlot
			.getDevice("TrackballTransformation");
	static InputSlot camPath = InputSlot.getDevice("WorldToCamera");

	boolean fixOrigin = true;

	public RotateTool() {
		super(activationSlot);
		addCurrentSlot(evolutionSlot);
		addCurrentSlot(camPath);
	}

	transient protected SceneGraphComponent selectedComponent;

	transient protected Matrix center = new Matrix();

	transient EffectiveAppearance eap;
	SelectionManager sm = null;
	SceneGraphPath selection;
	boolean subclassHandlesDeactivation = false;
	boolean useSelection = true;

	public void activate(ToolContext tc) {
		System.err.println("Activating rotate tool");
		startTime = tc.getTime();
		if (useSelection) {
			sm = SelectionManagerImpl.selectionManagerForViewer(tc.getViewer());
			// comp = (moveChildren ?
			// tc.getRootToLocal():tc.getRootToToolComponent()).getLastComponent();
			// stop possible animation
			if (sm.getSelection() == null)
				return;
			selection = sm.getSelection().getSGPath();
			selectedComponent = selection.getLastComponent();
			System.err.println("selected component "+Rn.toString(new Matrix(selectedComponent.getTransformation()).getColumn(3)));
		}
		AnimatorTool.getInstance(tc).deschedule(selectedComponent);
		if (selectedComponent.getTransformation() == null)
			selectedComponent.setTransformation(new Transformation());
		if (!fixOrigin)
			center = getCenter(selectedComponent);
		if (eap == null
				|| !EffectiveAppearance.matches(eap, tc
						.getRootToToolComponent())) {
			eap = EffectiveAppearance.create(tc.getRootToToolComponent());
		}
		metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
		System.err.println("metric = "+metric);
	}

	protected Matrix getCenter(SceneGraphComponent comp) {
		Matrix centerTranslation = new Matrix();
		Rectangle3D bb = BoundingBoxUtility.calculateChildrenBoundingBox(comp);
		// need to respect the metric here
		MatrixBuilder.init(null, metric).translate(bb.getCenter()).assignTo(
				centerTranslation);
		return centerTranslation;
	}

	transient private int metric;

	transient protected Matrix result = new Matrix();
	transient protected Matrix evolution = new Matrix();

	transient private double startTime;

	protected boolean moveChildren;

	private double animTimeMin = 250;
	private double animTimeMax = 750;
	protected boolean updateCenter;

	public void perform(ToolContext tc) {
		// System.err.println("Rotating");
		if (useSelection && selection == null)
			return;
		SceneGraphPath toComp = useSelection ? selection : tc.getRootToLocal();
		Matrix root2toComp = new Matrix(toComp.getInverseMatrix(null)); // (moveChildren
																		// ?
																		// tc.getRootToLocal():tc.getRootToToolComponent()).getInverseMatrix(null));
		double[] array = new Matrix(root2toComp).getArray();		
//		System.err.println("root2component "+Rn.toString(new Matrix(root2toComp).getColumn(3)));
		if (array[15] < 0) root2toComp.times(-1);
		evolution.assignFrom(tc.getTransformationMatrix(evolutionSlot));
//		System.err.println("quad path"+Rn.matrixToString(P3.getTransformedAbsolute(array, metric)));
//		System.err.println("quad select"+Rn.matrixToString(P3.getTransformedAbsolute(selectedComponent.getTransformation().getMatrix(), metric)));
		root2toComp.assignFrom(P3.extractOrientationMatrix(null, root2toComp.getArray(), P3.originP3, metric));
		evolution.assignFrom(tc.getTransformationMatrix(evolutionSlot));
		evolution.conjugateBy(root2toComp);
		result.assignFrom(selectedComponent.getTransformation());
		result.multiplyOnRight(evolution);
		selectedComponent.getTransformation().setMatrix(//result.getArray());
				P3.orthonormalizeMatrix(null, result.getArray(), 10 ^ -6,
						metric));
		tc.getViewer().renderAsync();
	}

	public void deactivate(ToolContext tc) {
		double t = tc.getTime() - startTime;
		if (t > animTimeMin && t < animTimeMax) {
			final Viewer vv = tc.getViewer();
			AnimatorTask task = new AnimatorTask() {
				FactoredMatrix e = new FactoredMatrix(evolution, Pn.EUCLIDEAN);
				double rotAngle = e.getRotationAngle();
				double[] axis = e.getRotationAxis();
				{
					if (rotAngle > Math.PI)
						rotAngle = -2 * Math.PI + rotAngle;
				}
				Matrix cen = new Matrix(center);
				SceneGraphComponent c = selectedComponent;

				public boolean run(double time, double dt) {
					if (updateCenter)
						cen = getCenter(c);
					MatrixBuilder m = MatrixBuilder.euclidean(c
							.getTransformation());
					m.times(cen);
					m.rotate(0.05 * dt * rotAngle, axis);
					m.times(cen.getInverse());
					m.assignTo(c);
					vv.renderAsync();
					return true;
				}
			};
			AnimatorTool.getInstance(tc).schedule(selectedComponent, task);
		}
	}

	public boolean getMoveChildren() {
		return moveChildren;
	}

	public void setMoveChildren(boolean moveChildren) {
		this.moveChildren = moveChildren;
	}

	public double getAnimTimeMax() {
		return animTimeMax;
	}

	public void setAnimTimeMax(double animTimeMax) {
		this.animTimeMax = animTimeMax;
	}

	public double getAnimTimeMin() {
		return animTimeMin;
	}

	public void setAnimTimeMin(double animTimeMin) {
		this.animTimeMin = animTimeMin;
	}

	public boolean isUpdateCenter() {
		return updateCenter;
	}

	public void setUpdateCenter(boolean updateCenter) {
		this.updateCenter = updateCenter;
		if (!updateCenter)
			center = new Matrix();
	}

	public boolean isFixOrigin() {
		return fixOrigin;
	}

	public void setFixOrigin(boolean fixOrigin) {
		this.fixOrigin = fixOrigin;
	}

	public SceneGraphComponent getSelectedComponent() {
		return selectedComponent;
	}

	public void setSelectedComponent(SceneGraphComponent selectedComponent) {
		if (selectedComponent != null) {
			this.selectedComponent = selectedComponent;
			useSelection = false;
		} else
			useSelection = true;
	}

}
