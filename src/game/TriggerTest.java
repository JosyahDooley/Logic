package game;

import engine.Debug;
import engine.LogicBehaviour;

public class TriggerTest extends LogicBehaviour
{
	public void OnTrigger()
	{
		System.out.println("Called");
		if(gameObject.Name().equals("CollisionTest"))
		{
			Debug.Log("This trigger is being called");
		}
	}
}
