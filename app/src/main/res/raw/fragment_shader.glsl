#version 300 es

in mediump vec3 surfaceNormal;
in mediump vec3 cameraVector;
in mediump vec2 textureCoord;

uniform sampler2D uTexture;
uniform bool useTexture;

out highp vec4 out_Colour;


void main(void)
{
    mediump float AMBIENT_LIGHT = 0.2;
    mediump float brightness = max(dot(normalize(surfaceNormal), normalize(cameraVector)), AMBIENT_LIGHT);

    if(useTexture)
    {
       out_Colour = vec4((texture(uTexture, textureCoord) * brightness).xyz, 1.0);
    }
    else
        out_Colour = vec4(vec3(1.0, 1.0, 1.0) * brightness , 1.0);
}