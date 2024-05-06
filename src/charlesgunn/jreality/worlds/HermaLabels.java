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


package charlesgunn.jreality.worlds;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.io.IOException;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.examples.CatenoidHelicoid;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;

public class HermaLabels extends Assignment {
  
	
	@Override
	public SceneGraphComponent getContent()  {
		SceneGraphComponent sgc = new SceneGraphComponent("Herma labels");
		SceneGraphComponent world = new SceneGraphComponent("world");
		Appearance ap = new Appearance();
		sgc.setAppearance(ap);
		DefaultGeometryShader dgs = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
		DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setDiffuseColor(Color.white);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			ImageData id = null;
		double scale = 1;
		// get the image for the texture first
		if (args.length > 0) {
			try {
				id = ImageData.load(Input.getInput(args[0]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else { // use a procedural texture
			SimpleTextureFactory stf = new SimpleTextureFactory();
			stf.setColor(0, new Color(0,0,0,0));	// gap color in weave pattern is totally transparent
			stf.setColor(1, new Color(255,0,100));
			stf.setColor(2, new Color(255,255,0));
			stf.update();
			id = stf.getImageData();
			scale = 10;
			dps.setDiffuseColor(Color.white);
		}
		
		double k =  278.0/199.0;  // A4 paper size height:width
		sgc.setGeometry(Primitives.texturedQuadrilateral());
		MatrixBuilder.euclidean().scale(-1, k/2,1).rotateZ(Math.PI).translate(0,0,0).assignTo(sgc);
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<6; ++j)	{
				SceneGraphComponent child = new SceneGraphComponent(""+i+j);
				MatrixBuilder.euclidean().translate(i-1.5,k*j/2 - k*1.0, 0).assignTo(child);
				child.addChild(sgc);
				world.addChild(child);
			}
		}
		double sc = 1.0/3.0;
		MatrixBuilder.euclidean().translate(.014,-.007,-1).scale(sc,sc,1).assignTo(world);
		Texture2D tex = TextureUtility.createTexture(ap, POLYGON_SHADER, id);
		tex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		tex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
//		System.err.println(ap.toString());
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
		// Attach a node below the textured one and show how to turn off texturing in this node
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(1.08, 1.08, 1).getMatrix());
		
		return world;
  }
	@Override
	public void display() {
		super.display();
		Camera cam = CameraUtility.getCamera(viewer);
		cam.setNear(.2);
		cam.setFar(10.0);
		cam.setFieldOfView(55.2);
	}
	static String[] args = null;
	public static void main(String[] xargs) throws IOException {
		args = xargs;
		new HermaLabels().display();
}
}