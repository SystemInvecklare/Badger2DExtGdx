#ifdef GL_ES
    precision mediump float;
#endif

//varying vec2 v_texCoords;
uniform vec4 u_color;


void main()
{
    //vec2 uv = v_texCoords.xy;
    gl_FragColor = vec4(u_color);
}