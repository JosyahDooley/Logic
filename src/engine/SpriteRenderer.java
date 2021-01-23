package engine;

import gui.Sprite;
import math.Matrix4x4;
import math.Rect;
import math.Vector2;

public class SpriteRenderer extends LogicBehaviour
{
	public Sprite sprite;
	public Vector2 anchor = new Vector2(0, 0);
	private int i;
	
	//Set the uniforms of the gameobject and it's sprite
	public void SetUniforms()
	{
		sprite.material.shader.SetUniform("anchor", anchor);
		
		sprite.material.shader.SetUniform("transformationMatrix", gameObject.Matrix());
		
		//Set the position uniform
		sprite.material.shader.SetUniform("screenPos", gameObject.Position().x, gameObject.Position().y);
		
		//Set the scale uniform
		sprite.material.shader.SetUniform("pixelScale", sprite.offset.width, sprite.offset.height);
		
		//Create and set the uv offset uniform
		Rect uv = sprite.UV();
		sprite.material.shader.SetUniform("offset", uv.x, uv.y, uv.width, uv.height);
		
		sprite.material.shader.SetUniform("depth", gameObject.depth);
	}
	
	public boolean Contains(Vector2 v)
	{
		if(sprite == null) return false;
		
		Vector2[] points = GetCorners();
		
		for(i = 0; i < points.length; i++)
		{
			Vector2 direction;
			if(i < 3) direction = points[i + 1].Sub(points[i]);
			else direction = points[0].Sub(points[i]);
			
			if(Math.signum(direction.x * (v.y - points[i].y) - direction.y * (v.x - points[i].x)) >= 0) return false;
		}
		return true;
	}
	
	public Vector2[] GetCorners()
	{
		if(sprite == null) return null;
		
		Matrix4x4 m = gameObject.Matrix();
		Vector2 offset = new Vector2(sprite.offset.width, sprite.offset.height).Mul(anchor).Neg();
		
		Vector2[] ret = new Vector2[4];
		ret[0] = m.TransformPoint(offset);
		ret[1] = m.TransformPoint(new Vector2(offset.x, offset.y + sprite.offset.height));
		ret[2] = m.TransformPoint(new Vector2(offset.x + sprite.offset.width, offset.y + sprite.offset.height));
		ret[3] = m.TransformPoint(new Vector2(offset.x + sprite.offset.width, offset.y));
		
		return ret;
	}
}
