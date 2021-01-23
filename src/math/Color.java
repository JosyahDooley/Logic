package math;

public class Color
{
	public float r = 1;
	public float g = 1;
	public float b = 1;
	public float a = 1;
	
	public static final Color black = new Color(0, 0, 0);
	public static final Color white = new Color(1, 1, 1);
	public static final Color red = new Color(1, 0, 0);
	public static final Color green = new Color(0, 1, 0);
	public static final Color grey = new Color(0.5f, 0.5f, 0.5f);
	public static final Color wine = new Color(0.5f, 0, 0);
	public static final Color forrest = new Color(0, 0.5f, 0);
	public static final Color marine = new Color(0, 0, 0.5f);
	public static final Color yellow = new Color(1, 1, 0);
	public static final Color cyan = new Color(0, 1, 1);
	public static final Color magenta = new Color(1, 0, 1);
	
	//Color constructor with alpha
	public Color(float r, float g, float b, float a)
	{
		this.r = Mathf.Clamp(r, 0, 1);
		this.g = Mathf.Clamp(g, 0, 1);
		this.b = Mathf.Clamp(b, 0, 1);
		this.a = Mathf.Clamp(a, 0, 1);
	}
	
	//Color constructor without alpha
	public Color(float r, float g, float b)
	{
		this.r = Mathf.Clamp(r, 0, 1);
		this.g = Mathf.Clamp(g, 0, 1);
		this.b = Mathf.Clamp(b, 0, 1);
	}
	
	public boolean Compare(Color c)
	{
		return c.r == r && c.g == g && c.b == b && c.a == a;
	}
	
	//Returns a string that contains the color values
	public String ToShortString() {return r + "," + g + "," + b + "," + a;}
	public String ToString() {return "(" + r + ", " + g + ", " + b + ", " + a + ")";}
}
