package com.tesladocet.threedprinting.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class StlObject {
	
	private final static String TAG = "StlObj";
	
	private Listener listener;
	private byte[] stlBytes = null;
	private List<Float> normalList;
	private FloatBuffer triangleBuffer;
	
	public float volume;
	public int trianglesCount;
	public float maxX;
	public float maxY;
	public float maxZ;
	public float minX;
	public float minY;
	public float minZ;

	private ProgressDialog prepareProgressDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle("Progess...");
		progressDialog.setMax(0);
		//progressDialog.setMessage(context.getString(R.string.stl_load_progress_message));
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(false);
		
		progressDialog.show();
		
		return progressDialog;
	}
	
	public StlObject(byte[] stlBytes, Context context, Listener l) {
		this.stlBytes = stlBytes;
		listener = l;
		
		processSTL(stlBytes, context);
	}
	
	private void adjustMaxMin(float x, float y, float z) {
		if (x > maxX) {
			maxX = x;
		}
		if (y > maxY) {
			maxY = y;
		}
		if (z > maxZ) {
			maxZ = z;
		}
		if (x < minX) {
			minX = x;
		}
		if (y < minY) {
			minY = y;
		}
		if (z < minZ) {
			minZ = z;
		}
	}

	private int getIntWithLittleEndian(byte[] bytes, int offset) {
		return (0xff & stlBytes[offset]) | ((0xff & stlBytes[offset + 1]) << 8) | ((0xff & stlBytes[offset + 2]) << 16) | ((0xff & stlBytes[offset + 3]) << 24);
	}
	
	/**
	 * checks 'text' in ASCII code
	 * 
	 * @param bytes
	 * @return
	 */
	boolean isText(byte[] bytes) {
		for (byte b : bytes) {
			if (b == 0x0a || b == 0x0d || b == 0x09) {
				// white spaces
				continue;
			}
			if (b < 0x20 || (0xff & b) >= 0x80) {
				// control codes
				return false;
			}
		}
		return true;
	}
	
	/**
	 * FIXME 'STL format error detection' depends exceptions.
	 * 
	 * @param stlBytes
	 * @param context
	 * @param progressDialog
	 * @return
	 */
	private boolean processSTL(byte[] stlBytes, final Context context) {
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		normalList = new ArrayList<Float>();
		
		final ProgressDialog progressDialog = prepareProgressDialog(context);

		final AsyncTask<byte[], Integer, List<Float>> task = new AsyncTask<byte[], Integer, List<Float>>() {

			List<Float> processText(String stlText) throws Exception {
				List<Float> vertexList = new ArrayList<Float>();
				float[][] tri = new float[3][];
				normalList.clear();
				volume = 0;

				String[] stlLines = stlText.split("\n");
				
				progressDialog.setMax(stlLines.length);
				
				int j = 0;
				for (int i = 0; i < stlLines.length; i++) {
					String string = stlLines[i].trim();
					if (string.startsWith("facet normal ")) {
						string = string.replaceFirst("facet normal ", "");
						String[] normalValue = string.split(" ");
						normalList.add(Float.parseFloat(normalValue[0]));
						normalList.add(Float.parseFloat(normalValue[1]));
						normalList.add(Float.parseFloat(normalValue[2]));
						Log.i(TAG, "normal add");
					}
					
					if (string.startsWith("vertex ")) {
						string = string.replaceFirst("vertex ", "");
						String[] vertexValue = string.split(" ");
						float x = Float.parseFloat(vertexValue[0]);
						float y = Float.parseFloat(vertexValue[1]);
						float z = Float.parseFloat(vertexValue[2]);
						adjustMaxMin(x, y, z);
						vertexList.add(x);
						vertexList.add(y);
						vertexList.add(z);
						tri[j++] = new float[] {x,y,z};
						if (j == 3) volume += signedVolumeOfTriangle(tri[0], tri[1], tri[2]);
						j %= 3;
					}
					
					if (i % (stlLines.length / 50) == 0) {
						publishProgress(i);
					}
				}
				
				return vertexList;
			}
			
			List<Float> processBinary(byte[] stlBytes) throws Exception {
				List<Float> vertexList = new ArrayList<Float>();
				float[] p1,p2,p3;
				normalList.clear();
				volume = 0;
				
				// Header size is 80bytes
				trianglesCount = getIntWithLittleEndian(stlBytes, 80);
				Log.i(TAG, "vectorSize:" + trianglesCount);
				
				progressDialog.setMax(trianglesCount);
				for (int i = 0; i < trianglesCount; i++) {
					normalList.add(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50)));
					normalList.add(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 4)));
					normalList.add(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 8)));
					
					float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
					float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
					float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
					adjustMaxMin(x, y, z);
					vertexList.add(x);
					vertexList.add(y);
					vertexList.add(z);
					p1 = new float[] {x,y,z};
					
					x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
					y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
					z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
					adjustMaxMin(x, y, z);
					vertexList.add(x);
					vertexList.add(y);
					vertexList.add(z);
					p2 = new float[] {x,y,z};
					
					x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
					y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
					z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
					adjustMaxMin(x, y, z);
					vertexList.add(x);
					vertexList.add(y);
					vertexList.add(z);
					p3 = new float[] {x,y,z};
					
					volume += signedVolumeOfTriangle(p1, p2, p3);
					
					if (i % (trianglesCount / 50) == 0) {
						publishProgress(i);
					}
				}
				
				return vertexList;
			}
			
			@Override
			protected List<Float> doInBackground(byte[]... stlBytes) {
				List<Float> processResult = null;
				try {
					if (isText(stlBytes[0])) {
						Log.i(TAG, "trying text...");
						processResult = processText(new String(stlBytes[0]));
					} else {
						Log.i(TAG, "trying binary...");
						processResult = processBinary(stlBytes[0]);
					}
				} catch (Exception e) {
				}
				if (processResult != null && processResult.size() > 0 && normalList != null && normalList.size() > 0) {
					return processResult;
				}
				
				return new ArrayList<Float>();
			}
			
			@Override
			public void onProgressUpdate(Integer... values) {
				progressDialog.setProgress(values[0]);
			}
			
			@Override
			protected void onPostExecute(List<Float> vertexList) {
				
				Log.i(TAG, "normalList.size: " + normalList.size());
				Log.i(TAG, "vertexList.size: " + vertexList.size());
				Log.i(TAG, "triangles: " + trianglesCount);
				Log.i(TAG, "volume (mm^3): " + volume);
				
				if (normalList.size() < 1 || vertexList.size() < 1) {
					//Toast.makeText(context, context.getString(R.string.error_fetch_data), Toast.LENGTH_LONG).show();
					
					progressDialog.dismiss();
					return;
				}
				
				float[] vertexArray = listToFloatArray(vertexList);
				ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
				vbb.order(ByteOrder.nativeOrder());
				triangleBuffer = vbb.asFloatBuffer();
				triangleBuffer.put(vertexArray);
				triangleBuffer.position(0);
				
				listener.onLoaded();

				progressDialog.dismiss();
			}
		};

		try {
			task.execute(stlBytes);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	/**
	 * Compute the volume of a tetrahedron in mm^3
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	private float signedVolumeOfTriangle(float[] p1, float[] p2, float[] p3) {
		float v321 = p3[0]*p2[1]*p1[2];
		float v231 = p2[0]*p3[1]*p1[2];
		float v312 = p3[0]*p1[1]*p2[2];
		float v132 = p1[0]*p3[1]*p2[2];
		float v213 = p2[0]*p1[1]*p3[2];
		float v123 = p1[0]*p2[1]*p3[2];
		
		return (1f/6f) * (-v321 + v231 + v312 - v132 - v213 + v123);
	}
	
	private float[] listToFloatArray(List<Float> list) {
		float[] result = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	public void draw(GL10 gl) {
		if (normalList == null || triangleBuffer == null) {
			return;
		}
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleBuffer);
		
		for (int i = 0; i < normalList.size() / 3; i++) {
			gl.glNormal3f(normalList.get(i * 3), normalList.get(i * 3 + 1), normalList.get(i * 3 + 2));
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 3, 3);
		}

	}
	
	abstract static class Listener {
		abstract void onLoaded();
	}
}