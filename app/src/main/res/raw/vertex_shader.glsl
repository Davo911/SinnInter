#version 300 es

in mediump vec3 position;
in mediump vec2 textureCoords;
in mediump vec3 normal;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float scale;

out vec3 surfaceNormal;
out vec3 cameraVector;
out vec2 textureCoord;

void main(void)
{
    vec4 worldPos = transformationMatrix * vec4(position * scale, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPos;

    surfaceNormal = (viewMatrix * transformationMatrix * vec4(normal, 0.0)).xyz;

    cameraVector = -(viewMatrix * worldPos).xyz;
    textureCoord = textureCoords;
}

