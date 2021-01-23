package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import engine.Material;
import math.Rect;

public class Sprite extends engine.Object
{
	public Material material;
	public Rect offset;
	public Rect padding;
	
	//Static information used for all the sprites
	private static List<Sprite> sprites = new ArrayList<Sprite>();
	private static int i;
	
	//Import a sprite by name
	public Sprite(String name)
	{
		//Create a buffered reader variable
		BufferedReader br;
		
		try
		{
			//Set the buffered reader by passing in the file reader for the path of the sprite in the folder
			if(name.startsWith("/"))
			{
				InputStreamReader isr = new InputStreamReader(Sprite.class.getResourceAsStream("/Sprites" + name + ".Sprite"));
				br = new BufferedReader(isr);
				Name(name.replaceFirst("/", ""));
			}
			else
			{
				//new
				f = new File(name + ".Sprite");
				lastModified = f.lastModified();
				br = new BufferedReader(new FileReader(f));
				String[] split = name.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
				Name(split[split.length - 1]);
			}
			
			//Find the texture for this sprite
			material = Material.Get(br.readLine().split(" ")[1]);
			
			//Read in the information in the file
			String[] o = br.readLine().split(" ")[1].split(",");
			String[] p = br.readLine().split(" ")[1].split(",");
			
			//Set the offset and padding using the information we read in
			offset = new Rect(Float.parseFloat(o[0]), Float.parseFloat(o[1]), Float.parseFloat(o[2]), Float.parseFloat(o[3]));
			padding = new Rect(Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3]));
			
			sprites.add(this);
			
			//And close the buffered reader
			br.close();
		}
		catch(IOException e)  {e.printStackTrace();}
	}
	
	//Return the UV of the sprite
	public Rect UV()
	{
		//If the offset doesn't exist, return null
		if(offset == null) return null;
		
		//And cache the texture size and return the calculated uv
		float w = material.texture.Width();
		float h = material.texture.Height();
		return new Rect(offset.x / w, offset.y / h, offset.width / w, offset.height / h);
	}
	
	//Get a sprite by name
	public static Sprite Get(String name)
	{
		//For all the sprites
		for(i = 0; i < sprites.size(); i++)
		{
			//If we have came across the sprite were looking for, return it
			if(sprites.get(i).Name().equals(name)) return sprites.get(i);
		}
		
		//If we didn't find a sprite by that name, return null
		return null;
	}
	
	public void Save()
	{
		if(f == null) return;
		
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			
			if(material == null)  bw.write("Material: NULL\r\nOffset: 0,0,16,16\r\npadding: 0,0,0,0");
			else bw.write("Material: " + material.Name() + "\r\nOffset: " + offset.ToShortString() + "\r\npadding: " + padding.ToShortString());
			bw.close();
		}
		catch (IOException e) {e.printStackTrace();}
		
		Sprite original = Get(Name());
		if(original == null) return;
		
		try {original.Refresh();}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public static void RefreshAll()
	{
		for(i = 0; i < sprites.size(); i++)
		{
			try {sprites.get(i).Refresh();}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	private void Refresh() throws IOException
	{
		if(f == null) return;
		File temp = new File(f.getAbsolutePath());
		if(!temp.exists()) return;
		
		if(temp.lastModified() == lastModified) return;
		f = temp;
		lastModified = f.lastModified();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		material = Material.Get(br.readLine().split(" ")[1]);
		String[] o = br.readLine().split(" ")[1].split(",");
		String[] p = br.readLine().split(" ")[1].split(",");
	
		offset = new Rect(Float.parseFloat(o[0]), Float.parseFloat(o[1]), Float.parseFloat(o[2]), Float.parseFloat(o[3]));
		padding = new Rect(Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3]));
		
		br.close();
	}
	
	public static final List<Sprite> Sprites() {return sprites;}
}
