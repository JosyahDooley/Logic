package gui;

import input.Mouse;
import math.Mathf;
import math.Rect;

public class GUIArea
{
	public Rect culled = new Rect();
	public Rect actual = new Rect();
	public int scrollHeight;
	private int scroll = 0;
	
	public GUIArea(Rect culled, Rect actual)
	{
		if(culled != null) {this.culled = culled; this.actual = actual;}
	}
	
	public final int Scroll() {return scroll;}
	public final int Scroll(int offset)
	{
		if(culled.Contains(Mouse.Position()))
		{
			float leftOver = scrollHeight - culled.height;
			if(leftOver <= 0) scroll = 0;
			else scroll = (int)Mathf.Clamp(offset + (-Mouse.Scroll() * 10), 0, leftOver);
		}
		else scroll = offset;
		
		return scroll;
	}
}
