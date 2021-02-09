package editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import engine.Application;
import engine.CustomClass;
import engine.Debug;
import engine.GameObject;
import engine.LogicBehaviour;
import engine.SceneManager;
import engine.SpriteRenderer;
import gui.GUI;
import gui.GUISkin;
import gui.GUIStyle;
import gui.Sprite;
import math.Mathf;
import math.Rect;
import math.Vector2;
import input.Input;
import input.KeyCode;
import input.Mouse;

public class Editor
{
	public static GUISkin skin;
	public static GUIStyle xClose;
	public static GUIStyle arrowDown;
	public static GUIStyle arrowRight;
	public static GUIStyle toggleOff;
	public static GUIStyle toggleOn;
	public static GUIStyle window;
	public static GUIStyle listViewOn;
	public static GUIStyle listViewOff;
	public static Rect editorDrawArea = new Rect(400, 30, 400, 230);
	public static Rect sceneDrawArea = new Rect();
	public static Vector2 snapping = new Vector2(32, 32);
	public static long cursorIndex = Mouse.GetCursor();
	
	public static Vector2 cameraPosition = new Vector2();
	public static final String workspaceDirectory = System.getProperty("user.dir") + "/logic-workspace/";
	public static final String editorVersion = "0.53";
	private static byte configInit = 0;
	private static String workingDirectory = "";
	
	private static Hierarchy h;
	private static Inspector i;
	private static ProjectPanel p;
	private static MenuBar m;
	private static engine.Object tempDraggedObject;
	private static engine.Object draggedObject;
	private static GameObject selected;
	private static engine.Object selectedAsset;
	private static engine.Object inspected;
	private static byte playing = 0;
	private static byte draggingScene = 0;
	private static Vector2 startDragPoint = new Vector2();
	private static Vector2 startCameraDragPoint = cameraPosition;
	private static int a;
	private static boolean listView = true;
	private static boolean disableDrop = false;
	
	public static void InitConfig()
	{
		if(configInit == 1) return;
		configInit = 1;
		File configFile = new File(workspaceDirectory + "config.properties");
		
		try
		{
			FileReader r = new FileReader(configFile);
			Properties p = new Properties();
			p.load(r);
			
			String recordedVersion = p.getProperty("Version");
			if(recordedVersion != null)
			{
				if(!editorVersion.equals(recordedVersion))
				{
					System.out.println("Version Changed");
				}
				else System.out.println("Same Version");
			}
			
			String lastAppSize = p.getProperty("LastAppSize");
			if(lastAppSize != null)
			{
				String[] splitSize = lastAppSize.replace(" ", "").split(",");
				if(splitSize.length == 2) Application.SetWindowSize((int) Float.parseFloat(splitSize[0]), (int) Float.parseFloat(splitSize[1]));
			}
			
			String view = p.getProperty("ListView");
			if(view != null)
			{
				listView = Boolean.parseBoolean(view);
			}
			
			String snap = p.getProperty("Snapping");
			if(snap != null)
			{
				String[] splitSize = snap.split(",");
				snapping.Set(Float.parseFloat(splitSize[0]), Float.parseFloat(splitSize[1]));
				
			}
			
			String editorDraw = p.getProperty("EditorRect");
			if(editorDraw != null)
			{
				String[] splitRect = editorDraw.split(",");
				editorDrawArea.Set(Float.parseFloat(splitRect[0]), Float.parseFloat(splitRect[1]), Float.parseFloat(splitRect[2]), Float.parseFloat(splitRect[3]));
			}
			
			r.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("New User");
			SaveConfig(configFile);
		}
		catch(IOException e) {e.printStackTrace();}
	}
	
	public static void Init()
	{
		skin = GUISkin.GetSkin("DefaultGUI");
		xClose = skin.Get("XClose");
		arrowDown = skin.Get("ArrowDown");
		arrowRight = skin.Get("ArrowRight");
		toggleOff = skin.Get("ToggleOff");
		toggleOn = skin.Get("ToggleOn");
		window = skin.Get("Window");
		listViewOn = skin.Get("ListViewOn");
		listViewOff = skin.Get("ListViewOff");
		
		h = new Hierarchy();
		i = new Inspector();
		p = new ProjectPanel();
		m = new MenuBar();
		
		p.listView = listView;
	}
	
	public static final boolean IsPlaying() {return playing == 1;}
	public static void Play(boolean shouldPlay)
	{
		if(shouldPlay)
		{
			try
			{
				SaveScene(SceneManager.CurrentScene());
				GameObject.StartAll();
				playing = 1;
				Debug.Clear();
			}
			catch (IOException e) {Debug.Log("Could not save current scene. Play mode could not be started!");}
		}
		else
		{
			SceneManager.LoadScene(SceneManager.CurrentScene());
			SetSelected(null);
			playing = 0;
		}
	}
	
	public static void Render()
	{
		if(selected != null)
		{
			if(Input.GetKeyDown(261) || Input.GetKeyDown(259))
			{
				selected.Destroy();
				Editor.SetSelected(null);
			}
		}
		
		//Prepare the gui for rendering
		GUI.Prepare();
		
		Debug.Draw();
		m.Render();
		
		if(playing == 0)
		{
			int tempCursor = 0;
			
			//If we're a dragging an object around
			if(draggedObject != null)
			{
				//If we clicked on this object in the scene view
				if(draggedObject instanceof SceneDraggable)
				{
					//Calculate the offset
					Vector2 gOffset = Mouse.Position().Sub(new Vector2(Application.Size().Mul(0.5f)));
					gOffset.y = -gOffset.y;
					
					if(snapping.x < 1) snapping.x = 1;
					else if(snapping.y < 1) snapping.y = 1;
					
					//Cache the draggable, calculate and set the object position base on the mousePosition in world space according to snapping
					SceneDraggable draggable = (SceneDraggable) draggedObject;
					Vector2 mousePosition = cameraPosition.Sub(draggable.dragOffset).Add(gOffset).Div(snapping).Floor().Mul(snapping);
					draggable.g.Position(mousePosition);
				}
			}
			
			sceneDrawArea.Set(editorDrawArea.x, 30, Application.Width() - (editorDrawArea.x + editorDrawArea.width), Application.Height() - editorDrawArea.height - 30);
			if(sceneDrawArea.Contains(Mouse.Position()))
			{
				if(Mouse.GetButtonDown(2))
				{
					startDragPoint = Mouse.Position();
					startCameraDragPoint = cameraPosition;
					draggingScene = 1;
				}
				else if(Mouse.GetButtonDown(0))
				{
					List<GameObject> l = GameObject.Instances();
					
					Vector2 halfScreen = new Vector2(Application.Width() * 0.5f, Application.Height() * 0.5f);
					Vector2 mousePos = Mouse.Position().Sub(halfScreen).Add(new Vector2(cameraPosition.x, -cameraPosition.y));
					
					for(a = 0; a < l.size(); a++)
					{
						GameObject g = l.get(a);
						SpriteRenderer sr = (SpriteRenderer)g.GetComponent("SpriteRenderer");
						if(sr != null)
						{
							Vector2 mouseToWorldSpace = new Vector2(mousePos.x, -mousePos.y);
							if(sr.Contains(mouseToWorldSpace) && g != selected)
							{
								SetSelected(g);
								SetDraggableObject(new SceneDraggable(g, new Vector2(1), mouseToWorldSpace.Sub(g.Position())));
								break;
							}
						}
					}
				}
			}
			if(draggingScene == 1)
			{
				Vector2 dragDistance = startDragPoint.Sub(Mouse.Position());
				cameraPosition = startCameraDragPoint.Add(dragDistance.x, -dragDistance.y);
				if(Mouse.GetButtonUp(2)) draggingScene = 0;
			}
			
			UndoRedo.Poll();
			
			boolean clicked = Mouse.GetButtonDown(0);
			Vector2 mousePos = Mouse.Position();
			
			GUI.Window(new Rect(0, 30, editorDrawArea.x, sceneDrawArea.height), "Hierarchy", h::Render, window);
			GUI.Window(new Rect(Application.Width() - editorDrawArea.width, 30, editorDrawArea.width, Application.Height() - (editorDrawArea.height + 30)), "Inspector", i::Render, window);
			GUI.Window(new Rect(0, Application.Height() - editorDrawArea.height, editorDrawArea.x, editorDrawArea.height - 30), "Asset Types", p::RenderTypes, window);
			GUI.Window(new Rect(editorDrawArea.x, Application.Height() - editorDrawArea.height, sceneDrawArea.width + editorDrawArea.width, editorDrawArea.height - 30), "Assets", t -> {
				try {p.RenderAssets(t);} catch (CloneNotSupportedException e) {e.printStackTrace();}}, window);
			//p.listView = GUI.Toggle(p.listView, new Rect(Application.Width() - (window.padding.width + 21), sceneArea.height + 37, 16, 16), listViewOn, listViewOff);
			
			//Check if we have clicked to drag a window
			if(mousePos.x >= editorDrawArea.x - 4 && mousePos.x <= editorDrawArea.x && mousePos.y > 30 && mousePos.y < Application.Height() - 30)
			{
				tempCursor = 2;
				if(clicked) SetDraggableObject(h);
			}
			else if(mousePos.x <= (Application.Width() - editorDrawArea.width) + 4 && mousePos.x >= Application.Width() - editorDrawArea.width && mousePos.y < Application.Height() - editorDrawArea.height && mousePos.y > 30)
			{
				tempCursor = 2;
				if(clicked) SetDraggableObject(i);
			}
			else if(mousePos.y >= Application.Height() - (editorDrawArea.height + 4) && mousePos.y <= Application.Height() - editorDrawArea.height)
			{
				tempCursor = 3;
				if(clicked) SetDraggableObject(p);
			}
			
			if(Mouse.GetButtonUp(0))
			{
				if(draggedObject != null)
				{
					if(!disableDrop && new Rect(editorDrawArea.x, editorDrawArea.y, Application.Width() - editorDrawArea.width - editorDrawArea.x, Application.Height() - editorDrawArea.height - editorDrawArea.y).Contains(Mouse.Position()))
					{
						if(draggedObject instanceof Sprite)
						{
							Sprite s = (Sprite) draggedObject;
							GameObject go = new GameObject(s.Name());

							Vector2 gOffset = Mouse.Position().Sub(new Vector2(Application.Size().Mul(0.5f)));
							gOffset.y = -gOffset.y;
							
							if(snapping.x < 1) snapping.x = 1;
							else if(snapping.y < 1) snapping.y = 1;
							
							go.Position(cameraPosition.Add(gOffset).Div(snapping).Floor().Mul(snapping));
							
							SpriteRenderer sr = new SpriteRenderer();
							sr.sprite = s;
							go.AddComponent(sr);
							
							go.Parent(selected);
							SetSelected(go);
						}
						//Have to make sure draggable is prefab and not already in the scene
						//else if(draggedObject instanceof GameObject)
						//{
							//GameObject g = (GameObject) draggedObject;
							//Alter drop position here
						//}
					}
					draggedObject = null;
					disableDrop = false;
				}
				else if(tempDraggedObject != null) tempDraggedObject = null;
			}
			else if(draggedObject != null)
			{
				if(Mouse.GetButtonDown(1))
				{
					if(new Rect(editorDrawArea.x, editorDrawArea.y, Application.Width() - editorDrawArea.width, Application.Height() - editorDrawArea.height).Contains(Mouse.Position()))
					{
						if(draggedObject instanceof Sprite)
						{
							Sprite s = (Sprite) draggedObject;
							GameObject go = new GameObject(s.Name());

							Vector2 gOffset = Mouse.Position().Sub(new Vector2(Application.Size().Mul(0.5f)));
							gOffset.y = -gOffset.y;
							
							if(snapping.x < 1) snapping.x = 1;
							else if(snapping.y < 1) snapping.y = 1;
							
							go.Position(cameraPosition.Add(gOffset).Div(snapping).Floor().Mul(snapping));
							
							SpriteRenderer sr = new SpriteRenderer();
							sr.sprite = s;
							go.AddComponent(sr);
							
							go.Parent(selected);
							//SetSelected(go);
						}
						//Have to make sure draggable is prefab and not already in the scene
						//else if(draggedObject instanceof GameObject)
						//{
							//GameObject g = (GameObject) draggedObject;
							//Alter drop position here
						//}
						disableDrop = true;
					}
				}
			}
			
			if(draggedObject != null)
			{
				if(draggedObject instanceof Hierarchy)
				{
					editorDrawArea.x = Mathf.Clamp(mousePos.x, 200, Application.Width() - (editorDrawArea.width + 200));
					tempCursor = 2;
				}
				else if(draggedObject instanceof Inspector)
				{
					editorDrawArea.width = Mathf.Clamp(Application.Width() - mousePos.x, 200, Application.Width() - (editorDrawArea.x + 200));
					tempCursor = 2;
				}
				else if(draggedObject instanceof ProjectPanel)
				{
					editorDrawArea.height = Mathf.Clamp(Application.Height() - mousePos.y, 204, Application.Height() - 230);
					tempCursor = 3;
				}
				else tempCursor = 1;
				
			}
			Mouse.SetCursor(tempCursor);
			if(Input.GetKey(KeyCode.LeftControl) && Input.GetKeyDown(KeyCode.S))
			{
				try {SaveScene(SceneManager.CurrentScene());}
				catch (IOException e) {Debug.Log("Scene could not be saved!"); e.printStackTrace();}
			}
		}
		
		//Unbind the gui
		GUI.Unbind();
	}
	
	public static final Rect GetSceneDrawArea() {return sceneDrawArea;}
	
	public static void DrawDragged()
	{
		//If this is a scene draggable
		if(draggedObject instanceof SceneDraggable)
		{
			
		}
		else
		{
			//This is not a scene draggable, it is an editor draggable
			GUI.Label(draggedObject.Name(), Mouse.Position());
			
			if(sceneDrawArea.Contains(Mouse.Position()))
			{
				if(draggedObject instanceof Sprite)
				{
					Sprite s = (Sprite) draggedObject;
					GUI.BeginArea(sceneDrawArea);
					Rect drawRect = new Rect(Mouse.Position().Sub(new Vector2(0, s.offset.height)).Sub(sceneDrawArea.GetPosition()), s.offset.GetSize());
					GUI.DrawTextureWithTexCoords(s.material.texture, drawRect, s.UV());
					GUI.EndArea();
				}
			}
		}
	}
	
	public static final engine.Object DraggedObject()
	{
		if(tempDraggedObject != null)
		{
			if(!Mouse.Position().Floor().Equals(startDragPoint.Floor()))
			{
				draggedObject = tempDraggedObject;
				tempDraggedObject = null;
			}
		}
		return draggedObject;
	}
	public static void SetDraggableObject(engine.Object draggable)
	{
		startDragPoint.Set(Mouse.Position());
		tempDraggedObject = draggable;
	}
	
	public static final Object GetInspected() {return inspected;}
	private static void SetInspected(engine.Object o){inspected = o; if(o != null) i.SetAttributes(o, true);}
	public static void RefreshInspected() {i.SetAttributes(inspected, false);}
	
	public static final GameObject GetSelected() {return selected;}
	public static void SetSelected(GameObject g)
	{
		selected = g;
		SetInspected((engine.Object) g);
	}
	
	public static final engine.Object GetSelectedAsset() {return selectedAsset;}
	public static void SetSelectedAsset(engine.Object o)
	{
		selectedAsset = o;
		SetInspected(o);
	}
	
	public static void SaveConfig(File f)
	{
		try
		{
			File dir = new File(workspaceDirectory);
			dir.mkdir();
			
			Properties p = new Properties();
			p.setProperty("Version", editorVersion);
			p.setProperty("LastAppSize", Application.Size().ToShortString());
			p.setProperty("ListView", String.valueOf(Editor.p.listView));
			p.setProperty("Snapping", snapping.ToShortString());
			p.setProperty("EditorRect", editorDrawArea.ToShortString());
			
			FileWriter writer = new FileWriter(f);
			p.store(writer, "Logic Configuration");
			writer.close();
		}
		catch (FileNotFoundException e) {e.printStackTrace();}
		catch(IOException e) {e.printStackTrace();}
	}
	
	public static void OpenProject(String name)
	{
		Application.name += " -" + name + "-";
		workingDirectory = workspaceDirectory + name + "/";
		
		File projDir = new File(workingDirectory);
		Boolean newProject = projDir.mkdir();
		
		new File(workingDirectory + "Audio/").mkdir();
		new File(workingDirectory + "Font/").mkdir();
		new File(workingDirectory + "Materials/").mkdir();
		new File(workingDirectory + "Shaders/").mkdir();
		new File(workingDirectory + "Skins/").mkdir();
		new File(workingDirectory + "Sprites/").mkdir();
		new File(workingDirectory + "Textures/").mkdir();
		new File(workingDirectory + "Scenes/").mkdir();
		new File(workingDirectory + "Scripts/").mkdir();
		
		if(newProject) return;
		
		
		//Put opening of project stuff here
	}
	
	public static final String WorkingDirectory() {return workingDirectory;}
	
	public static void SaveScene(String sceneName) throws IOException
	{
		File f = new File(workingDirectory + "Scenes/" + sceneName + ".scene");
		FileWriter fw = new FileWriter(f);
		
		List<GameObject> objectList = new ArrayList<GameObject>();
		
		GameObject master = GameObject.Master();
		objectList.add(master);
		while(objectList.size() > 0)
		{
			GameObject g = objectList.get(0);
			List<GameObject> children = g.Children();
			
			if(children.size() > 0)
			{
				for(int i = 0; i < children.size(); i++) objectList.add(0, children.get(i));
			}
			if(g == master)
			{
				objectList.remove(objectList.size() - 1);
				continue;
			}
			
			WriteTransform(g, fw);
			
			List<LogicBehaviour> b = g.GetComponents();
			for(int i = 0; i < b.size(); i++) WriteComponent(b.get(i), fw);
			if(g.Parent() != master) fw.write("\t<P Name=\"" + g.Parent().Name() + "\">\n</G>\n");
			else fw.write("</G>\n");
			objectList.remove(g);
		}
		
		fw.close();
	}
	
	private static void WriteTransform(GameObject g, FileWriter fw) throws IOException
	{
		String line = "<G Name=\"" + g.Name() + "\" ";
		line += "Position=\"" + g.Position().x + " " + g.Position().y + "\" ";
		line += "Scale=\"" + g.Scale().x + " " + g.Scale().y + "\" ";
		line += "Rotation=\"" + g.Rotation() + "\" ";
		line += "Depth=\"" + g.depth + "\" ";
		line += "Layer=\"" + g.GetLayer() + "\" ";
		line += "ID=\"" + g.instanceID() + "\">\n";
		fw.write(line);
	}
	
	//Write a behaviour
	private static void WriteComponent(LogicBehaviour b, FileWriter fw) throws IOException
	{
		WriteClass(b, fw, "\t", "");
		fw.write("\t</B>\n");
	}
	//Write a class, this method is only called for an attached behaviour and custom classes
	private static void WriteClass(engine.Object o, FileWriter fw, String starter, String fieldName) throws IOException
	{
		Class<?> c = o.getClass();
		if(o instanceof engine.LogicBehaviour) fw.write("\t<B Name\"" + c.getCanonicalName() + "\">\n");
		else fw.write(starter + "<C " + fieldName + "=\"" + c.getCanonicalName() + "\">\n");
		
		Field[] fields = c.getFields();
		
		for(int i = 0; i < fields.length; i++)
		{
			try {WriteVariable(fields[i], o, fw, starter + "\t");}
			catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		}
		
		if(o instanceof CustomClass) fw.write(starter + "</C>\n");
	}
	
	//Write a variable inside a class
	private static void WriteVariable(Field f, Object o, FileWriter fw, String starter) throws IllegalArgumentException, IllegalAccessException, IOException
	{
		if(f.getType().isArray())
		{
			Class<?> c = f.get(o).getClass();
			Class<?> type = c.getComponentType();
			if(c.getComponentType().isArray()) return;
			
			//New to add array type to it so we know what kind of array we need to create
			if(type == int.class) {int[] arr = (int[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(type == double.class) {double[] arr = (double[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(type == short.class) {short[] arr = (short[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(type == byte.class) {byte[] arr = (byte[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(type == boolean.class) {boolean[] arr = (boolean[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(type == String.class) {String[] arr = (String[]) f.get(o); fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n"); for(int i = 0; i < arr.length; i++) fw.write(starter + "\t<V Element" + i + "=\"" + arr[i] + "\">\n");}
			else if(!c.getComponentType().isPrimitive())
			{
				Object[] arr = (Object[]) f.get(o);
				fw.write(starter + "<A " + f.getName() + "=\"" + arr.length + "\">\n");
				for(int i = 0; i < arr.length; i++)
				{
					Object current = arr[i];
					if(current == null)
					{
						fw.write(starter + "\t<V Element" + i + "=\"NULL\">\n");
					}
					else if(current instanceof engine.CustomClass)
					{
						//Dont do anything for now until I figure out a good way to write this
						//WriteClass((engine.Object) current, fw, starter + "\t", "Element" + i);
					}
					else if(current instanceof GameObject)
					{
						if(f.get(o) == null) fw.write(starter + "\t<V Element" + i + "=\"NULL\">\n");
						else fw.write(starter + "\t<V Element" + i + "=\"" + ((GameObject) current).instanceID() + "\">\n");
					}
					else if(current instanceof engine.Object)
					{
						if(f.get(o) == null) fw.write(starter + "\t<V Element" + i + "=\"NULL\">\n");
						else fw.write(starter + "\t<V Element" + i + "=\"" + ((engine.Object) current).Name() + "\">\n");
					}
					else if(current.getClass() == Vector2.class)
					{
						Vector2 v = (Vector2) current;
						fw.write(starter + "\t<V Element" + i + "=\"" + v.x + " " + v.y + "\">\n");
					}
					else if(current.getClass() == Rect.class)
					{
						Rect v = (Rect) current;
						fw.write(starter + "\t<V Element" + i + "=\"" + v.x + " " + v.y + " " + v.width + " " + v.height + "\">\n");
					}
				}
			}
		}
		else if(engine.CustomClass.class.isAssignableFrom(f.getType()))
		{
			WriteClass((engine.Object) f.get(o), fw, starter, f.getName());
		}
		else if(engine.Object.class.isAssignableFrom(f.getType()))
		{
			if(f.get(o) == null) fw.write(starter + "<V " + f.getName() + "=\"NULL\">\n");
			else if(GameObject.class.isAssignableFrom(f.getType()))
			{
				engine.Object go = (engine.Object) f.get(o);
				fw.write(starter + "<V " + f.getName() + "=\"" + go.instanceID() + "\" " + ">\n");
			}
			else if(LogicBehaviour.class.isAssignableFrom(f.getType()))
			{
				LogicBehaviour behaviour = (LogicBehaviour) f.get(o);
				fw.write(starter + "<V " + f.getName() + "=\"" + behaviour.gameObject().instanceID() + "\" " + ">\n");
			}
			else fw.write(starter + "<V " + f.getName() + "=\"" + ((engine.Object) f.get(o)).Name() + "\">\n");
		}
		else if(f.getType().isPrimitive() || f.getType().getSimpleName().equals("String")) fw.write(starter + "<V " + f.getName() + "=\"" + f.get(o) + "\">\n");
		else if(f.getType() == Vector2.class)
		{
			Vector2 v = (Vector2) f.get(o);
			fw.write(starter + "<V " + f.getName() + "=\"" + v.x + " " + v.y + "\">\n");
		}
	}
	
	public static final Hierarchy hierarchy() {return h;}
	public static final Inspector inspector() {return i;}
	public static final ProjectPanel projectPanel() {return p;}
}
