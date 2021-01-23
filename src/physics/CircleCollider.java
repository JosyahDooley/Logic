package physics;

import engine.SpriteRenderer;
import math.Vector2;

public class CircleCollider extends Collider
{
	public float radius = 16;
	public Vector2 anchor = new Vector2();
	
	public void Init()
	{
		SpriteRenderer sr = gameObject.GetRenderer();
		if(sr != null)
		{
			if(sr.sprite != null)
			{
				Vector2 scale = sr.sprite.offset.GetSize().Mul(gameObject.Scale()).Mul(0.5f);
				radius = scale.Min();
				
				//This anchor setting is wrong
				anchor = scale.Mul(sr.anchor).Neg().Add(radius).Sub(scale.Mul(sr.anchor));
			}
		}
	}
	
	public Vector2 Center() {return gameObject.Position().Add(anchor);}
}
