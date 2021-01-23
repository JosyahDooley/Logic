package physics;

import engine.SpriteRenderer;
import math.Vector2;

public class LineCollider extends Collider
{
	public Vector2 start = new Vector2();
	public Vector2 end = new Vector2(16, 0);
	
	public void Init()
	{
		SpriteRenderer sr = gameObject.GetRenderer();
		if(sr != null)
		{
			if(sr.sprite != null)
			{
				Vector2 scale = sr.sprite.offset.GetSize().Mul(gameObject.Scale());
				start = scale.Mul(sr.anchor).Neg();
				end = start.Add(new Vector2(scale.x, 0));
			}
		}
	}
	
	public Vector2 GetWorldStart() {return gameObject.Position().Add(start);}
	public Vector2 GetWorldEnd() {return gameObject.Position().Add(end);}
}
