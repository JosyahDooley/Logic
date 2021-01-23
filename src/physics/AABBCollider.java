package physics;

import java.util.ArrayList;
import java.util.List;

import engine.SpriteRenderer;
import math.Vector2;

public class AABBCollider extends Collider
{
	public Vector2 size = new Vector2(16, 16);
	public Vector2 anchor = new Vector2();
	
	public void Init()
	{
		SpriteRenderer sr = gameObject.GetRenderer();
		if(sr != null)
		{
			if(sr.sprite != null)
			{
				size = sr.sprite.offset.GetSize().Mul(gameObject.Scale());
				anchor = size.Mul(sr.anchor).Neg();
			}
		}
	}
	
	public List<Vector2> GetCorners()
	{
		List<Vector2> corners = new ArrayList<Vector2>();
		Vector2 anchorPos = gameObject.Position().Add(anchor);
		corners.add(anchorPos);
		corners.add(anchorPos.Add(new Vector2(0, size.y)));
		corners.add(anchorPos.Add(size));
		corners.add(anchorPos.Add(new Vector2(size.x, 0)));
		return corners;
	}
}
