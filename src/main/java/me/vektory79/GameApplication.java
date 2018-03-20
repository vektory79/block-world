package me.vektory79;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import me.vektory79.jme3.cubeterrain.TerrainNode;

/**
 * The game application class.
 */
public class GameApplication extends SimpleApplication {
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

        createIllumination();

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
        TerrainNode terrain = new TerrainNode(this.assetManager, "terrain", 1024 * 7);
        terrain.fillMap(8, 1);
        terrain.updateBuffers();
        rootNode.attachChild(terrain);

        cam.setLocation(new Vector3f(512, 100, 512));
        cam.lookAt(new Vector3f(1, -50, 1), Vector3f.UNIT_Y);
//        cam.setLocation(new Vector3f(0, 2, 0));
//        cam.lookAtDirection(new Vector3f(1, 0, 1), Vector3f.UNIT_Y);
    }

    private void createIllumination() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(al);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        SSAOFilter ssaoFilter = new SSAOFilter(5.1f, 1.2f, 0.2f, 0.1f);
        fpp.addFilter(ssaoFilter);

        viewPort.addProcessor(fpp);
    }
}