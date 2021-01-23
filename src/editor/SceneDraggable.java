package editor;

import engine.GameObject;
import math.Vector2;

public class SceneDraggable extends engine.Object
{
	public GameObject g;
	public Vector2 direction;
	public Vector2 dragOffset;
	
	public SceneDraggable(GameObject g, Vector2 dir, Vector2 offset)
	{
		this.g = g;
		direction = dir;
		dragOffset = offset;
	}
}
