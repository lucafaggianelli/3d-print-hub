package com.tesladocet.threedprinting.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//import android.content.DialogInterface.OnKeyListener;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
//import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FilePicker {

	public final static int FILE_OPEN = 0;
	public final static int FILE_SAVE = 1;
	public final static int FOLDER_CHOOSE = 2;
	private final static String[] dialogTitles = {
		"Open", "Save As", "Choose folder"
	};

	private int Select_type = FILE_SAVE;
	private String m_sdcardDirectory = "";
	private Context m_context;
	private TextView m_titleView;
	private String selected_file_name = "";
	
	private String m_dir = "";
	private List<String> m_subdirs = null;
	private SimpleFileDialogListener m_SimpleFileDialogListener = null;
	private ArrayAdapter<String> m_listAdapter = null;
	private boolean m_goToUpper = false;
	
	public interface SimpleFileDialogListener {
		public void onChosenDir(String chosenDir);
	}
	
	public FilePicker(Context context, SimpleFileDialogListener SimpleFileDialogListener) {		
		m_context = context;
		m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
		m_SimpleFileDialogListener = SimpleFileDialogListener;

		try {
			m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setType(int type, boolean up) {
		if (type >= 0 && type <= 2) {
			Select_type = type;
			m_goToUpper = up;
		} else {
			throw new IllegalArgumentException("Use FILE_OPEN, FILE_SAVE or FOLDER_CHOOSE");
		}
	}
	
	/**
	 * chooseFile_or_Dir() - load directory chooser dialog for initial
	 * default sdcard directory
	 */
	public void launch()	{
		// Initial directory is sdcard directory
		if (m_dir.equals(""))	launch(m_sdcardDirectory);
		else launch(m_dir);
	}

	/**
	 * chooseFile_or_Dir(String dir) - load directory chooser dialog for initial 
	 * input 'dir' directory
	 */
	public void launch(String dir) {
		File dirFile = new File(dir);
		while (! dirFile.exists() || ! dirFile.isDirectory())
		{
			dir = dirFile.getParent();
			dirFile = new File(dir);
Log.d("~~~~~","dir="+dir);
		}
Log.d("~~~~~","dir="+dir);
		//m_sdcardDirectory
		try
		{
			dir = new File(dir).getCanonicalPath();
		}
		catch (IOException ioe)
		{
			return;
		}

		m_dir = dir;
		m_subdirs = getDirectories(dir);

		class FilePickedListener implements DialogInterface.OnClickListener {
			
			@Override
			public void onClick(DialogInterface dialog, int item) {
				String m_dir_old = m_dir;
				String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
				if (sel.charAt(sel.length()-1) == '/')	sel = sel.substring(0, sel.length()-1);
				
				// Navigate into the sub-directory
				if (sel.equals("..")) {
					m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
					if("".equals(m_dir)) {
						m_dir = "/";
					}
				} else {
					   m_dir += "/" + sel;
				}
				
				selected_file_name = "";
				// If the selection is a regular file
				if ((new File(m_dir).isFile())) {
					m_dir = m_dir_old;
					selected_file_name = sel;
				}
				
				updateDirectory();
			}
		}

		AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, m_subdirs, 
				new FilePickedListener());

		dialogBuilder.setPositiveButton("OK", new OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				// Current directory chosen
				// Call registered listener supplied with the chosen directory
				if (m_SimpleFileDialogListener != null){
					if (Select_type == FILE_OPEN || Select_type == FILE_SAVE) {
						m_SimpleFileDialogListener.onChosenDir(m_dir + "/" + selected_file_name);
					} else {
						m_SimpleFileDialogListener.onChosenDir(m_dir);
					}
				}
			}
		}).setNegativeButton("Cancel", null);

		final AlertDialog dirsDialog = dialogBuilder.create();

		// Show directory chooser dialog
		dirsDialog.show();
	}

	private boolean createSubDir(String newDir) {
		File newDirFile = new File(newDir);
		if   (! newDirFile.exists() ) return newDirFile.mkdir();
		else return false;
	}
	
	private List<String> getDirectories(String dir) {
		List<String> dirs = new ArrayList<String>();
		try {
			File dirFile = new File(dir);
			
			// if directory is not the base sd card directory add ".." for going up one directory
			if ((m_goToUpper || ! m_dir.equals(m_sdcardDirectory) ) && !"/".equals(m_dir)) {
				dirs.add("..");
			}

			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return dirs;
			}

			for (File file : dirFile.listFiles()) {
				if (file.isDirectory()) {
					// Add "/" to directory names to identify them in the list
					dirs.add( file.getName() + "/" );
				} else if (Select_type == FILE_SAVE || Select_type == FILE_OPEN) {
					// Add file names to the list if we are doing a file save or file open operation
					dirs.add( file.getName() );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(dirs, new Comparator<String>() {	
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return dirs;
	}
	
	private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
			DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);
		
		dialogBuilder.setTitle(dialogTitles[Select_type]);

		if (Select_type == FOLDER_CHOOSE || Select_type == FILE_SAVE) {
			
			dialogBuilder.setNeutralButton("New Folder", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					final EditText input = new EditText(m_context);

					// Show new folder name input dialog
					new AlertDialog.Builder(m_context)
						.setTitle("New Folder Name")
						.setView(input)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								Editable newDir = input.getText();
								String newDirName = newDir.toString();
								// Create new directory
								if ( createSubDir(m_dir + "/" + newDirName) )
								{
									// Navigate into the new directory
									m_dir += "/" + newDirName;
									updateDirectory();
								} else {
									Toast.makeText(	m_context, "Failed to create '" 
											+ newDirName + "' folder", Toast.LENGTH_SHORT).show();
								}
							}
						})
						.setNegativeButton("Cancel", null)
						.show(); 
				}
			});
		}
		
		m_titleView = new TextView(m_context);
		m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		m_titleView.setText(title);
		
		//////////////////////////////////////////
		// Set Views and Finish Dialog builder  //
		//////////////////////////////////////////
		dialogBuilder.setView(m_titleView);
		m_listAdapter = new FileAdapter(listItems);
		dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
		dialogBuilder.setCancelable(false);
		return dialogBuilder;
	}

	private void updateDirectory() {
		m_subdirs.clear();
		m_subdirs.addAll( getDirectories(m_dir) );
		m_titleView.setText(m_dir + "/" + selected_file_name);
		m_listAdapter.notifyDataSetChanged();
	}

	class FileAdapter extends ArrayAdapter<String> {

		public FileAdapter(List<String> items) {
			super(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			if (v instanceof TextView) {
				// Enable list item (directory) text wrapping
				TextView tv = (TextView) v;
				tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				tv.setEllipsize(null);
			}
			return v;
		}
	};
} 