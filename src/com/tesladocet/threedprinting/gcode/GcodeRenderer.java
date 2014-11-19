package com.tesladocet.threedprinting.gcode;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GcodeRenderer implements Renderer {

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

        if (height == 0) height = 1; // prevent divide by zero
        float aspectRatio = (float)width / height;

        this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        this.panMultiplierX = VisualizerUtils.getRelativeMovementMultiplier(this.objectMin.x, this.objectMax.x, this.xSize);
        this.panMultiplierY = VisualizerUtils.getRelativeMovementMultiplier(this.objectMin.y, this.objectMax.y, this.ySize);

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);
        
        gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		GLU.gluPerspective(gl, 45f, aspectRatio, 10f, 500f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		GLU.gluLookAt(gl, 100f, 100f, 100f, 0, 0, 0, -1f, -1f, 1f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepthf(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL10.GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL10.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
	}

}
