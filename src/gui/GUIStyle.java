package gui;

import engine.Texture;
import math.Rect;

public class GUIStyle
{
	public String name;
	public Rect offset;
	public Rect padding;
	public Rect uv;
	public Rect paddingUV;
	
	//Constructor to create a style
	public GUIStyle(String name, Texture t, Rect offset, Rect padding)
	{
		//Set the information passed
		this.name = name;
		this.offset = offset;
		this.padding = padding;
		
		float w = t.Width();
		float h = t.Height();
		
		//And set the uv information as a 0-1 float value since in the file we use in values between 0 and size
		uv = new Rect(offset.x / w, offset.y / h, offset.width / w, offset.height / h);
		paddingUV = new Rect(padding.x / w, padding.y / h, padding.width / w, padding.height / h);
	}
}
