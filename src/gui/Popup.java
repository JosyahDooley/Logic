package gui;

import java.util.List;
import java.util.function.Consumer;

import input.Mouse;
import math.Color;
import math.Rect;
import math.Vector2;

public class Popup
{
	private Rect nameArea;
	private List<String> list;
	private Consumer<String> func;
	private int i;
	private int g;
	private Rect listArea;
	private GUIStyle box;
	private Color prevColor;
	private int[] selected;
	
	public Popup(Rect nameArea, List<String> list, Consumer<String> func, int[] selected)
	{
		this.selected = selected;
		this.nameArea = nameArea;
		this.list = list;
		float widest = 0;
		for(i = 0; i < list.size(); i++)
		{
			float w = GUI.font.StringWidth(list.get(i));
			if(w > widest) widest = w;
		}
		listArea = new Rect(nameArea.x, nameArea.y + nameArea.height - 1, widest + 12, 28 * list.size() + 4);
		box = GUI.skin.Get("Box");
		this.func = func;
	}
	
	public final Rect BoxArea() {return listArea;}
	
	public Popup Draw()
	{
		Vector2 mousePos = Mouse.Position();
		if(!listArea.Contains(mousePos) && !nameArea.Contains(mousePos)) return null;
		Rect drawArea = GUI.Box(listArea, box);
		prevColor = GUI.textColor;
		GUI.textColor = Color.white;
		for(i = 0; i < list.size(); i++)
		{
			if(selected != null)
			{
				byte drewSelected = 0;
				for(g = 0; g < selected.length; g++)
				{
					if(selected[g] == i)
					{
						if(GUI.CenteredButton(list.get(i), new Rect(drawArea.x, drawArea.y + (28 * i), drawArea.width, 28), box, box))
						{
							func.accept(list.get(i));
							GUI.textColor = prevColor;
							return null;
						}
						drewSelected = 1;
						break;
					}
				}
				if(drewSelected == 0)
				{
					if(GUI.CenteredButton(list.get(i), new Rect(drawArea.x, drawArea.y + (28 * i), drawArea.width, 28), null, box))
					{
						func.accept(list.get(i));
						GUI.textColor = prevColor;
						return null;
					}
				}
			}
			else
			{
				if(GUI.CenteredButton(list.get(i), new Rect(drawArea.x, drawArea.y + (28 * i), drawArea.width, 28), null, box))
				{
					func.accept(list.get(i));
					GUI.textColor = prevColor;
					return null;
				}
			}
		}
		GUI.textColor = prevColor;
		return this;
	}
}
