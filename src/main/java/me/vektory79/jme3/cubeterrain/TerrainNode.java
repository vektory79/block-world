package me.vektory79.jme3.cubeterrain;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import me.vektory79.mapgen.MapGenerator;
import org.jetbrains.annotations.NotNull;

import static me.vektory79.jme3.cubeterrain.AbstractTerrainBlockBuffer.CHUNK_DIMENSION;
import static me.vektory79.jme3.cubeterrain.AbstractTerrainBlockBuffer.CHUNK_DIMENSION_FACTOR;
import static me.vektory79.jme3.cubeterrain.BlockTypeDescriptorsBuffer.Type.*;

public class TerrainNode extends Node {
    private final TerrainChunksMesh mesh;
    private final Material material;
    private Camera cam;

    private final Material mat;

    public TerrainNode(Camera cam, @NotNull AssetManager contentMan, String name, int chunks) {
        super(name);
        this.cam = cam;
        mat = new Material(contentMan, "Common/MatDefs/Misc/ShowNormals.j3md");   // create material


        DebugBuffer debug = new DebugBuffer(5, chunks);

        mesh = new TerrainChunksMesh(chunks);
        material = new Material(contentMan, "BlockWorld/Materials/BlockTerrain.j3md");
        material.setShaderStorageBufferObject("ChunkDescriptors", mesh.getChunkDescriptors());
        material.setShaderStorageBufferObject("BlockTypeDescriptors", mesh.getBlockTypeDescriptors());
        material.setShaderStorageBufferObject("EdgeTemplatesIndex", mesh.getEdgeTemplatesIndex());
        material.setShaderStorageBufferObject("EdgeTemplates", mesh.getEdgeTemplates());
        material.setShaderStorageBufferObject("DebugBuffer", debug);

        Texture texture = contentMan.loadTexture("BlockWorld/Textures/block-textures.png");
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        material.setTexture("ColorMap", texture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Geometry opaque = new Geometry(name + "_opaque");
        opaque.setQueueBucket(RenderQueue.Bucket.Opaque);
        opaque.setMesh(mesh);
        opaque.setMaterial(material);
        opaque.getMaterial().setBoolean("DrawTransparent", false);
        opaque.setCullHint(CullHint.Never);
        this.attachChild(opaque);

/*
        Geometry transparent = new Geometry(name + "_transparent");
        transparent.setQueueBucket(RenderQueue.Bucket.Transparent);
        transparent.setMesh(mesh);
        transparent.setMaterial(material);
        transparent.getMaterial().setBoolean("DrawTransparent", true);
        transparent.setCullHint(CullHint.Never);
        this.attachChild(transparent);
*/
        cullHint = CullHint.Never;

/*
        addControl(new AbstractControl() {
            private long lastTime = System.nanoTime();

            @Override
            protected void controlUpdate(float tpf) {
                long currTime = System.nanoTime();
                if (currTime - lastTime > 1000000000) {
                    lastTime = currTime;
                    System.out.println(String.format("X: %f\tY: %f\tZ: %f", cam.getLocation().x, cam.getLocation().y, cam.getLocation().z));
                }
            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {

            }
        });
*/
    }

    public void updateBuffers() {
        material.setShaderStorageBufferObject("ChunkDescriptors", mesh.getChunkDescriptors());
        material.setShaderStorageBufferObject("BlockTypeDescriptors", mesh.getBlockTypeDescriptors());
    }

    public final TerrainChunksMesh getMesh() {
        return mesh;
    }

    public void fillMap(double roughness, long seed) {
        MapGenerator map = new MapGenerator(10, roughness, seed);
        map.generate();
        for (int chunkZ = 0; chunkZ < 64; chunkZ++) {
            for (int chunkX = 0; chunkX < 64; chunkX++) {
                fillChunks(map, chunkX, chunkZ);
            }
        }
        mesh.rebuildCulling();
        setRequiresUpdates(true);
    }

    private void fillChunks(MapGenerator map, int chunkX, int chunkZ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int xStart = chunkX << CHUNK_DIMENSION_FACTOR;
        int zStart = chunkZ << CHUNK_DIMENSION_FACTOR;
        for (int z = zStart; z < zStart + CHUNK_DIMENSION ; z++) {
            for (int x = xStart; x < xStart + CHUNK_DIMENSION ; x++) {
                int current = convertHeight(map.getData(x, z));
                if (current > max) {
                    max = current;
                }
                if (current < min) {
                    min = current;
                }
            }
        }
        int chunkYMin = min >> CHUNK_DIMENSION_FACTOR;
        int chunkYMax = max >> CHUNK_DIMENSION_FACTOR;
        fillChunks(map, chunkX, chunkZ, chunkYMin, chunkYMax);
    }

    private void fillChunks(MapGenerator map, int chunkX, int chunkZ, int chunkYMin, int chunkYMax) {
        for (int chunkY = chunkYMin; chunkY <= chunkYMax; chunkY++) {
            fillChunk(map, chunkX, chunkY, chunkZ);
        }
    }

    private void fillChunk(MapGenerator map, int chunkX, int chunkY, int chunkZ) {
        int xStart = chunkX << CHUNK_DIMENSION_FACTOR;
        int yStart = chunkY << CHUNK_DIMENSION_FACTOR;
        int zStart = chunkZ << CHUNK_DIMENSION_FACTOR;

/*
        Box boxMesh = new Box(new Vector3f(xStart, yStart, zStart), new Vector3f(xStart + 1, yStart + 1, zStart + 1));
        Geometry geom = new Geometry("A shape", boxMesh); // wrap shape into geometry
        geom.setMaterial(mat);                         // assign material to geometry
        attachChild(geom);
*/

        for (int z = zStart; z < zStart + CHUNK_DIMENSION; z++) {
            for (int x = xStart; x < xStart + CHUNK_DIMENSION; x++) {
                int height = convertHeight(map.getData(x, z));
                for (int y = yStart; y < yStart + CHUNK_DIMENSION; y++) {
                    if (y > height) {
                        mesh.getBlockType().setType(Position.get(x, y, z), AIR);
                    } else if (height >= -2 && height <= 2 && y >= height - 3) {
                        mesh.getBlockType().setType(Position.get(x, y, z), SAND);
                    } else if (height > 2 && y >= height - 3) {
                        mesh.getBlockType().setType(Position.get(x, y, z), DIRT);
                    } else {
                        mesh.getBlockType().setType(Position.get(x, y, z), STONE);
                    }
                }
            }
        }
    }

    private int convertHeight(double mapHeight) {
        return (int) (mapHeight * 25.0);
    }
}
