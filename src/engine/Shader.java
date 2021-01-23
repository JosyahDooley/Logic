package engine;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.BufferUtils;

import math.Color;
import math.Matrix4x4;
import math.Vector2;

public class Shader extends engine.Object
{
	private int program;
	private int vs;
	private int fs;
	
	//Static variables to handle all of our shaders
	private static List<Shader> shaders = new ArrayList<Shader>();
	private static int i;
	
	//Constructor that takes in the name of the shader
	public Shader(String fileName)
	{
		//Create and store the shader program
		program = glCreateProgram();
		String[] shader = CreateShader(fileName);
		
		//Creates and stores the vertex shader. Then compiles it and checks for errors
		vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, shader[0]);
		glCompileShader(vs);
		if(glGetShaderi(vs, GL_COMPILE_STATUS) != 1)
		{
			System.err.println(glGetShaderInfoLog(vs));
			System.exit(1);
		}
		
		//Creates and stores the fragment shader. Then Compiles it and checks for errors
		fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, shader[1]);
		glCompileShader(fs);
		if(glGetShaderi(fs, GL_COMPILE_STATUS) != 1)
		{
			System.err.println(glGetShaderInfoLog(fs));
			System.exit(1);
		}
		
		//Attach both the fragment and vertex shaders to the program
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		
		//Binds the location of verts and uvs to the program
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "uv");
		
		//Link the program and check for errors
		glLinkProgram(program);
		if(glGetProgrami(program, GL_LINK_STATUS) != 1)
		{
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		//Validate the program and check for errors
		glValidateProgram(program);
		if(glGetProgrami(program, GL_VALIDATE_STATUS) != 1)
		{
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		//And add this shader to the list
		shaders.add(this);
	}
	
	//Create a shader from a specified filename
	private String[] CreateShader(String fileName)
	{
		//Create a stringbuilder and buffered reader
		StringBuilder sb = new StringBuilder();
		BufferedReader br;
		
		try
		{
			//Grab the shader file and store it in the buffered reader
			if(fileName.startsWith("/"))
			{
				InputStreamReader isr = new InputStreamReader(Shader.class.getResourceAsStream("/Shaders" + fileName + ".Shader"));
				br = new BufferedReader(isr);
				Name(fileName.replaceFirst("/", ""));
			}
			else
			{
				f = new File(fileName + ".Shader");
				lastModified = f.lastModified();
				br = new BufferedReader(new FileReader(f));
				String[] split = fileName.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
				Name(split[split.length - 1]);
			}
			
			//Create a line for reading
			String line;
			
			//Read the line and if it isn't null
			while((line = br.readLine()) != null)
			{
				//append the line and a return
				sb.append(line);
				sb.append("\n");
			}
			
			//Close the buffered reader after it's done reading
			br.close();
		}
		catch(IOException e)
		{
			//If reading the file fails, print the stack trace
			e.printStackTrace();
		}
		
		//Then return the stringbuilder
		return sb.toString().split("ENDVERTEX");
	}
	
	//Get a shader by name
	public static Shader Find(String name)
	{
		//For all the shaders
		for(i = 0; i < shaders.size(); i++)
		{
			//If we have came across the shader were looking for, return it
			if(shaders.get(i).Name().equals(name)) return shaders.get(i);
		}
			
		//If we didn't find a shader by that name, return null
		return null;
	}
	
	public static void RefreshAll()
	{
		for(i = 0; i < shaders.size(); i++)
		{
			shaders.get(i).Refresh();
		}
	}
	
	private void Refresh()
	{
		if(f == null) return;
		if(f.lastModified() == lastModified) return;
		lastModified = f.lastModified();
		
		glDeleteProgram(program);
		program = glCreateProgram();
		String[] shader = CreateShader(f.getAbsolutePath().split("\\.")[0]);
		
		//Creates and stores the vertex shader. Then compiles it and checks for errors
		vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, shader[0]);
		glCompileShader(vs);
		if(glGetShaderi(vs, GL_COMPILE_STATUS) != 1)
		{
			System.err.println(glGetShaderInfoLog(vs));
			System.exit(1);
		}
		
		//Creates and stores the fragment shader. Then Compiles it and checks for errors
		fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, shader[1]);
		glCompileShader(fs);
		if(glGetShaderi(fs, GL_COMPILE_STATUS) != 1)
		{
			System.err.println(glGetShaderInfoLog(fs));
			System.exit(1);
		}
		
		//Attach both the fragment and vertex shaders to the program
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		
		//Binds the location of verts and uvs to the program
		glBindAttribLocation(program, 0, "vertices");
		glBindAttribLocation(program, 1, "uv");
		
		//Link the program and check for errors
		glLinkProgram(program);
		if(glGetProgrami(program, GL_LINK_STATUS) != 1)
		{
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		//Validate the program and check for errors
		glValidateProgram(program);
		if(glGetProgrami(program, GL_VALIDATE_STATUS) != 1)
		{
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
	}
	
	//Set afloat uniform using the of the uniform and it's 1 value
	public void SetUniform(String name, float v)
	{
		//Get the location of the uniform and if it exists, pass in the values
		int location = glGetUniformLocation(program, name);
		if(location != -1) glUniform1f(location, v);
	}
	
	//Set a vector2 uniform using the name of the uniform and its 2 values
	public void SetUniform(String name, Vector2 v) {SetUniform(name, v.x, v.y);}
	public void SetUniform(String name, float x, float y)
	{
		//Get the location of the uniform and if it exists, pass in the values
		int location = glGetUniformLocation(program, name);
		if(location != -1) glUniform2f(location, x, y);
	}
	
	//Set a vector4 uniform using the name of the uniform and its 4 values
	public void SetUniform(String name, float x, float y, float z, float w)
	{
		//Get the location of the uniform and if it exists, pass in the values
		int location = glGetUniformLocation(program, name);
		if(location != -1) glUniform4f(location, x, y, z, w);
	}
	
	//Set a color uniform using the name of the uniform and the color you want it to be
	public void SetUniform(String name, Color c)
	{
		//Get the location of the uniform and if it exists, pass in the values
		int location = glGetUniformLocation(program, name);
		if(location != -1) glUniform4f(location, c.r, c.g, c.b, c.a);
	}
	
	//Set a matrix uniform using the name of the uniform and the matrix you want to use
	public void SetUniform(String name, Matrix4x4 m)
	{
		//Get the location of the uniform, create a 16 float buffer, set the data in the buffer and pass the data to the shader
		int location = glGetUniformLocation(program, name);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		m.GetBuffer(buffer);
		if(location != -1) glUniformMatrix4fv(location, false, buffer); buffer.flip();
	}
	
	public void Bind()
	{
		//Bind this shader program
		glUseProgram(program);
	}
	
	public void Unbind()
	{
		//Unbind it by setting it to 0
		glUseProgram(0);
	}
	
	public static final List<Shader> Shaders() {return shaders;}
}
