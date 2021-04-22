package charlesgunn.jreality.newtools;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.Texture2D;

public class TexturePlacementTool extends AbstractTool  {

	 static InputSlot dragActivate = InputSlot.LEFT_BUTTON;
	 static InputSlot scaleActivate = InputSlot.RIGHT_BUTTON;
	 static InputSlot rotateActivate = InputSlot.SHIFT_LEFT_BUTTON;
	 static InputSlot preScaleActivate = InputSlot.SHIFT_RIGHT_BUTTON;
	 static InputSlot pointer = InputSlot.getDevice("PointerTransformation");

	Matrix origTexMatrix;
	Texture2D texture2d;
	private double[] origTexCoords, tformedOTC;
	double[] centerIt = P3.makeTranslationMatrix(null, new double[]{.5,.5,0}, Pn.EUCLIDEAN);
	double[] centerOnCursor;
	int mode = 0;
	Viewer viewer;
		
	public TexturePlacementTool() {
		this(null);
	}
	public TexturePlacementTool(Texture2D td) {
		super(dragActivate, scaleActivate, rotateActivate, preScaleActivate);
		texture2d = td;
	}

	public void activate(ToolContext tc) {
		System.err.println("Activating texture tool, mode = "+mode);
		if (texture2d == null) return;
		addCurrentSlot(pointer, "drags the texture");
		PickResult currentPick = tc.getCurrentPick();
		double[] foo = currentPick.getTextureCoordinates();
		origTexCoords = new double[]{foo[0], foo[1], 0};
		origTexMatrix = new Matrix(texture2d.getTextureMatrix().getArray());
		tformedOTC = Rn.matrixTimesVector(null, origTexMatrix.getArray(), origTexCoords);
		centerOnCursor = P3.makeTranslationMatrix(null, tformedOTC, Pn.EUCLIDEAN);
		if (tc.getSource() == dragActivate) mode = 0;
		else if (tc.getSource() == scaleActivate) mode = 1;
		else if (tc.getSource() == rotateActivate) mode = 2;
		else mode = 3;
	}
	double strength = 3.0;
	public void perform(ToolContext tc) {
		PickResult currentPick = tc.getCurrentPick();
		if (texture2d == null || currentPick == null ||
				currentPick.getTextureCoordinates() == null ||
				currentPick.getTextureCoordinates().length < 1) return;
//		System.err.println("Pick point ="+Rn.toString(tc.getCurrentPick().getObjectCoordinates()));
//		System.err.println("Pick point ="+Rn.toString(tc.getCurrentPick().getTextureCoordinates()));
		double[] foo = currentPick.getTextureCoordinates();
		double[] texCoords = {foo[0], foo[1], 0};
		if (origTexCoords == null) origTexCoords = texCoords;
		double[] tcT = Rn.matrixTimesVector(null, origTexMatrix.getArray(), texCoords);
		double[] diff = Rn.subtract(null, origTexCoords, texCoords); //origTexCoords, texCoords);
		double[] change = null;
		double[] diffT = Rn.subtract(null, tformedOTC, tcT);
		if (mode == 0)	{
			change = P3.makeTranslationMatrix(null, diffT, Pn.EUCLIDEAN);			
		} else if (mode == 1){
			double scale = 1+strength*.2*diff[0];
			change = P3.makeScaleMatrix(null, scale, scale, 1.0);
		} else if (mode == 2)	{
			double angle = -strength*.5*diff[0];
			change = P3.makeRotationMatrixZ(null, angle);
		} else if (mode == 3)	{
			double scale = 1+strength*.2*diff[0];
			change = buildInAspectRatio(scale);
		}
		if (mode < 3) {
			Rn.conjugateByMatrix(change, change, centerOnCursor); //centerIt);
//			Rn.times(origTexMatrix.getArray(), change, origTexMatrix.getArray());
			texture2d.setTextureMatrix(new Matrix(Rn.times(null, change, origTexMatrix.getArray())));
			if (viewer != null) {
//				Rn.times(texture2d.getTextureMatrix().getArray(), change, origTexMatrix.getArray());
				viewer.renderAsync();
			}
			System.err.println("setting texture matrix "+texture2d.getTextureMatrix().toString());
		}
//		else {
//			texture2d.setTextureMatrix(new Matrix(Rn.times(null, origTexMatrix.getArray(), change)));
//		}
	}

    private double[] buildInAspectRatio(double scale) {
		double[] foo;
		double[] change;
		double[] m = origTexMatrix.getArray();
		foo = m.clone();
		foo[3] = foo[7] = foo[11] = 0.0;
		double[] tlate = P3.makeTranslationMatrix(null, new double[]{m[3]/scale, m[7],0}, Pn.EUCLIDEAN);
		change = P3.makeScaleMatrix(null, 1.0/scale, 1.0, 1.0);
		change = Rn.times(null, tlate, Rn.times(null, change, foo));
		texture2d.setTextureMatrix(new Matrix(change));
		return change;
	}
    
	public void deactivate(ToolContext tc) {
		if (texture2d == null) return;
		System.err.println("texmatrix = "+Rn.matrixToJavaString(texture2d.getTextureMatrix().getArray()));
		removeCurrentSlot(pointer);
	}

	public String getDescription(InputSlot slot) {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public Texture2D getTexture2d() {
		return texture2d;
	}

	public void setTexture2d(Texture2D texture2d) {
		this.texture2d = texture2d;
	}
	
	public void setViewer(Viewer v)	{
		viewer = v;
	}
};
