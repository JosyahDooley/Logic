package editor;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class EngineBootLoader
{
	private static JFrame frame;
	private static Container pane;
	private static String returnString = null;
	
	public static void Init()
	{
		if(returnString != null) return;
		
		CreateWindow();
		
		while(returnString == null)
		{
			try{Thread.sleep(1);}
			catch(InterruptedException e) {e.printStackTrace();}
			if(frame == null) break;
		}
		
		frame.dispose();
		Editor.OpenProject(returnString);
	}
	
	private static void CreateWindow()
	{
		frame = new JFrame("Logic Bootloader");
		frame.pack();
		
		frame.setSize(300, 350);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pane = frame.getContentPane();
		
		try {CreatePanel();}
		catch (IOException e) {e.printStackTrace();}
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private static void CreatePanel() throws IOException
	{
		BufferedImage image = ImageIO.read(EngineBootLoader.class.getResourceAsStream("/Textures/BootLoad.png"));
		
		JPanel panel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				if(image != null) g.drawImage(image, 10, 10, 263, 215, null);
			}
		};
		panel.setLayout(null);
		
		File file = new File(Editor.workspaceDirectory);
		File[] temp = file.listFiles(File::isDirectory);
		List<String> dirs = new ArrayList<String>();
		for(int i = 0; i < temp.length; i++)
		{
			if(!temp[i].getName().equals("null")) dirs.add(temp[i].getName());
		}
		dirs.add("New Project");
		String[] output = new String[dirs.size()];
		output = dirs.toArray(output);
		
		JComboBox<String> projectBox = new JComboBox<String>(output);
		projectBox.setBounds(70, 240, 150, 25);
		
		JButton begin = new JButton("Begin");
		begin.setBounds(100, 276, 80, 25);
		
		begin.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String c = e.getActionCommand();
				
				if(c.equals("Begin"))
				{
					String selectedString = (String) projectBox.getSelectedItem();
					if(selectedString.equals("New Project")) returnString = "My Project";
					else returnString = selectedString;
				}
			}
		});
		panel.add(projectBox);
		panel.add(begin);
		pane.add(panel);
	}
}
