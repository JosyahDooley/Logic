package engine;

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

import math.Color;

public class Material extends engine.Object
{
	public Texture texture;
	public Color color;
	public Shader shader;
	
	//Static variables used for the materials list
	private static List<Material> materials = new ArrayList<Material>();
	private static int i;
	
	//Create a texture on the fly
	public Material(String n, Texture t, Color c, Shader s)
	{
		//Set all the information to the info passed in
		Name(n);
		texture = t;
		color = c;
		shader = s;
	}
	
	//Import a material by name
	public Material(String name)
	{
		//Create a buffered reader variable
		BufferedReader br;
		
		try
		{
			//Set the buffered reader by passing in the file reader for the path of the material in the folder
			if(name.startsWith("/"))
			{
				InputStreamReader isr = new InputStreamReader(Material.class.getResourceAsStream("/Materials" + name + ".Material"));
				br = new BufferedReader(isr);
				Name(name.replaceFirst("/", ""));
			}
			else
			{
				f = new File(name + ".Material");
				lastModified = f.lastModified();
				br = new BufferedReader(new FileReader(f));
				String[] split = name.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
				Name(split[split.length - 1]);
			}
			
			//Set the texture this material will be using
			texture = Texture.Find(br.readLine().split(" ")[1]);
			
			//Cache the color of this material
			String[] o = br.readLine().split(" ")[1].split(",");
			color = new Color(Float.parseFloat(o[0]), Float.parseFloat(o[1]), Float.parseFloat(o[2]), Float.parseFloat(o[3]));
			
			//Set the shader we will be using
			shader = Shader.Find(br.readLine().split(" ")[1]);
			
			//add the material to the list of materials
			materials.add(this);
			
			//And close the buffered reader
			br.close();
		}
		catch(IOException e)  {e.printStackTrace();}
	}
	
	//Get a material by name
	public static Material Get(String name)
	{
		//For all the materials
		for(i = 0; i < materials.size(); i++)
		{
			//If we have came across the material were looking for, return it
			if(materials.get(i).Name().equals(name)) return materials.get(i);
		}
		
		//If we didn't find a material by that name, return null
		return null;
	}
	
	public void Save()
	{
		if(f == null) return;
		
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			
			if(texture != null) bw.write("Texture: " + texture.Name());
			else bw.write("Texture: NULL");
			
			bw.write("\r\nColor: " + color.ToShortString());
			
			if(shader != null) bw.write("\r\nShader: " + shader.Name());
			else bw.write("\r\nShader: NULL");
			
			bw.close();
		}
		catch (IOException e) {e.printStackTrace();}
		
		Material original = Get(Name());
		if(original == null) return;
		
		try {original.Refresh();}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public static void RefreshAll()
	{
		for(i = 0; i < materials.size(); i++)
		{
			try {materials.get(i).Refresh();}
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
		
		//Set the texture this material will be using
		texture = Texture.Find(br.readLine().split(" ")[1]);
		
		//Cache the color of this material
		String[] o = br.readLine().split(" ")[1].split(",");
		color = new Color(Float.parseFloat(o[0]), Float.parseFloat(o[1]), Float.parseFloat(o[2]), Float.parseFloat(o[3]));
		
		//Set the shader we will be using
		shader = Shader.Find(br.readLine().split(" ")[1]);
		
		//And close the buffered reader
		br.close();
	}
	
	//Bind the material
	public void Bind()
	{
		//Bind the shader and texture
		shader.Bind();
		texture.Bind();
	}
	
	//Unbind the material
	public void Unbind()
	{
		//Unbind the shader and texture
		shader.Unbind();
		texture.Unbind();
	}
	
	public static final List<Material> Materials() {return materials;}
}
