#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinate;
layout(location = 2) in vec3 normal;

//uniforms
uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 proj_matrix;

//light
uniform vec3 lightPosition;
uniform float alienFactor;

out struct VertexData
{
    vec3 toLight;
    vec3 toCamera;
    vec2 textureCoordinate;
    vec3 normal;
} vertexData;

void main() {
    // M - Material
    // L = M_a * L_a * M_d * cos(alpha) * M_s * cos(beta) * k * l
    // alphs = Winkel zwischen normael und lichtquelle
    // beta  = Winkel zwischen kamerarichtung und der reflektierenten Lichtquelle // für spekulare reflexion

    // allgemein verktor von A nach B --> v = B-A;

    // wir benötigen den Vektor vom Punkt zur lichtquelle
//    vec3 lightVec = lightPosition - vertexData.normal;

    // Normalen-Matrix normal_matrix, indem Sie die Inverse der kombinierten Model-View-Matrix model_view transponieren.
    mat4 model_view = view_matrix * model_matrix;
    mat4 normal_mat = transpose(inverse(model_view));

    // Oberflächennormale berechnen und an fragement shader übergeben
    vertexData.normal = (normal_mat * vec4(normal, 1.0f)).xyz;

    // damit wir später in kamera coordinaten berechenen können, müssen wir alle daten in Kamera koordinaten überführen
    // erst mit model und dann mit view matrix multiplizieren (rechtsbündig)
    vec4 vertexPos_camera = model_view * vec4( position, 1.0f ); // nicht position der camera
    // switch between pointlight und flashlight
     // gleich vectoren die wir voher auch benutzt haben...
     // position der lichtquelle verändert sich, wir müssen jetzt die position der camera nutzen
    vec4 lightPos_camera1 = view_matrix * vec4(lightPosition, 1.0f); // sind schon in weltKoords, nur noch mit view verrechnen
    vec4 lightPos_camera2 = -vertexPos_camera;
    vec4 lightPos_camera  = mix(lightPos_camera1, lightPos_camera2, alienFactor);



    // camera liegt momentan im ursprung
    vertexData.toCamera =  - vertexPos_camera.xyz;
    vertexData.toLight  = (lightPos_camera - vertexPos_camera).xyz;



    vec4 pos = proj_matrix * view_matrix * model_matrix * vec4(position, 1.0f);
    gl_Position = pos;
    vertexData.textureCoordinate = textureCoordinate;
//    vertexData.normal = inverse(transpose(view_matrix * model_matrix)) * normal;

}