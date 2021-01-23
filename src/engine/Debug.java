package engine;

import java.util.ArrayList;
import java.util.List;

import gui.GUI;
import math.Rect;

public class Debug
{
	private static List<String> log = new ArrayList<String>();
	private static int i = 0;
	
	public static void Log(String message) {AddMessage(message);}
	
	public static void Clear() {log.clear();}
	
	private static void AddMessage(String message)
	{
		log.add(message);
		if(log.size() > 50) log.remove(0);
	}
	
	public static final String Log()
	{
		String ret = "";
		for(i = 0; i < log.size(); i++) ret += (log.get(i) + "\n");
		return ret;
	}
	
	public static final String LastEntry()
	{
		if(log.size() == 0) return "";
		return log.get(log.size() - 1);
	}
	
	public static void Draw()
	{
		if(GUI.Button(Debug.LastEntry(), new Rect(0, Application.Height() - 30, Application.Width(), 30), "Button", "ButtonHover"))
		{
			Dialog.MessageDialog("Debug Window", Log());
		}
	}
}
