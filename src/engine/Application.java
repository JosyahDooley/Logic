package engine;

import static org.lwjgl.opengl.GL11.glClearColor;

import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import editor.EditorUtil;
import gui.GUISkin;
import gui.Sprite;
import input.Input;
import input.Mouse;
import math.Rect;
import math.Vector2;

public class Application
{
	//Window specific variables
	public static String name = "Logic Engine";
	
	private static Vector2 size = new Vector2(1200, 600);
	private static long window;
	private static Rect r = new Rect();
	private static byte minimized = 0;
	private static byte initialized = 0;
	
	private static GLFWWindowSizeCallback windowSizeCallback;
	private static GLFWWindowFocusCallback windowFocusCallback;
	
	//Initialization function
	public static long Init()
	{
		//If glfw cannot be initialized
		if(!glfwInit())
		{
			//Print an error to the console and close the application
			System.err.println("GLFW Failed to Initialize");
			System.exit(1);
		}
		
		initialized = 1;
		r.SetSize(size);
		
		//Create the window, show it and make the context current
		window = glfwCreateWindow((int) size.x, (int) size.y, name, 0, 0);
		glfwShowWindow(window);
		glfwMakeContextCurrent(window);
		
		//Create capabilities and set the background color
		GL.createCapabilities();
		glClearColor(0, 0, 0, 1);
		
		glfwSetMouseButtonCallback(window, new Mouse());
		glfwSetKeyCallback(window, new Input());
		
		windowSizeCallback = GLFWWindowSizeCallback.create(Application::OnWindowResized);
		glfwSetWindowSizeCallback(window, windowSizeCallback);
		
		windowFocusCallback = GLFWWindowFocusCallback.create(Application::OnWindowChangedFocus);
		glfwSetWindowFocusCallback(window, windowFocusCallback);
		
		//Return the window
		return window;
	}
	
	private static void OnWindowChangedFocus(long win, boolean focused)
	{
		if(focused)
		{
			EditorUtil.RefreshAllScripts();
			Sprite.RefreshAll();
			GUISkin.RefreshAll();
			Material.RefreshAll();
			Texture.RefreshAll();
			Shader.RefreshAll();
		}
	}
	
	public static void SetWindowSize(int w, int h) {if(initialized == 1) return; size.Set(w, h);}
	public static void SetWindowSize(long win, int w, int h) {glfwSetWindowSize(win, w, h);}
	
	public static void OnWindowResized(long win, int w, int h)
	{
		if(w == 0 && h == 0) minimized = 1;
		else minimized = 0;
		size.Set(w, h);
		r.Set(0, 0, w, h);
		ProjectSettings.previousAppSize.Set(w, h);
		GL11.glViewport(0, 0, w, h);
		
		Renderer.UpdateFBO(w, h);
	}
	public static boolean IsMinimized() {return minimized == 1;}
	
	public static final Vector2 Size() {return size;}
	public static final float Width() {return size.x;}
	public static final float Height() {return size.y;}
	public static Rect GetRect() {return r;}
	
	public static long Window() {return window;}
}
