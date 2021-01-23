package engine;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import editor.Editor;
import editor.EditorUtil;
import gui.GUI;
import math.Color;
import math.Mathf;
import math.Matrix4x4;
import math.Rect;
import math.Vector2;

public class Renderer
{
	private static List<Map<Material, List<SpriteRenderer>>> renderLayers = new ArrayList<Map<Material, List<SpriteRenderer>>>();
	private static Mesh mesh;
	private static Matrix4x4 projection;
	
	private static FBO fbo;
	private static int layerCount = 8;
	private static int i;
	
	//Initialize the renderer
	public static void Init()
	{
		for(i = 0; i < layerCount; i++) renderLayers.add(new HashMap<Material, List<SpriteRenderer>>());
		
		//Create the vertices and uvs and generated mesh using them
		float[] verts = new float[] {0, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1};
		float[] uvs = new float[] {0, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 0};
		mesh = new Mesh(verts, uvs);
		
		//Generate an fbo that we can render to
		UpdateFBO(Application.Size());
	}
	
	public static final int LayerCount() {return layerCount;}
	
	//Add a sprite render to get rendered this frame
	public static void AddToRenderer(SpriteRenderer r)
	{
		Map<Material, List<SpriteRenderer>> batch = renderLayers.get(r.gameObject.GetLayer());
		
		//Get all the sprite renderers using the material of the passed in renderer
		List<SpriteRenderer> matRenderers = batch.get(r.sprite.material);
		
		//If that material does not exist yet or we have no renderers for that material
		if(matRenderers == null)
		{
			//Create a new list of renderers and set it to the passed in material
			matRenderers = new ArrayList<SpriteRenderer>();
			batch.put(r.sprite.material, matRenderers);
		}
		//And add the sprite renderer to the list
		matRenderers.add(r);
	}
	
	//Render the fbo in a specified rectangle
	public static void Render(Rect r, Vector2 cameraPosition)
	{
		//Bind the fbo an clear the color buffer to black
		fbo.BindFrameBuffer();
		glEnable(GL_DEPTH_TEST);
		
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1f);
		
		glClearColor(0, 1, 1, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		//Cache the applications size and half size
		float w = Application.Width();
		float h = Application.Height();
		float halfW = w * 0.5f;
		float halfH = h * 0.5f;
		
		//And create a projection matrix that is zeroed in the center of the screen with a flipped y direction
		projection = Matrix4x4.Ortho(-halfW, halfW, halfH, -halfH, -1, 1);
		
		//Bind the mesh
		mesh.Bind();
		
		List<Camera> cameras = Camera.Cameras();
		for(i = 0; i < cameras.size(); i++)
		{
			Camera c = cameras.get(i);
			c.renderLayer = (int) Mathf.Clamp((float)c.renderLayer, 0, (float)layerCount - 1);
			Map<Material, List<SpriteRenderer>> batch = renderLayers.get(c.renderLayer);
			
			//For all the materials being used
			for(Material material : batch.keySet())
			{
				//Bind the material
				material.Bind();
				
				//Set the projection and color uniforms
				material.shader.SetUniform("projection", projection);
				if(cameraPosition != null) material.shader.SetUniform("camPos", cameraPosition);
				else material.shader.SetUniform("camPos", c.gameObject().Position());
				
				material.shader.SetUniform("matColor", material.color);
				
				//Get the list of renderers belonging to that material and loop through them
				List<SpriteRenderer> renderers = batch.get(material);
				for(SpriteRenderer renderer : renderers)
				{
					//And set all the renderers uniforms and render the mesh
					renderer.SetUniforms();
					mesh.Render();
				}
				//Then unbind the material
				material.Unbind();
			}
		}
		
		//And unbind the mesh and clear the batch for the next set of batches
		mesh.Unbind();
		for(int i = 0; i < renderLayers.size(); i++)
		{
			renderLayers.get(i).clear();
		}
		
		//Draw shapes if in the editor
		if(cameraPosition != null)
		{
			EditorUtil.DrawEditorShapes();
		}
		
		//Camera cam = (Camera) GameObject.Find("Camera").GetComponent("Camera");
		//Editor.DrawCollider(cam, GameObject.Find("Hero"));
		//Editor.DrawCollider(cam, GameObject.Find("Stone"));
		//Editor.DrawCollider(cam, GameObject.Find("Dirt Hole"));
		
		//Unbind the fbo
		fbo.UnBind();
		
		//Prepare the gui, render the fbo in the specified rectangle and unbind the gui
		GUI.Prepare();
		GUI.DrawTextureWithTexCoords(fbo.Image(), r, new Rect(r.x / w, r.y / h, r.width / w, r.height / h));
		if(cameraPosition != null)
		{
			Color temp = GUI.textColor;
			GUI.textColor = Color.black;
			GUI.Label(Editor.cameraPosition.ToString(), r.x, r.y);
			GUI.textColor = temp;
		}
		GUI.Unbind();
	}
	
	public static void DrawLine(Camera c, Vector2 start, Vector2 end, Color color, Vector2 offset)
	{
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(color.r, color.g, color.b);
		
		Vector2 pos = c.gameObject().Position().Add(Application.Size().Mul(0.5f));
		if(offset != null) pos = pos.Add(offset); //editorCameraPosition
		GL11.glVertex2f(pos.x - start.x, pos.y - start.y);
		GL11.glVertex2f(pos.x - end.x, pos.y - end.y);
		
		GL11.glEnd();
	}
	
	public static void DrawLineStrip(Camera c, List<Vector2> v, Color color, Vector2 offset)
	{
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glColor3f(color.r, color.g, color.b);
		Vector2 tempOffset = c.gameObject().Position().Add(Application.Size().Mul(0.5f));
		if(offset != null) tempOffset = tempOffset.Add(offset);
		
		for(int i = 0; i < v.size(); i++)
		{
			Vector2 pos = tempOffset.Sub(v.get(i));
			GL11.glVertex2f(pos.x, pos.y);
		}
		GL11.glEnd();
	}
	
	public static void UpdateFBO(Vector2 v) {fbo = new FBO(v);}
	public static void UpdateFBO(int w, int h) {fbo = new FBO(w, h);}
}
