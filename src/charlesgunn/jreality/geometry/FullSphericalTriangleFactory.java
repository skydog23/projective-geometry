package charlesgunn.jreality.geometry;

import de.jreality.math.Rn;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

public class FullSphericalTriangleFactory {

	SphericalTriangleFactory triangle = new SphericalTriangleFactory(),
		dualTriangle = new SphericalTriangleFactory();
	SceneGraphComponent both = new SceneGraphComponent("both");
	
	public FullSphericalTriangleFactory(SphericalTriangleFactory o)	{
		triangle = o;
		triangle.setDual(false);
		dualTriangle.setDual(true);
		update();
		DragEventTool t = new DragEventTool();
		t.addPointDragListener(new PointDragListener() {

			public void pointDragStart(PointDragEvent e) {
//				System.out.println("drag start of vertex no "+e.getIndex());				
			}

			public void pointDragged(PointDragEvent e) {
				PointSet pointSet = e.getPointSet();
				double[][] verts=new double[pointSet.getNumPoints()][];
		        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(verts);
				double[] position = new double[3];
				for (int i = 0; i<3; ++i) position [i] = e.getPosition()[i];
				Rn.normalize(position, position);
				for (int i = 0; i<3; ++i) verts[e.getIndex()][i] = position[i];
				//verts[e.getIndex()][3] = 1.0;
		        triangle.setVerts(verts);
		        update();
			}

			public void pointDragEnd(PointDragEvent e) {
			}
			
		});
		
		triangle.getVerticesSGC().addTool(t);
		both.addChildren(triangle.getSceneGraphComponent(), dualTriangle.getSceneGraphComponent());
	}
	
	public void update()	{
		triangle.update();
		double[][] sides = triangle.getSides().clone();
		dualTriangle.setVerts(sides);
		dualTriangle.update();
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return both;
	}

	public SphericalTriangleFactory getTriangle() {
		return triangle;
	}

	public SphericalTriangleFactory getDualTriangle() {
		return dualTriangle;
	}
}
