package charlesgunn.jreality.texture;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.Texture2D;

public abstract class TextureFactory {
	Texture2D tex2d = null;
	Appearance ap = null;
	double n = 1,  m = 1,  angle = 0;
	public TextureFactory(Appearance ap)	{
		this.ap = ap;
		if (ap == null) 
			throw new IllegalStateException("Null appearance");
		tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "polygonShader.texture2d", ap, true);
	}

	public TextureFactory()	{
		this(null);
	}

	public void update()	{
	    Matrix mat = new Matrix();	    
	    MatrixBuilder.euclidean().rotateZ(angle).scale(n,m,1.0).assignTo(mat);
	    tex2d.setTextureMatrix(mat);
	}
	
	public double getM() {
		return m;
	}

	public void setM(double m) {
		this.m = m;
	}

	public double getN() {
		return n;
	}

	public void setN(double n) {
		this.n = n;
	}


	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public Texture2D getTexture2D()	{
		return tex2d;
	}

}
