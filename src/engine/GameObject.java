package engine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import editor.Editor;
import editor.EditorUtil;
import math.Mathf;
import math.Matrix4x4;
import math.Vector2;
import physics.Collider;

public class GameObject extends engine.Object
{
	public boolean enabled = true;
	public String tag = "Untagged";
	public float depth = 0;
	
	private Vector2 position = new Vector2(0, 0);
	private Vector2 scale = new Vector2(1, 1);
	private float rotation = 0;
	
	private Vector2 localPosition = new Vector2();
	private Vector2 localScale = new Vector2();
	private float localRotation = 0;
	
	private GameObject parent;
	private List<GameObject> children = new ArrayList<GameObject>();
	private List<LogicBehaviour> components = new ArrayList<LogicBehaviour>();
	private int inline = 0;
	private Matrix4x4 matrix = new Matrix4x4();
	private Collider collider;
	private SpriteRenderer renderer;
	
	private int layer = 0;
	private byte dirty = 1;
	private boolean expanded = false;
	private int i;
	
	private static GameObject master = new GameObject(false);
	private static List<GameObject> instances = new ArrayList<GameObject>();
	private static int h;
	private static int j;
	private static Matrix4x4 temp = new Matrix4x4();
	private static int total = 0;
	
	public GameObject(boolean addToHierarchy)
	{
		if(!addToHierarchy)
		{
			inline = -1;
			expanded = true;
			return;
		}
		else total += 1;
		Name("New GameObject");
		instances.add(this);
		Parent(master);
	}
	
	public GameObject(String name)
	{
		Name(name);
		instances.add(this);
		Parent(master);
		total += 1;
	}
	
	public final int GetLayer() {return layer;}
	public void SetLayer(int l) {layer = (int)Mathf.Clamp((float)l, 0, Renderer.LayerCount() - 1);}
	
	public final boolean isDirty() {return dirty == 1;}
	public void ResetDirty() {dirty = 0;}
	
	//Get rid of update matrix because Matrix() already updates it if it's dirty when retrieved
	public final Matrix4x4 Matrix() {if(matrix.isDirty()) matrix.SetTransformation(position, rotation, scale); matrix.setDirty(false); return matrix;}
	
	public final Vector2 Position() {return position;}
	public void Position(Vector2 v) {Position(v.x, v.y);}
	public void Position(float x, float y){position.Set(x, y); RecalculateLocalTransformation();}
	public final Vector2 LocalPosition() {return localPosition;}
	public void LocalPosition(Vector2 v) {LocalPosition(v.x, v.y);}
	public void LocalPosition(float x, float y) {localPosition.Set(x, y); RecalculateGlobalTransformations();}
	
	public void Move(Vector2 v) {Position(position.Add(v));} 
	
	public final Vector2 Scale() {return scale;}
	public void Scale(Vector2 v) {Scale(v.x, v.y);}
	public void Scale(float x, float y) {scale.Set(x, y); RecalculateLocalTransformation();}
	public final Vector2 LocalScale() {return localScale;}
	public void LocalScale(Vector2 v) {LocalScale(v.x, v.y);}
	public void LocalScale(float x, float y) {localScale.Set(x, y); RecalculateGlobalTransformations();}
	
	public final float Rotation() {return rotation;}
	public void Rotation(float v) {rotation = Mathf.Wrap(v, 0, 360); RecalculateLocalTransformation();}
	public final float LocalRotation() {return localRotation;}
	public void LocalRotation(float v) {localRotation = Mathf.Wrap(v, 0, 360); RecalculateGlobalTransformations();}
	
	private void RecalculateLocalTransformation()
	{
		dirty = 1;
		matrix.setDirty(true);
		localScale = scale.Div(parent.scale);
		localRotation = Mathf.Wrap(rotation - parent.rotation, 0, 360);
		temp.SetTransformation(null, -parent.rotation, new Vector2(1, 1).Div(parent.scale));
		localPosition.Set(temp.TransformPoint(position.Sub(parent.position)));
		
		for(int i = 0; i < children.size(); i++) children.get(i).RecalculateGlobalTransformations();
	}
	private void RecalculateGlobalTransformations()
	{
		dirty = 1;
		matrix.setDirty(true);
		scale = parent.scale.Mul(localScale);
		rotation = Mathf.Wrap(parent.rotation + localRotation, 0, 360);
		position.Set(parent.Matrix().TransformPoint(localPosition));
		
		for(int i = 0; i < children.size(); i++)
		{
			children.get(i).RecalculateGlobalTransformations();
		}
	}
	
	public static void Recalculate() {for(int i = 0; i < master.children.size(); i++) master.children.get(i).RecalculateGlobalTransformations();}
	
	public static GameObject Master() {return master;}
	
	public GameObject Parent() {return parent;}
	public void Parent(GameObject g)
	{
		if(g == null) g = master;
		if(parent != null)
		{
			if(parent != g) parent.RemoveChild(this);
			else return;
		}
		
		parent = g;
		parent.children.add(this);
		SetInline(parent.inline + 1);
		RecalculateLocalTransformation();
	}
	
	public final List<LogicBehaviour> GetComponents() {return components;}
	public LogicBehaviour GetComponent(String s)
	{
		for(j = 0; j < components.size(); j++)
		{
			LogicBehaviour b = components.get(j);
			if(b.Name().equals(s)) return b;
		}
		return null;
	}
	
	public void RemoveComponent(LogicBehaviour b)
	{
		for(i = 0; i < components.size(); i++)
		{
			if(components.get(i) == b)
			{
				if(b == renderer) renderer = null;
				else if(b == collider) collider = null;
				components.remove(i);
				return;
			}
		}
	}
	public void RemoveComponent(String s)
	{
		for(i = 0; i < components.size(); i++)
		{
			LogicBehaviour b = components.get(i);
			if(b.Name().equals(s))
			{
				if(b == renderer) renderer = null;
				else if(b == collider) collider = null;
				components.remove(b);
				return;
			}
		}
	}
	public void RemoveComponent(int i)
	{
		LogicBehaviour b = components.get(i);
		
		if(b == renderer) renderer = null;
		else if(b == collider) collider = null;
		components.remove(b);
	}
	
	public final SpriteRenderer GetRenderer() {return renderer;}
	public final Collider GetCollider() {return collider;}
	
	static void Clear()
	{
		instances.clear();
		master.children.clear();
	}
	
	public LogicBehaviour AddComponent(LogicBehaviour b)
	{
		if(b == null)
		{
			Debug.Log("Could not find behaviour!");
			return null;
		}
		else if(b instanceof Collider)
		{
			if(collider != null)
			{
				Debug.Log("Only one collider per object is allowed!");
				return null;
			}
			collider = (Collider) b;
		}
		else if(b instanceof SpriteRenderer)
		{
			if(renderer != null)
			{
				Debug.Log("Only one renderer per object is allowed!");
				return null;
			}
			renderer = (SpriteRenderer) b;
		}
		b.Init(this);
		components.add(b);
		return b;
	}
	
	public LogicBehaviour AddComponent(String v)
	{
		if(v.equals("SpriteRenderer") && renderer != null)
		{
			Debug.Log("Only one renderer per object is allowed!");
			return null;
		}
		else if(v.equals("BoxCollider"))
		{
			if(collider != null)
			{
				Debug.Log("Only one collider per object is allowed!");
				return null;
			}
		}
		
		Class<?> cls;
		
		try
		{
			try{cls = Class.forName(v);}
			catch(NoClassDefFoundError e) {return null;}
			try
			{
				LogicBehaviour b =  AddComponent((LogicBehaviour)cls.getConstructor().newInstance());
				if(b instanceof Collider) collider = (Collider) b;
				else if(b instanceof SpriteRenderer) renderer = (SpriteRenderer) b;
				return b;
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{Debug.Log("Instance of " + v + " could not be instantiated!");}
			
		}
		catch (ClassNotFoundException e)
		{
			LogicBehaviour l = EditorUtil.GetBehaviour(v);
			if(l != null) return AddComponent(l);
			Debug.Log("Class " + v + " could not be found!");
		}
		return null;
	}
	
	public void RefreshComponent(LogicBehaviour b)
	{
		for(int c = 0; c < components.size(); c++)
		{
			LogicBehaviour temp = components.get(c);
			
			if(temp.Name() != b.Name()) continue;
			
			try
			{
				components.set(c, (LogicBehaviour) b.getClass().getConstructor().newInstance());
				
				LogicBehaviour behaviour = components.get(c);
				
				Field[] newFields = behaviour.getClass().getFields();
				Field[] oldFields = temp.getClass().getFields();
				
				for(int f1 = 0; f1 < oldFields.length; f1++)
				{
					Field oF = oldFields[f1];
					
					for(int f2 = 0; f2 < newFields.length; f2++)
					{
						Field nF = newFields[f2];
						if(nF.getName().equals(oF.getName()))
						{
							nF.set(behaviour, oF.get(temp));
						}
					}
				}
				
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {e.printStackTrace();}
		}
	}
	
	public void Expand(boolean b) {expanded = b;}
	public boolean Expanded() {return expanded;}
	
	public  int Inline() {return inline;}
	public void SetInline(int v)
	{
		inline = v;
		for(i = 0; i < children.size(); i++) children.get(i).SetInline(v + 1);
	}
	
	public static final List<GameObject> Instances() {return instances;}
	
	public static GameObject FindObjectByID(String s)
	{
		for(j = 0; j < instances.size(); j++)
		{
			GameObject g = instances.get(j);
			if(g.instanceID().equals(s)) return g;
		}
		return null;
	}
	
	public static GameObject Find(String s)
	{
		for(j = 0; j < instances.size(); j++)
		{
			GameObject g = instances.get(j);
			if(g.Name().equals(s)) return g;
		}
		return null;
	}
	
	public List<GameObject> Children() {return children;}
	public void RemoveChild(GameObject g)
	{
		for(i = 0; i < children.size(); i++)
		{
			if(children.get(i) == g)
			{
				RemoveChild(i);
				return;
			}
		}
	}
	
	public void RemoveChild(int v)
	{
		if(v >= children.size()) return;
		GameObject g = children.get(v);
		children.remove(v);
		g.inline = 0;
	}
	
	public void Destroy()
	{
		RemoveChildrenInstances();
		Parent(null);
		master.children.remove(this);
		total -= 1;
	}
	
	private void RemoveChildrenInstances()
	{
		for(i = 0; i < children.size(); i++) children.get(i).RemoveChildrenInstances();
		instances.remove(this);
	}
	
	public void Start()
	{
		for(int i = 0; i < components.size(); i++)
		{
			LogicBehaviour component = components.get(i);
			component.Start();
		}
	}
	
	public void Update()
	{
		for(int i = 0; i < components.size(); i++)
		{
			LogicBehaviour component = components.get(i);
			component.Update();
		}
	}
	
	public void CallTriggerCallback()
	{
		for(int i = 0; i < components.size(); i++)
		{
			LogicBehaviour component = components.get(i);
			component.OnTrigger();
		}
	}
	
	public void CallCollisionCallback()
	{
		for(int i = 0; i < components.size(); i++)
		{
			LogicBehaviour component = components.get(i);
			component.OnCollision();
		}
	}
	
	public void PrepareToRender()
	{
		for(i = 0; i < components.size(); i++)
		{
			LogicBehaviour component = components.get(i);
			if(component.Name().equals("SpriteRenderer"))
			{
				SpriteRenderer sr = (SpriteRenderer) component;
				if(sr.sprite != null) Renderer.AddToRenderer(sr);
			}
			else if(component instanceof Collider) Collider.AddFrameCollider((Collider) component);
		}
	}
	
	public static final int Total() {return total;}
	
	public static void StartAll()
	{
		for(h = 0; h < instances.size(); h++)
		{
			GameObject g = instances.get(h);
			g.Start();
		}
		if(!ProjectSettings.isEditor) prevFrame = false; 
	}
	
	private static boolean prevFrame = false;
	public static void PrepareObjects()
	{
		if(Editor.IsPlaying() || !ProjectSettings.isEditor)
		{
			if(prevFrame == false)
			{
				Collider.ClearFrame();
			}
			for(h = 0; h < instances.size(); h++)
			{
				GameObject g = instances.get(h);
				g.Update();
			}
		}
		
		for(h = 0; h < instances.size(); h++)
		{
			GameObject g = instances.get(h);
			g.PrepareToRender();
		}
		if(ProjectSettings.isEditor) prevFrame = Editor.IsPlaying();
		else prevFrame = true;
		
		if(Editor.IsPlaying() || !ProjectSettings.isEditor)
		{
			Collider.CompareCollisions();
			Collider.ClearFrame();
		}
	}
}
