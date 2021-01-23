package editor;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BehaviourAttributes
{
	public int something;
	public Class<?> c;
	public List<BehaviourField> fields = new ArrayList<BehaviourField>();
	
	public int height;
	
	public BehaviourAttributes(engine.Object o)
	{
		c = o.getClass();
		
		try {SetClass(o, 0);}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		height -= 2;
	}
	
	private void SetClass(engine.Object o, int offset) throws IllegalArgumentException, IllegalAccessException
	{
		Field[] fields = o.getClass().getFields();
		for(int i = 0; i < fields.length; i++)
		{
			Field f = fields[i];
			if(f.getName().equals("enabled") || f.getName().equals("expanded") || f.getName().equals("f") || f.getName().equals("lastModified")) continue;
			
			this.fields.add(new BehaviourField(o, f, offset));
			if(f.getType().isArray())
			{
				Class<?> c = f.get(o).getClass();
				Class<?> type = c.getComponentType();
				if(c.getComponentType().isArray()) return;
				
				//New to add array type to it so we know what kind of array we need to create
				if(type == int.class) {int[] arr = (int[]) f.get(o); height += 24 * arr.length;}
				else if(type == double.class) {double[] arr = (double[]) f.get(o); height += 24 * (arr.length + 1);}
				else if(type == short.class) {short[] arr = (short[]) f.get(o); height += 24 * (arr.length + 1);}
				else if(type == byte.class) {byte[] arr = (byte[]) f.get(o); height += 24 * (arr.length + 1);}
				else if(type == boolean.class) {boolean[] arr = (boolean[]) f.get(o); height += 24 * (arr.length + 1);}
				else if(type == String.class) {String[] arr = (String[]) f.get(o); height += 24 * (arr.length + 1);}
				else if(!c.getComponentType().isPrimitive())
				{
					Object[] arr = (Object[]) f.get(o);
					height += 24 * (arr.length + 1);
				}
			}
			if(engine.CustomClass.class.isAssignableFrom(f.getType()))
			{
				try
				{
					if(((engine.CustomClass) f.get(o)).expanded) SetClass((engine.Object) f.get(o), offset + 16);
				}
				catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
			}
			
			if(math.Rect.class.isAssignableFrom(f.getType())) height += 48;
			else height += 24;
		}
	}
}

class BehaviourField
{
	engine.Object object;
	Field field;
	int offset;
	
	public BehaviourField(engine.Object o, Field f, int offset) {object = o; field = f; this.offset = offset;}
}
