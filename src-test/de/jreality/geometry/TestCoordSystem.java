package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;

public class TestCoordSystem {

public static void main(String[] args) {
    SceneGraphComponent base = new SceneGraphComponent();
    SceneGraphComponent content = new SceneGraphComponent();
    SceneGraphComponent content2 = new SceneGraphComponent();
    content.addChild(content2);
   content2.setGeometry(Primitives.icosahedron());

   MatrixBuilder.euclidean().translate(2,0,0).assignTo(content2);
    CoordinateSystemFactory csf = new CoordinateSystemFactory(content);
    csf.showAxes(false);
    csf.showGrid(true);
    csf.showBox(true);
    csf.setBoxColor(Color.blue);
    csf.setLabelColor(Color.red);
    csf.setLabelScale(0.0015);
    csf.setAxisScale(0.2);
    csf.showLabels(true);
    base.addChild(content);
   
    JRViewer.display(base);
}
}

