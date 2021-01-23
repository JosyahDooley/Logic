package editor;

import java.util.ArrayList;
import java.util.List;

import engine.Debug;
import engine.Dialog;
import engine.GameObject;
import engine.LogicBehaviour;
import engine.Material;
import engine.Shader;
import engine.Texture;
import gui.Font;
import gui.GUI;
import gui.GUISkin;
import gui.GUIStyle;
import gui.Sprite;
import math.Rect;
import math.Vector2;
import physics.Collider;
import sound.AudioClip;

public class Inspector extends engine.Object
{
	private List<BehaviourAttributes> a = new ArrayList<BehaviourAttributes>();
	private List<LogicBehaviour> l = new ArrayList<LogicBehaviour>();
	private int i = 0;
	private int offsetY = 0;
	private int scroll = 0;
	private GUIStyle window;
	
	public Inspector()
	{
		window = Editor.skin.Get("Window");
	}
	
	public void Render(Rect r)
	{
		engine.Object inspected = (engine.Object) Editor.GetInspected();
		if(inspected == null) return;
		
		if(inspected instanceof GameObject)
		{
			int addition = 170;
			for(i = 0; i < a.size(); i++) addition += a.get(i).height + (window.padding.y + window.padding.height) + 2;
			scroll = GUI.SetScrollView(addition, scroll);
			
			GameObject selected = (GameObject)inspected;
			
			//Name field
			String ng = GUI.TextField(new Rect(0, 0, r.width, 22), "GameObject", selected.Name(), 100);
			if(!ng.equals(selected.Name())) selected.Name(ng);
			
			//Position field
			Vector2 pg = GUI.VectorField(new Rect(0, 24, r.width, 22), "Position", selected.LocalPosition(), 100);
			if(!pg.equals(selected.LocalPosition())) selected.LocalPosition(pg);
			
			//Scale field
			Vector2 sg = GUI.VectorField(new Rect(0, 48, r.width, 22), "Scale", selected.LocalScale(), 100);
			if(!sg.equals(selected.LocalScale())) selected.LocalScale(sg);
			
			//Rotation field
			float rg = GUI.FloatField(new Rect(0, 72, r.width, 22), "Rotation", selected.LocalRotation(), 100);
			if(rg != selected.LocalRotation()) selected.LocalRotation(rg);
			
			//Depth field
			float dg =  GUI.FloatField(new Rect(0, 96, r.width, 22), "Depth", selected.depth, 100);
			if(dg != selected.depth)
			{
				try
				{
					UndoRedo.RegisterUndo(selected, selected.getClass().getDeclaredField("depth"));
					selected.depth = dg;
				}
				catch (NoSuchFieldException | SecurityException e) {e.printStackTrace();}
			}
			
			//Layer field
			int lg = (int) GUI.FloatField(new Rect(0, 120, r.width, 22), "Layer", selected.GetLayer(), 100);
			if(lg != selected.GetLayer()) selected.SetLayer(lg);
		
			offsetY = 144;
			for(i = 0; i < a.size(); i++)
			{
				BehaviourAttributes att = a.get(i);
			
				float h = att.height + (window.padding.y + window.padding.height);
				GUI.Window(new Rect(0, offsetY, r.width, h), att.c.getSimpleName() + ".Java", this::DrawVariables, window);
				if(GUI.Button("", new Rect(r.width - 23, offsetY + 7, 16, 16), Editor.xClose, Editor.xClose))
				{
					selected.RemoveComponent(i); 
					SetAttributes(selected, false);
				}
				offsetY += h + 2;
			}
			
			if(GUI.Button("+ Add Component +", new Rect(0, offsetY, r.width, 26), "Button", "ButtonHover"))
			{
				String output = Dialog.InputDialog("Add Component", "");
				if(output == null) return;
				
				LogicBehaviour l = selected.AddComponent(output);
				if(l != null)
				{
					if(l instanceof Collider) ((Collider) l).Init();
					SetAttributes(selected, true);
				}
			}
			offsetY = 0;
		}
		else
		{
			i = 0;
			
			GUI.Window(new Rect(0, offsetY, r.width, a.get(0).height + (window.padding.y + window.padding.height)), a.get(0).c.getSimpleName(), this::DrawVariables, window);
			
			//This is temporary until we get annotations done
			//If this is an asset we can save
			if(inspected instanceof Sprite || inspected instanceof Material || inspected instanceof GUISkin)
			{
				if(GUI.CenteredButton("Save", new Rect(0, offsetY + (a.get(0).height + (window.padding.y + window.padding.height)) + 2,  r.width, 26), "Button", "ButtonHover"))
				{
					inspected.Save();
				}
			}
		}
	}
	
	public void DrawVariables(Rect r)
	{
		List<BehaviourField> fields = a.get(i).fields;
		int padding = 0;
		int offset = 0;
		for(int f = 0; f < fields.size(); f++)
		{
			BehaviourField bf = fields.get(f);
			
			if(Rect.class.isAssignableFrom(bf.field.getType()))
			{
				DrawVariable(new Rect(bf.offset, (f * 22 + padding) + offset, r.width - bf.offset, 46), bf, padding, 0);
				padding += 24;
			}
			else if(bf.field.getType().isArray())
			{
				try
				{
					Class<?> c = bf.field.get(bf.object).getClass();
					Class<?> type = c.getComponentType();
					if(c.getComponentType().isArray()) return;
					
					if(type == int.class)
					{
						int p = ((int[]) (bf.field.get(bf.object))).length;
						String v = GUI.TextField(new Rect(bf.offset, (f * 22 + padding), r.width - bf.offset, 22), bf.field.getName(), String.valueOf(p), 100);
						if(!String.valueOf(p).equals(v))
						{
							int[] res = new int[Integer.parseInt(v)];
							int[] src = ((int[]) (bf.field.get(bf.object)));
							for(int i = 0; i < res.length; i++)
							{
								if(i > src.length - 1) break;
								res[i] = src[i];
							}
							bf.field.set(bf.object, res);
						}
						offset += 22;
						padding += 2;
						
						int[] arr = (int[]) bf.field.get(bf.object);
						for(int i = 0; i < arr.length; i++)
						{
							DrawVariable(new Rect(bf.offset, (f * 22 + padding) + offset, r.width - bf.offset, 22), bf, padding, i);
							if(i != arr.length - 1)
							{
								offset += 22;
								padding += 2;
							}
						}
					}
					else if(type == float.class)
					{
						int p = ((float[]) (bf.field.get(bf.object))).length;
						String v = GUI.TextField(new Rect(bf.offset, (f * 22 + padding) + offset, r.width - bf.offset, 22), bf.field.getName(), String.valueOf(p), 100);
						if(!String.valueOf(p).equals(v))
						{
							float[] res = new float[Integer.parseInt(v)];
							float[] src = ((float[]) (bf.field.get(bf.object)));
							for(int i = 0; i < res.length; i++)
							{
								if(i > src.length - 1) break;
								res[i] = src[i];
							}
							bf.field.set(bf.object, res);
						}
						offset += 22;
						padding += 2;
						
						float[] arr = (float[]) bf.field.get(bf.object);
						for(int i = 0; i < arr.length; i++)
						{
							DrawVariable(new Rect(bf.offset, (f * 22 + padding) + offset, r.width - bf.offset, 22), bf, padding, i);
							if(i != arr.length - 1)
							{
								offset += 22;
								padding += 2;
							}
						}
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
			}
			else DrawVariable(new Rect(bf.offset, (f * 22 + padding) + offset, r.width - bf.offset, 22), bf, padding, 0);
			padding += 2;
		}
	}
	
	public void DrawVariable(Rect r, BehaviourField bf, int padding, int index)
	{
		Class<?> type = bf.field.getType();
		String name = type.getSimpleName();
		int isArray = 0;
		if(name.contains("[]"))
		{
			isArray = 1;
			name = name.replace("[", "").replace("]", "");
		}
		
		if(name.equals("String"))
		{
			try
			{
				if(isArray == 0)
				{
					String p = bf.field.get(bf.object).toString();
					String v = GUI.TextField(r, bf.field.getName(), p, 100);
					if(!p.equals(v))
					{
						UndoRedo.RegisterUndo(bf.object, bf.field); bf.field.set(bf.object, v);
					}
				}
				else
				{
					String[] ret = ((String[]) (bf.field.get(bf.object)));
					String p = String.valueOf(ret[index]);
					String v = GUI.TextField(r, "", p, 100);
					if(!p.equals(v))
					{
						UndoRedo.RegisterUndo(bf.object, bf.field);
						ret[index] = v;
						bf.field.set(bf.object, ret);
					}
				}
			}
			catch(IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("boolean"))
		{
			try
			{
				if(isArray == 0)
				{
					boolean p = (boolean) bf.field.get(bf.object);
					boolean v = GUI.Toggle(p, r, bf.field.getName(), Editor.toggleOn, Editor.toggleOff);
					
					if(p != v)
					{
						UndoRedo.RegisterUndo(bf.object, bf.field);
						bf.field.set(bf.object, v);
					}
				}
				else
				{
					Boolean[] ret = ((Boolean[]) (bf.field.get(bf.object)));
					boolean p = ret[index];
					boolean v = GUI.Toggle(p, r, "", Editor.toggleOn, Editor.toggleOff);
					if(p != v)
					{
						UndoRedo.RegisterUndo(bf.object, bf.field);
						ret[index] = v;
						bf.field.set(bf.object, ret);
					}
				}
			}
			catch(IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("float"))
		{
			try
			{
				if(isArray == 0)
				{
					String p = bf.field.get(bf.object).toString();
					String v = GUI.TextField(r, bf.field.getName(), p, 100);
					if(!p.equals(v))
					{
						try{UndoRedo.RegisterUndo(bf.object, bf.field); bf.field.set(bf.object, Float.parseFloat(v));}
						catch(NumberFormatException e) {Debug.Log("Input is not in a number format! Rejecting value.");}
					}
				}
				else
				{
					float[] ret = ((float[]) (bf.field.get(bf.object)));
					String p = String.valueOf(ret[index]);
					String v = GUI.TextField(r, "", p, 100);
					if(!p.equals(v))
					{
						try{UndoRedo.RegisterUndo(bf.object, bf.field); ret[index] = Float.parseFloat(v); bf.field.set(bf.object, ret);}
						catch(NumberFormatException e) {Debug.Log("Input is not in a number format! Rejecting value.");}
					}
				}
			}
			catch(IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("int"))
		{
			try
			{
				if(isArray == 0)
				{
					String p = bf.field.get(bf.object).toString();
					String v = GUI.TextField(r, bf.field.getName(), p, 100);
					if(!p.equals(v))
					{
						try{UndoRedo.RegisterUndo(bf.object, bf.field); bf.field.set(bf.object, Integer.parseInt(v));}
						catch(NumberFormatException e) {Debug.Log("Input is not in a number format! Rejecting value.");}
					}
				}
				else
				{
					int[] ret = ((int[]) (bf.field.get(bf.object)));
					String p = String.valueOf(ret[index]);
					String v = GUI.TextField(r, "", p, 100);
					if(!p.equals(v))
					{
						try{UndoRedo.RegisterUndo(bf.object, bf.field); ret[index] = Integer.parseInt(v); bf.field.set(bf.object, ret);}
						catch(NumberFormatException e) {Debug.Log("Input is not in a number format! Rejecting value.");}
					}
				}
			}
			catch(IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		} //Left off of arrays right here, will get to later
		else if(name.equals("Vector2"))
		{
			try
			{
				Vector2 p = (Vector2) bf.field.get(bf.object);
				Vector2 v = GUI.VectorField(r, bf.field.getName(), p, 100);
				if(!p.equals(v))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, v);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Sprite"))
		{
			try
			{
				Sprite sprite = (Sprite)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), sprite, Sprite.class, 100);
				if(!o.equals(sprite))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("GameObject"))
		{
			try
			{
				GameObject go = (GameObject)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), go, GameObject.class, 100);
				if(!go.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Texture"))
		{
			try
			{
				Texture tex = (Texture)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), tex, Texture.class, 100);
				if(!tex.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("GUISkin"))
		{
			try
			{
				GUISkin skin = (GUISkin)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), skin, GUISkin.class, 100);
				if(!skin.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Font"))
		{
			try
			{
				Font font = (Font)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), font, Font.class, 100);
				if(!font.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Material"))
		{
			try
			{
				Material mat = (Material)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), mat, Material.class, 100);
				if(!mat.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Shader"))
		{
			try
			{
				Shader shader = (Shader)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), shader, Shader.class, 100);
				if(shader == null) return;
				if(!shader.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("AudioClip"))
		{
			try
			{
				AudioClip clip = (AudioClip)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), clip, AudioClip.class, 100);
				if(!clip.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(engine.CustomClass.class.isAssignableFrom(bf.field.getType()))
		{
			try
			{
				boolean p = ((engine.CustomClass)bf.field.get(bf.object)).expanded;
				boolean v = GUI.Toggle(p, r, bf.field.getName(), Editor.arrowDown, Editor.arrowRight);
				if(p != v)
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					((engine.CustomClass)bf.field.get(bf.object)).expanded = v;
					SetAttributes((engine.Object) Editor.GetInspected(), false);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
			GUI.Label(bf.field.getName(), new Vector2(r.x, r.y));
		}
		else if(engine.LogicBehaviour.class.isAssignableFrom(bf.field.getType()))
		{
			try
			{
				LogicBehaviour behaviour = (LogicBehaviour)bf.field.get(bf.object);
				engine.Object o = GUI.ObjectField(r, bf.field.getName(), behaviour, LogicBehaviour.class, 100);
				if(!behaviour.equals(o))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, o);
				}
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else if(name.equals("Rect"))
		{
			try
			{
				Rect fieldRect = (Rect) bf.field.get(bf.object);
				Rect v = GUI.VectorField(r, bf.field.getName(), fieldRect, 100);
				if(!fieldRect.equals(v))
				{
					UndoRedo.RegisterUndo(bf.object, bf.field);
					bf.field.set(bf.object, v);
				}
				//padding += 22;
			}
			catch(IllegalArgumentException e) {e.printStackTrace();}
			catch(IllegalAccessException e) {e.printStackTrace();}
		}
		else
		{
			GUI.TextField(r, bf.field.getName(), "", 100);
		}
		
		padding += 2;
	}
	
	public void SetAttributes(engine.Object o, boolean resetScroll)
	{
		if(resetScroll) scroll = 0;
		a.clear();
		
		if(o instanceof GameObject)
		{
			l = ((GameObject)o).GetComponents();
			for(i = 0; i < l.size(); i++)
			{
				BehaviourAttributes b = new BehaviourAttributes(o);
				if(b != null) a.add(new BehaviourAttributes(l.get(i)));
			}
			return;
		}
		BehaviourAttributes b = new BehaviourAttributes(o);
		if(b != null) a.add(b);
	}
}
