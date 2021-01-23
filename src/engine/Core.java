package engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

//import org.lwjgl.openal.ALC;

//import audio.AudioClip;
import editor.Editor;
import editor.EditorUtil;
import editor.EngineBootLoader;
import gui.GUI;
import input.Input;
import input.Mouse;
import math.Color;
import math.Rect;
import math.Vector2;
import sound.AudioClip;
import sound.AudioSource;

public class Core
{
	public static void main(String[] args)
	{
		Editor.InitConfig();
		
		InputStream is = Core.class.getResourceAsStream("/Project.Settings");
		if(is == null) EngineBootLoader.Init();
		else
		{
			ProjectSettings.Load(new BufferedReader(new InputStreamReader(is)), false);
		}
		
		//Create the window
		long window = Application.Init();
		
		//Audio initialization
		long device = ALC10.alcOpenDevice((ByteBuffer) null);
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		long context = ALC10.alcCreateContext(device, (IntBuffer) null);
		ALC10.alcMakeContextCurrent(context);
		AL.createCapabilities(deviceCaps);
		
		Color clear = Color.black;
		
		AssetDatabase.LoadAllResources();
		
		//Initialize the GUI
		GUI.Init();
		Editor.Init();
		Renderer.Init();
		Time.Init();
		
		SceneManager.LoadScene("New Scene");
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		//While the application should not be closed
		while(!glfwWindowShouldClose(window))
		{
			//At the start, poll events
			Mouse.Reset();
			Input.Reset();
			glfwPollEvents();
			Time.Process();
			
			if(!Application.IsMinimized())
			{
				//Before drawing, clear what's been drawn previously and set the background color
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				if(ProjectSettings.isEditor) Editor.Render();
				GameObject.PrepareObjects();
				
				if(!ProjectSettings.isEditor)
				{
					Renderer.Render(new Rect(0, 0, Application.Width(), Application.Height()), null);
				}
				else if(Editor.IsPlaying())
				{
					Renderer.Render(new Rect(0, 30, Application.Width(), Application.Height() - 60), null);
				}
				else
				{
					Renderer.Render(Editor.GetSceneDrawArea(), Editor.cameraPosition);
				}
				
				glClearColor(clear.r, clear.g, clear.b, 1);
				
				GUI.Prepare();
				GUI.Label("FPS: " + String.valueOf(Time.FrameRate()), new Vector2(Application.Width() - 65, 3));
				GUI.DrawPopup();
				engine.Object dragged = Editor.DraggedObject();
				if(dragged != null) Editor.DrawDragged();
				GUI.Unbind();
			}
			GUI.checkDrag = 0;
			
			//At the very end, swap the buffers
			glfwSwapBuffers(window);
		}
		
		if(ProjectSettings.isEditor) Editor.SaveConfig(new File(Editor.workspaceDirectory + "config.properties"));
		
		//Cleanup the memory
		AudioClip.CleanUp();
		AudioSource.CleanUp();
		Texture.CleanUp();
		Mesh.CleanAllMesh();
		EditorUtil.CleanUp();
		ALC10.alcCloseDevice(device);
		
		//Destroy the window
		glfwTerminate();
	}
}
