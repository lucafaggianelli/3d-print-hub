/*
 * Window manager for visualizer. Creates 3D canvas and manages data.
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tesladocet.threedprinting.gcode;

import com.tesladocet.threedprinting.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 *
 * @author wwinder
 */
public class GcodeViewerActivity extends Activity implements ControllerListener {

	private final static String TAG = "GCViewer";
	
    // Interactive members.
    private Point3f machineCoordinate;
    private Point3f workCoordinate;
    private int completedCommandNumber = -1;
    private String gcodeFile = null;
    private GcodeView gcodeView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.gcode_viewer);
    	
        // Create the OpenGL rendering canvas
        gcodeView = (GcodeView) findViewById(R.id.gcode_view);
        
        Uri file = getIntent().getData();
        if (file != null) {
        	Log.d(TAG, "file: " + file.toString());
        	Log.d(TAG, "path: " + file.getPath());
        	setGcodeFile(file.getPath());
        	//context.getContentResolver().openInputStream(uri);
        }
    }

    public void setGcodeFile(String file) {
        gcodeFile = file;
        gcodeView.setGcodeFile(gcodeFile);
    }
    
    public void setCompletedCommandNumber(int num) {
        completedCommandNumber = num;
        gcodeView.renderer.setCurrentCommandNumber(num);
    }

    public double getMinArcLength() {
        return gcodeView.renderer.getMinArcLength();
    }

    public void setMinArcLength(float minArcLength) {
        gcodeView.renderer.setMinArcLength(minArcLength);
    }

    public double getArcLength() {
        return  gcodeView.renderer.getArcLength();
    }

    public void setArcLength(float arcLength) {
        gcodeView.renderer.setArcLength(arcLength);
    }

    @Override
    public void statusStringListener(String state, Point3f machineCoord, Point3f workCoord) {
        machineCoordinate = machineCoord;
        workCoordinate = workCoord;
        
        // Give coordinates to canvas.
        gcodeView.renderer.setMachineCoordinate(this.machineCoordinate);
        gcodeView.renderer.setWorkCoordinate(this.workCoordinate);
    }

	@Override
	public void fileStreamComplete(String filename, boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandQueued(GcodeCommand command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandSent(GcodeCommand command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandComplete(GcodeCommand command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandComment(String comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageForConsole(String msg, Boolean verbose) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcessData(int numRows) {
		// TODO Auto-generated method stub
		
	}
}
