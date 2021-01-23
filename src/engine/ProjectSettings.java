package engine;

import java.io.BufferedReader;

import math.Vector2;

public class ProjectSettings
{
	static boolean isEditor = true;
	static String gameName = "Logic Game Test";
	static Vector2 previousAppSize = new Vector2();
	static boolean fullScreen = false;
	
	static void Load(BufferedReader br, boolean editor)
	{
		isEditor = editor;
	}
	
	static void Save()
	{
		
	}
}
