#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 color;

varying vec3 vcolor;

void main(){
    gl_Position.xyz = pos;
    gl_Position.w = 1.0;
    vcolor = color;
}
