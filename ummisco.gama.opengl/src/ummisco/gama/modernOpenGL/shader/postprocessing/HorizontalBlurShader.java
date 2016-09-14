package ummisco.gama.modernOpenGL.shader.postprocessing;

import com.jogamp.opengl.GL2;

public class HorizontalBlurShader extends AbstractPostprocessingShader {

	private static String VERTEX_FILE = "horizontalBlurVertex.txt";
	private static String FRAGMENT_FILE = "blurFragment.txt";
	
	private int location_targetWidth;
	
	public HorizontalBlurShader(GL2 gl) {
		super(gl,VERTEX_FILE,FRAGMENT_FILE);
	}
	
	public HorizontalBlurShader(HorizontalBlurShader shader) {
		super(shader.gl,VERTEX_FILE,FRAGMENT_FILE);
	}
	
	@Override
	protected void getAllUniformLocations() {
		super.getAllUniformLocations();
		location_targetWidth = getUniformLocation("targetWidth");
	}

	public void loadTargetWidth(float width){
		super.loadFloat(location_targetWidth, width);
	}
}