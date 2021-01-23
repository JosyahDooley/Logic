package math;

import java.util.ArrayList;
import java.util.List;

public class Vector2 implements Cloneable
{
	public float x;
	public float y;
	
	public Vector2() {x = 0; y = 0;}
	public Vector2(float v) {x = v; y = v;}
	public Vector2(Vector2 v) {x = v.x; y = v.y;}
	public Vector2(float x, float y) {this.x = x; this.y = y;}
	
	public void Set(Vector2 v) {x = v.x; y = v.y;}
	public void Set(float x, float y) {this.x = x; this.y = y;}
	
	public Vector2 Add(float v) {return new Vector2(x + v, y + v);}
	public Vector2 Add(Vector2 v) {return new Vector2(x + v.x, y + v.y);}
	public Vector2 Add(float x, float y) {return new Vector2(this.x + x, this.y + y);}
	
	public Vector2 Sub(float v) {return new Vector2(x - v, y - v);}
	public Vector2 Sub(Vector2 v) {return new Vector2(x - v.x, y - v.y);}
	public Vector2 Sub(float x, float y) {return new Vector2(this.x - x, this.y - y);}
	
	public Vector2 Mul(float v) {return new Vector2(x * v, y * v);}
	public Vector2 Mul(Vector2 v) {return new Vector2(x * v.x, y * v.y);}
	public Vector2 Mul(float x, float y) {return new Vector2(this.x * x, this.y * y);}
	
	public Vector2 Div(float v) {return new Vector2(x / v, y / v);}
	public Vector2 Div(Vector2 v) {return new Vector2(x / v.x, y / v.y);}
	public Vector2 Div(float x, float y) {return new Vector2(this.x / x, this.y / y);}
	
	public float Dot(Vector2 v) {return x * v.x + y * v.y;}
	public float Length() {return (float) Math.sqrt(x * x + y * y);}
	public float SqrLength() {return x * x + y * y;}
	public float Min() {return Math.min(x, y);}
	public float Max() {return Math.max(x, y);}
	public Vector2 Floor() {return new Vector2((float) Math.floor(x), (float) Math.floor(y));}
	public Vector2 Ceil() {return new Vector2((float) Math.ceil(x), (float) Math.ceil(y));}
	public Vector2 Normalized() {Vector2 v = new Vector2(this); v.Normalize(); return v;}
	
	public void Normalize(Vector2 v) {float normal = (float) (1.0 / Math.sqrt(v.x * v.x + v.y * v.y)); x = v.x * normal;  y = v.y * normal;}
	public void Normalize() {float normal = (float) (1.0 / Math.sqrt(x * x + y * y)); x *= normal; y *= normal;}
	
	//Project this vector onto a given line segment
	public Vector2 Project(Vector2 lineStart, Vector2 lineEnd)
	{
		Vector2 dir = Direction(lineStart, lineEnd);
		float dot = Mathf.Clamp(Dot(this.Sub(lineStart), dir), 0, Distance(lineStart, lineEnd));
		return lineStart.Add(dir.Mul(dot));
	}
	
	//Project this vector onto a given list of line segments (strip of vertices)
	public Vector2 Project(List<Vector2> vertices)
	{
		Vector2 closestPoint = Project(vertices.get(0), vertices.get(1));
		float closestDistance = Distance(closestPoint, this);
		for(int i = 1; i < vertices.size(); i++)
		{
			int j = (i + 1) % vertices.size();
			
			Vector2 temp = Project(vertices.get(i), vertices.get(j));
			float tempDistance = Distance(temp, this);
			if(tempDistance < closestDistance)
			{
				closestPoint.Set(temp);
				closestDistance = tempDistance;
			}
		}
		return closestPoint;
	}
	
	public static final Vector2 up = new Vector2(0, 1);
	public static final Vector2 down = new Vector2(0, -1);
	public static final Vector2 left = new Vector2(-1, 0);
	public static final Vector2 right = new Vector2(1, 0);
	
	public static float Dot(Vector2 v1, Vector2 v2) {return v1.x * v2.x + v1.y * v2.y;}
	public static float Distance(Vector2 v1, Vector2 v2) {return (v2.Sub(v1)).Length();}
	public static float Angle(Vector2 from, Vector2 to) {return (float) (Math.acos(Mathf.Clamp(Vector2.Dot(from.Normalized(), to.Normalized()), -1f, 1f)) * 57.29578f);}
	public static Vector2 Lerp(Vector2 v1, Vector2 v2, float time) {return new Vector2(v1.x + (v2.x - v1.x) * time, v1.y + (v2.y - v1.y) * time);}
	public static Vector2 Reflect(Vector2 dir, Vector2 normal) {return normal.Mul(-2f * Dot(normal, dir)).Add(dir);}
	public static Vector2 Direction(Vector2 start, Vector2 end) {return end.Sub(start).Normalized();}
	
	public static Vector2 Intersection(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2)
	{
		float denom = (end2.y - start2.y) * (end1.x - start1.x) - (end2.x - start2.x) * (end1.y - start1.y);
		if(denom == 0) {return null;}
		
		float a = ((end2.x - start2.x) * (start1.y - start2.y) - (end2.y - start2.y) * (start1.x - start2.x)) / denom;
		float b = ((end1.x - start1.x) * (start1.y - start2.y) - (end1.y - start1.y) * (start1.x - start2.x)) / denom;
		if(a >= 0 && a <= 1 && b >= 0 && b <= 1)
		{
			return new Vector2(start1.x + a * (end1.x - start1.x), start1.y + a * (end1.y - start1.y));
		}
		return null;
	}
	
	public static List<Vector2> GetPointsAround(Vector2 center, float radius, int numPoints)
	{
		List<Vector2> temp = new ArrayList<Vector2>();
		for(int o = 0; o < numPoints; o++)
		{
			float angle = (float) Math.toRadians(((float)o / 16f) * 360f);
			temp.add(new Vector2(center.x + (float)(radius * Math.cos(angle)), center.y + (float)(radius * Math.sin(angle))));
		}
		return temp;
	}
	
	public Vector2 Neg() {return new Vector2(-x, -y);}
	public boolean Equals(Vector2 v) {return v.x == x && v.y == y;}
	
	public String ToShortString() {return x + "," + y;}
	public String ToString() {return "(" + x + ", " + y + ")";}
	
	@Override
	public boolean equals(Object o)
	{ 
		if (!(o instanceof Vector2)) return false;
		
		Vector2 v = (Vector2) o;
		return v.x == x && v.y == y;
	}
	
	@Override
	public Vector2 clone() throws CloneNotSupportedException
	{
		return (Vector2) super.clone();
	}
}
