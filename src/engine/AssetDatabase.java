package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import editor.Editor;
import editor.EditorUtil;
import gui.Font;
import gui.GUISkin;
import gui.Sprite;
import sound.AudioClip;

public class AssetDatabase
{
	private static Class<AssetDatabase> clazz = AssetDatabase.class;
	private static ClassLoader cl = clazz.getClassLoader();
	private static List<String> textures = new ArrayList<String>();
	private static List<String> fonts = new ArrayList<String>();
	private static List<String> shaders = new ArrayList<String>();
	private static List<String> materials = new ArrayList<String>();
	private static List<String> sprites = new ArrayList<String>();
	private static List<String> skins = new ArrayList<String>();
	private static List<String> audio = new ArrayList<String>();
	private static List<String> scenes = new ArrayList<String>();
	private static List<String> scripts = new ArrayList<String>();
	
	
	private static int i;
	
	public static void LoadAllResources()
	{
		InitResourcePaths();
		
		for(i = 0; i < textures.size(); i++) new Texture(textures.get(i));
		for(i = 0; i < fonts.size(); i++) new Font(fonts.get(i), 16);
		for(i = 0; i < shaders.size(); i++) new Shader(shaders.get(i));
		for(i = 0; i < materials.size(); i++) new Material(materials.get(i));
		for(i = 0; i < sprites.size(); i++) new Sprite(sprites.get(i));
		for(i = 0; i < skins.size(); i++) new GUISkin(skins.get(i));
		for(i = 0; i < audio.size(); i++) new AudioClip(audio.get(i));
		for(i = 0; i < scenes.size(); i++) new Scene(scenes.get(i));
		
		for(i = 0; i < scripts.size(); i++) EditorUtil.ImportClass(scripts.get(i));
		
		textures.clear();
		fonts.clear();
		shaders.clear();
		materials.clear();
		sprites.clear();
		skins.clear();
		audio.clear();
		scenes.clear();
		scripts.clear();
	}
	
	private static void InitResourcePaths()
	{
		URL dirURL = cl.getResource("engine");
		String protocol = dirURL.getProtocol();
		
		if(dirURL != null && protocol.equals("file")) ImportFromDirectory();
		else
		{
			try {ImportFromJar(dirURL);}
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	private static void ImportFromDirectory()
	{
		textures = ImportFromLocalDirectory("Textures", 1);
		fonts = ImportFromLocalDirectory("Font", 0);
		shaders = ImportFromLocalDirectory("Shaders", 0);
		materials = ImportFromLocalDirectory("Materials", 0);
		sprites = ImportFromLocalDirectory("Sprites", 0);
		skins = ImportFromLocalDirectory("Skins", 0);
		audio = ImportFromLocalDirectory("Audio", 1);
		
		scripts = ImportFromExternalDirectory("Scripts", 1);
		
		textures.addAll(ImportFromExternalDirectory("Textures", 1));
		fonts.addAll(ImportFromExternalDirectory("Font", 0));
		shaders.addAll(ImportFromExternalDirectory("Shaders", 0));
		materials.addAll(ImportFromExternalDirectory("Materials", 0));
		sprites.addAll(ImportFromExternalDirectory("Sprites", 0));
		skins.addAll(ImportFromExternalDirectory("Skins", 0));
		audio.addAll(ImportFromExternalDirectory("Audio", 1));
		scenes.addAll(ImportFromExternalDirectory("Scenes", 0));
	}
	
	private static List<String> ImportFromLocalDirectory(String path, int useExtension)
	{
		List<String> paths = new ArrayList<String>();
		InputStream in = cl.getResourceAsStream(path); if(in == null) return paths;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		try
		{
			while((line = br.readLine()) != null)
			{
				if(useExtension == 1) paths.add("/" + line);
				else paths.add("/" + line.split("\\.")[0]);
			}
		}
		catch (IOException e) {e.printStackTrace();}
		
		try {br.close();}
		catch (IOException e) {e.printStackTrace();}
		
		return paths;
	}
	
	private static List<String> ImportFromExternalDirectory(String path, int useExtension)
	{
		List<String> paths = new ArrayList<String>();
		File dir = new File(Editor.WorkingDirectory() + path);
		File[] files = dir.listFiles();
		for(i = 0; i < files.length; i++)
		{
			String aPath = files[i].getAbsolutePath();
			if(path.endsWith("/")) continue;
			if(useExtension == 0) aPath = aPath.split("\\.")[0];
			paths.add(aPath);
		}
		return paths;
	}
	
	private static void ImportFromJar(URL dirURL) throws UnsupportedEncodingException, IOException
	{
		String me = clazz.getName().replace(".", "/") + ".class";
		dirURL = cl.getResource(me);
		
		if(dirURL.getProtocol().equals("jar"))
		{
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries();
			
			while(entries.hasMoreElements())
			{
				String name = entries.nextElement().getName();
				if(name.endsWith("/")) continue;
				else if(name.startsWith("Textures")) {textures.add("/" + name.split("/")[1]);}
				else if(name.startsWith("Font")) {fonts.add("/" + name.split("/")[1].split("\\.")[0]);}
				else if(name.startsWith("Materials")) {materials.add("/" + name.split("/")[1].split("\\.")[0]);}
				else if(name.startsWith("Shaders")) {shaders.add("/" + name.split("/")[1].split("\\.")[0]);}
				else if(name.startsWith("Sprites")) {sprites.add("/" + name.split("/")[1].split("\\.")[0]);}
				else if(name.startsWith("Skins")) {skins.add("/" + name.split("/")[1].split("\\.")[0]);}
				else if(name.startsWith("Audio")) {audio.add("/" + name.split("/")[1]);}
			}
			jar.close();
		}
		
		if(ProjectSettings.isEditor)
		{
			scripts = ImportFromExternalDirectory("Scripts", 1);
			
			textures.addAll(ImportFromExternalDirectory("Textures", 1));
			fonts.addAll(ImportFromExternalDirectory("Font", 0));
			shaders.addAll(ImportFromExternalDirectory("Shaders", 0));
			materials.addAll(ImportFromExternalDirectory("Materials", 0));
			sprites.addAll(ImportFromExternalDirectory("Sprites", 0));
			skins.addAll(ImportFromExternalDirectory("Skins", 0));
			audio.addAll(ImportFromExternalDirectory("Audio", 1));
			scenes.addAll(ImportFromExternalDirectory("Scenes", 0));
		}
	}
}
