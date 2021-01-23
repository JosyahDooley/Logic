package engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.BufferUtils;

public class Texture extends engine.Object
{
	//Stored info about the texture
	private int id;
	private int width;
	private int height;
	
	//Temp, iterator and list of textures variables
	private static List<Texture> textureInstances = new ArrayList<Texture>();
	private static Texture tmp = null;
	private static int i = 0;
	
	private IntBuffer w;
	private IntBuffer h;
	private IntBuffer c;
	
	//Create a texture using a file name
	public Texture(String fileName)
	{
		//Create and store width, height and channels int buffers;
		w = BufferUtils.createIntBuffer(1);
		h = BufferUtils.createIntBuffer(1);
		c = BufferUtils.createIntBuffer(1);
		
		ByteBuffer data;
		if(fileName.startsWith("/"))
		{
			String name = fileName.replaceFirst("/", "");
			Name(name.split("\\.")[0]);
			
			InputStream is = Texture.class.getResourceAsStream("/Textures/" + name);
			byte[] bytes = new byte[8000];
			int curByte = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			try {while((curByte = is.read(bytes)) != -1) bos.write(bytes, 0, curByte);}
			catch (IOException e) {e.printStackTrace();}
			
			bytes = bos.toByteArray();
			ByteBuffer buffer = (ByteBuffer)BufferUtils.createByteBuffer(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			data = stbi_load_from_memory(buffer, w, h, c, 4);
		}
		else
		{
			String[] split = fileName.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
			Name(split[split.length - 1].split("\\.")[0]);
			data = stbi_load(fileName, w, h, c, 4);
			
			f = new File(fileName);
			lastModified = f.lastModified();
		}
		
		//Generate the texture id and set the size of the texture
		id = glGenTextures();
		this.width = w.get();
		this.height = h.get();
		
		//Bind the texture
		glBindTexture(GL_TEXTURE_2D, id);
		
		//Set the min and mag filter texture parameters to nearest
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		//Set the image data into the texture using rgba then free the image data
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		stbi_image_free(data);
		
		//Add the texture to our list of textures
		textureInstances.add(this);
	}
	
	//Create a texture with a pregenerated id
	public Texture(int id, int width, int height)
	{
		//Set the information to info passed in
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	//Return the id of this texture
	public int ID() {return id;}
	
	public void Bind()
	{
		//Bind this texture into texture position 0
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	//Unbind the texture
	public void Unbind() {glBindTexture(GL_TEXTURE_2D, 0);}
	
	//Find a texture with a given name
	public static Texture Find(String textureName)
	{
		//For every texture in our list of textures
		for(i = 0; i < textureInstances.size(); i++)
		{
			//If the name starts with the texture name were wanting, return that texture
			tmp = textureInstances.get(i);
			if(tmp.Name().startsWith(textureName)) return tmp;
		}
		//If the specified texture can't be found, return null
		return null;
	}
	
	public static void RefreshAll()
	{
		for(i = 0; i < textureInstances.size(); i++)
		{
			textureInstances.get(i).Refresh();
		}
	}
	
	public void Refresh()
	{
		if(f == null) return;
		if(f.lastModified() == lastModified) return;
		lastModified = f.lastModified();
		
		w = BufferUtils.createIntBuffer(1);
		h = BufferUtils.createIntBuffer(1);
		c = BufferUtils.createIntBuffer(1);
		
		ByteBuffer data = stbi_load(f.getAbsolutePath(), w, h, c, 4);
		this.width = w.get();
		this.height = h.get();
		
		glBindTexture(GL_TEXTURE_2D, id);
		
		//Set the min and mag filter texture parameters to nearest
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		//Set the image data into the texture using rgba then free the image data
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		stbi_image_free(data);
	}
	
	//Loop through all the textures and delete them when the application closes
	public static void CleanUp()
	{
		for(i = 0; i < textureInstances.size(); i++) glDeleteTextures(textureInstances.get(i).ID());
	}
	
	//Getter methods for texture list, width and length of the texture
	public static List<Texture> GetTextures(){return textureInstances;}
	public int Width() {return width;}
	public int Height() {return height;}
}
