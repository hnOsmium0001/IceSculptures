#version 330 core

uniform vec3 lightPos;
uniform float ambientStrength;

in vec3 fragPos;
in vec3 interpolatedNormal;

out vec4 fragColor;

const float lightStrength = 0.1;
const float gamma = 2.2;
const vec3 lightColor = vec3(1, 1, 1);

// Code adpated from opengl-tutorial.org
void main() {
    vec3 n = normalize(interpolatedNormal);
    vec3 l = normalize(lightPos);

    // Cosine of the angle between the normal and the light direction,
    // clamped above 0
    //  - light is at the vertical of the triangle -> 1
    //  - light is perpendicular to the triangle -> 0
    //  - light is behind the triangle -> 0
    float cosTheta = clamp(dot(n, l), 0, 1);
    float distance = length(lightPos - fragPos);

    vec3 object = vec3(137.0/255.0, 183.0/255.0, 255.0/255.0);
    vec3 light = lightStrength * lightColor;
    vec3 ambient = ambientStrength * lightColor;

    vec3 color = object * (ambient + light) * cosTheta / (distance*distance);
    vec3 corrected = pow(color, vec3(1.0 / gamma));
    fragColor = vec4(corrected, 1.0);
}