package gui;

public class Glyph
{
	public float x;
	public float y;
	public float w;
	public float h;
	public float scaleX;
	public float scaleY;
	
	//Constructor for holding glyph information of a character in a font
	public Glyph(float x, float y, float w, float h, float sX, float sY)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		scaleX = sX;
		scaleY = sY;
	}
}
