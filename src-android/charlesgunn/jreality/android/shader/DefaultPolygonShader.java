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


package charlesgunn.jreality.android.shader;

import static de.jreality.shader.CommonAttributes.REFLECTION_MAP;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING_DEFAULT;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_1;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_2;
import static de.jreality.shader.CommonAttributes.USE_GLSL;

import java.awt.Color;

import javax.microedition.khronos.opengles.GL11;

import charlesgunn.jreality.android.AndroidRenderer;
import charlesgunn.jreality.android.AndroidRendererHelper;
import charlesgunn.jreality.android.AndroidRenderingState;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

	public static final int FRONT_AND_BACK = GL11.GL_FRONT_AND_BACK;
	public static final int FRONT = GL11.GL_FRONT;
	public static final int BACK = GL11.GL_BACK;
	
	boolean		smoothShading = true;
	Texture2D texture2D,  texture2D_1, texture2D_2;
	AndroidTexture2D joglTexture2D, joglTexture2D_1, joglTexture2D_2;
	CubeMap reflectionMap;
	AndroidCubeMap joglCubeMap;
	public DefaultVertexShader vertexShader = new DefaultVertexShader();
	boolean useGLSL = false; //, oneTexturePerImage = false;
	int texUnit = 0, refMapUnit = 0;
	GlslProgram glslProgram;
	transient boolean geometryHasTextureCoordinates = false, 
		hasTextures = false,
		firstTime = true,
		noneuclideanInitialized = false;
	
	transient de.jreality.shader.DefaultPolygonShader templateShader;
	// try loading the OpenGL11 shader for the non-euclidean cases
//	boolean		poincareModel = false;		// interpolate shaded values between vertices
//	SceneGraphPath poincarePath;
//	static GlslProgram noneuclideanShader = null;
//	static String shaderLocation = "de/jreality/jogl/shader/resources/noneuclidean.vert";
//	NoneuclideanGLSLShader noneuc = new NoneuclideanGLSLShader();
	boolean hasNoneuc = false;
	
	public DefaultPolygonShader()	{
		
	}
	
	public DefaultPolygonShader(de.jreality.shader.DefaultPolygonShader ps) {
		templateShader = ps;
	}

	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap,name);
		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,SMOOTH_SHADING), SMOOTH_SHADING_DEFAULT);	
		useGLSL = eap.getAttribute(ShaderUtility.nameSpace(name, USE_GLSL), false);	
		//oneTexturePerImage = eap.getAttribute(ShaderUtility.nameSpace(name,ONE_TEXTURE2D_PER_IMAGE), true);	
	    joglTexture2D = joglTexture2D_1 = joglTexture2D_2 = null;
	    joglCubeMap = null;
	    hasTextures = false;
		if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D), eap)) {
			texture2D = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,CommonAttributes.TEXTURE_2D), eap);			
			joglTexture2D = new AndroidTexture2D(texture2D);
			hasTextures = true;
		}
	    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name, REFLECTION_MAP), eap)){
	    	reflectionMap = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name, REFLECTION_MAP));		    	
	    	joglCubeMap = new AndroidCubeMap(reflectionMap);
	    	hasTextures = true;
	    }
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_1), eap)) {
	    	texture2D_1 = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_1), eap);		    	
	    	joglTexture2D_1 = new AndroidTexture2D(texture2D_1);
	    	hasTextures = true;
	    }
      
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_2), eap)) {
	    	texture2D_2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_2), eap);		    	
	    	joglTexture2D_2 = new AndroidTexture2D(texture2D_2);
	    	hasTextures = true;
	    }
      
	    if (useGLSL)		{
//			if (GlslProgram.hasGlslProgram(eap, name)) {
//				// dummy to write glsl values like "lightingEnabled"
//				Appearance app = new Appearance();
//				glslProgram = new GlslProgram(app, eap, name);
//				hasNoneuc = false;
//			} 
//			else {
//				noneuc.setFromEffectiveAppearance(eap, name);
//				hasNoneuc = true;
//				glslProgram = noneuc.getNoneuclideanShader();
////			    System.err.println("using non euc shader");
//			}
	    } else hasNoneuc = false;
		vertexShader.setFromEffectiveAppearance(eap, name);
		geometryHasTextureCoordinates = false;
		firstTime = true;
 	}
	
	public void preRender(AndroidRenderingState jrs)	{
		AndroidRenderer jr = jrs.renderer;
		GL11 gl = jr.globalGL;
		if (smoothShading) gl.glShadeModel(GL11.GL_SMOOTH);
		else		gl.glShadeModel(GL11.GL_FLAT);
		jrs.smoothShading = smoothShading;
		int texunitcoords = 0;
//    	hasTextures = false;
		if (hasTextures) {
			// TODO ES diff
//			gl.glPushAttrib(GL11.GL_TEXTURE_BIT);
			texUnit = GL11.GL_TEXTURE0; 
		    Geometry curgeom = jr.renderingState.currentGeometry;
		    if (firstTime)	// assume geometry stays constant between calls to setFromEffectiveAppearance() ...
		    	if (curgeom != null && (curgeom instanceof IndexedFaceSet) &&
		    		((IndexedFaceSet) curgeom).getVertexAttributes(Attribute.TEXTURE_COORDINATES) != null) {
		    			geometryHasTextureCoordinates = true; 
		    	}
		    if (geometryHasTextureCoordinates) {

			    if (joglTexture2D != null) {
			    	gl.glActiveTexture(GL11.GL_TEXTURE0);
			      	gl.glEnable(GL11.GL_TEXTURE_2D);
					Texture2DLoaderAndroid.render(gl, joglTexture2D, jrs.oneTexture2DPerImage);
				    texUnit++;
				    texunitcoords++;		
			    }
			    if (joglTexture2D_1 != null) {
				    gl.glActiveTexture(GL11.GL_TEXTURE0+1);
				    gl.glEnable(GL11.GL_TEXTURE_2D);
					Texture2DLoaderAndroid.render(gl, joglTexture2D_1, jrs.oneTexture2DPerImage);
				    texUnit++;
				    texunitcoords++;
			    }
			    if (joglTexture2D_2 != null) {
			    	gl.glActiveTexture(GL11.GL_TEXTURE0+2);
			      	gl.glEnable(GL11.GL_TEXTURE_2D);
					Texture2DLoaderAndroid.render(gl, joglTexture2D_2, jrs.oneTexture2DPerImage);
				    texUnit++;
				    texunitcoords++;		
			    }
		    }
		}

	    if (joglCubeMap != null)  {
	      	gl.glActiveTexture(texUnit);
			//TODO ES diff
	      	//gl.glEnable(GL11.GL_TEXTURE_CUBE_MAP);
			refMapUnit = texUnit;
			Texture2DLoaderAndroid.render(jr, joglCubeMap);
			texUnit++;
		} 	
    
		jr.renderingState.texUnitCount = texunitcoords; 
		vertexShader.render(jrs); 
//		if (useGLSL)	{
//	    	if ( hasNoneuc)	{
//	    		noneuc.render(jr);
//	    	}
//	    	else GlslLoader.render(glslProgram, jr);		
//		}
	    firstTime = false;
}
	
	public void postRender(AndroidRenderingState jrs)	{
		if (!jrs.shadeGeometry) return;
		AndroidRenderer jr = jrs.renderer;
		GL11 gl = jrs.renderer.globalGL;
//		if (useGLSL) {
//			GlslLoader.postRender(glslProgram, gl);			
//		}
//		for (int i = GL11.GL_TEXTURE0; i <  GL11.GL_TEXTURE0+3; ++i) {
		if (hasTextures)	{
		    if (joglTexture2D != null) {
				gl.glActiveTexture(GL11.GL_TEXTURE0);
				gl.glDisable(GL11.GL_TEXTURE_2D);
		    }
		    if (joglTexture2D_1 != null) {
				gl.glActiveTexture(GL11.GL_TEXTURE0+1);
				gl.glDisable(GL11.GL_TEXTURE_2D);
		    }
		    if (joglTexture2D_2 != null) {
				gl.glActiveTexture(GL11.GL_TEXTURE0+2);
				gl.glDisable(GL11.GL_TEXTURE_2D);
		    }			
		}
//		}
		if (joglCubeMap != null) {
			gl.glActiveTexture(refMapUnit);
			// TODO ES diff
//			gl.glDisable(GL11.GL_TEXTURE_CUBE_MAP);
//			gl.glDisable(GL11.GL_TEXTURE_GEN_S);
//			gl.glDisable(GL11.GL_TEXTURE_GEN_T);
//			gl.glDisable(GL11.GL_TEXTURE_GEN_R);
		}
		jr.renderingState.texUnitCount=0;
		// TODO fix this to return to previous state -- maybe textures NOT active
//		if (hasTextures) gl.glPopAttrib();
	}

	public boolean providesProxyGeometry() {		
		return false;
	}

	static Color[] cdbg = {Color.BLUE, Color.GREEN, Color.YELLOW,  Color.RED,Color.GRAY, Color.WHITE};
	public void render(final AndroidRenderingState jrs)	{
		final Geometry g = jrs.currentGeometry;
		final AndroidRenderer jr = jrs.renderer;
		final boolean useDisplayLists = jrs.useDisplayLists;
		if (jrs.shadeGeometry) preRender(jrs);
		
		// I had to do locking here, seems that jogl backend only locks on the corresponding component...
		// maybe this needs to be done at other places too...?
		if (g != null)	
		Scene.executeReader(g, new Runnable() {
			
			public void run() 
		{
			if (g instanceof Sphere || g instanceof Cylinder)	{	
				int i = 3;
				int dlist;
//				if (g instanceof Sphere) {
//					jr.renderingState.polygonCount += 24*(i*(i+1)+3);
//					dlist = jr.renderingState.getSphereDisplayLists(i);
//				}
//				else 			{
//					jr.renderingState.polygonCount += 4*Math.pow(2, i);
//					dlist = jr.renderingState.getCylinderDisplayLists(i);
//				}
				// TODO ES diff
//				jr.globalGL.glCallList(dlist);
				displayListsDirty = false;
			}
			else if ( g instanceof IndexedFaceSet)	{
				jr.renderingState.polygonCount += ((IndexedFaceSet) g).getNumFaces();
//				if (providesProxyGeometry())	{
//					// TODO ES diff
//					dListProxy  = proxyGeometryFor(jrs);
//					displayListsDirty = false;
//					jr.globalGL.glCallList(dListProxy);
//				}
//				else 	{
					AndroidRendererHelper.drawFaces(jr, (IndexedFaceSet) g);			
//				}	
			}
		}
		});
	}

    
	public void flushCachedState(AndroidRenderer jr) {
		displayListsDirty = true;
	}
}
