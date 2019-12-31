#version 330 core

uniform mat4 modelView;
uniform mat4 projection;

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
//layout(location = 2) in vec3 color;

out vec3 fragPos;
out vec3 interpolatedNormal;

void main(){
    // OpenGL-defined position output for the vertex
    gl_Position = projection * modelView * vec4(pos, 1.0);
    // Widll be interpolated per-fragment
    fragPos = pos;
    interpolatedNormal = normal;
}
