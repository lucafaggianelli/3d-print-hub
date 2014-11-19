package com.tesladocet.threedprinting.gcode;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GcodeView extends GLSurfaceView {

	public GcodeRenderer renderer;
	private boolean touchDown = false;
	private Point3f touchCurrent, touchPrevious;
	
	public GcodeView(Context context) {
		super(context);
		init();
	}
	
	public GcodeView(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}
	
	private void init() {
		touchCurrent = new Point3f();
		touchPrevious = new Point3f();
		renderer = new GcodeRenderer();
		setRenderer(renderer);
	}
	
	public void setGcodeFile(String file) {
		renderer.setGcodeFile(file);
	}
	
	private void handleDrag(MotionEvent event) {
		touchCurrent.x = event.getX();
		touchCurrent.y = event.getY();
		
        float dx = touchCurrent.x - touchPrevious.x;
        float dy = touchCurrent.y - touchPrevious.y;

        renderer.rotation.x += dx / 2.0;
        renderer.rotation.y -= dy / 2.0;
        if (GcodeRenderer.ortho) {
            renderer.setHorizontalTranslationVector();
            renderer.setVerticalTranslationVector();
        }
        // Now that the motion has been accumulated, reset last.
        touchCurrent = touchPrevious;
	}

	private void handleTwoFingers(MotionEvent event) {
		// TODO fix for two fingers
		touchCurrent.x = event.getX(0);
		touchCurrent.y = event.getY(0);
		
		float dx = touchCurrent.x - touchPrevious.x;
        float dy = touchCurrent.y - touchPrevious.y;
        
		if (GcodeRenderer.ortho) {
            // Treat dx and dy as vectors relative to the rotation angle.
            renderer.eye.x -= (dx * renderer.translationVectorH.x * renderer.panMultiplierX) +
            		(dy * renderer.translationVectorV.x * renderer.panMultiplierY);
            renderer.eye.y += (dy * renderer.translationVectorV.y * renderer.panMultiplierY) - 
            		(dx * renderer.translationVectorH.y * renderer.panMultiplierX);
            renderer.eye.z -= (dx * renderer.translationVectorH.z * renderer.panMultiplierX) +
            		(dy * renderer.translationVectorV.z * renderer.panMultiplierY);
        } else {
            renderer.eye.x += dx;
            renderer.eye.y += dy;
        }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int fingers = event.getPointerCount();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDown = true;
			break;

		case MotionEvent.ACTION_MOVE:
			if (fingers == 1) handleDrag(event);
			if (fingers >= 2) handleTwoFingers(event);
			break;
			
		case MotionEvent.ACTION_UP:
			performClick();
			touchDown = false;
			break;
			
		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		double DELTA_SIZE = 0.1;
        
        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                renderer.eye.y += DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                renderer.eye.y -= DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                renderer.eye.x-=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                renderer.eye.x+=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_MINUS:
                if (event.isCtrlPressed())
                    renderer.zoomOut(1);
                break;
            case KeyEvent.KEYCODE_0:
                if (event.isCtrlPressed()) {
                    renderer.zoomMultiplier = 1;
                    renderer.scaleFactor = renderer.scaleFactorBase;
                }
                break;
            case KeyEvent.KEYCODE_ESCAPE:
                renderer.zoomMultiplier = 1;
                renderer.scaleFactor = renderer.scaleFactorBase;
                renderer.eye.x = 0;
                renderer.eye.y = 0;
                renderer.eye.z = 1.5f;
                renderer.rotation.x = 0;
                renderer.rotation.y = -30;
                renderer.rotation.z = 0;
                break;
            case KeyEvent.KEYCODE_P:
                renderer.eye.z+=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_SEMICOLON:
                renderer.eye.z-=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_W:
                renderer.center.y+=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_S:
                renderer.center.y-=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_A:
                renderer.center.x-=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_D:
                renderer.center.x+=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_R:
                renderer.center.z+=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_F:
                renderer.center.z-=DELTA_SIZE;
                break;
            case KeyEvent.KEYCODE_PLUS:
                if (event.isCtrlPressed())
                    renderer.zoomIn(1);
                break;
        }
		return true;
	}
	
	@Override
	public boolean performClick() {
		return super.performClick();
	}
}