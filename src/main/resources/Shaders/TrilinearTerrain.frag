#extension GL_EXT_texture_array : enable

#import "Common/ShaderLib/Parallax.glsllib"
#import "Common/ShaderLib/Optics.glsllib"
#define ATTENUATION
//#define HQ_ATTENUATION

#import "Shaders/Lib/FragScattering.glsllib"

// Trinlinear mapping related stuff
uniform mat3 g_NormalMatrix;
varying vec3 worldNormal;

varying vec3 worldTangent;

varying float z;
uniform sampler2D m_Noise;

uniform float m_LowResDistance;
// uniform sampler2D m_DiffuseMapLow;
// uniform sampler2D m_DiffuseMapX;
// uniform sampler2D m_NormalMapX;
// uniform sampler2D m_DiffuseMapY;
// uniform sampler2D m_NormalMapY;
// uniform sampler2D m_DiffuseMapZ;
// uniform sampler2D m_NormalMapZ;

uniform sampler2DArray m_DiffuseArray;
uniform sampler2DArray m_NormalArray;

flat varying vec2 voxelData;

varying vec3 texCoord;
#ifdef SEPARATE_TEXCOORD
  varying vec2 texCoord2;
#endif

varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 SpecularSum;

#ifndef VERTEX_LIGHTING
  uniform vec4 g_LightDirection;
  //varying vec3 vPosition;
  varying vec3 vViewDir;
  varying vec4 vLightDir;
  varying vec3 lightVec;
#else
  varying vec2 vertexLightValues;
#endif

#ifdef DIFFUSEMAP
  uniform sampler2D m_DiffuseMap;
#endif

#ifdef SPECULARMAP
  uniform sampler2D m_SpecularMap;
#endif

#ifdef PARALLAXMAP
  uniform sampler2D m_ParallaxMap;
#endif
#if (defined(PARALLAXMAP) || (defined(NORMALMAP_PARALLAX) && defined(NORMALMAP))) && !defined(VERTEX_LIGHTING)
    uniform float m_ParallaxHeight;
#endif

#ifdef LIGHTMAP
  uniform sampler2D m_LightMap;
#endif

#ifdef NORMALMAP
  uniform sampler2D m_NormalMap;

  // For debugging we want it either way
  varying vec3 vNormal;
#else
  varying vec3 vNormal;
#endif

#ifdef ALPHAMAP
  uniform sampler2D m_AlphaMap;
#endif

#ifdef COLORRAMP
  uniform sampler2D m_ColorRamp;
#endif

uniform float m_AlphaDiscardThreshold;

#ifndef VERTEX_LIGHTING
uniform float m_Shininess;

#ifdef HQ_ATTENUATION
uniform vec4 g_LightPosition;
#endif

#ifdef USE_REFLECTION
    uniform float m_ReflectionPower;
    uniform float m_ReflectionIntensity;
    varying vec4 refVec;

    uniform ENVMAP m_EnvMap;
#endif

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    #ifdef V_TANGENT
        d = 1.0 - d*d;
        return step(0.0, d) * sqrt(d);
    #else
        return d;
    #endif
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    #ifdef MINNAERT
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
        return NdotL * pow(max(NdotL * NdotV, 0.1), -1.0) * 0.5;
    #else
        return max(0.0, dot(norm, lightdir));
    #endif
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shiny){
    // NOTE: check for shiny <= 1 removed since shininess is now
    // 1.0 by default (uses matdefs default vals)
    #ifdef LOW_QUALITY
       // Blinn-Phong
       // Note: preferably, H should be computed in the vertex shader
       vec3 H = (viewdir + lightdir) * vec3(0.5);
       return pow(max(tangDot(H, norm), 0.0), shiny);
    #elif defined(WARDISO)
        // Isotropic Ward
        vec3 halfVec = normalize(viewdir + lightdir);
        float NdotH  = max(0.001, tangDot(norm, halfVec));
        float NdotV  = max(0.001, tangDot(norm, viewdir));
        float NdotL  = max(0.001, tangDot(norm, lightdir));
        float a      = tan(acos(NdotH));
        float p      = max(shiny/128.0, 0.001);
        return NdotL * (1.0 / (4.0*3.14159265*p*p)) * (exp(-(a*a)/(p*p)) / (sqrt(NdotV * NdotL)));
    #else
       // Standard Phong
       vec3 R = reflect(-lightdir, norm);
       return pow(max(tangDot(R, viewdir), 0.0), shiny);
    #endif
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
   float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, m_Shininess);

   #ifdef HQ_ATTENUATION
    float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
   #else
    float att = vLightDir.w;
   #endif

   if (m_Shininess <= 1.0) {
       specularFactor = 0.0; // should be one instruction on most cards ..
   }

   specularFactor *= diffuseFactor;

   return vec2(diffuseFactor, specularFactor) * vec2(att);
}
#endif

vec4 getColor(in float voxelId, in vec2 tc, out vec3 normal) {

    vec4 diffuseColor = texture2DArray(m_DiffuseArray, vec3(tc, voxelId));
    vec4 normalHeight = texture2DArray(m_NormalArray, vec3(tc, voxelId));

    normal = normalize((normalHeight.xyz * vec3(2.0) - vec3(1.0)));

    return diffuseColor;
}

void main(){

    float voxelId = voxelData.x;

    // vec2 newTexCoord;
    float alpha = 1.0;

    // Collect the basic axis textures for x, y, and z
    vec3 normalX;
    vec3 normalY;
    vec3 normalZ;
    vec3 normalTop;

    vec4 xColor = getColor(voxelId, texCoord.zy, normalX);
    vec4 yColor = getColor(voxelId, texCoord.xz, normalY);
    vec4 zColor = getColor(voxelId, texCoord.xy, normalZ);

    vec3 blend = abs(normalize(worldNormal));
    blend /= (blend.x + blend.y + blend.z);

    vec4 diffuseColor = xColor * blend.x
                        + yColor * blend.y
                        + zColor * blend.z;

    normalX = vec3(0.0, -normalX.y, normalX.x);
    normalY = vec3(normalY.x, 0.0, normalY.y);
    normalZ = vec3(normalZ.x, -normalZ.y, 0.0);

    // Mix the normal map normals together based on blend
    vec3 bumpNormal = normalX * blend.x
                        + normalY * blend.y
                        + normalZ * blend.z;
    vec3 normal = normalize(bumpNormal);

    normal = normalize(vNormal + g_NormalMatrix * bumpNormal);

    // Moved this to after trilinear mapping is performed so that the color
    // will be accurate
    #ifndef VERTEX_LIGHTING
        float spotFallOff = 1.0;

        #if __VERSION__ >= 110
          // allow use of control flow
          if(g_LightDirection.w != 0.0){
        #endif

          vec3 L       = normalize(lightVec.xyz);
          vec3 spotdir = normalize(g_LightDirection.xyz);
          float curAngleCos = dot(-L, spotdir);
          float innerAngleCos = floor(g_LightDirection.w) * 0.001;
          float outerAngleCos = fract(g_LightDirection.w);
          float innerMinusOuter = innerAngleCos - outerAngleCos;
          spotFallOff = (curAngleCos - outerAngleCos) / innerMinusOuter;

          #if __VERSION__ >= 110
              if(spotFallOff <= 0.0){
                  gl_FragColor.rgb = AmbientSum * diffuseColor.rgb;
                  gl_FragColor.a   = alpha;
                  return;
              }else{
                  spotFallOff = clamp(spotFallOff, 0.0, 1.0);
              }
             }
          #else
             spotFallOff = clamp(spotFallOff, step(g_LightDirection.w, 0.001), 1.0);
          #endif
    #endif

    #ifdef SPECULARMAP
      vec4 specularColor = texture2D(m_SpecularMap, newTexCoord);
    #else
      vec4 specularColor = vec4(1.0);
    #endif

    #ifdef LIGHTMAP
       vec3 lightMapColor;
       #ifdef SEPARATE_TEXCOORD
          lightMapColor = texture2D(m_LightMap, texCoord2).rgb;
       #else
          lightMapColor = texture2D(m_LightMap, texCoord.xz).rgb;
       #endif
       specularColor.rgb *= lightMapColor;
       diffuseColor.rgb  *= lightMapColor;
    #endif

    #ifdef VERTEX_LIGHTING
       vec2 light = vertexLightValues.xy;
       #ifdef COLORRAMP
           light.x = texture2D(m_ColorRamp, vec2(light.x, 0.0)).r;
           light.y = texture2D(m_ColorRamp, vec2(light.y, 0.0)).r;
       #endif

        #ifndef USE_SCATTERING
            gl_FragColor.rgb =  AmbientSum     * diffuseColor.rgb +
                                DiffuseSum.rgb * diffuseColor.rgb  * vec3(light.x) +
                                SpecularSum    * specularColor.rgb * vec3(light.y);
        #else
            vec3 color = AmbientSum     * diffuseColor.rgb +
                         DiffuseSum.rgb * diffuseColor.rgb  * vec3(light.x) +
                         SpecularSum    * specularColor.rgb * vec3(light.y);
            gl_FragColor.rgb =  calculateGroundColor(vec4(color, 1.0)).rgb;
        #endif
    #else
       vec4 lightDir = vLightDir;
       lightDir.xyz = normalize(lightDir.xyz);
       vec3 viewDir = normalize(vViewDir);

       vec2   light = computeLighting(normal, viewDir, lightDir.xyz) * spotFallOff;
       #ifdef COLORRAMP
           diffuseColor.rgb  *= texture2D(m_ColorRamp, vec2(light.x, 0.0)).rgb;
           specularColor.rgb *= texture2D(m_ColorRamp, vec2(light.y, 0.0)).rgb;
       #endif

       // Workaround, since it is not possible to modify varying variables
       vec4 SpecularSum2 = vec4(SpecularSum, 1.0);
       #ifdef USE_REFLECTION
            vec4 refColor = Optics_GetEnvColor(m_EnvMap, refVec.xyz);

            // Interpolate light specularity toward reflection color
            // Multiply result by specular map
            specularColor = mix(SpecularSum2 * light.y, refColor, refVec.w) * specularColor;

            SpecularSum2 = vec4(1.0);
            light.y = 1.0;
       #endif

        #ifndef USE_SCATTERING
            gl_FragColor.rgb =  AmbientSum     * diffuseColor.rgb +
                                DiffuseSum.rgb * diffuseColor.rgb  * vec3(light.x) +
                                SpecularSum    * specularColor.rgb * vec3(light.y);
        #else
            vec3 color = AmbientSum     * diffuseColor.rgb +
                         DiffuseSum.rgb * diffuseColor.rgb  * vec3(light.x) +
                         SpecularSum    * specularColor.rgb * vec3(light.y);
            gl_FragColor.rgb =  calculateGroundColor(vec4(color, 1.0)).rgb;
        #endif
    #endif
    gl_FragColor.a = alpha;
}