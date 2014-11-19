package com.tesladocet.threedprinting.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

public class StlRenderer implements Renderer {
	
	private final static String TAG = "StlRenderer";
	
	public static final int FRAME_BUFFER_COUNT = 2;
	
	public float angleX = -75;
	public float angleY = 0;
	public float[] axis = new float[3];
	public float positionX = 0f;
	public float positionY = 0f;
	public float distanceZ = 100f;
	
	float red;
	float green;
	float blue;
	float alpha;
	
	public static boolean displayAxes = false;
	public static boolean displayGrids = true;
	private static int bufferCounter = 2;

	private StlObject stlObject;
	
	public StlRenderer(StlObject stlObject) {
		this.stlObject = stlObject;
	}

	public void requestRedraw() {
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	public void setColor(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	
	private void drawGrids(GL10 gl) {
		List<Float> lineList = new ArrayList<Float>();

		for (float x = -stlObject.sizeX; x <= stlObject.sizeX; x += 5) {
			lineList.add(x);
			lineList.add(-stlObject.sizeX);
			lineList.add(0f);
			lineList.add(x);
			lineList.add(stlObject.sizeX);
			lineList.add(0f);
		}

		for (float y = stlObject.sizeY; y <= stlObject.sizeY; y += 5) {
			lineList.add(-stlObject.sizeY);
			lineList.add(y);
			lineList.add(0f);
			lineList.add(stlObject.sizeY);
			lineList.add(y);
			lineList.add(0f);
		}

		FloatBuffer lineBuffer = getFloatBufferFromList(lineList);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

		gl.glLineWidth(1f);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
		gl.glDrawArrays(GL10.GL_LINES, 0, lineList.size() / 3);
		
		Rectangle plane = new Rectangle();
		gl.glPushMatrix();
		gl.glTranslatex(0, 0, 1);
	    gl.glScalef(stlObject.maxX-stlObject.minX, stlObject.maxY-stlObject.minY, 1);
	    plane.draw(gl);
	    gl.glPopMatrix();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
//		if (bufferCounter < 1) {
//			return;
//		}
//		bufferCounter--;

		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glTranslatef(positionX, -positionY, 0);

		// rotation and apply Z-axis
		if (stlObject != null) {
			gl.glTranslatef(
					-(stlObject.maxX + stlObject.minX) / 2, 
					-(stlObject.maxY + stlObject.minY) / 2, 
					-(stlObject.maxZ + stlObject.minZ) - distanceZ);
		} else {
			gl.glTranslatef(0, 0, -distanceZ);
		}
		
		gl.glRotatef(angleY, 0, 1, 0);
		gl.glRotatef(angleX, 1, 0, 0);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// draw X-Y field
		if (displayGrids) {
			drawGrids(gl);
		}

		// draw axis
		if (displayAxes) {
			gl.glLineWidth(3f);
			float[] vertexArray = { -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100 };
			FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);
		
			// X : red
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 1.0f, 0f, 0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 1.0f, 0f, 0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 0, 2);

			// Y : blue
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 0f, 1.0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 0f, 1.0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 2, 2);

			// Z : green
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 1.0f, 0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 1.0f, 0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 4, 2);
		}

		// draw object
		if (stlObject != null) {
			gl.glPushMatrix();
			gl.glTranslatex(0, 0, 2);
			// FIXME transparency applying does not correctly
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0.75f, 0.75f, 0.75f, 0.95f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { red, green, blue, alpha }, 0);
			stlObject.draw(gl);
			gl.glPopMatrix();
		}
	}
	
	private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		triangleBuffer.put(vertexArray);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

	private FloatBuffer getFloatBufferFromList(List<Float> vertexList) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexList.size() * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		float[] array = new float[vertexList.size()];
		for (int i = 0; i < vertexList.size(); i++) {
			array[i] = vertexList.get(i);
		}
		triangleBuffer.put(array);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		float aspectRatio = (float) width / height;

		gl.glViewport(0, 0, width, height);
		
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (stlObject != null) {
			Log.i(TAG, "maxX:" + stlObject.maxX);
			Log.i(TAG, "minX:" + stlObject.minX);
			Log.i(TAG, "maxY:" + stlObject.maxY);
			Log.i(TAG, "minY:" + stlObject.minY);
			Log.i(TAG, "maxZ:" + stlObject.maxZ);
			Log.i(TAG, "minZ:" + stlObject.minZ);
		}

		GLU.gluPerspective(gl, 45f, aspectRatio, 10f, 500f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		GLU.gluLookAt(gl, 100f, 100f, 100f, 0, 0, 0, -1f, -1f, 1f);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		gl.glClearColor(.957f, .957f, .957f, 0.5f);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// FIXME This line seems not to be needed?
		gl.glEnable(GL10.GL_DEPTH_TEST);

		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glMatrixMode(GL10.GL_PROJECTION);

		// Lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 0.85f}, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[]{1f, 1f, 1f, 0.75f}, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { 0f, 0f, 1000f, 1f }, 0); // light comes above of screen
	}
	
	public class Rectangle {

	    private float vertices[] = {
	        -1.0f, 1.0f, 0.0f,
	        -1.0f,-1.0f,0.0f,
	        1.0f,-1.0f,0.0f,
	        1.0f,1.0f,0.0f
	    };
	    
	    private short[] indices = {0,1,2,0,2,3};

	    private FloatBuffer vertexBuffer;
	    private ShortBuffer indexBuffer;

	    public Rectangle(){
	        ByteBuffer vbb  = ByteBuffer.allocateDirect(vertices.length * 4);
	        vbb.order(ByteOrder.nativeOrder());
	        vertexBuffer = vbb.asFloatBuffer();
	        vertexBuffer.put(vertices);
	        vertexBuffer.position(0);

	        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
	        ibb.order(ByteOrder.nativeOrder());
	        indexBuffer = ibb.asShortBuffer();
	        indexBuffer.put(indices);
	        indexBuffer.position(0);
	    }

	    public void draw(GL10 gl){
	        gl.glFrontFace(GL10.GL_CCW);
//	        gl.glEnable(GL10.GL_CULL_FACE);
//	        gl.glCullFace(GL10.GL_BACK);
//	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
	        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0.75f, 0.75f, 0.75f, 1 }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0, 0.40f, 0.75f, 1 }, 0);
	        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
//	        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//	        gl.glDisable(GL10.GL_CULL_FACE);
	    }

	}
}
