/*
 * 3D Canvas for GCode Visualizer.
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

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

/**
 *
 * @author wwinder
 * 
 */
@SuppressWarnings("serial")
public class VisualizerCanvas implements Renderer {
    static boolean ortho = true;
    static float orthoRotation = -45;
    static boolean forceOldStyle = false;
    static boolean debugCoordinates = false; // turn on coordinate debug output

    // Machine data
    private Point3f machineCoord;
    private Point3f workCoord;
    
    // Gcode file data
    private String gcodeFile = null;
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private List<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;
    private int lastCommandNumber = 0;

    // GL Utility
    private GLU glu;
    
    // Projection variables
    private Point3f center, eye;
    private Point3f objectMin, objectMax;
    private float maxSide;
    private float minArcLength;
    private float arcLength;
    private int height, width;
    private float aspectRatio;

    // Scaling
    private float scaleFactor;
    private float scaleFactorBase;
    private float zoomMultiplier = 1;
    private boolean invertZoom = false; // TODO: Make configurable
    // const values until added to settings
    private final float minZoomMultiplier = 1;
    private final float maxZoomMultiplier = 30;
    private final float zoomIncrement = 0.2f;

    // Movement
    private int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable
    private float panMultiplierX = 1;
    private float panMultiplierY = 1;
    private Vector3d translationVectorH;
    private Vector3d translationVectorV;

    // Mouse rotation data
    Point3f last;
    Point3f current;
    private Point3f rotation;

    // OpenGL Object Buffer Variables
    private int numberOfVertices = -1;
    private float[] lineVertexData = null;
    private byte[] lineColorData = null;
    private FloatBuffer lineVertexBuffer = null;
    private ByteBuffer lineColorBuffer = null;
    
    // Track when arrays need to be updated due to changing data.
    private boolean colorArrayDirty = false;
    private boolean vertexArrayDirty = false;
    
    /**
     * Constructor.
     */
    public VisualizerCanvas() {
       this.addGLEventListener(this);
       this.addKeyListener(this);
       this.addMouseMotionListener(this);
       this.addMouseWheelListener(this);

       this.eye = new Point3f(0, 0, 1.5f);
       this.center = new Point3f(0, 0, 0);
       
       this.workCoord = new Point3f(0, 0, 0);
       this.machineCoord = new Point3f(0, 0, 0);
       
       this.rotation = new Point3f(0, -30, 0);
       if (ortho) {
           setVerticalTranslationVector();
           setHorizontalTranslationVector();
       }
    }
    
    @Override
	public void onDrawFrame(GL10 gl) {
    	this.setupPerpective(gl, width, height, ortho);
        
        // Scale the model so that it will fit on the window.
        gl.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        
        // Rotate prior to translating so that rotation happens from middle of
        // object.
        if (ortho) {
            // Manual rotation
            gl.glRotatef(this.rotation.x, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(this.rotation.y, 1.0f, 0.0f, 0.0f);
            gl.glTranslatef(-this.eye.x - this.center.x,
            		-this.eye.y - this.center.y,
            		-this.eye.z - this.center.z);
        } else {
            // Shift model to center of window.
            gl.glTranslatef(-this.center.x, -this.center.y, 0f);
        }
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // Draw model
        if (isDrawable) {
            renderAxes(gl);
            renderModel(gl);
            renderTool(gl);
        }
        
        gl.glDisable(GL10.GL_DEPTH_TEST);

        gl.glPopMatrix();
        update();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

        if (height == 0) height = 1; // prevent divide by zero
        this.height = height;
        this.width = width;
        this.aspectRatio = (float)width / height;

        this.scaleFactorBase = VisualizerUtils.findScaleFactor(
        		this.width, this.height, objectMin, objectMax);
        
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        this.panMultiplierX = VisualizerUtils.getRelativeMovementMultiplier(
        		this.objectMin.x, this.objectMax.x, width);
        this.panMultiplierY = VisualizerUtils.getRelativeMovementMultiplier(
        		this.objectMin.y, this.objectMax.y, height);

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
    
    /**
     * This is used to gray out completed commands.
     */
    public void setCurrentCommandNumber(int num) {
        this.currentCommandNumber = num;
        this.createVertexBuffers();
        this.colorArrayDirty = true;
    }
    
    /**
     * Returns the last command number used for generating the gcode object.
     */
    public int getLastCommandNumber() {
        return this.lastCommandNumber;
    }
    
    /**
     * Assign a gcode file to drawing.
     */
    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        this.isDrawable = false;
        this.currentCommandNumber = 0;
        this.lastCommandNumber = 0;

        generateObject();
    }
    
    public void setWorkCoordinate(Point3f p) {
        this.workCoord.set(p);
    }
    
    public void setMachineCoordinate(Point3f p) {
        this.machineCoord.set(p);
    }

    // ------ Implement methods declared in GLEventListener ------

    private void renderAxes(GL10 gl) {
        
		float[] vertexArray = { -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100 };
		FloatBuffer buf = FloatBuffer.allocate(18);
		buf.put(vertexArray);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buf);
	
		gl.glLineWidth(3f);
		
		// X : red
		gl.glColor4f(1, 0, 0, 1);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		// Y : blue
		gl.glColor4f(0, 0, 1, 1);
		gl.glDrawArrays(GL10.GL_LINES, 2, 2);

		// Z : green
		gl.glColor4f(0, 1, 0, 1);
		gl.glDrawArrays(GL10.GL_LINES, 4, 2);
    }
    
    /**
     * Draws a tool at the current work coordinates.
     */
    private void renderTool(GL10 gl) {
    	float[] vertexArray = {
    			this.workCoord.x, this.workCoord.y, this.workCoord.z,
    			this.workCoord.x, this.workCoord.y, this.workCoord.z+(1/this.scaleFactor)
    	};
		FloatBuffer buf = FloatBuffer.allocate(6);
		buf.put(vertexArray);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buf);
    	
        gl.glLineWidth(8.0f);
        gl.glColor4f(1, 1, 0, 1);
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
    }
    
    /**
     * Render the GCode object.
     */
    private void renderModel(GL10 gl) {
        
        // Batch mode if available 
        if(!forceOldStyle
                && gl.isFunctionAvailable( "glGenBuffers" )
                && gl.isFunctionAvailable( "glBindBuffer" )
                && gl.isFunctionAvailable( "glBufferData" )
                && gl.isFunctionAvailable( "glDeleteBuffers" ) ) {
            // Initialize OpenGL arrays if required.
            if (this.colorArrayDirty) {
                this.updateGLColorArray(drawable);
                this.colorArrayDirty = false;
            }
            if (this.vertexArrayDirty) {
                this.updateGLGeometryArray(drawable);
                this.vertexArrayDirty = false;
            }
            gl.glLineWidth(1.0f);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDrawArrays(GL10.GL_LINES, 0, numberOfVertices);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        } else {
        	// Traditional OpenGL
            // TODO: By using a GL_LINE_STRIP I can easily use half the number of
            //       verticies. May lose some control over line colors though.
            //gl.glEnable(GL2.GL_LINE_SMOOTH);
            gl.glBegin(GL10.GL_LINES);
            gl.glLineWidth(1.0f);

            int verts = 0;
            int colors = 0;
            for(LineSegment ls : gcodeLineList)
            {
                gl.glColor3ub(lineColorData[colors++],lineColorData[colors++],lineColorData[colors++]);
                gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
                gl.glColor3ub(lineColorData[colors++],lineColorData[colors++],lineColorData[colors++]);
                gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
            }

            gl.glEnd();
        }

        // makes the gui stay on top of elements
        // drawn before.
    }
    
    /**
     * Setup the perspective matrix.
     */
    private void setupPerpective(GL10 gl, int x, int y, boolean ortho) {

        if (ortho) {
            gl.glDisable(GL10.GL_DEPTH_TEST);
            //gl.glDisable(GL_LIGHTING);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            // Object's longest dimension is 1, make window slightly larger.
            gl.glOrthof((float) -0.51*this.aspectRatio,
            		(float) 0.51*this.aspectRatio,
            		-0.51f, 0.51f, -10f, 10f);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
        } else {
            gl.glEnable(GL10.GL_DEPTH_TEST);

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL10.GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix

            GLU.gluPerspective(gl, 45f, this.aspectRatio, 0.1f, 100.0f);
            // Move camera out and point it at the origin
            GLU.gluLookAt(gl, (float)this.eye.x, (float)this.eye.y, (float)this.eye.z,
                          0f, 0f, 0f,
                          0f, 1f, 0f);
            
            // Enable the model-view transform
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity(); // reset
        }
    }
    
    /**
     * Parse the gcodeFile and store the resulting geometry and data about it.
     */
    private void generateObject()
    {
        if (this.gcodeFile == null){ return; }
        
        try {

        GcodeViewParse gcvp = new GcodeViewParse();
        List<String> linesInFile;
        linesInFile = VisualizerUtils.readFiletoArrayList(this.gcodeFile);
        gcodeLineList = gcvp.toObjRedux(linesInFile, 0.3f);
        
        this.objectMin = gcvp.getMinimumExtremes();
        this.objectMax = gcvp.getMaximumExtremes();

        // Grab the line number off the last line.
        this.lastCommandNumber = gcodeLineList.get(gcodeLineList.size() - 1).getLineNumber();
        
        System.out.println("Object bounds: X ("+objectMin.x+", "+objectMax.x+")");
        System.out.println("               Y ("+objectMin.y+", "+objectMax.y+")");
        System.out.println("               Z ("+objectMin.z+", "+objectMax.z+")");
        
        this.center = VisualizerUtils.findCenter(objectMin, objectMax);
        System.out.println("Center = " + center.toString());
        System.out.println("Num Line Segments :" + gcodeLineList.size());

        this.maxSide = VisualizerUtils.findMaxSide(objectMin, objectMax);
        
        this.scaleFactorBase = 1 / this.maxSide;
        this.scaleFactorBase = VisualizerUtils.findScaleFactor(
        		this.width, this.height, this.objectMin, this.objectMax);
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;

        this.isDrawable = true;
        
        // Now that the object is known, fill the buffers.
        this.createVertexBuffers();
        this.colorArrayDirty = true;
        this.vertexArrayDirty = true;
        
        } catch (IOException e) {
            System.out.println("Error opening file: " + e.getLocalizedMessage());
        }

    }

    /**
     * Convert the gcodeLineList into vertex and color arrays.
     */
    private void createVertexBuffers() {
        if (this.isDrawable) {
            this.numberOfVertices = gcodeLineList.size() * 2;
            this.lineVertexData = new float[numberOfVertices * 3];
            this.lineColorData = new byte[numberOfVertices * 3];
            
            VisualizerUtils.Color color;
            int vertIndex = 0;
            int colorIndex = 0;
            for(LineSegment ls : gcodeLineList) {
                // Find the lines color.
                if (ls.isArc()) {
                    color = VisualizerUtils.Color.RED;
                } else if (ls.isFastTraverse()) {
                    color = VisualizerUtils.Color.BLUE;
                } else if (ls.isZMovement()) {
                    color = VisualizerUtils.Color.GREEN;
                } else {
                    color = VisualizerUtils.Color.WHITE;
                }

                // Override color if it is cutoff
                if (ls.getLineNumber() < this.currentCommandNumber) {
                    color = VisualizerUtils.Color.GRAY;
                }

                // Draw it.
                {
                    Point3f p1 = ls.getStart();
                    Point3f p2 = ls.getEnd();
                    byte[] c = VisualizerUtils.getVertexColor(color);

                    // colors
                    //p1
                    lineColorData[colorIndex++] = c[0];
                    lineColorData[colorIndex++] = c[1];
                    lineColorData[colorIndex++] = c[2];
                    
                    //p2
                    lineColorData[colorIndex++] = c[0];
                    lineColorData[colorIndex++] = c[1];
                    lineColorData[colorIndex++] = c[2];
                    
                    // p1 location
                    lineVertexData[vertIndex++] = (float)p1.x;
                    lineVertexData[vertIndex++] = (float)p1.y;
                    lineVertexData[vertIndex++] = (float)p1.z;
                    //p2
                    lineVertexData[vertIndex++] = (float)p2.x;
                    lineVertexData[vertIndex++] = (float)p2.y;
                    lineVertexData[vertIndex++] = (float)p2.z;
                }
            }
        }
    }
    
    /**
     * Initialize or update open gl geometry array in native buffer objects.
     */
    private void updateGLGeometryArray(GL10 gl) {
        
        // Reset buffer and set to null of new geometry doesn't fit.
        if (lineVertexBuffer != null) {
            lineVertexBuffer.clear();
            if (lineVertexBuffer.remaining() < lineVertexData.length) {
                lineVertexBuffer = null;
            }
        }
        
        if (lineVertexBuffer == null) {
            lineVertexBuffer = FloatBuffer.allocate(lineVertexData.length);
        }
        
        lineVertexBuffer.put(lineVertexData);
        lineVertexBuffer.flip();
        gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, lineVertexBuffer );
    }
    
    /**
     * Initialize or update open gl color array in native buffer objects.
     */
    private void updateGLColorArray(GL10 gl) {
        
        // Reset buffer and set to null of new colors don't fit.
        if (lineColorBuffer != null) {
            lineColorBuffer.clear();

            if (lineColorBuffer.remaining() < lineColorData.length) {
                lineColorBuffer = null;
            }
        }
        
        if (lineColorBuffer == null) {
            lineColorBuffer = ByteBuffer.allocateDirect(this.lineColorData.length);
        }
        
        lineColorBuffer.put(lineColorData);
        lineColorBuffer.flip();
        gl.glColorPointer(3, GL10.GL_UNSIGNED_BYTE, 0, lineColorBuffer );
    }
    
    // For seeing the tool path.
    //private int count = 0;
    //private boolean increasing = true;
    /**
     * Called after each render.
     */
    private void update() {
        if (debugCoordinates) {
            System.out.println("Machine coordinates: " + this.machineCoord.toString());
            System.out.println("Work coordinates: " + this.workCoord.toString());
            System.out.println("-----------------");
        }
        
        /*
        // Increases the cutoff number each frame to show the tool path.
        count++;
        
        if (increasing) currentCommandNumber+=10;
        else            currentCommandNumber-=10;

        if (this.currentCommandNumber > this.lastCommandNumber) increasing = false;
        else if (this.currentCommandNumber <= 0)             increasing = true;
        */ 
    }
    
    /**
     * Called back before the OpenGL context is destroyed. 
     * Release resource such as buffers.
     * GLEventListener method.
     */
    @Override
    public void dispose(GL10 gl) { 
        this.lineColorBuffer = null;
        this.lineVertexBuffer = null;
    }
    
    /**
     * KeyListener method.
     */
    @Override
    public void keyTyped(KeyEvent ke) {
        //System.out.println ("key typed");
    }

    /**
     * KeyListener method.
     */
    @Override
    public void keyPressed(KeyEvent ke) {
        double DELTA_SIZE = 0.1;
            
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                this.eye.y+=DELTA_SIZE;
                break;
            case KeyEvent.VK_DOWN:
                this.eye.y-=DELTA_SIZE;
                break;
            case KeyEvent.VK_LEFT:
                this.eye.x-=DELTA_SIZE;
                break;
            case KeyEvent.VK_RIGHT:
                this.eye.x+=DELTA_SIZE;
                break;
            case KeyEvent.VK_MINUS:
                if (ke.isControlDown())
                    this.zoomOut(1);
                break;
            case KeyEvent.VK_0:
                if (ke.isControlDown()) {
                    this.zoomMultiplier = 1;
                    this.scaleFactor = this.scaleFactorBase;
                }
                break;
            case KeyEvent.VK_ESCAPE:
                this.zoomMultiplier = 1;
                this.scaleFactor = this.scaleFactorBase;
                this.eye.x = 0;
                this.eye.y = 0;
                this.eye.z = 1.5f;
                this.rotation.x = 0;
                this.rotation.y = -30;
                this.rotation.z = 0;
        }
        
        switch(ke.getKeyChar()) {
            case 'p':
                this.eye.z+=DELTA_SIZE;
                break;
            case ';':
                this.eye.z-=DELTA_SIZE;
                break;
            case 'w':
                this.center.y+=DELTA_SIZE;
                break;
            case 's':
                this.center.y-=DELTA_SIZE;
                break;
            case 'a':
                this.center.x-=DELTA_SIZE;
                break;
            case 'd':
                this.center.x+=DELTA_SIZE;
                break;
            case 'r':
                this.center.z+=DELTA_SIZE;
                break;
            case 'f':
                this.center.z-=DELTA_SIZE;
                break;
            case '+':
                if (ke.isControlDown())
                    this.zoomIn(1);
                break;
        }
        
        //System.out.println("Eye: " + eye.toString()+"\nCent: "+cent.toString());
    }
    
    /**
     * KeyListener method.
     */
    @Override
    public void keyReleased(KeyEvent ke) {
        //System.out.println ("key released");
    }

    
    /** Mouse Motion Listener Events **/
    @Override
    public void mouseDragged(MouseEvent me) {
        this.current = me.getPoint();

        float dx = this.current.x - this.last.x;
        float dy = this.current.y - this.last.y;

        if (me.isShiftDown() || me.getModifiers() == this.panMouseButton) {
            if (ortho) {
                // Treat dx and dy as vectors relative to the rotation angle.
                this.eye.x -= ((dx * this.translationVectorH.x * this.panMultiplierX) + (dy * this.translationVectorV.x * panMultiplierY));
                this.eye.y += ((dy * this.translationVectorV.y * panMultiplierY) - (dx * this.translationVectorH.y * this.panMultiplierX));
                this.eye.z -= ((dx * this.translationVectorH.z * this.panMultiplierX) + (dy * this.translationVectorV.z * panMultiplierY));
            } else {
                this.eye.x += dx;
                this.eye.y += dy;
            }
        } else {
            this.rotation.x += dx / 2.0;
            this.rotation.y -= dy / 2.0;
            if (ortho) {
                setHorizontalTranslationVector();
                setVerticalTranslationVector();
            }
        }
        // Now that the motion has been accumulated, reset last.
        this.last = this.current;
    }

    private void setHorizontalTranslationVector() {
        double x = Math.cos(Math.toRadians(this.rotation.x));
        double xz = Math.sin(Math.toRadians(this.rotation.x));

        double y = xz * Math.sin(Math.toRadians(this.rotation.y));
        double yz = xz * Math.cos(Math.toRadians(this.rotation.y));

        translationVectorH = new Vector3d(x, y, yz);
        translationVectorH.normalize();
    }

    private void setVerticalTranslationVector(){
        double y = Math.cos(Math.toRadians(this.rotation.y));
        double yz = Math.sin(Math.toRadians(this.rotation.y));

        translationVectorV = new Vector3d(0, y, yz);
        translationVectorV.normalize();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        // Keep last location up to date so that we're ready to start dragging.
        last = me.getPoint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int delta = e.getWheelRotation();
        if (delta == 0)
            return;

        if (delta > 0) {
            if (this.invertZoom)
                zoomOut(delta);
            else
                zoomIn(delta);
        } else if (delta < 0) {
            if (this.invertZoom)
                zoomIn(delta * -1);
            else
                zoomOut(delta * -1);
        }
    }

    private void zoomOut(int increments) {
        if (ortho) {
            if (this.zoomMultiplier <= this.minZoomMultiplier)
                return;

            this.zoomMultiplier -= increments * zoomIncrement;
            if (this.zoomMultiplier < this.minZoomMultiplier)
                this.zoomMultiplier = this.minZoomMultiplier;

            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        } else {
            this.eye.z += increments;
        }
    }

    private void zoomIn(int increments) {
        if (ortho) {
            if (this.zoomMultiplier >= this.maxZoomMultiplier)
                return;

            this.zoomMultiplier += increments * zoomIncrement;
            if (this.zoomMultiplier > this.maxZoomMultiplier)
                this.zoomMultiplier = this.maxZoomMultiplier;

            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        } else {
            this.eye.z -= increments;
        }
    }

    public double getMinArcLength() {
        return minArcLength;
    }

    public void setMinArcLength(float minArcLength) {
        if (this.minArcLength != minArcLength) {
            this.minArcLength = minArcLength;
            if (this.gcodeFile != null) {
                this.setGcodeFile(this.gcodeFile);
            }
        }
    }

    public double getArcLength() {
        return arcLength;
    }

    public void setArcLength(float arcLength) {
        if (this.arcLength != arcLength) {
            this.arcLength = arcLength;
            if (this.gcodeFile != null) {
                this.setGcodeFile(this.gcodeFile);
            }
        }
    }
}