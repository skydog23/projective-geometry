package charlesgunn.jreality.geometry;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;

/**
 * @author gunn
 *
 */
public class LabelSet extends Geometry {
	public LabelSet()	{
		this("unnamed");
	}
	public LabelSet(String name) {
		super(name);
	}
	public static final int GLUT_BITMAP_9_BY_15		= 2;
	public static final int GLUT_BITMAP_8_BY_13		= 3;
	public static final int GLUT_BITMAP_TIMES_ROMAN_10	= 4;
	public static final int GLUT_BITMAP_TIMES_ROMAN_24	= 5;
	public static final int GLUT_BITMAP_HELVETICA_10	= 6;
	public static final int GLUT_BITMAP_HELVETICA_12	= 7;
	public static final int GLUT_BITMAP_HELVETICA_18	= 8;

	String[] labels;
	DataList positions;
	double[] ndcOffset = {.015, .015,-.01};
	int bitmapFont = GLUT_BITMAP_HELVETICA_12;

	
	public static LabelSet labelSetFactory(DataList p, String[] l) {
		LabelSet ls = new LabelSet();
		ls.setPositions(p);
		ls.setLabels(l);
		if (ls.getPositions() == null) return null;
		return ls;
	}
	
	public static LabelSet labelSetFactory(PointSet p, String[] l) {
		LabelSet ls = new LabelSet();
		ls.setPositions(p.getVertexAttributes(Attribute.COORDINATES));
		ls.setLabels(l);
		if (ls.getPositions() == null) return null;
		return ls;
	}
	
	/**
	 * @return
	 */
	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] l) {
		if (l == null) { labels = null; return; }
		if (l.length != positions.size())	{
			throw new IllegalArgumentException("Need as many labels as positions");
		}
		labels = l;
	}

	public DataList getPositions() {
		return positions;
	}
	public void setPositions(DataList positions) {
		this.positions = positions;
	}
	public double[] getNDCOffset() {
		return ndcOffset;
	}
	public void setNDCOffset(double[] offset) {
		this.ndcOffset =offset;
	}

	public int getBitmapFont() {
		return bitmapFont;
	}
	public void setBitmapFont(int bitmapFont) {
		this.bitmapFont = bitmapFont;
	}
}
