package gui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import editor.Editor;
import engine.Application;
import engine.Debug;
import engine.Dialog;
import engine.GameObject;
import engine.LogicBehaviour;
import engine.Mesh;
import engine.Shader;
import engine.Texture;
import input.Mouse;
import math.Color;
import math.Matrix4x4;
import math.Rect;
import math.Vector2;

public class GUI
{
	public static Color backgroundColor = Color.white;
	public static Color textColor = Color.white;
	public static Font font;
	public static GUISkin skin;
	
	private static Mesh mesh;
	private static Shader shader;
	private static Matrix4x4 ortho;
	
	private static int i;
	private static char[] c;
	private static float xTemp;
	private static int boundTex = -1;
	private static Color boundColor = Color.white;
	private static Popup popup;
	public static byte checkDrag;
	private static boolean ignoreMouseUp = false;
	
	private static int area = 0;
	private static List<GUIArea> areas = new ArrayList<GUIArea>();
	private static Vector2 clickPosition = new Vector2();
	
	public static void Init()
	{
		//Generate mesh information that has inverted uv's
		float[] meshData = new float[] {0, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1};
		
		//Create a mesh using the mesh data we created
		mesh = new Mesh(meshData, meshData);
		
		//Set the shader, skin and font we will be using
		shader = Shader.Find("DefaultShader");
		skin = GUISkin.GetSkin("DefaultGUI");
		font = new Font(new java.awt.Font("TimesRoman", java.awt.Font.PLAIN, 16));
	}
	
	//Prepare the gui for rendering
	public static void Prepare()
	{
		//Clear the areas, and the screen area and set the current area to the screen size
		areas.clear();
		//areas.add(new GUIArea(Application.GetRect());
		areas.add(new GUIArea(Application.GetRect(), Application.GetRect()));
		area = 0;
		
		//Disable depth, pass create and pass in the ortho matrix and bg color and bind the mesh
		glDisable(GL_DEPTH_TEST);
		shader.Bind();
		shader.SetUniform("matColor", backgroundColor);
		ortho = Matrix4x4.Ortho(0, Application.Width(), Application.Height(), 0, -1, 1);
		shader.SetUniform("projection", ortho);
		mesh.Bind();
		
		if(Mouse.GetButtonDown(0)) clickPosition.Set(Mouse.Position());
	}
	
	public static boolean HasPopup() {return popup != null;}
	public static void SetPopup(Rect nameRect, List<String> list, Consumer<String> func, int[] selectedItems) {popup = new Popup(nameRect, list, func, selectedItems); ignoreMouseUp = true;}
	public static void CancelPopup() {ignoreMouseUp = false; popup = null;}
	public static void DrawPopup() {if(popup != null) {ignoreMouseUp = false; popup = popup.Draw();}}
	
	//Create a vector field with a name and box
	public static Rect VectorField(Rect r, String name, Rect r2, float padding)
	{
		//Display the name of the field in an area so it wont be too long
		Label(name, r.x, r.y);
		
		
		//Label is correct but the vector field is incorrect
		//Look in the previous version to see the differences of the rectangle
		
		//Get a quarter of the total render area and render a float field for x and y at those locations
		float half = (r.width - padding) / 2.0f;
		float halfH = r.height / 2.0f;
		float x = FloatField(new Rect(r.x + padding, r.y, half, halfH), "x", r2.x, 10);
		float y = FloatField(new Rect(r.x + padding + half, r.y, half, halfH), "y", r2.y, 10);
		float w = FloatField(new Rect(r.x + padding, r.y + halfH, half, halfH), "w", r2.width, 10);
		float h = FloatField(new Rect(r.x + padding + half, r.y + halfH, half, halfH), "h", r2.height, 10);
		
		//And return the new vector
		return new Rect(x, y, w, h);
	}
	
	//Create a vector field with a name and box
	public static Vector2 VectorField(Rect r, String name, Vector2 v, float padding)
	{
		//Display the name of the field in an area so it wont be too long
		Label(name, r.x, r.y);
		
		//Get a quarter of the total render area and render a float field for x and y at those locations
		float half = (r.width - padding) / 2.0f;
		float x = FloatField(new Rect(r.x + padding, r.y, half, r.height), "x", v.x, 10);
		float y = FloatField(new Rect(r.x + padding + half, r.y, half, r.height), "y", v.y, 10);
		
		//And return the new vector
		return new Vector2(x, y);
	}
	
	
	//Create a float field with a name and box
	public static float FloatField(Rect r, String name, float v, float padding)
	{
		//Get the return value of the text field
		String ret = GUI.TextField(r, name, String.valueOf(v), padding);
		
		//Try to cast it to a float
		float f = v;
		try{f = Float.parseFloat(ret);}
		catch(NumberFormatException e) {Debug.Log("Input is not in a number format! Rejecting value.");}
		
		//And return whatever value we end up with
		return f;
	}
	
	//Create a text field with a name and box
	public static String TextField(Rect r, String name, String v, float padding)
	{
		//Display the name of the field in an area so it wont be too long
		Label(name, r.x, r.y);
		
		//If we click on the input box
		//This is temporary, will change in later episodes
		if(Button(v, new Rect(r.x + padding, r.y, r.width - padding, r.height), "Box", "Box"))
		{
			//Show an input box and give us a return value
			String s = Dialog.InputDialog("Changing " + name + "!", v);
			if(s != null) return s.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", "");
		}
		//And return the value we end up with
		return v;
	}
	
	public static engine.Object ObjectField(Rect r, String name, engine.Object o, Class<?> type, float padding)
	{
		//Get the return value of the text field
		String ret = "";
		if(o != null) ret = o.Name();
		ret = GUI.TextField(r, name, ret, padding);
		
		GUIArea a = areas.get(area);
		Rect rf = r.AddPosition(a.culled);
		rf.y -= a.Scroll();
		
		if(Mouse.GetButtonUp(0) && rf.Contains(Mouse.Position()) && type != null)
		{
			engine.Object dragged = Editor.DraggedObject();
			if(dragged != null)
			{
				if(dragged.getClass() == type) return dragged; 
				else if(dragged.getClass() == GameObject.class && type == LogicBehaviour.class)
				{
					//Work in progress for setting components
				}
				
			}
		}
		
		return o;
	}
	
	//Button function using string values
	public static boolean Button(String text, Rect r, String normalStyle, String hoverStyle)
	{
		//Return the return value from the other button function using the styles aquired from the strings passed in
		return Button(text, r, skin.Get(normalStyle), skin.Get(hoverStyle));
	}
	//Button function using style variables
	public static boolean Button(String text, Rect r, GUIStyle normalStyle, GUIStyle hoverStyle)
	{
		GUIArea a = areas.get(area);
		Rect rf = r.AddPosition(a.actual); //Issue was here. I put a.culled instead of a.actual
		rf.y -= a.Scroll();
		rf = a.culled.GetIntersection(rf);
		
		if(rf == null) {if(text.equals("New")) System.out.println("Culled"); return false;}
		else if(text.equals("New")) System.out.println(rf.ToString());
		
		//If the button contains the mouse position
		if(rf.Contains(Mouse.Position()))
		{
			//Draw the hover box style
			Rect p = Box(r, hoverStyle);
			
			//If the box fails, draw the text at the start of the rect, else, draw it at the start of the center of the style
			if(p != null) Label(text, p.x, p.y);
			else Label(text, r.x, r.y);
			
			//If we left clicked, return true
			if(!ignoreMouseUp)
			{
				if(Mouse.GetButtonDown(0)) checkDrag = 1;
				else if(Mouse.GetButtonUp(0))
				{
					if(rf.Contains(clickPosition)) return true;
				}
			}
		}
		else
		{
			//If the button doesn't contain the mouse, draw the normal box style
			Rect p = Box(r, normalStyle);
			
			//If the box fails, draw the text at the start of the rect, else, draw it at the start of the center of the style
			if(p != null) Label(text, p.x, p.y);
			else Label(text, r.x, r.y);
		}
		
		//If mouse position is not inside the rect and the mouse was not clicked, return false
		return false;
	}
	
	public static boolean CenteredButton(String text, Rect r, String normalStyle, String hoverStyle)
	{
		return CenteredButton(text, r, skin.Get(normalStyle), skin.Get(hoverStyle));
	}
	public static boolean CenteredButton(String text, Rect r, GUIStyle normalStyle, GUIStyle hoverStyle)
	{
		GUIArea a = areas.get(area);
		Rect rf = r.AddPosition(a.actual);
		rf.y -= a.Scroll();
		rf = a.culled.GetIntersection(rf);
		if(rf == null) return false;
		
		float x = r.x + ((r.width / 2f) - ((float)font.StringWidth(text) / 2f));
		float y = r.y + ((r.height / 2f) - (font.FontHeight() / 2f));
			
		//If the button contains the mouse position
		if(rf.Contains(Mouse.Position()))
		{
			//Draw the hover box style
			Box(r, hoverStyle);
			Label(text, x, y);
				
			//If we left clicked, return true
			if(!ignoreMouseUp)
			{
				if(Mouse.GetButtonDown(0)) checkDrag = 1;
				else if(Mouse.GetButtonUp(0) && !ignoreMouseUp)
				{
					if(rf.Contains(clickPosition)) return true;
				}
			}
		}
		else
		{
			//If the button doesn't contain the mouse, draw the normal box style
			Box(r, normalStyle);
			Label(text, x, y);
		}
			
		//If mouse position is not inside the rect and the mouse was not clicked, return false
		return false;
	}
	
	//Create a check box toggle with label
	public static boolean Toggle(boolean b, Rect r, String name, GUIStyle on, GUIStyle off)
	{
		//Begin an area for culling and draw the label
		BeginArea(new Rect(r.x, r.y, r.width - 15, r.height));
		Label(name, 0, 0);
		EndArea();
		
		//Return a normal toggle
		return Toggle(b, r.x + r.width - 15, r.y, on, off);
	}
	
	//Create a check box toggle using a rect
	public static boolean Toggle(boolean b, Rect r, GUIStyle on, GUIStyle off)
	{
		//Set the style based on whether it's true or false
		GUIStyle s;
		if(b) s = on;
		else s = off;
		
		//If we click on it, return the opposite value
		if(GUI.Button("", r, s, s)) return !b;
		
		//Else, return the same value we had
		return b;
	}
	
	//Create a check box toggle
	public static boolean Toggle(boolean b, float x, float y, GUIStyle on, GUIStyle off)
	{
		//Set the style based on whether it's true or false
		GUIStyle s;
		if(b) s = on;
		else s = off;
		
		//If we click on it, return the opposite value
		if(GUI.Button("", new Rect(x, y, 15, 15), s, s)) return !b;
		
		//Else, return the same value we had
		return b;
	}
	
	//Draw a box and return the center rectangle
	public static Rect Box(Rect r, String style) {return Box(r, skin.Get(style));}
	public static Rect Box(Rect r, GUIStyle e)
	{
		//If there is no style, return null because it needs a style to return the center of it
		if(e == null) return null;
		
		//Cache a short variable for the texture, just so we only have to type a character anytime we use it
		Texture t = skin.texture;
		
		//Get the top left corner of the box using corresponding padding values and draw it using a texture drawing method
		Rect tl = new Rect(r.x, r.y, e.padding.x, e.padding.y);
		Rect tlu = new Rect(e.uv.x, e.uv.y, e.paddingUV.x, e.paddingUV.y);
		DrawTextureWithTexCoords(t, tl, tlu);
		
		//Get the top right corner of the box using corresponding padding values and draw it using a texture drawing method
		Rect tr = new Rect((r.x + r.width) - e.padding.width, r.y, e.padding.width, e.padding.y);
		Rect tru = new Rect((e.uv.x + e.uv.width) - e.paddingUV.width, e.uv.y, e.paddingUV.width, e.paddingUV.y);
		DrawTextureWithTexCoords(t, tr, tru);
		
		//Get the bottom left corner of the box using corresponding padding values and draw it using a texture drawing method
		Rect bl = new Rect(r.x, (r.y + r.height) - e.padding.height, e.padding.x, e.padding.height);
		Rect blu = new Rect(e.uv.x, (e.uv.y + e.uv.height) - e.paddingUV.height, e.paddingUV.x, e.paddingUV.height);
		DrawTextureWithTexCoords(t, bl, blu);
		
		//Get the bottom right corner of the box using corresponding padding values and draw it using a texture drawing method
		Rect br = new Rect(tr.x, bl.y, e.padding.width, e.padding.height);
		Rect bru = new Rect(tru.x, blu.y, e.paddingUV.width, e.paddingUV.height);
		DrawTextureWithTexCoords(t, br, bru);
		
		//Get the left side of the box using corresponding padding values and draw it using a texture drawing method
		Rect l = new Rect(r.x, r.y + e.padding.y, e.padding.x, r.height - (e.padding.y + e.padding.height));
		Rect lu = new Rect(e.uv.x, e.uv.y + e.paddingUV.y, e.paddingUV.x, e.uv.height - (e.paddingUV.y + e.paddingUV.height));
		DrawTextureWithTexCoords(t, l, lu);
		
		//Get the right side of the box using corresponding padding values and draw it using a texture drawing method
		Rect ri = new Rect(tr.x, r.y + e.padding.y, e.padding.width, l.height);
		Rect ru = new Rect(tru.x, lu.y, e.paddingUV.width, lu.height);
		DrawTextureWithTexCoords(t, ri, ru);
		
		//Get the top of the box using corresponding padding values and draw it using a texture drawing method
		Rect ti = new Rect(r.x + e.padding.x, r.y, r.width - (e.padding.x + e.padding.width), e.padding.y);
		Rect tu = new Rect(e.uv.x + e.paddingUV.x, e.uv.y, e.uv.width - (e.paddingUV.x + e.paddingUV.width), e.paddingUV.y);
		DrawTextureWithTexCoords(t, ti, tu);
		
		//Get the bottom of the box using corresponding padding values and draw it using a texture drawing method
		Rect b = new Rect(ti.x, bl.y, ti.width, e.padding.height);
		Rect bu = new Rect(tu.x, blu.y, tu.width, e.paddingUV.height);
		DrawTextureWithTexCoords(t, b, bu);
		
		//Get the center of the box using corresponding padding values and draw it using a texture drawing method
		Rect c = new Rect(ti.x, l.y, ti.width, l.height);
		Rect cu = new Rect(tu.x, lu.y, tu.width, lu.height);
		DrawTextureWithTexCoords(t, c, cu);
		
		//Return the center rectangle
		return c;
	}
	
	//A standard text label
	public static void Label(String text, Vector2 v) {Label(text, v.x, v.y);}
	public static void Label(String text, float x, float y)
	{
		//Bind the font image, get our font characters and set the text starting position
		Map<Character, Glyph> chars = font.GetCharacters();
		xTemp = x;
		
		//Convert the text to an array of characters, set the color uniform set our color bind variable
		c = text.toCharArray();
		
		//For all the characters in the text
		for(i = 0; i < c.length; i++)
		{
			//Get the glyph information for this character
			Glyph r = chars.get(c[i]);
			
			if(r != null)
			{
				DrawTextureWithTexCoords(font.GetTexture(), new Rect(xTemp, y, r.scaleX, r.scaleY), new Rect(r.x, r.y, r.w, r.h), textColor);
				
				//And offset our x placement for the next character
				xTemp += r.scaleX;
			}
		}
	}
	
	//Draw a full sized texture of a given scale at a given position
	public static void DrawTexture(Texture tex, Rect r) {DrawTextureWithTexCoords(tex, r, new Rect(0, 0, 1, 1));}
	
	public static void DrawTextureWithTexCoords(Texture tex, Rect drawRect, Rect uv) {DrawTextureWithTexCoords(tex, drawRect, uv, backgroundColor);}
	
	//Draw a texture of a given scale at a given position with a given uv coordinate
	public static void DrawTextureWithTexCoords(Texture tex, Rect drawRect, Rect uv, Color c)
	{
		//If we have an area, get the intersection between this rect and the current rect
		//if(area == null) return;
		//Rect r = area.GetIntersection(new Rect(drawRect.x + area.x, drawRect.y + area.y, drawRect.width, drawRect.height));
		GUIArea a = areas.get(area);
		if(a.culled == null) return;
		//Rect r = a.area.GetIntersection(new Rect(drawRect.x + a.area.x, (drawRect.y + a.area.y) - a.Scroll(), drawRect.width, drawRect.height));
		Rect r = a.culled.GetIntersection(new Rect(drawRect.x + a.actual.x, (drawRect.y + a.actual.y) - a.Scroll(), drawRect.width, drawRect.height));
		
		//If the rect isn't visible, return
		if(r == null) return;
		
		//Calculate the x and y positions and uv offset by cropping it out
		float x = uv.x + ((((r.x - drawRect.x) - a.actual.x) / drawRect.width) * uv.width);
		float y = uv.y + ((((r.y - drawRect.y) - (a.actual.y - a.Scroll())) / drawRect.height) * uv.height);
		Rect u = new Rect(x, y, (r.width / drawRect.width) * uv.width, (r.height / drawRect.height) * uv.height); 
		
		//Bind the texture
		if(tex.ID() != boundTex) glBindTexture(GL_TEXTURE_2D, tex.ID());
		
		//Set the uv, postion, scale and color variables in the shader and render the mesh
		shader.SetUniform("offset", u.x, u.y, u.width, u.height);
		shader.SetUniform("pixelScale", r.width, r.height);
		shader.SetUniform("screenPos", r.x, r.y);
		
		//If the color we passed in is not the same as the color that is bound
		if(!boundColor.Compare(c))
		{
			//Set the color uniform and tell our color binding variable that we are using this color
			shader.SetUniform("matColor", c);
			boundColor = c;
		}
		
		//Then render the mesh
		mesh.Render();
	}
	
	//Draw a window gui element
	public static void Window(Rect r, String title, Consumer<Rect> f, String style) {Window(r, title, f, skin.Get(style));}
	public static void Window(Rect r, String title, Consumer<Rect> f, GUIStyle style)
	{
		Rect center = r;
		
		//If we have a style
		if(style != null)
		{
			//Draw the style, place a label on the top of it and begin a drawing area in the center
			center = Box(r, style);
			Label(title, r.x + style.padding.x, r.y + 4);
			BeginArea(center);
		}
		else
		{
			//If we don't have a style, draw a label and begin a drawing area using the passed in value
			Label(title, r.x, r.y);
			BeginArea(r);
		}
		
		//Call the callback function and pass in the index of this window
		f.accept(center);
		
		//End the drawing area
		EndArea();
	}
	
	public static int SetScrollView(int scrollHeight, int offset)
	{
		GUIArea a = areas.get(area);
		a.scrollHeight = scrollHeight;
		return a.Scroll(offset);
	}
	
	//Begin and end a drawing area
	public static void BeginArea(Rect r)
	{
		GUIArea a = areas.get(area);
		//areas.add(new GUIArea(a.area.GetIntersection(new Rect(a.area.x + r.x, (a.area.y + r.y) - a.Scroll(), r.width, r.height))));
		Rect actual = new Rect(a.actual.x + r.x, (a.actual.y + r.y) - a.Scroll(), r.width, r.height);
		areas.add(new GUIArea(a.culled.GetIntersection(actual), actual));
		area = areas.size() - 1;
	}
	public static void EndArea()
	{
		if(areas.size() == 1) return;
		areas.remove(areas.size() - 1);
		area = areas.size() - 1;
	}
	
	//Unbind the mesh
	public static void Unbind() {mesh.Unbind(); shader.Unbind();}
}
