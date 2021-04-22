package charlesgunn.jreality.geometry;

import java.awt.Color;
import java.util.Random;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;
import de.jtem.numericalMethods.geometry.hyperSurface.MarchingCubes;

public class MarchingCubesTest {

  private static Camera camera;
  private static ViewerApp va;
  static SceneGraphComponent sgc = SceneGraphUtility
          .createFullSceneGraphComponent();
  static Random r = new Random();
    public static void main(String[] args) {
            IndexedFaceSetFactory ilsf_s = new IndexedFaceSetFactory();

      int size = 4;
      double x = 0.5;

      double[] min = new double[] { 0, 0, 0 };
      double[] max = new double[] { size, size, size };

      double[][][] grid = new double[size][size][size];

      for (int i = 0; i < grid.length; i++) {
          for (int j = 0; j < grid.length; j++) {
              for (int k = 0; k < grid.length; k++) {
                  if(r.nextBoolean() == true)
                      grid[i][j][k] = i + j/2*k+1;
                  else grid[i][j][k] = -1;
              }
          }
      }
      System.err.println("done, grid dimensions are: "+size+" x "+ size+" x "+size);
     
      MarchingCubes mc = new MarchingCubes(grid, 1, min, max);

      ilsf_s.setVertexCount(mc.getPoints().length/3);
      ilsf_s.setVertexCoordinates(mc.getPoints());
      ilsf_s.setFaceCount(mc.getIndices().length / 3);
      ilsf_s.setFaceIndices(mc.getIndices(), 3);
      ilsf_s.setGenerateEdgesFromFaces(true);
      ilsf_s.setGenerateFaceNormals(true);
      ilsf_s.update();

      sgc.setGeometry(ilsf_s.getIndexedFaceSet());
      va = new ViewerApp(MarchingCubesTest.sgc);
      va.setBackgroundColor(Color.white);
      camera = (Camera) va.getViewerSwitch().getCameraPath().getLastElement();
      camera.setOnAxis(false);
      camera.setFieldOfView(30);
      camera.setPerspective(false);
      va.update();
      va.display();
  }
}
