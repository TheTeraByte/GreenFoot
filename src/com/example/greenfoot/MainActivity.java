package com.example.greenfoot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This is the class that handles the "Green Tasks" list.
 * 
 * Current Task:
 * 1.) Green ticks persist for a day using a time-stamp [partially accomplished] and internal storage file (Tough)
 * 2.) Background for app 
 * 3.) Rapid automated score update (optional)
 * 4.) View PDF function for "About" menu option [ Done! ]
 * 5.) Posting using FB API (Tough and New) 
 * 6.) A score-board based on our own server page (Super tough) 
 * 7.) For previous we need to enter and store user's name using a simple text edit
 * or something
 * 
 * Current task: Green ticks persistence and timestamp persistence 
 * 
 * Debug: Green Ticks persistence not working! Put debug logs to see what array
 * holds, I think array not loading properly
 * 
 * @author tusharb1995
 *
 */

public class MainActivity extends ListActivity {
	
	// instance variables
	public static final int[] _relativeTasksScores={100,70,20,50,60,20,50,30,40,20,30,70,60,60,80,80,90,40,20,200};
	// Keeps track of the "Green score"
	public static int _greenScore=0;
	public static final String file_name="score_file";
	public static final String greenTicksPersistenceFile="green_ticks_record" ;
	public MenuItem score_keeper;
	// An array that stores the tick persistence
	public static int[] ticksLocation=new int[20];
	public ListView greenTasks;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.initializeTasksAndDisplayListView();
		// Initializing the array -- the zeros mean there are no ticks there
		for(int i=0;i<20;i++)
			ticksLocation[i]=0;
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		// Writes the ticks to ensure there persistence
		// Happens when the app is paused and may or may not be resumed
		this.writeTicks();
	}// end method

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		 // Updating score and making sure it's shown in the action bar
	     // The score_keeper is a dynamic addition to the menu
	     score_keeper=menu.add(Integer.toString(this.get_score()));
	     // This ensures that score_keeper is always shown and not hidden in the overflow menu 
	     // This single line of code is preventing a minimum of 8 API, i.e.
	     // forcing me on HoneyComb
	     score_keeper.setShowAsAction(2);  
	     
		
		return true;
	}

	
	// This helps define the actions that are to be performed when menu items of the action
	// bar are clicked
	    
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			// Handle item selection
			
			switch(item.getItemId())
			{
			
			case R.id.about_the_app:
				view_PDF("manual.pdf");
				return true;
				
			case R.id.about_us:
				view_PDF("team1.pdf");
				return true;
				
			case R.id.about_the_levels:
				view_PDF("levels2.pdf");
				return true;
			
			// Clicking any other item (such as the 'Score' text etc.) performs the 'default'
			// action which is *nothing*. Clicking these items does not lead to any change 
			// occurring
			default:
				return super.onOptionsItemSelected(item);
						
			
			}// end switch statement
			
		}// end method
	
	// This methods returns the list of "green tasks" as a String Array
	public String[] initializeAndGetGreenTasks()
	{
		 
		String[] GreenTasks = 

			{ "Grow your own food",
			"Use less than 2 processed food products",
			"Open blinds to let in natural light",
			"Fight 'vampire power' ", 
			"Take a shower in less than 20 minutes",
			"Wash clothes using cold water",
			"Hang clothes on a clothesline to dry",
			"Use leftover bathwater or \"greywater\" to water plants",
			"Turn off lights when not in use",
			"Turn off water when brushing teeth",
			"Don't let water run while washing dishes",
			"Run the dishwasher or washing machine only when there is a full load",
			"Take public transportation",
			"Walk or ride your bike",
			"Print 0 documents",
			"Recycle bottles, cans, newspapers, etc. ",
			"Donate items you no longer need or use",
			"Use reusable bags at the grocery store",
			"Use reusable containers at home",
			"Compost"
			};
		
		return GreenTasks;
	}// end method
	
	public void initializeTasksAndDisplayListView()
	{
		// Making the ListView visible, and setting it to multiple choice mode
		greenTasks=getListView();
		greenTasks.setVisibility(View.VISIBLE);
		greenTasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		// This allows us to search within the ListView
		greenTasks.setTextFilterEnabled(true);
		
		// Setting an adapter for ListView -- which has how each tasks should 
		// look and the task source
		setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,this.initializeAndGetGreenTasks()));
		
		// Loading the stored ticks 
		this.loadTicks();
		
	}// end method
	
	// This method handles incrementing the score when a task is done
	// Also if item is unchecked reduces the score
	public void onListItemClick(ListView parent,View V,int position,long id)
    {
		// If they check the item --> score increases! Immediate feedback from toast
		if(parent.isItemChecked(position))
		{
		// Over here we will also add the position to an ArrayList/Array of ticked
		// items, so that it gets stored in the persistence file
		// Similarly we will remove it if the "else" option gets activated	
		// The array will get written to the a file when the app goes
		// "on pause" 	
		ticksLocation[position]=1;	
		this.score_add(_relativeTasksScores[position]);
		Toast.makeText(getApplicationContext(), "Congratulations! You took a step towards saving the plant!", Toast.LENGTH_SHORT).show();
		}// end if statement
		
		// If they go back on their word reduce the score! (unless we uncheck ourselves using the date)
		else
		{
		 ticksLocation[position]=0;	
		 this.score_add(-_relativeTasksScores[position]);
		}// end else statement
    }// end method
	
	// This function loads the ticks based on the file and also makes changes to the 
	// UI based on that 
	public void loadTicks()
	{
		
		// Creates a file with the name of the string stored in 'file_name' in the 
		// internal directory of the app
		File file=new File(getFilesDir(), greenTicksPersistenceFile);
		
		// If the file does not exist then we assume the user has just opened the app and
		// the score is zero
		if(!file.exists())
		{
			System.out.println("File does not exist!");
		}// end if statement
			
		   try
		   {
			FileInputStream fis = openFileInput(greenTicksPersistenceFile);
			InputStreamReader in=new InputStreamReader(fis);
			BufferedReader br=new BufferedReader(in);
		//	String tickLocation=br.readLine();	
			// Modified for junk text (one character)
			for(int i=0;i<20;i++)
				ticksLocation[i]=br.read();
		   }// end try statement
		   
		   catch (Exception e)
		   {
			e.printStackTrace();
		   }// end catch statement
		   
		   // Changes to the UI -- comment out to prevent weirdness
		   for(int i=0;i<20;i++)
		   {
			   if(ticksLocation[i]==1)
			   {
				   greenTasks.setItemChecked(i, true);
			   }// end if statement
		   }// end for loop
		
	}// end method
	
	// This function writes the filled positions array to a list for tick persistence
	// Call it from "onPause()"
	public void writeTicks()
	{
		
		// Loading the ticks properly before deleting (may have to comment out,
		// if it leads to recursive case)
		this.loadTicks();
		
		File oldFileDel=new File(getFilesDir(),greenTicksPersistenceFile);
		oldFileDel.delete();
		
		File ticksFile=new File(getFilesDir(),greenTicksPersistenceFile);
		
		FileOutputStream outputStream;
		
		try
		{
			BufferedWriter bwriter=new BufferedWriter(new FileWriter(greenTicksPersistenceFile));
			outputStream=openFileOutput(greenTicksPersistenceFile,Context.MODE_PRIVATE);
		    // If this doesn't work turn append on in the FileOutput stream
			
			// This string stores the text ; putting one character to get string working
			char[] tickLocationString=new char[20];
			
			// Writing the output stream byte by byte 
			for(int i=0;i<20;i++)
			// Make the output stream append instead of destructively create
			{
				bwriter.append(((char)ticksLocation[i]));
				tickLocationString[i]=((char)(ticksLocation[i]));
				//outputStream.write(ticksLocation[i]);
			}// end for loop
			
			//bwriter.write(tickLocationString);
			
		//	Toast.makeText(getApplicationContext(), "Text: "+tickLocationString, Toast.LENGTH_SHORT).show();
		//	outputStream.write(tickLocationString,);
	//	bwriter.write(tickLocationString);	
			
			bwriter.close();
			outputStream.close();
		}
		catch(Exception e)
		{
		e.printStackTrace();	
		}// end catch statement
		
	}// end method
	
	
	 /*
	  *  This function returns the current score of the user. The score is stored in a file
	  *   in the Internal Storage of the app.
	  */
	  public int get_score()
	  {
			int score=0;
			
			// Creates a file with the name of the string stored in 'file_name' in the 
			// internal directory of the app
			File file=new File(getFilesDir(), file_name);
			
			// If the file does not exist then we assume the user has just opened the app and
			// the score is zero
			if(!file.exists())
			{
				return score;
			}// end if statement
				
			   try
			   {
				FileInputStream fis = openFileInput(file_name);
				InputStreamReader in=new InputStreamReader(fis);
				BufferedReader br=new BufferedReader(in);
				score=Integer.parseInt(br.readLine());
			   }// end try statement
			   
			   catch (Exception e)
			   {
				e.printStackTrace();
			   }// end catch statement
			
			   return score;
			   
	}// end function
	 
		/* This function provides PDF viewing functionality for the app. You pass the file name
		* with the '.pdf' postscript and it opens the file provided it is in the correct folder.
	    * This function essentially calls an 'Intent' that opens the PDF file in a PDF Viewer
		* application. The PDF viewer application must be installed in the device. 
		*/
		
		 public void view_PDF(String file_name)
			{
				// I'm assuming all my files will be stored on the SD card directly ; using bluetooth
				// for tablet testing

				File file = new File("/sdcard/"+/*Environment.getExternalStorageDirectory()*/"downloads/bluetooth/"+file_name);
				
				if(file.exists())
				{
				// Now I am going to use an intent to let the default application handle PDF viewing
				Uri path=Uri.fromFile(file);
				Intent pdf_view = new Intent(android.content.Intent.ACTION_VIEW);
			    pdf_view.setDataAndType(path,"application/pdf");
			    pdf_view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    
			    
			    try
			    {
			    	startActivity(pdf_view);
			    }// end try statement
				
				catch(ActivityNotFoundException e)
				{
					Toast no_app=Toast.makeText(getBaseContext(), "Application to view PDF missing", Toast.LENGTH_SHORT);
					no_app.show();
				}// end catch statement
				}// end if statement
				
				else
				{
					System.out.println("File does not exist !");
					Toast no_file=Toast.makeText(getBaseContext(), "File does not exist !", Toast.LENGTH_SHORT);
					no_file.show();
				}// end else statement

				
			}// end function 
  
	  
	// Function that allows us to add to the score to the text
	public void score_add(int add_val)
	{
		int original_sc=get_score();
		int new_sc=original_sc+add_val;
				
				// a string to write to the file
				String score=Integer.toString(new_sc);
				
				FileOutputStream outputStream;
				
				try
				{
					outputStream=openFileOutput(file_name,Context.MODE_PRIVATE);
					outputStream.write(score.getBytes());
					outputStream.close();
				}
				catch(Exception e)
				{
				e.printStackTrace();	
				}// end catch statement
				
			}// end function
	
	
	// This function returns the level based on the score (it dynamically
	// does this)
	public int updateLevel() { 
		if(this.get_score()>=150) {
		return 1;
		}
		else if (this.get_score()>=500) {
		return 2;
		}
		else if (this.get_score()>=1000) {
		return 3;
		}
		else if (this.get_score()>=1500) {
		return 4;
		}
		else if (this.get_score()>=2200) {
		return 5;
		}
		else if (this.get_score()>=4000) {
		return 6;
		}
		else if (this.get_score()>=5500) {
		return 7;
		} 
		else if (this.get_score()>=8000) {
		return 8;
		}
		else if (this.get_score()>=10500) {
		return 9;
		}
		else if (this.get_score()>=12000) {
		return 10;
		}
		
		else
		{
			return 0;
		}// end else statement
		
		}// end method
	
}// end class
