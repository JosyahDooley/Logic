package engine;

public abstract class LogicBehaviour extends Object
{
	public boolean enabled = true;
	
	protected GameObject gameObject;
	
	public LogicBehaviour() {Name(getClass().getSimpleName());}
	public void Init(GameObject g) {if(gameObject == null) gameObject = g;}
	
	public void Start() {}
	public void Update() {}
	public void OnWillRender() {}
	public void OnGUI() {}
	
	public void OnTrigger() {System.out.println(Name());}
	public void OnCollision() {}
	
	public void print(String s) {System.out.println(s);}
	public final GameObject gameObject() {return gameObject;}
}
