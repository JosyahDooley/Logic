package game;

import engine.LogicBehaviour;
import engine.SpriteRenderer;
import engine.Time;
import gui.Sprite;
import input.Input;
import math.Vector2;

public class TopDownMovement extends LogicBehaviour
{
	public float moveSpeed = 10;
	
	public Sprite[] walkDown = new Sprite[3];
	public Sprite[] walkLeft = new Sprite[3];
	public Sprite[] walkRight = new Sprite[3];
	public Sprite[] walkUp = new Sprite[3];
	private Sprite[] current;
	private float walkTime = 0;
	SpriteRenderer sr;
	
	public void Start()
	{
		sr = gameObject.GetRenderer();
		current = walkDown;
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
			if(current != null) sr.sprite = current[0];
		}
		
		if(walkTime > 0.0f && current != null)
		{
			if(current.length > 2)
			{
				int whole = (int) walkTime;
				if(walkTime - (float) whole <= 0.25f) sr.sprite = current[1];
				else if(walkTime - (float) whole <= 0.50f) sr.sprite = current[0];
				else if(walkTime - (float) whole <= 0.75f) sr.sprite = current[2];
				else sr.sprite = current[0];
			}
		}
	}
}
