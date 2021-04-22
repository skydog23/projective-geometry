//
// Fragment shader for jReality tutorial
//
// Authors: Charles Gunn

uniform sampler2D  sampler;
uniform sampler2D sampler2;
uniform bool showTerrain;
uniform bool showGrid;
uniform float BlendFactor;
uniform float attenExagg;
uniform float attenBlend;
uniform bool doRadiosity;
void main(void)
{
    vec4 currentSample = texture2D(sampler,gl_TexCoord[0].st); 
    vec4 currentSample2 = texture2D(sampler2,gl_TexCoord[1].st);
    vec4 color = gl_Color;
    if (!showGrid)  {
    	if (showTerrain) color *= currentSample;
    }
    else {
 	   	float alpha = BlendFactor * currentSample2.a; 
 	   	vec4 baseColor = color;
 	   	if (showTerrain) {
 	   		currentSample *= color;
 	   	} else currentSample = color;
 	   	color.rgb = mix(currentSample.rgb, currentSample2.rgb, alpha); //( currentSample.rgb * (1.0-alpha) + currentSample2.rgb *alpha); 
	    color.a = 1.0;
	}
	float atten = 1.0;
	if (doRadiosity)	{
    	vec2 p = gl_TexCoord[2].st;
    	float r = attenExagg*sqrt(p.x*p.x+p.y*p.y);
    	atten =  attenBlend + (1.0-attenBlend)*r/sqrt(r*r+1.0);
     }   
     if (gl_Fog.density != 0.0)	{
   		float fog;
    	fog = (gl_Fog.end - gl_FogFragCoord - 5.0) * gl_Fog.scale;
    	fog = exp(-(fog*gl_Fog.density));
    	fog = clamp(fog, 0.0, 1.0);
    	color = vec4(mix( vec3(gl_Fog.color), vec3(color), fog), color.a);
	}     	
     gl_FragColor.rgb = color.rgb * atten;
     gl_FragColor.a = 1.0;
	
}
