package gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.io.BufferedReader;
//import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import engine.Texture;
import math.Vector2;

public class Font extends engine.Object implements Cloneable
{
	private int fontID;
	private BufferedImage bufferedImage;
	private Vector2 imageSize;
	private java.awt.Font font;
	private FontMetrics fontMetrics;
	private static int i;
	private Texture texture;
	private static List<Font> fonts = new ArrayList<Font>();
	private float h;
	
	//Kind of like a library of all the characters glyphs in this font
	private Map<Character, Glyph> chars = new HashMap<Character, Glyph>();
	
	//Constructor to Create a font using a name in the fonts folder and font size
	public Font(String name, float size)
	{
		//Try to import a font from the fonts folder and set the size and store the awt font in a variable, if it fails, tell us why it failed
		if(name.startsWith("/"))
		{
			try
			{
				font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, Font.class.getResourceAsStream("/Font" + name + ".ttf")).deriveFont(size);
				name = name.replaceFirst("/", "");
			}
			catch (FontFormatException e) {e.printStackTrace();}
			catch (IOException e) {e.printStackTrace();}
		}
		else
		{
			try
			{
				font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(name + ".ttf")).deriveFont(size);
				String[] split = name.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
				name = split[split.length - 1];
			}
			catch (FontFormatException e) {e.printStackTrace();}
			catch (IOException e) {e.printStackTrace();}
		}
		Name(name);
		
		//Call to generate the font image
		GenerateFont();
	}
	
	//Constructor to create a font using an already existing awt font
	public Font(java.awt.Font font)
	{
		//Set the font and generate the texture
		this.font = font;
		Name(font.getFontName());
		GenerateFont();
	}
	
	//Generate the texture font
	private void GenerateFont()
	{
		//Get the default graphics configuration, create a translucent graphics image and set the font using the awt font variable
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Graphics2D graphics = gc.createCompatibleImage(1, 1, Transparency.TRANSLUCENT).createGraphics();
		graphics.setFont(font);
		
		//Get the font metrics for the font we passed to the graphics
		fontMetrics = graphics.getFontMetrics();
		h = (float) (fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent());
		
		//Set the image size to a power of 2 and create a buffered image of that size
		imageSize = new Vector2(1024, 1024);
		bufferedImage = graphics.getDeviceConfiguration().createCompatibleImage((int) imageSize.x, (int) imageSize.y, Transparency.TRANSLUCENT);
		
		//Generate the texture id, enable textures, bind it and set in the texture data
		fontID = glGenTextures();
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, fontID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, (int) imageSize.x, (int) imageSize.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, GenerateImage());
		
		//Then set the texture parameters for the image
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		texture = new Texture(fontID, 1024, 1024);
		
		fonts.add(this);
	}
	
	//Generate and return a byte buffer containing information about the image
	private ByteBuffer GenerateImage()
	{
		//Get the graphics, set the font and enable antialiasing
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		//Call the function to draw all characters using the graphics we cerate and return the buffer
		DrawCharacters(g);
		return CreateBuffer();
	}
	
	//Draw all the characters using a specified graphics component
	private void DrawCharacters(Graphics2D g)
	{
		//Start on the top left 
		int tempX = 0;
		int tempY = 0;
		
		//For all the characters (that we care about)
		for(i = 32; i < 256; i++)
		{
			//If this character is not one of the usable ones, skip it
			if(i == 127) continue;
			
			//Get the character in this iteration and get the characters width
			char c = (char) i;
			float charWidth = fontMetrics.charWidth(c);
			
			//Add a little padding to get rid of artifacts
			float advance = charWidth + 8;
			
			//If this character is going to be drawn outside of the screen
			if(tempX + advance > imageSize.x)
			{
				//Drop down one character and start from the left side of the image again
				tempX = 0;
				tempY += 1;
			}
			//Put this characters information in a glyph and store it. Then draw the character on the texture
			chars.put(c, new Glyph(tempX / imageSize.x, (tempY * h) / imageSize.y, charWidth / imageSize.x, h / imageSize.y, charWidth, h));
			g.drawString(String.valueOf(c), tempX, fontMetrics.getMaxAscent() + (h * tempY));
			
			//Then prepare to draw the next character by moving our x drawing position
			tempX += advance;
		}
	}
	
	//Create and return a byte buffer
	private ByteBuffer CreateBuffer()
	{
		//Store the image size values and create a buffer (int) array
		int w = (int)imageSize.x;
		int h = (int)imageSize.y;
		int[] pixels = new int[w * h];
		
		//Set the data buffer data into the image and allocate it
		bufferedImage.getRGB(0, 0, w, h, pixels, 0, w);
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(w * h * 4);
		
		//For every pixel in the image
		for(i = 0; i < pixels.length; i++)
		{
			//Set the color vaues
			byteBuffer.put((byte) ((pixels[i] >> 16) & 0xFF)); //Red
			byteBuffer.put((byte) ((pixels[i] >> 8) & 0xFF));  //Green
			byteBuffer.put((byte) (pixels[i] & 0xFF));         //Blue
			byteBuffer.put((byte) ((pixels[i] >> 24) & 0xFF)); //Alpha
		}
		
		//Flip the buffer
		byteBuffer.flip();
		
		//Then return the buffer
		return byteBuffer;
	}
	
	@Override
	public Font clone() throws CloneNotSupportedException
	{
		return (Font) super.clone();
	}
	
	public static final List<Font> Fonts() {return fonts;}
	
	//Standard getters
	public int ID() {return fontID;}
	public Texture GetTexture() {return texture;}
	public Map<Character, Glyph> GetCharacters(){return chars;}
	
	public static Font Find(String name)
	{
		for(i = 0; i < fonts.size(); i++)
		{
			if(fonts.get(i).Name().equals(name)) return fonts.get(i);
		}
		return null;
	}
	
	//Get the size of a string as if we were to draw it
	public int StringWidth(String s) {return fontMetrics.stringWidth(s);}
	public float FontHeight() {return h;}
}
