package me.vektory79;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.CompareMode;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import me.vektory79.jme3.cubeterrain.TerrainNode;

/**
 * The game application class.
 */
public class GameApplication extends SimpleApplication {
    private static final int SHADOWMAP_SIZE = 1024 * 4;

    private final boolean debug;

    public GameApplication(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void start() {
        this.settings.setResizable(true);
        this.settings.setUseInput(!debug);
//        this.settings.setFrameRate(10);
        this.setPauseOnLostFocus(!debug);
        this.setSettings(settings);
        super.start();
    }

    @Override
    public void simpleInitApp() {
        createFastMap();

        //createIllumination();

        cam.setFrustumFar(65000f);
        if (!debug) {
            flyCam.setMoveSpeed(10);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        System.exit(0);
    }

    private void createFastMap() {
        TerrainNode terrain = new TerrainNode(cam, this.assetManager, "terrain", 1024*7);
        terrain.fillMap(8, 1);
        terrain.updateBuffers();
        rootNode.attachChild(terrain);

/*
        Box mesh = new Box(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        Geometry geom = new Geometry("A shape", mesh); // wrap shape into geometry
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");   // create material
        geom.setMaterial(mat);                         // assign material to geometry
        rootNode.attachChild(geom);
*/

        cam.setLocation(new Vector3f(512, 100, 512));
        cam.lookAt(new Vector3f(1, 0, 1), Vector3f.UNIT_Y);
//        cam.setLocation(new Vector3f(0, 2, 0));
//        cam.lookAtDirection(new Vector3f(1, 0, 1), Vector3f.UNIT_Y);
    }

    @SuppressWarnings("Duplicates")
    private void createIllumination() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(al);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 4);
        dlsr.setLight(sun);
        dlsr.setLambda(0.9f);
        dlsr.setShadowIntensity(0.6f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
        dlsr.setEnabledStabilization(true);
        dlsr.setEdgesThickness(1);
        dlsr.setShadowCompareMode(CompareMode.Hardware);
        viewPort.addProcessor(dlsr);

        SSAOFilter ssaoFilter = new SSAOFilter(4f, 15f, 0.33f, 0.60f);
        fpp.addFilter(ssaoFilter);

        viewPort.addProcessor(fpp);
    }
}