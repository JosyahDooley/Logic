package editor;


import java.util.ArrayList;
import java.util.List;

import engine.GameObject;
import engine.SpriteRenderer;
import gui.GUI;
import gui.Sprite;
import input.Mouse;
import math.Rect;

public class Hierarchy extends engine.Object
{
	private Rect clickRect;
	private int i;
	private int scroll;
	private Rect drawRect;
	
	public Hierarchy()
	{
		
	}
	
	public void Render(Rect r)
	{
		drawRect = r;
		List<GameObject> loop = new ArrayList<GameObject>();
		int offset = 0;
		loop.add(GameObject.Master());
		while(loop.size() > 0)
		{
			GameObject g = loop.get(0);
			List<GameObject> children = g.Children();
			
			if(g.Expanded()) for(i = 0; i < children.size(); i++) loop.add(0, children.get(i));
			
			if(g == GameObject.Master())
			{
				loop.remove(loop.size() - 1);
				continue;
			}
			
			offset += 20;
			loop.remove(g);
		}
		scroll = GUI.SetScrollView(offset, scroll);
		//Scroll issue was bc hierarchy window was drawn to tall. Also changed multiplier from fontheight to 20 because that's the size of our selection box
		
		byte dropped = 0;
		List<GameObject> updateList = new ArrayList<GameObject>();
		int offsetY = 0;
		
		updateList.add(GameObject.Master());
		while(updateList.size() > 0)
		{
			GameObject g = updateList.get(0);
			List<GameObject> children = g.Children();
			
			if(children.size() > 0)
			{
				if(g.Expanded()) for(i = 0; i < children.size(); i++) updateList.add(0, children.get(i));
			}
			if(g == GameObject.Master())
			{
				updateList.remove(updateList.size() - 1);
				continue;
			}
			
			float inline = (g.Inline()) * 16;
			clickRect = new Rect(0, offsetY, r.width, 20);
			
			GameObject selected = Editor.GetSelected();
			if(selected != null)
			{
				if(g == selected)
				{
					GUI.Box(clickRect, "Box");
				}
			}
			
			if(children.size() > 0)
			{
				g.Expand(GUI.Toggle(g.Expanded(), inline, offsetY + 2, Editor.arrowDown, Editor.arrowRight));
				GUI.Label(g.Name(), inline + 15, offsetY);
			}
			else GUI.Label(g.Name(), inline, offsetY);
			
			clickRect.Set(0, (r.y + offsetY) - scroll, r.width, 20);
			
			if(clickRect.Contains(Mouse.Position()) && !GUI.HasPopup())
			{
				if(Mouse.GetButtonUp(0)) Editor.SetSelected(g);
				else if(Mouse.GetButtonDown(0)) Editor.SetDraggableObject(g);
				if(dropped == 0) dropped = CheckDrop(g);
				if(dropped == 1) return;
			}
			
			offsetY += 20;
			updateList.remove(g);
		}
		
		if(r.Contains(Mouse.Position()) && dropped == 0)
		{
			CheckDrop(null);
		}
	}
	
	private byte CheckDrop(GameObject parent)
	{
		if(Mouse.GetButtonUp(0) && drawRect.Contains(Mouse.Position()))
		{
			engine.Object dragged = Editor.DraggedObject();
			if(dragged != null)
			{
				if(dragged instanceof Sprite)
				{
					Sprite s = (Sprite) dragged;
					GameObject go = new GameObject(dragged.Name());
					SpriteRenderer sr = new SpriteRenderer();
					sr.sprite = s;
					go.AddComponent(sr);
					go.Parent(parent);
					Editor.SetSelected(go);
					return 1;
				}
				else if(dragged instanceof GameObject)
				{
					GameObject g = (GameObject) dragged;
					if(g == parent) return 1;
					g.Parent(parent);
					Editor.SetSelected(g);
					return 1;
				}
			}
		}
		return 0;
	}
}
