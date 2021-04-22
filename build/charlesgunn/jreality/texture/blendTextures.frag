//
// Fragment shader for jReality tutorial
//
// Authors: Charles Gunn

uniform sampler2D  sampler;
uniform sampler2D sampler2;
uniform bool showTerrain;
uniform bool showGrid;
uniform float BlendFactor;
uniform bool doRadiosity;
void main(void)
{
    vec4 currentSample = texture2D(sampler,gl_TexCoord[1].st); 
    vec4 currentSample2 = texture2D(sampler2,gl_TexCoord[2].st);
    vec4 color = gl_Color;
    if (!showGrid)  {
    	if (!showTerrain) gl_FragColor = color;
    	else  gl_FragColor = color * currentSample;
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
    	vec2 p = gl_TexCoord[0].st;
    	float r = 2.0*sqrt(p.x*p.x+p.y*p.y);
    	atten = r/sqrt(r*r+1.0);
     }   	
     gl_FragColor.rgb = color.rgb * atten;
     gl_FragColor.a = 1.0;
	
}
