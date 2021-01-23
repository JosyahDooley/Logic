package physics;

import java.util.ArrayList;
import java.util.List;

import engine.LogicBehaviour;
import engine.SpriteRenderer;
import math.Rect;
import math.Vector2;

public class Collider extends LogicBehaviour
{
	public boolean isTrigger = false; 
	
	static List<Collider> colliders = new ArrayList<Collider>();
	private static List<Collider> movers = new ArrayList<Collider>();
	private static List<Collider> solids = new ArrayList<Collider>();
	
	public void Init() {}
	
	public static void Clear() {colliders.clear();}
	public static void ClearFrame() {movers.clear(); solids.clear();}
	
	public static void AddFrameCollider(Collider c)
	{
		if(c == null) return;
		
		//isDirty is resetting somewhere for some reason
		if(c.gameObject.isDirty()) movers.add(c);
		else solids.add(c);
	}
	
	public static void CompareCollisions()
	{
		for(int m = 0; m < movers.size(); m++)
		{
			Collider mover = movers.get(m);
			for(int s = 0; s < solids.size(); s++)
			{
				Collider solid = solids.get(s);
				if(mover instanceof BoundsCollider && solid instanceof BoundsCollider) CompareCollision((BoundsCollider) mover, (BoundsCollider) solid);
				
				else if(mover instanceof AABBCollider && solid instanceof AABBCollider) CompareCollision((AABBCollider) mover, (AABBCollider) solid);
				else if(mover instanceof AABBCollider && solid instanceof CircleCollider) CompareCollision((AABBCollider) mover, (CircleCollider) solid, 0);
				else if(mover instanceof AABBCollider && solid instanceof LineCollider) CompareCollision((AABBCollider) mover, (LineCollider) solid);
				
				else if(mover instanceof CircleCollider && solid instanceof CircleCollider) CompareCollision((CircleCollider) mover, (CircleCollider) solid);
				else if(mover instanceof CircleCollider && solid instanceof AABBCollider) CompareCollision((AABBCollider) solid, (CircleCollider) mover, 1);
				else if(mover instanceof CircleCollider && solid instanceof LineCollider) CompareCollision((CircleCollider) mover, (LineCollider) solid);
			}
			mover.gameObject.ResetDirty();
			solids.add(mover);
		}
	}
	
	private static void CompareCollision(CircleCollider mover, CircleCollider solid)
	{
		Vector2 moverCenter = mover.gameObject.Position().Add((mover.anchor));
		Vector2 solidCenter = solid.gameObject.Position().Add((solid.anchor));
		
		float distance = solidCenter.Sub(moverCenter).Length();
		
		if(distance >= mover.radius + solid.radius) return;
		
		//Collision point = solid - (direction to mover * radius);
		if(mover.isTrigger || solid.isTrigger){mover.gameObject.CallTriggerCallback(); solid.gameObject.CallTriggerCallback();}
		else
		{
			mover.gameObject.CallCollisionCallback();
			solid.gameObject.CallCollisionCallback();
			mover.gameObject.Move(moverCenter.Sub(solidCenter).Normalized().Mul((mover.radius + solid.radius) - distance));
		}
	}
	
	private static void CompareCollision(AABBCollider mover, AABBCollider solid)
	{
		Vector2 moverCorner = new Vector2(mover.gameObject().Position().Add(mover.anchor));
		Rect moverRect = new Rect(moverCorner, mover.size);
		
		Vector2 solidCorner = new Vector2(solid.gameObject().Position().Add(solid.anchor));
		Rect solidRect = new Rect(solidCorner, solid.size);
		
		Rect overlap = moverRect.GetIntersection(solidRect);
		if(overlap != null)
		{
			if(mover.isTrigger || solid.isTrigger){mover.gameObject.CallTriggerCallback(); solid.gameObject.CallTriggerCallback();}
			else
			{
				mover.gameObject.CallCollisionCallback();
				solid.gameObject.CallCollisionCallback();
				
				if(overlap.width < overlap.height)
				{
					if(moverRect.x < overlap.x) mover.gameObject.Move(new Vector2(-overlap.width, 0));
					else mover.gameObject.Move(new Vector2(overlap.width, 0));
				}
				else
				{
					if(moverRect.y < overlap.y) mover.gameObject.Move(new Vector2(0, -overlap.height));
					else mover.gameObject.Move(new Vector2(0, overlap.height));
				}
			}
		}
	}
	
	//Bounds Collider vs Bounds Collider
	private static void CompareCollision(BoundsCollider mover, BoundsCollider solid)
	{
		SpriteRenderer moverRenderer = (SpriteRenderer) mover.gameObject.GetRenderer();
		SpriteRenderer solidRenderer = (SpriteRenderer) solid.gameObject.GetRenderer();
		if(moverRenderer == null || solidRenderer == null) return;
		if(moverRenderer.sprite == null || solidRenderer.sprite == null) return;
		
		Vector2[] poly1 = moverRenderer.GetCorners();
		Vector2[] poly2 = solidRenderer.GetCorners();
		
		Rect poly1Bounds = new Rect(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		Rect poly2Bounds = new Rect(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for(int i = 0; i < 4; i++)
		{
			poly1Bounds.x = Math.min(poly1Bounds.x, poly1[i].x);
			poly2Bounds.x = Math.min(poly2Bounds.x, poly2[i].x);
			
			poly1Bounds.width = Math.max(poly1Bounds.width, poly1[i].x);
			poly2Bounds.width = Math.max(poly2Bounds.width, poly2[i].x);
			
			poly1Bounds.y = Math.min(poly1Bounds.y, poly1[i].y);
			poly2Bounds.y = Math.min(poly2Bounds.y, poly2[i].y);
			
			poly1Bounds.height = Math.max(poly1Bounds.height, poly1[i].y);
			poly2Bounds.height = Math.max(poly2Bounds.height, poly2[i].y);
		}
		poly1Bounds.width = poly1Bounds.width - poly1Bounds.x;
		poly2Bounds.width = poly2Bounds.width - poly2Bounds.x;
		poly1Bounds.height = poly1Bounds.height - poly1Bounds.y;
		poly2Bounds.height = poly2Bounds.height - poly2Bounds.y;
		
		Rect overlap = poly1Bounds.GetIntersection(poly2Bounds);
		if(overlap != null)
		{
			if(mover.isTrigger || solid.isTrigger){mover.gameObject.CallTriggerCallback(); solid.gameObject.CallTriggerCallback();}
			else
			{
				mover.gameObject.CallCollisionCallback();
				solid.gameObject.CallCollisionCallback();
				
				if(overlap.width < overlap.height)
				{
					if(poly1[0].x < overlap.x) mover.gameObject.Move(new Vector2(-overlap.width, 0));
					else mover.gameObject.Move(new Vector2(overlap.width, 0));
				}
				else
				{
					if(poly1[0].y < overlap.y) mover.gameObject.Move(new Vector2(0, -overlap.height));
					else mover.gameObject.Move(new Vector2(0, overlap.height));
				}
			}
		}
	}
	
	private static void CompareCollision(AABBCollider mover, LineCollider solid)
	{
		List<Vector2> corners = mover.GetCorners();
		Vector2 start = corners.get(0);
		Vector2 center = start.Add(corners.get(2).Sub(start).Mul(0.5f));
		
		Vector2 lineStart = solid.GetWorldStart();
		Vector2 lineEnd = solid.GetWorldEnd();
		
		int collided = 0;
		for(int i = 0; i < 4; i++)
		{
			Vector2 corner = corners.get(i);
			if(Vector2.Intersection(center, corner, lineStart, lineEnd) != null)
			{
				Vector2 projection = corner.Project(lineStart, lineEnd);
				mover.gameObject.Move(projection.Sub(corner));
			}
		}
		if(collided == 0)
		{
			for(int i = 0; i < 4; i++)
			{
				Vector2 corner = corners.get(i);
				Vector2 next = corners.get((i + 1) % corners.size());
				
				if(Vector2.Intersection(corner, next, lineStart, lineEnd) != null)
				{
					Rect r = new Rect(start, corners.get(2).Sub(corners.get(0)));
					if(r.Contains(lineStart))
					{
						Vector2 projection = lineStart.Project(corner, next);
						mover.gameObject.Move(lineStart.Sub(projection));
					}
					else if(r.Contains(lineEnd))
					{
						Vector2 projection = lineEnd.Project(corner, next);
						mover.gameObject.Move(lineEnd.Sub(projection));
					}
				}
			}
		}
	}
	
	private static void CompareCollision(AABBCollider aabb, CircleCollider circle, int moverStatus)
	{
		Vector2 center = circle.Center();
		Vector2 closestPoint = center.Project(aabb.GetCorners());
		float distance = Vector2.Distance(center, closestPoint);
		
		if(distance < circle.radius)
		{
			if(aabb.isTrigger || circle.isTrigger){aabb.gameObject.CallTriggerCallback(); circle.gameObject.CallTriggerCallback();}
			else
			{
				aabb.gameObject.CallCollisionCallback();
				circle.gameObject.CallCollisionCallback();
				
				Vector2 offset = Vector2.Direction(center, closestPoint).Mul(circle.radius - distance);
				if(moverStatus == 0) aabb.gameObject.Move(offset);
				else circle.gameObject.Move(offset.Neg());
			}
		}
	}
	
	private static void CompareCollision(CircleCollider circle, LineCollider line)
	{
		Vector2 start = line.GetWorldStart();
		Vector2 end = line.GetWorldEnd();
		Vector2 center = circle.Center();
		
		Vector2 proj = center.Project(start, end);
		float distance = Vector2.Distance(center, proj);
		if(distance < circle.radius)
		{
			if(line.isTrigger || circle.isTrigger){line.gameObject.CallTriggerCallback(); circle.gameObject.CallTriggerCallback();}
			else
			{
				line.gameObject.CallCollisionCallback();
				circle.gameObject.CallCollisionCallback();
				
				circle.gameObject.Move((Vector2.Direction(center, proj).Mul(circle.radius - distance)).Neg());
			}
		}
	}
}
