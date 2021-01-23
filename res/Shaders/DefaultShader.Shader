#version 140

in vec2 position;
in vec2 textureCoords;

out vec4 color;
out vec2 uvCoords;

uniform vec4 matColor;
uniform mat4 projection;
uniform vec4 offset;
uniform vec2 pixelScale;
uniform vec2 screenPos;
uniform vec2 camPos;

void main()
{
	color = matColor;
	
	gl_Position = projection * (vec4((position * pixelScale) + screenPos, 0, 1) - vec4(camPos, 0, 0));
	uvCoords = (textureCoords * offset.zw) + offset.xy;
}

ENDVERTEX

#version 140

uniform sampler2D sampler;

in vec4 color;
in vec2 uvCoords;

void main()
{
	gl_FragColor = color * texture2D(sampler, uvCoords);
}