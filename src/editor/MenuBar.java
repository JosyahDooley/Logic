package editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import engine.Debug;
import engine.GameObject;
import engine.LogicBehaviour;
import engine.SceneManager;
import gui.GUI;
import gui.GUIStyle;
import math.Color;
import math.Rect;

public class MenuBar
{
	public Map<String, List<MenuItem>> menu = new LinkedHashMap<String, List<MenuItem>>();
	
	private GUIStyle box;
	private GUIStyle empty;
	private GUIStyle play;
	private GUIStyle stop;
	private String selected;
	private Color prevColor;
	private int selectedIndex = 0;
	
	public MenuBar()
	{
		box = Editor.skin.Get("Box");
		play = Editor.skin.Get("PlayButton");
		stop = Editor.skin.Get("StopButton");
		
		Add("File", new MenuItem("New Scene", this::File));
		Add("File", new MenuItem("Open Scene", this::File));
		Add("File", new MenuItem("Save Scene", this::File));
		Add("File", new MenuItem("Export Build", this::File));
		Add("File", new MenuItem("Quit", this::File));
		Add("Edit", new MenuItem("Deselect", this::Edit));
		Add("Edit", new MenuItem("Undo", this::Edit));
		Add("Edit", new MenuItem("Redo", this::Edit));
		Add("Asset", new MenuItem("New GameObject", this::Asset));
		Add("Create", new MenuItem("Macs Tileset", this::Create));
		
		if(Editor.snapping.x == 1) selectedIndex = 0;
		else if(Editor.snapping.x == 8) selectedIndex = 1;
		else if(Editor.snapping.x == 16) selectedIndex = 2;
		else if(Editor.snapping.x == 32) selectedIndex = 3;
		else selectedIndex = 4;
		Add("Snapping", new MenuItem("1x1", this::Snapping));
		Add("Snapping", new MenuItem("8x8", this::Snapping));
		Add("Snapping", new MenuItem("16x16", this::Snapping));
		Add("Snapping", new MenuItem("32x32", this::Snapping));
		Add("Snapping", new MenuItem("64x64", this::Snapping));
	}
	
	public void Add(String parent, MenuItem item)
	{
		List<MenuItem> menuItem = menu.get(parent);
		
		if(menuItem == null)
		{
			menuItem = new ArrayList<MenuItem>();
			menu.put(parent,  menuItem);
		}
		menuItem.add(item);
	}
	
	public void Render()
	{
		if(selected != null)
		{
			if(GUI.HasPopup() == false) selected = null;
		}
		
		prevColor = GUI.textColor;
		GUI.textColor = Color.white;
		
		float offset = 0;
		float index = 0;
		for(String m : menu.keySet())
		{
			float width = GUI.font.StringWidth(m) + 10;
			Rect nameRect = new Rect(offset - index, 0, width, 30);
			if(selected == null)
			{
				if(GUI.CenteredButton(m, nameRect, empty,  box))
				{
					List<MenuItem> list = menu.get(m);
					List<String> v = new ArrayList<String>();
					for(int i = 0; i < list.size(); i++) v.add(list.get(i).name);
					if(m.equals("Snapping")) GUI.SetPopup(nameRect, v, this::Clicked, new int[] {selectedIndex});
					else GUI.SetPopup(nameRect, v, this::Clicked, null);
					selected = m;
				}
			}
			else
			{
				if(selected.equals(m))
				{
					if(GUI.CenteredButton(m,  nameRect, box, box)) GUI.CancelPopup();
				}
				else
				{
					if(GUI.CenteredButton(m, nameRect, empty,  box))
					{
						List<MenuItem> list = menu.get(m);
						List<String> v = new ArrayList<String>();
						for(int i = 0; i < list.size(); i++) v.add(list.get(i).name);
						GUI.SetPopup(nameRect, v, this::Clicked, null);
						selected = m;
					}
				}
			}
			offset += width;
			index++;
		}
		
		GUIStyle style = play;
		boolean isPlaying = Editor.IsPlaying();
		if(isPlaying)
		{
			style = stop;
		}
		if(GUI.Button("", new Rect(offset - index + 4, 8, 16, 16), style, style)) Editor.Play(!isPlaying);
		
		GUI.textColor = prevColor;
	}
	
	public void Clicked(String v)
	{
		List<MenuItem> list = menu.get(selected);
		for(MenuItem item : list)
		{
			if(item.name.equals(v))
			{
				item.Accept();
				return;
			}
		}
	}
	
	public void Snapping(MenuItem m)
	{
		if(m.name.equals("1x1")) {Editor.snapping.Set(1, 1); selectedIndex = 0;}
		if(m.name.equals("8x8")) {Editor.snapping.Set(8, 8); selectedIndex = 1;}
		if(m.name.equals("16x16")) {Editor.snapping.Set(16, 16); selectedIndex = 2;}
		if(m.name.equals("32x32")) {Editor.snapping.Set(32, 32); selectedIndex = 3;}
		if(m.name.equals("64x64")) {Editor.snapping.Set(64, 64); selectedIndex = 4;}
	}
	
	public void File(MenuItem m)
	{
		if(m.name.equals("New Scene"))
		{
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(Editor.WorkingDirectory() + "Scenes/"));
			fc.setFileFilter(new FileNameExtensionFilter("Scene File", "scene"));
			int result = fc.showSaveDialog(null);
			
			if(result == JFileChooser.APPROVE_OPTION)
			{
				File temp = fc.getSelectedFile();
				String name = temp.getName();
				if(name.endsWith(".scene")) name = name.replace(".scene", "");
				try
				{
					File f = new File(Editor.WorkingDirectory() + "Scenes/" + name + ".scene");
					f.createNewFile();
					SceneManager.LoadScene(name);
				}
				catch (IOException e) {Debug.Log("Could not save new scene at path");}
			}
		}
		if(m.name.equals("Open Scene"))
		{
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(Editor.WorkingDirectory() + "Scenes/"));
			fc.setFileFilter(new FileNameExtensionFilter("Scene File", "scene"));
			int result = fc.showOpenDialog(null);
			
			if(result == JFileChooser.APPROVE_OPTION)
			{
				SceneManager.LoadScene(fc.getSelectedFile().getName().split("\\.")[0]);
			}
		}
		if(m.name.equals("Save Scene"))
		{
			try {Editor.SaveScene(SceneManager.CurrentScene());}
			catch (IOException e) {Debug.Log("Could not save scene: " + SceneManager.CurrentScene());}
		}
		if(m.name.equals("Export Build"))
		{
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(Editor.WorkingDirectory()));
			fc.setFileFilter(new FileNameExtensionFilter("Export Build", "jar"));
			int result = fc.showSaveDialog(null);
			
			if(result == JFileChooser.APPROVE_OPTION)
			{
				File temp = fc.getSelectedFile();
				String path = temp.getAbsolutePath();
				if(!path.endsWith(".jar")) path += ".jar";
				
				try {Export(path);}
				catch (IOException | URISyntaxException e) {e.printStackTrace();}
			}
		}
	}
	
	public void Edit(MenuItem m)
	{
		if(m.name.equals("Undo")) UndoRedo.Undo();
		else if(m.name.equals("Redo")) UndoRedo.Redo();
		else if(m.name.equals("Deselect")) Editor.SetSelected(null);
	}
	
	public void Asset(MenuItem m)
	{
		if(m.name.equals("New GameObject")) new GameObject("New GameObject");
	}
	
	public void Create(MenuItem m)
	{
		if(m.name.equals("Macs Tileset"))
		{
			Debug.Log("This feature is in development and is currently disabled!");
			System.err.println("Tileset creation is currently in development and is disabled! Sprite reformatting also needs to be done before this feature is unlocked and useable.");
			/*
			String result = Dialog.InputDialog("What material will you use?", "");
			Material mat = Material.Get(result);
			
			if(mat == null)
			{
				Debug.Log("Creating a macs tileset requires that you use a pre-existing material!");
				return;
			}
			for(int y = 0; y < 16; y++)
			{
				for(int x = 0; x < 16; x++)
				{
					if(x % 2 == 0)
					{
						try
						{
							Sprite s = Editor.projectPanel().CreateSprite(mat.Name() + "_" + x + "_" + y);
							s.material = mat;
							s.offset = new Rect(x * 32, y * 32, 32, 32);
							Editor.projectPanel().SaveSprite(s);
						}
						catch (IOException e) {e.printStackTrace();}
						
						if(y % 3 == 0)
						{
							try
							{
								Sprite s = Editor.projectPanel().CreateSprite(mat.Name() + "_" + x + "_" + y + "_Center");
								s.material = mat;
								s.offset = new Rect(x * 32 + 16, y * 32 + 48, 32, 32);
								Editor.projectPanel().SaveSprite(s);
							}
							catch (IOException e) {e.printStackTrace();}
						}
					}
					else
					{
						try
						{
							Sprite s = Editor.projectPanel().CreateSprite(mat.Name() + "_" + x + "_" + y);
							s.material = mat;
							s.offset = new Rect(x * 32, y * 32, 32, 32);
							Editor.projectPanel().SaveSprite(s);
						}
						catch (IOException e) {e.printStackTrace();}
					}
				}
			}
			*/
		}
	}
	
	private void Export(String path) throws IOException, URISyntaxException
	{
		JarFile engineJar = new JarFile(new File(MenuBar.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
		
		File f = new File(path);
		f.createNewFile();
		
		System.out.println("Exporting Dependencies...");
		
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
		Enumeration<JarEntry> entries = engineJar.entries();
		while(entries.hasMoreElements())
		{
			JarEntry entry = entries.nextElement();
			InputStream is = engineJar.getInputStream(entry);
			
			/* Later will remove the editor from the build, but is not in the scope of the tutorial series
			if(entry.getName().startsWith("editor"))
			{
				System.out.println("Skipping " + entry.getName());
				continue;
			}
			*/
			
			jos.putNextEntry(new JarEntry(entry.getName()));
			WriteBytes(is, jos);
		}
		
		//Add the assets
		AddAssets("Audio", jos);
		AddAssets("Font", jos);
		AddAssets("Materials", jos);
		AddAssets("Scenes", jos);
		AddAssets("Shaders", jos);
		AddAssets("Skins", jos);
		AddAssets("Sprites", jos);
		AddAssets("Textures", jos);
		
		//Add Behaviours
		AddBehaviours(jos);
		
		jos.putNextEntry(new JarEntry("Project.Settings"));
		jos.write("Something".getBytes());
		jos.closeEntry();
		
		System.out.println("Export Complete!");
		
		jos.close();
		engineJar.close();
	}
	
	private static void AddAssets(String path, JarOutputStream jos)
	{
		System.out.println("Exporting " + path + "...");
		
		File dir = new File(Editor.WorkingDirectory() + path);
		File[] files = dir.listFiles();
		if(files == null) return;
		for(int i = 0; i < files.length; i++)
		{
			try
			{
				AddAsset(files[i], jos);
			}
			catch (FileNotFoundException e) {e.printStackTrace(); System.out.println(files[i].getName() + " can't be found!");}
		}
	}
	
	private static void AddAsset(File f, JarOutputStream jos) throws FileNotFoundException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
		try {jos.putNextEntry(new JarEntry(f.getParentFile().getName() + "/" + f.getName())); WriteBytes(in, jos);}
		catch (IOException e) {e.printStackTrace(); System.out.println(f.getName() + " could not write bytes!");}
	}
	
	private static void AddBehaviours(JarOutputStream jos) throws IOException
	{
		System.out.println("Exporting Behaviours...");
		
		List<LogicBehaviour> behaviours = EditorUtil.GetImportedClasses();
		for(int i = 0; i < behaviours.size(); i++)
		{
			Class<?> c = behaviours.get(i).getClass();
			String path = c.getName().replace('.', '/') + ".class";
			jos.putNextEntry(new JarEntry(path));
			InputStream is = c.getClassLoader().getResourceAsStream(path);
			WriteBytes(is, jos);
		}
	}
	
	private static void WriteBytes(InputStream is, JarOutputStream jos) throws IOException
	{
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while((bytesRead = is.read(buffer)) != -1) jos.write(buffer, 0, bytesRead);
		is.close();
		jos.flush();
		jos.closeEntry();
	}
}
