uniform float FOV, cosFOV, sinFOV;
void main(void)
{
    vec3 surfaceCameraCoordinates, npos;
   
    gl_Position = ftransform();
    vec4 pos = gl_ModelViewMatrix * gl_Vertex;
    npos = normalize((vec3 (pos))/pos.w);

    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;

    float blender = smoothstep(.9*cosFOV, cosFOV, -npos.z);
    // The idea is to flatten the desired spherical disk to a flat disk so that
    // equiangular radial distances on sphere go to equidistant radial steps on the plane
    // this requires some simple trig
    gl_FrontColor = mix(vec4(0.0, 0.0, 0.0, 0.0), vec4(1.0,1.0,1.0,1.0),  blender);
     if ((-npos.z) > .9*cosFOV)  {
        float r = sqrt(pos.x*pos.x + pos.y*pos.y);
        float beta = atan(r, (-pos.z-1.0));
        float factor = sin(2.0*FOV)*beta/(2.0*FOV);
        vec2 nr = normalize(vec2(pos.x, pos.y));
        vec4 newpos = vec4(factor * nr.x, factor*nr.y, -1.0-cos(2.0*FOV), 1.0);
        gl_Position = gl_ProjectionMatrix * newpos;
    }

}
