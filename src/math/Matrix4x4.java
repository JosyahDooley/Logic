package math;

import java.nio.FloatBuffer;

public class Matrix4x4
{
	//A private 2D float array that stores our 4 float by 4 float matrix
	private float[][] m = new float[4][4];
	private boolean dirty = false;
	
	//Constructor with no parameters and simple calls our defaulting method
	public Matrix4x4()
	{
		SetIdentity();
	}
	
	public boolean isDirty() {return dirty;}
	public void setDirty(boolean dirty) {this.dirty = dirty;}
	
	//Set the matrix to its default values
	public final void SetIdentity()
	{
		m[0][0] = 1; m[0][1] = 0; m[0][2] = 0; m[0][3] = 0;
		m[1][0] = 0; m[1][1] = 1; m[1][2] = 0; m[1][3] = 0;
		m[2][0] = 0; m[2][1] = 0; m[2][2] = 1; m[2][3] = 0;
		m[3][0] = 0; m[3][1] = 0; m[3][2] = 0; m[3][3] = 1;
	}
	
	//Receive a buffer and put in the values of the matrix in order
	public void GetBuffer(FloatBuffer buffer)
	{
		buffer.put(m[0][0]).put(m[0][1]).put(m[0][2]).put(m[0][3]);
		buffer.put(m[1][0]).put(m[1][1]).put(m[1][2]).put(m[1][3]);
		buffer.put(m[2][0]).put(m[2][1]).put(m[2][2]).put(m[2][3]);
		buffer.put(m[3][0]).put(m[3][1]).put(m[3][2]).put(m[3][3]);
		buffer.flip();
	}
	
	public void SetTransformation(Vector2 p, float r, Vector2 s)
	{
		float radians = (float)Math.toRadians(r);
		float cos = (float)Math.cos(radians);
		float sin = (float)Math.sin(radians);
		
		m[0][0] = cos * s.x; m[0][1] = sin * s.y; 
		m[1][0] = -sin * s.x; m[1][1] = cos * s.y; 
		if(p != null) {m[0][3] = p.x; m[1][3] = p.y;}
	}
	
	public Vector2 TransformPoint(Vector2 v)
	{
		Vector2 p = new Vector2();
		p.x = m[0][3] + (m[0][0] * v.x + -m[1][0] * v.y);
		p.y = m[1][3] + (-m[0][1] * v.x + m[1][1] * v.y);
		return p;
	}
	
	//Create and return an orthographic projection matrix based on custom values that are past in
	public static Matrix4x4 Ortho(float left, float right, float bottom, float top, float near, float far)
	{
		//Create a new matrix
		Matrix4x4 matrix = new Matrix4x4();
		
		//Calculate and store the size of our 3d viewport
		float width = right - left;
		float height = top - bottom;
		float depth = far - near;
		
		//Set the data into the matrix
		matrix.m[0][0] = 2f / width;
		matrix.m[1][1] = 2f / height;
		matrix.m[2][2] = -2f / depth;
		matrix.m[3][0] = -(right + left) / width;
		matrix.m[3][1] = -(top + bottom) / height;
		matrix.m[3][2] = -(far + near) / depth;
		
		//Then return the matrix
		return matrix;
	}
}
