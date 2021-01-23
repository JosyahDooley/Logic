package engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Mesh
{
	private int v_id;
	private int u_id;
	private int vao;
	
	private static List<Mesh> meshList = new ArrayList<Mesh>();
	
	//Creates a mesh using specified verts and uv's
	public Mesh(float[] vertices, float[] uvs)
	{
		//Create and bind the vertex array object
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		
		//Generate a buffer for the vertex pointer, set the size and unbind the buffer
		v_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, v_id);
		glBufferData(GL_ARRAY_BUFFER, CreateBuffer(vertices), GL_STATIC_DRAW);
		glVertexAttribPointer(0, vertices.length/3, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		//Generate a buffer for the uv pointer and unbind the buffer
		u_id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, u_id);
		glBufferData(GL_ARRAY_BUFFER, CreateBuffer(uvs), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		//Add this mesh to our list of meshes
		meshList.add(this);
	}
	
	public void Render()
	{
		//Draw the mesh using triangles starting for 0 and going to six (the amount of vertices were using)
		glDrawArrays(GL_TRIANGLES, 0, 6);
	}
	
	public void Bind()
	{
		//Bind the vao and enable the vertex and uv attributes
		GL30.glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		//Bind the buffer for vertices and point to the attribute
		glBindBuffer(GL_ARRAY_BUFFER, v_id);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		
		//Bind the buffer for uv's and point to the attribute
		glBindBuffer(GL_ARRAY_BUFFER, u_id);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
	}
	
	public void Unbind()
	{
		//Unbind the the buffer, disables the attributes and unbind the vertices array
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	
	//Create a buffer using an array of floats
	public FloatBuffer CreateBuffer(float[] data)
	{
		//Create a buffer the size of the data past. Put the data into the buffer, flip it and return it
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	//Local function to delete vaos and vbos
	public void CleanUp()
	{
		//Delete the vertex array object and delete the vertices buffer
		GL30.glDeleteVertexArrays(vao);
		GL15.glDeleteBuffers(v_id);;
	}
	
	//Static function to clean up all meshes
	public static void CleanAllMesh()
	{
		//For every mesh, call the local cleanup on it
		for(int i = 0; i < meshList.size(); i++) meshList.get(i).CleanUp();
	}
}
