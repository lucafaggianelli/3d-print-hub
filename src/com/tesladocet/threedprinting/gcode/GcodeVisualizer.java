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

import android.app.Activity;
import android.os.Bundle;

/**
 *
 * @author wwinder
 */
public class GcodeVisualizer extends Activity implements ControllerListener {

    private static final int FPS = 20; // animator's target frames per second

    // Interactive members.
    private Point3d machineCoordinate;
    private Point3d workCoordinate;
    private int completedCommandNumber = -1;
    private String gcodeFile = null;
    private VisualizerCanvas canvas = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        // Create the OpenGL rendering canvas
        this.canvas = new VisualizerCanvas();

    }

    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        canvas.setGcodeFile(this.gcodeFile);
    }
    
    public void setCompletedCommandNumber(int num) {
        this.completedCommandNumber = num;
        this.canvas.setCurrentCommandNumber(num);
    }

    public double getMinArcLength() {
        return this.canvas.getMinArcLength();
    }

    public void setMinArcLength(double minArcLength) {
        this.canvas.setMinArcLength(minArcLength);
    }

    public double getArcLength() {
        return  this.canvas.getArcLength();
    }

    public void setArcLength(double arcLength) {
        this.canvas.setArcLength(arcLength);
    }

    @Override
    public void statusStringListener(String state, Point3d machineCoord, Point3d workCoord) {
        machineCoordinate = machineCoord;
        workCoordinate = workCoord;
        
        // Give coordinates to canvas.
        this.canvas.setMachineCoordinate(this.machineCoordinate);
        this.canvas.setWorkCoordinate(this.workCoordinate);
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
