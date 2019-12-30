#version 330 core

const vec3 light = vec3(0.5, 0.5, 0.5);

in vec3 normal;

void main() {
    float lv = dot(normal, light);
    float corrected = pow(lv, 1.0 / 2.2);
    //gl_FragColor = vec4(137.0 / 255.0, 183.0 / 255.0, 1.0, 1.0);
    gl_FragColor = vec4(corrected, corrected, corrected, 1.0);
}
