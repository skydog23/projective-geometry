package charlesgunn.jreality.newtools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import charlesgunn.jreality.tools.UserTool;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jtem.projgeom.PlueckerLineGeometry;

public class SelectLineTool extends UserTool {

	private SceneGraphComponent[] pointRepn = new SceneGraphComponent[3],
		whereAttached = new SceneGraphComponent[3];
	double[][] points = new double[3][];
	int which = 0;
	Color[] colors = {Color.green, Color.yellow, Color.PINK};
	double sphereRadius = .012, tubeRadius = .006;
	SceneGraphComponent worldSGC;
	SceneGraphPath attachmentPath = null;
	double[] world2attach = new double[16], object2attach = new double[16];
	
	public SelectLineTool(SceneGraphPath apath)	{
		super();
		worldSGC = apath.getLastComponent();
		this.attachmentPath = apath;
	}
	@Override
	public void activate(ToolContext tc) {
		super.activate(tc);
		if (button > 2) return;
		if (button == 1)	{
			which = shift ? 1 : 0;
		}
		else which = 2;
		System.err.println("Shift is "+shift);
		if (whereAttached[which] != null && whereAttached[which].isDirectAncestor(pointRepn[which]))
			whereAttached[which].removeChild(pointRepn[which]);
		perform(tc);
	}


	public void perform(ToolContext tc) {
//		System.err.println("In perform");
		super.perform(tc);
		if (button > 2) return;
		PickResult currentPick = tc.getCurrentPick();
		if (currentPick == null) return;
		if (currentPick.getPickType() != PickResult.PICK_TYPE_LINE) return;
		int whichEdge = currentPick.getIndex();
		System.err.println("path "+tc.getRootToLocal());
		System.err.println("Edge "+whichEdge);
		IndexedLineSet ils = (IndexedLineSet) currentPick.getPickPath().getLastElement();
		double[][] verts = IndexedLineSetUtility.extractCurve(null, ils, whichEdge);
		double[] position = currentPick.getObjectCoordinates();
		position = PlueckerLineGeometry.projectPointOntoLine(null, position, verts[0], verts[1], Pn.EUCLIDEAN);
		if (pointRepn[which] == null)	{
			pointRepn[which] = Primitives.sphere(sphereRadius, position);
			pointRepn[which].setOwner(this);
			pointRepn[which].getAppearance().setAttribute(CommonAttributes.PICKABLE, false);
			pointRepn[which].getAppearance().setAttribute("polygonShader.diffuseColor", colors[which]);					
		}
		double[] o2w = currentPick.getPickPath().getMatrix(null);
		attachmentPath.getInverseMatrix(world2attach);
		Rn.times(object2attach, world2attach, o2w);
		double[] attachedPosition = Rn.matrixTimesVector(null, object2attach, position);
		Pn.dehomogenize(attachedPosition, attachedPosition);
		points[which] = attachedPosition;
		if (pointRepn[0] != null && pointRepn[1] != null)	{
			if (which < 2)	{
				System.err.println("Point0: "+Rn.toString(points[0]));
				System.err.println("Point1: "+Rn.toString(points[1]));
				fireChanged(PickResult.PICK_TYPE_LINE);				
			} else {
				fireChanged(PickResult.PICK_TYPE_POINT);
			}
		}
		// update the transformation w/o actually changing the sgc
		SceneGraphComponent sgc = Primitives.sphere(sphereRadius, position);
		sgc.getTransformation().setReadOnly(true);
		pointRepn[which].setTransformation(sgc.getTransformation());
		SceneGraphPath sgp = tc.getRootToLocal();	
		whereAttached[which] = sgp.getLastComponent();
		if (!whereAttached[which].isDirectAncestor(pointRepn[which]))
				whereAttached[which].addChild(pointRepn[which]);
		viewer.renderAsync();
	}


	@Override
	public void deactivate(ToolContext tc) {
		super.deactivate(tc);
	}
	
	@Override
	public ImageIcon getIcon(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "select line";
	}	


	@Override
	public void registerHelp(HelpOverlay overlay) {
	}


	@Override
	public SceneGraphPath getAttachmentPath() {
		return attachmentPath;
	}
	
	public double getSphereRadius() {
		return sphereRadius;
	}

	public void setSphereRadius(double sphereRadius) {
		this.sphereRadius = sphereRadius;
	}
	public SceneGraphComponent getWorldSGC() {
		return worldSGC;
	}
	
	public double[] getLine()	{
		return PlueckerLineGeometry.lineFromPoints(null, points[0], points[1]);
	}
	
	public double[] getPoint()	{
		return points[2];
	}
	
	public class LineSelectionEvent	{
		int type;
		public LineSelectionEvent(SelectLineTool slt, int t)	{
			this.slt = slt;
			type = t;
		}
		SelectLineTool slt;
		public SelectLineTool getSelectLineTool() {
			return slt;
		}
		public int getType() {
			return type;
		}
	}
	public interface LineSelectionListener {
		public void selectionChanged(LineSelectionEvent ev);
	}
	List<LineSelectionListener> listeners = new ArrayList<LineSelectionListener>();
	
	public void addLineSelectionListener(LineSelectionListener lst)	{
		listeners.add(lst);
	}
	
	public void removeLineSelectionListener(LineSelectionListener lst)	{
		listeners.remove(lst);
	}
	
	public void fireChanged(int type)	{
		LineSelectionEvent lse = new LineSelectionEvent(this, type);
		for (LineSelectionListener lst : listeners)	{
			lst.selectionChanged(lse);
		}
	}
//	public void setWorldSGC(SceneGraphComponent worldSGC) {
//		if (this.worldSGC != null && this.worldSGC.isDirectAncestor(lineSGC))
//			worldSGC.removeChild(lineSGC);
//		this.worldSGC = worldSGC;
//		this.worldSGC.addChild(lineSGC);
//	}
	
	
}
