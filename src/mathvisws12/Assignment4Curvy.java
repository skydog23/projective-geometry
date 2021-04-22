/**
 *
 * This package is open source software, made available under a BSD license:
 *
 * Copyright (c) 2009, Charles Gunn
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


package mathvisws12;

import java.awt.Color;
import java.awt.Component;

import charlesgunn.jreality.geometry.BezierCurve;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.TriangleGroup;
/**
 * This assignment introduces you to the use of the discretegroup package.  It instantiates an instance of the
 * full icosahedral group (the triangle group *235).  It allocates an instance of {@link DiscreteGroupSceneGraphRepresentation}.
 * Finally it gets a default fundamental region for the group offered as a static method on {@link TriangleGroup}  (a triangle) and sets the
 * world node of the scene graph representation to this domain.
 * 
 * The assignment consists of two somewhat independent tasks: 
 *  1) Replace the geometry (the triangle) used in this template with a geometry (or preferably, a complete scene graph
 *    	component).  This should still be tailored to the underlying group (*235) but it should create some more interesting
 *      shape or effect when tessellated.  
 *  2) Replace the method setValueAtTime(){} with your own code to carry out some animation, either of geometry or 
 *      of appearance attributes (as the example code does).  You can assume that the parameter d runs from 0 to 1.  I will show you
 *      in class (or in the blog) how to activate the animation system that invokes the setValueAtTime() method
 *      and allows you to "play back" your animation. 
 *  YOu can kill two birds with one stone by animating part 1): that is, introduce a fundamental domain that is animated in some way.
 * @author gunn
 *
 */
public class Assignment4Curvy extends Assignment {

	TriangleGroup dg;
	DiscreteGroupSceneGraphRepresentation dgsgr;
	SceneGraphComponent fundDomSGC = SceneGraphUtility.createFullSceneGraphComponent("fundDomSGC"),
			fundDom2SGC = new SceneGraphComponent("fundDom2SGC");
	double c = .5, gamma = Math.PI * .1;
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	private double[][] triangleVerts;
	
	@Override
	public SceneGraphComponent getContent()	{

		dg = TriangleGroup.instanceOfGroup("235");
		for (int i = 0; i<3; ++i)	{
			System.err.println("gen"+i+" = "+Rn.matrixToString(dg.getGenerators()[i].getArray()));
		}
		// create a scene graph representation of the group
		dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		// construct a scene graph component to represent one fundamental domain
		fundDomSGC.addChild(fundDom2SGC);
		IndexedFaceSet tri = (IndexedFaceSet) TriangleGroup.getDefaultFundamentalRegion(dg);
		triangleVerts = tri.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		System.err.println("triangle = "+Rn.toString(triangleVerts));
		ifsf = IndexedFaceSetUtility.constructPolygonFactory(null, triangleVerts, Pn.EUCLIDEAN);
		fundDom2SGC.setGeometry(ifsf.getGeometry()); 
		fundDomSGC.addTool(new de.jtem.discretegroup.util.TranslateTool());
		fundDomSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		fundDomSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		fundDomSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
		// attach it to the scene graph representation
		dgsgr.setWorldNode(fundDomSGC);
		// this will generate a jReality scene graph
		dgsgr.update();
		return dgsgr.getRepresentationRoot();
	}
	public static void main(String[] args) {
		Assignment4Curvy se2d = new Assignment4Curvy();
		se2d.display();	
	}
	
	protected void updateGeometry()	{
		double[] t1 = rotateAround(triangleVerts[1], triangleVerts[2], c, gamma);
		double[] t2 = rotateAround(triangleVerts[2], triangleVerts[1], c, -gamma);
		BezierCurve bc = new BezierCurve(3, new double[][]{triangleVerts[2], t2, t1, triangleVerts[1]});
		for (int i = 0; i<3; ++i)	bc.refine();
		double[][] pts = bc.getPolygonPoints();
		double[][] pts2 = Rn.matrixTimesVector(null, dg.getGenerators()[2].getArray(), pts);
		double[][] allpts = new double[pts.length*2 + 1][];
		for (int i = 0; i<pts.length; ++i)	{
			allpts[i] = pts[i];
			allpts[pts.length*2-i] = pts2[i];
		}
		allpts[pts.length] = triangleVerts[0];
		IndexedFaceSetUtility.constructPolygon(ifsf, allpts);
	}
	
	private double[] rotateAround(double[] A, double[] B, double d, double gamma)	{
		double[] v = Rn.subtract(null, B, A);
		Rn.normalize(v, v);
		double c = Math.cos(gamma),
				s = Math.sin(gamma);
		double[] u = {c*v[0] - s*v[1], s*v[0] + c*v[1], 0,0};
		return Rn.add(null, A, Rn.times(null, d, u));
	}
	/**
	 * The animation is trivial: cycle the colors of the faces and the edges as combinations of red and blue,
	 * in opposite ways.
	 */
	@Override
	public void setValueAtTime(double d) {
		updateGeometry();
	}
	@Override
	public String getDocumentationFile() {
		return "html/Assignment4.html";
	}
	@Override
	public Component getInspector() {
		return null;
	}

}
