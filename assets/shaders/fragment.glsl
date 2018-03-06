#version 330 core

//input from vertex shader
in struct VertexData
{
    vec3 toLight;
    vec3 toCamera;
    vec2 textureCoordinate;
    vec3 normal;
} vertexData;


uniform vec2 screensize;
uniform vec3 lightColor;

// texturen
uniform sampler2D diff;
uniform sampler2D spec;
uniform sampler2D emit;
uniform sampler2D alienTex;

uniform float alienFactor;
uniform float shininess;
uniform float uvMultiplier; // mvMultiplier > 1 --> man macht größere schritte in der Textur, die Textru wirkt kleiner

float ambient = 0.2f; // ambienter teil des Lichtfarbe hier erstmal 20 %
float light_attenuation_c = 1.0f;
float light_attenuation_l = 0.4f; // werte rumspielen
float light_attenuation_q = 0.4f;

//fragment shader output
out vec4 color;

void main(){
    color = vec4(0, 0, 0, 0);

    // eingehende vectoren normaisieren
    vec3 toLightNorm           = normalize(vertexData.toLight);
    vec3 toCameraNorm          = normalize(vertexData.toCamera);
    vec3 normalNorm            = normalize(vertexData.normal);

    // switch flashlight
    vec4 flashlightColor = texture(alienTex, gl_FragCoord.xy / screensize);

    // L = M_a * L_a + (M_d * cos(alpha) + M_s * cos(beta)^k) * L

    // ambiente
    // ambient lighting - grundlegende rundumbeleuchtung L_a
    vec4 ambientColor = vec4(lightColor * ambient, 1.0f); // lightColor * ambient

    // defuse M_d * cos(alpha)
    // cos(alpha)
    float cosAlpha = max(dot(normalNorm, toLightNorm), 0);

    // Texture lookUp für M_d
    vec4 defuseColor = texture(diff, vertexData.textureCoordinate * uvMultiplier);

    // specular
    // cos(beta)^k (k = shininess)
    vec3 reflec = normalize(reflect(-toLightNorm, normalNorm));
    float cosBeta = pow(max(dot(reflec, toCameraNorm), 0), shininess);

    // Texture lookUp für M_s
    vec4 specularColor = texture(spec, vertexData.textureCoordinate * uvMultiplier);

    // emmisiv
    vec4 emitColor = mix(texture(emit, vertexData.textureCoordinate * uvMultiplier), vec4(0.0f, 0.0f, 0.0f, 0.0f), alienFactor);

    //color = defuseColor * ambientColor + (defuseColor * cosAlpha ) * vec4(lightColor, 1.0f);//+ specularColor * cosBeta) * vec4(lightColor, 1.0f);

    // Abstand zwischen punkt und lichtquelle (vertexData.toLight)
    // Vector zwischen punkt und lichtquelle ist gegeben und davon der betrag
    float distance = length(vertexData.toLight);
    float light_attenuation = 1 / (light_attenuation_c + light_attenuation_l * distance + light_attenuation_q*distance*distance);

    vec4 myLightColor = mix(vec4(lightColor, 1.0f), flashlightColor, alienFactor);

    color = emitColor + defuseColor * ambientColor + (defuseColor * cosAlpha + specularColor * cosBeta)*myLightColor*light_attenuation;


}
