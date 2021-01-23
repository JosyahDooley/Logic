package engine;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Dialog
{
	private static String returnValue;
	private static int finished = 0;
	
	public static String InputDialog(String title, String defaultValue)
	{
		JFrame frame = new JFrame(title);
		frame.pack();
		
		frame.setSize(300, 103);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container pane = frame.getContentPane();
		pane.setLayout(null);
		
		returnValue = defaultValue;
		JTextField field = new JTextField(defaultValue);
		field.setBounds(5, 5, 275, 25);
		AbstractAction action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = field.getText();
				finished = 1;
			}
		};
		field.addActionListener(action);
		pane.add(field);
		
		JButton ok = new JButton("OK");
		ok.setBounds(115, 34, 80, 24);
		
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = field.getText();
				finished = 1;
			}
		});
		pane.add(ok);
		
		JButton cancel = new JButton("CANCEL");
		cancel.setBounds(198, 34, 80, 24);
		
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				returnValue = null;
				finished = 1;
			}
		});
		pane.add(cancel);
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		finished = 0;
		
		while(finished != 1)
		{
			try {Thread.sleep(1);}
			catch (InterruptedException e1) {e1.printStackTrace();}
			if(frame == null) break;
			else if(!frame.isDisplayable()) break;
		}
		
		frame.dispose();
		return returnValue;
	}
	
	public static void MessageDialog(String title, String message)
	{
		JFrame frame = new JFrame(title);
		frame.pack();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JTextArea textArea = new JTextArea(message);
		JScrollPane scroll = new JScrollPane(textArea);
		textArea.setEditable(false);
		
		frame.add(scroll);
		frame.setVisible(true);
	}
}
