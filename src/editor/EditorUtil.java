package editor;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.lwjgl.opengl.GL11;

import engine.Application;
import engine.Camera;
import engine.GameObject;
import engine.LogicBehaviour;
import engine.Renderer;
import engine.SpriteRenderer;
import math.Color;
import math.Rect;
import math.Vector2;
import physics.AABBCollider;
import physics.BoundsCollider;
import physics.CircleCollider;
import physics.Collider;
import physics.LineCollider;

public class EditorUtil
{
	private static List<LogicBehaviour> importedClasses = new ArrayList<LogicBehaviour>();
	private static int i;
	
	public static List<LogicBehaviour> GetImportedClasses() {return importedClasses;}
	
	public static LogicBehaviour ImportClass(String path)
	{
		InputStream stream = null;
		try {stream = new FileInputStream(path);}
		catch (FileNotFoundException e) {e.printStackTrace();}
		if(stream == null) return null;
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String seperator = System.getProperty("line.separator");
		String tempProperty = System.getProperty("java.io.tmpdir");
		
		String[] name = path.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
		Path srcPath = Paths.get(tempProperty, name[name.length - 1]);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {Files.write(srcPath, reader.lines().collect(Collectors.joining(seperator)).getBytes(StandardCharsets.UTF_8));}
		catch (IOException e) {e.printStackTrace();}
		compiler.run(null, null, null, srcPath.toString());
		Path p = srcPath.getParent().resolve(name[name.length - 1].split("\\.")[0] + ".class");
		
		URL classURL = null;
		try {classURL = p.getParent().toFile().toURI().toURL();}
		catch (MalformedURLException e) {e.printStackTrace();}
		if(classURL == null) return null;
		
		URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {classURL});
		
		Class<?> myClass = null;
		try {myClass = classLoader.loadClass(name[name.length - 1].split("\\.")[0]);}
		catch(ClassNotFoundException e1) {e1.printStackTrace();}
		if(myClass == null) return null;
		
		//Return if class is not LogicBehaviour, but isn't compiling correctly
		//if(!LogicBehaviour.class.isAssignableFrom(myClass)) return;
		
		LogicBehaviour l = null;
		try{l = (LogicBehaviour) myClass.getConstructor().newInstance();}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {e.printStackTrace();}
		if(l == null) return null;
		
		l.f = new File(Editor.WorkingDirectory() + "Scripts/" + l.Name() + ".java");
		l.lastModified = l.f.lastModified();
		
		for(i = 0; i < importedClasses.size(); i++)
		{
			if(importedClasses.get(i).Name().equals(l.Name()))
			{
				importedClasses.set(i, l);
				return l;
			}
		}
		importedClasses.add(l);
		return l;
	}
	
	public static void RefreshAllScripts()
	{
		for(i = 0; i < importedClasses.size(); i++)
		{
			LogicBehaviour b = importedClasses.get(i);
			if(b.f == null) return;
			File temp = new File(b.f.getAbsolutePath());
			if(!temp.exists()) return;
			
			if(temp.lastModified() == b.lastModified) return;
			
			b = ImportClass(temp.getAbsolutePath());
			
			List<GameObject> goList = GameObject.Instances();
			for(int g = 0; g < goList.size(); g++) goList.get(g).RefreshComponent(b);
			Editor.RefreshInspected();
		}
	}
	
	public static LogicBehaviour GetBehaviour(String name)
	{
		for(i = 0; i < importedClasses.size(); i++)
		{
			if(importedClasses.get(i).Name().equals(name))
			{
				try {return importedClasses.get(i).getClass().getConstructor().newInstance();}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {e.printStackTrace();}
			}
		}
		return null;
	}
	
	public static void DrawEditorShapes()
	{
		GameObject selected = Editor.GetSelected();
		if(selected == null) return;
		List<Camera> cameras = Camera.Cameras();
		
		glDisable(GL_DEPTH_TEST);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(Application.Width(), 0, 0, Application.Height(), 1, -1);
		
		GL11.glLineWidth(1f);
		
		for(Camera camera : cameras)
		{
			if(selected.GetLayer() != camera.renderLayer) continue;
			
			SpriteRenderer sr = selected.GetRenderer();
			Collider col = selected.GetCollider();
			if(sr != null)
			{
				Vector2[] corners = sr.GetCorners();
				if(corners != null)
				{
					if(col != null)
					{
						if(col instanceof BoundsCollider)
						{
							Renderer.DrawLineStrip(camera, Rect.GetBounds(corners).GetPoints(), Color.green, Editor.cameraPosition);
							continue;
						}
						else Renderer.DrawLineStrip(camera, Rect.GetBounds(corners).GetPoints(), Color.white, Editor.cameraPosition);
					}
					else Renderer.DrawLineStrip(camera, Rect.GetBounds(corners).GetPoints(), Color.white, Editor.cameraPosition);
				}
			}
			if(col != null)
			{
				if(col instanceof CircleCollider)
				{
					CircleCollider cc = (CircleCollider) col;
					Renderer.DrawLineStrip(camera, Vector2.GetPointsAround(col.gameObject().Position().Add(cc.anchor), ((CircleCollider) col).radius, 16), Color.green, Editor.cameraPosition);
				}
				else if(col instanceof AABBCollider)
				{
					AABBCollider ac = (AABBCollider) col;
					Rect r = new Rect(col.gameObject().Position().Add(ac.anchor), ac.size);
					Renderer.DrawLineStrip(camera, r.GetPoints(), Color.green, Editor.cameraPosition);
				}
				else if(col instanceof LineCollider)
				{
					LineCollider lc = (LineCollider) col;
					Renderer.DrawLine(camera, lc.GetWorldStart(), lc.GetWorldEnd(), Color.green, Editor.cameraPosition);
				}
			}
		}
		
		GL11.glEnable(GL_DEPTH_TEST);
	}
	
	public static void CleanUp()
	{
		String path;
		File javaFile;
		File classFile;
		
		for(i = 0; i < importedClasses.size(); i++)
		{
			LogicBehaviour l = importedClasses.get(i);
			path = l.getClass().getProtectionDomain().getCodeSource().getLocation() + l.Name();
			javaFile = new File(path + ".java");
			classFile = new File(path + ".class");
			
			javaFile.delete();
			classFile.delete();
		}
	}
}
