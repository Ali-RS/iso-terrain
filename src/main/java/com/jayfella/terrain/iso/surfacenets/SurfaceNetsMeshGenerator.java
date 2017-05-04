package com.jayfella.terrain.iso.surfacenets;

import com.jayfella.terrain.iso.DensityVolume;
import com.jayfella.terrain.iso.MeshGenerator;
import com.jayfella.terrain.iso.volume.ArrayDensityVolume;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 30/04/2017.
 */
public class SurfaceNetsMeshGenerator implements MeshGenerator {

    private int cx;
    private int cy;
    private int cz;
    private int[] masks;
    private int[] cells;
    private int cellIndex = 0;
    private int triangleCount = 0;
    private int vertCount = 0;
    private int maskIndex = 0;
    private boolean[] edgeHit = new boolean[12];
    private int[][][][] edgeVerts;
    private float xzScale = 1;

    public SurfaceNetsMeshGenerator( int cx, int cy, int cz ) {
        this(cx, cy, cz, 1);
    }

    public SurfaceNetsMeshGenerator( int cx, int cy, int cz, float xzScale ) {
        //cx++;
        //cy++;
        //cz++;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.masks = new int[cx * cy * cz];
        this.cells = new int[cx * cy * cz];
        this.edgeVerts = new int[cx+1][cy+1][cz+1][3];
        this.xzScale = xzScale;
    }

    @Override
    public Vector3f getRequiredVolumeSize() {
        // When the creator passed in a size of "cells", we actually
        // sample corners and so neded +1
        // However, we also sample an extra border on all sides which
        // is another +2... +3 in all.  But the +1 is already incorporated
        // into our size fields.
        return new Vector3f(cx + 2, cy + 2, cz + 2);
    }

    @Override
    public Vector3f getGenerationSize() {
        int x = cx - 1;
        int y = cy - 1;
        int z = cz - 1;
        return new Vector3f(x * xzScale, y, z * xzScale);
    }

    private static final int[] cube_edges = new int[24];
    private static final int[] edge_table = new int[256];
    private static int[] vertexBuffer = new int[4096];

    void initCubeEdges() {
        int k = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 1; j <= 4; j <<= 1) {
                int p = i ^ j;
                if (i <= p) {
                    cube_edges[k++] = i;
                    cube_edges[k++] = p;
                }
            }
        }
    }

    void initEdgeTable() {
        for (int i = 0; i < 256; ++i) {
            int em = 0;
            for (int j = 0; j < 24; j += 2) {
                boolean a = bool(i & (1 << cube_edges[j]));
                boolean b = bool(i & (1 << cube_edges[j + 1]));
                em |= a != b ? (1 << (j >> 1)) : 0;
            }
            edge_table[i] = em;
        }
    }

    @Override
    public Mesh buildMesh(DensityVolume volume) {

        initCubeEdges();
        initEdgeTable();

        float[] data = ((ArrayDensityVolume)volume).getDenistyArray();

        // location (location[0]=x, location[1]=y, location[2]=z)
        final int[] location = new int[3];

        // layout for one-dimensional data array
        // we use this to reference vertex buffer
        final int[] R = {
                // x
                1,
                // y * width
                // cx + 1,
                cx + 1,
                // z * width * height
                // (cx + 1) * (cy + 1)
                (cx + 1) * (cy + 1)
        };
        // grid cell
        final double grid[] = new double[8];

        // TODO: is is the only mystery that is left
        int buf_no = 1;


        // Resize buffer if necessary
        if (R[2] * 2 > vertexBuffer.length) {
            vertexBuffer = new int[R[2] * 2];
        }

        // we make some assumptions about the number of vertices and faces
        // to reduce GC overhead
        final List<double[]> vertices = new ArrayList<>(vertexBuffer.length/2);
        final List<int[]> faces = new ArrayList<>(vertices.size()/4);

        final List<Vector3f> normals = new ArrayList<>();

        int n = 0;

        // March over the voxel grid
        // for (location[2] = 0; location[2] < dims[2] - 1; ++location[2], n += cx, buf_no ^= 1 /*even or odd*/, R[2] = -R[2]) {
        for (location[2] = 0; location[2] < cx - 1; ++location[2], n += cz, buf_no ^= 1 /*even or odd*/, R[2] = -R[2]) {

            // m is the pointer into the buffer we are going to use.
            // This is slightly obtuse because javascript does not
            // have good support for packed data structures, so we must
            // use typed arrays :(
            // The contents of the buffer will be the indices of the
            // vertices on the previous x/y slice of the volume
            int m = 1 + (cx + 1) * (1 + buf_no * (cy + 1));

            for (location[1] = 0; location[1] < cy - 1; ++location[1], ++n, m += 2) {
                for (location[0] = 0; location[0] < cx - 1; ++location[0], ++n, ++m) {

                    // Read in 8 field values around this vertex
                    // and store them in an array
                    // Also calculate 8-bit mask, like in marching cubes,
                    // so we can speed up sign checks later
                    int mask = 0, g = 0, idx = n;
                    for (int k = 0; k < 2; ++k, idx += cx * (cy - 2)) {
                        for (int j = 0; j < 2; ++j, idx += cx - 2) {
                            for (int i = 0; i < 2; ++i, ++g, ++idx) {
                                final double p = data[idx];
                                // normals.add(new Vector3f())
                                grid[g] = p;
                                mask |= (p < 0) ? (1 << g) : 0;
                            }
                        }
                    }

                    // Check for early termination
                    // if cell does not intersect boundary
                    if (mask == 0 || mask == 0xff) {
                        continue;
                    }

                    // Sum up edge intersections
                    int edge_mask = edge_table[mask];
                    double[] v = {0.0, 0.0, 0.0};
                    int e_count = 0;

                    // For every edge of the cube...
                    for (int i = 0; i < 12; ++i) {

                        // Use edge mask to check if it is crossed
                        if (!bool((edge_mask & (1 << i)))) {
                            continue;
                        }

                        // If it did, increment number of edge crossings
                        ++e_count;

                        // Now find the point of intersection
                        int firstEdgeIndex = i << 1;
                        int secondEdgeIndex = firstEdgeIndex + 1;
                        // Unpack vertices
                        int e0 = cube_edges[firstEdgeIndex];
                        int e1 = cube_edges[secondEdgeIndex];
                        // Unpack grid values
                        double g0 = grid[e0];
                        double g1 = grid[e1];

                        // Compute point of intersection (linear interpolation)
                        double t = g0 - g1;
                        if (Math.abs(t) > 1e-6) {
                            t = g0 / t;
                        } else {
                            continue;
                        }

                        // Interpolate vertices and add up intersections
                        // (this can be done without multiplying)
                        for (int j = 0; j < 3; j++) {
                            int k = 1 << j; // (1,2,4)
                            int a = e0 & k;
                            int b = e1 & k;
                            if (a != b) {
                                v[j] += bool(a) ? 1.0 - t : t;
                            } else {
                                v[j] += bool(a) ? 1.0 : 0;
                            }
                        }
                    }

                    // Now we just average the edge intersections
                    // and add them to coordinate
                    double s = 1.0 / e_count;
                    for (int i = 0; i < 3; ++i) {
                        v[i] = location[i] + s * v[i];
                    }

                    // Add vertex to buffer, store pointer to
                    // vertex index in buffer
                    vertexBuffer[m] = vertices.size();
                    vertices.add(v);

                    // Now we need to add faces together, to do this we just
                    // loop over 3 basis components
                    for (int i = 0; i < 3; ++i) {

                        // The first three entries of the edge_mask
                        // count the crossings along the edge
                        if (!bool(edge_mask & (1 << i))) {
                            continue;
                        }

                        // i = axes we are point along.
                        // iu, iv = orthogonal axes
                        int iu = (i + 1) % 3;
                        int iv = (i + 2) % 3;

                        // If we are on a boundary, skip it
                        if (location[iu] == 0 || location[iv] == 0) {
                            continue;
                        }

                        // Otherwise, look up adjacent edges in buffer
                        int du = R[iu];
                        int dv = R[iv];

                        // finally, the indices for the 4 vertices
                        // that define the face
                        final int indexM = vertexBuffer[m];
                        final int indexMMinusDU = vertexBuffer[m - du];
                        final int indexMMinusDV = vertexBuffer[m - dv];
                        final int indexMMinusDUMinusDV = vertexBuffer[m - du - dv];

                        // Remember to flip orientation depending on the sign
                        // of the corner.
                        if (bool(mask & 1)) {
                            faces.add(new int[]{
                                    indexM,
                                    indexMMinusDU,
                                    indexMMinusDUMinusDV,
                                    indexMMinusDV
                            });
                        } else {
                            faces.add(new int[]{
                                    indexM,
                                    indexMMinusDV,
                                    indexMMinusDUMinusDV,
                                    indexMMinusDU
                            });
                        }
                    }
                } // end x
            } // end y
        } // end z

        if (vertices.isEmpty()) {
            return null;
        }

        Mesh mesh = new Mesh();

        List<Vector3f> verts = new ArrayList<>();
        vertices.forEach(v -> verts.add(new Vector3f((float) v[0], (float) v[1], (float) v[2])));

        Vector3f[] vertArray = new Vector3f[verts.size()];
        vertArray = verts.toArray(vertArray);

        FloatBuffer pb = BufferUtils.createFloatBuffer(vertArray);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, pb);


        // int[] indexArray = new int[faces.size() * 4];
        List<Integer> indexes = new ArrayList<>();
        faces.forEach(f -> {
            indexes.add(f[0]);
            indexes.add(f[1]);
            indexes.add(f[2]);
            indexes.add(f[3]);
        });

        int[] triIndexes = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            triIndexes[i] = indexes.get(i);
        }

        IntBuffer ib = BufferUtils.createIntBuffer(triIndexes);
        mesh.setBuffer(VertexBuffer.Type.Index, 4, ib);

        //All done!  Return the result
        // return new Mesh(vertices, faces);

        return mesh;
    }

    /**
     * Converts int to bool.
     *
     * @param i integer to convert
     * @return {@code true} if i > 0; {@code false} otherwise
     */
    private static boolean bool(int i) {
        return i > 0;
    }

}
