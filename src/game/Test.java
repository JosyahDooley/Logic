package game;

import engine.LogicBehaviour;
import engine.SpriteRenderer;
import engine.Time;
import gui.Sprite;
import input.Input;
import math.Rect;
import math.Vector2;

public class Test extends LogicBehaviour
{
	public float moveSpeed = 10;
	public String s = "Something";
	public int[] myArray = new int[5];
	public float[] myFloats = new float[2];
	public Rect something = new Rect();
	public Rect another = new Rect();
	public Rect andAgain = new Rect();
	public Vector2 vector = new Vector2();
	
	private Sprite[] walkDown = new Sprite[3];
	private Sprite[] walkLeft = new Sprite[3];
	private Sprite[] walkRight = new Sprite[3];
	private Sprite[] walkUp = new Sprite[3];
	private Sprite[] current;
	private float walkTime = 0;
	SpriteRenderer sr;
	
	public void Start()
	{
		sr = gameObject.GetRenderer();
		for(int i = 0; i < 3; i++)
		{
			walkDown[i] = Sprite.Get("Hero" + i);
			walkUp[i] = Sprite.Get("Hero" + (i + 3));
			walkRight[i] = Sprite.Get("Hero" + (i + 6));
			walkLeft[i] = Sprite.Get("Hero" + (i + 9));
			current = walkDown;
		}
	}
	
	public void Update()
	{
		if(Input.GetKey('w'))
		{
			gameObject.Move(new Vector2(0, moveSpeed).Mul(Time.DeltaTime()));
			current = walkUp;
			walkTime += Time.DeltaTime() * 2.0f;
		}
		else if(Input.GetKey('a'))
		{
			gameObject.Move(new Vector2(-moveSpeed, 0).Mul(Time.DeltaTime()));
			current = walkLeft;
			walkTime += Time.DeltaTime() * 2.0f;
		}
		else if(Input.GetKey('s'))
		{
			gameObject.Move(new Vector2(0, -moveSpeed).Mul(Time.DeltaTime()));
			current = walkDown;
			walkTime += Time.DeltaTime() * 2.0f;
		}
		else if(Input.GetKey('d'))
		{
			gameObject.Move(new Vector2(moveSpeed, 0).Mul(Time.DeltaTime()));
			current = walkRight;
			walkTime += Time.DeltaTime() * 2.0f;
		}
		else
		{
			walkTime = 0.0f;
			sr.sprite = current[0];
		}
		
		if(walkTime > 0.0f)
		{
			int whole = (int) walkTime;
			if(walkTime - (float) whole <= 0.25f) sr.sprite = current[1];
			else if(walkTime - (float) whole <= 0.50f) sr.sprite = current[0];
			else if(walkTime - (float) whole <= 0.75f) sr.sprite = current[2];
			else sr.sprite = current[0];
		}
	}
}
