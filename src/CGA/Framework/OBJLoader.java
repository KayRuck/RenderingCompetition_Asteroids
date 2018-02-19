package CGA.Framework;
import org.joml.*;
import org.joml.Math;

import java.util.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Fabian on 16.09.2017.
 */
public class OBJLoader
{
    private static final int OBJECT_SHELL_SIZE   = 8;
    private static final int OBJREF_SIZE         = 4;
    private static final int LONG_FIELD_SIZE     = 8;
    private static final int INT_FIELD_SIZE      = 4;
    private static final int SHORT_FIELD_SIZE    = 2;
    private static final int CHAR_FIELD_SIZE     = 2;
    private static final int BYTE_FIELD_SIZE     = 1;
    private static final int BOOLEAN_FIELD_SIZE  = 1;
    private static final int DOUBLE_FIELD_SIZE   = 8;
    private static final int FLOAT_FIELD_SIZE    = 4;
    //public types:
    static public class OBJException extends Exception
    {
        public OBJException(String message)
        {
            super(message);
        }
    }

    static public class Vertex
    {
        public Vector3f position;
        public Vector2f uv;
        public Vector3f normal;

        public Vertex()
        {
            position = new Vector3f(0.0f, 0.0f, 0.0f);
            uv = new Vector2f(0.0f, 0.0f);
            normal = new Vector3f(0.0f, 0.0f, 0.0f);
        }

        public Vertex(Vector3f position,
                      Vector2f uv,
                      Vector3f normal)
        {
            this.position = position;
            this.uv = uv;
            this.normal = normal;
        }
    }

    static public class OBJMesh
    {
        public boolean hasPositions;
        public boolean hasUVs;
        public boolean hasNormals;
        public String name;

        public ArrayList<Vertex> vertices;
        public ArrayList<Integer> indices;

        public OBJMesh()
        {
            hasPositions = false;
            hasUVs = false;
            hasNormals = false;
            vertices = new ArrayList<>();
            indices = new ArrayList<>();
            name = "";
        }

        public float[] getVertexData()
        {
            float[] data = new float[8 * vertices.size()];
            int di = 0;
            for(Vertex v : vertices)
            {
                data[di++] = v.position.x; data[di++] = v.position.y; data[di++] = v.position.z;
                data[di++] = v.uv.x; data[di++] = v.uv.y;
                data[di++] = v.normal.x; data[di++] = v.normal.y; data[di++] = v.normal.z;
            }
            return data;
        }

        public int[] getIndexData()
        {
            int[] data = new int[indices.size()];
            int di = 0;
            for(int i : indices)
            {
                data[di++] = i;
            }
            return data;
        }

        public int indexCount()
        {
            return indices.size();
        }
    }

    static public class OBJObject
    {
        public String name;
        public ArrayList<OBJMesh> meshes;

        public OBJObject()
        {
            meshes = new ArrayList<>();
            name = "";
        }

        public void recenter()
        {
            Vector3f max = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE).mul(-1.0f);
            Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);


            for(OBJMesh mesh : meshes)
            {
                for (Vertex vert : mesh.vertices)
                {
                    Vector3f p = vert.position;
                    max.x = Math.max(max.x, p.x);
                    max.y = Math.max(max.y, p.y);
                    max.z = Math.max(max.z, p.z);

                    min.x = Math.min(min.x, p.x);
                    min.y = Math.min(min.y, p.y);
                    min.z = Math.min(min.z, p.z);
                }
            }

            Vector3f midpoint = new Vector3f(min).add(new Vector3f(max).sub(min).mul(0.5f));

            for(OBJMesh mesh : meshes)
            {
                for (Vertex vert : mesh.vertices)
                {
                    vert.position.sub(midpoint);
                }
            }
        }

        public void normalize()
        {
            Vector3f max = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE).mul(-1.0f);
            Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

            for(OBJMesh mesh : meshes)
            {
                for (Vertex vert : mesh.vertices)
                {
                    Vector3f p = vert.position;
                    max.x = Math.max(max.x, p.x);
                    max.y = Math.max(max.y, p.y);
                    max.z = Math.max(max.z, p.z);

                    min.x = Math.min(min.x, p.x);
                    min.y = Math.min(min.y, p.y);
                    min.z = Math.min(min.z, p.z);
                }
            }

            Vector3f midpoint = new Vector3f(min).add((new Vector3f(max).sub(min)).mul(0.5f));

            float diff = Math.max(max.x - min.x, Math.max(max.y - min.y, max.z - min.z));

            if(diff > 2.0f)
            {
                float scale = 2.0f / diff;

                for(OBJMesh mesh : meshes)
                {
                    for (Vertex vert : mesh.vertices)
                    {
                       vert.position.sub(midpoint);
                       vert.position.mul(scale);
                       vert.position.add(midpoint);
                    }
                }
            }



        }


    }

    static public class OBJResult
    {
        public ArrayList<OBJObject> objects;
        public String name;

        public OBJResult()
        {
            objects = new ArrayList<>();
            name = "";
        }
    }

    //private types for internal use
    static private class DataCache
    {
        public ArrayList<Vector3f> positions;
        public ArrayList<Vector2f> uvs;
        public ArrayList<Vector3f> normals;

        public DataCache()
        {
            positions = new ArrayList<>();
            uvs = new ArrayList<>();
            normals = new ArrayList<>();
        }
    }

    static private class VertexDef
    {
        public int p_idx = 0;
        public int uv_idx = 0;
        public int n_idx = 0;
        public boolean p_defined = false;
        public boolean uv_defined = false;
        public boolean n_defined = false;

        @Override
        public int hashCode()
        {
            return Objects.hash(p_idx, uv_idx, n_idx);
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof VertexDef && (p_idx == ((VertexDef)obj).p_idx && uv_idx == ((VertexDef)obj).uv_idx && n_idx == ((VertexDef)obj).n_idx);
        }
    }

    static private class Face
    {
        public ArrayList<VertexDef> verts;

        public Face()
        {
            verts = new ArrayList<>();
        }
    }

    //public interface
    public static OBJResult loadOBJ(String objpath, boolean recenterObjects, boolean normalizeObjects) throws OBJException
    {
        OBJResult result = new OBJResult();
        try
        {
            File objFile = new File(objpath);
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(objFile));
            Scanner scanner = new Scanner(stream);
            scanner.useLocale(Locale.US);
            DataCache cache = new DataCache();
            while(scanner.hasNext())
            {
                if(scanner.hasNext("o") || scanner.hasNext("v") || scanner.hasNext("vt") || scanner.hasNext("vn") || scanner.hasNext("g")|| scanner.hasNext("f"))
                {
                    result.objects.add(parseObject(cache, scanner));
                }
                else
                {
                    scanner.nextLine();
                }
            }
            result.name = objFile.getName();
            stream.close();

            if(recenterObjects)
            {
                for(OBJObject obj : result.objects)
                {
                    obj.recenter();
                }
            }

            if(normalizeObjects)
            {
                for(OBJObject obj : result.objects)
                {
                    obj.normalize();
                }
            }
            return result;
        }
        catch (Exception ex)
        {
            throw new OBJException("Error reading OBJ file:\n" + ex.getMessage());
        }


    }

    public static void recalculateNormals(OBJMesh mesh) throws OBJException
    {
        try
        {
            for (int i = 0; i < mesh.vertices.size(); i++) //initialize all Vertex normals with nullvectors
            {
                mesh.vertices.get(i).normal = new Vector3f(0.0f, 0.0f, 0.0f);
            }

            for (int i = 0; i < mesh.indices.size(); i += 3)
            {
                Vector3f v1 = new Vector3f(mesh.vertices.get(mesh.indices.get(i)).position);
                Vector3f v2 = new Vector3f(mesh.vertices.get(mesh.indices.get(i + 1)).position);
                Vector3f v3 = new Vector3f(mesh.vertices.get(mesh.indices.get(i + 2)).position);

                //counter clockwise winding
                Vector3f edge1 = new Vector3f();
                v2.sub(v1, edge1);
                Vector3f edge2 = new Vector3f();
                v3.sub(v1, edge2);

                Vector3f normal = new Vector3f();
                edge1.cross(edge2, normal);

                //for each Vertex all corresponding normals are added. The result is a non unit length vector which is the average direction of all assigned normals.
                mesh.vertices.get(mesh.indices.get(i)).normal.add(normal);
                mesh.vertices.get(mesh.indices.get(i + 1)).normal.add(normal);
                mesh.vertices.get(mesh.indices.get(i + 2)).normal.add(normal);
            }

            for (int i = 0; i < mesh.vertices.size(); i++)	//normalize all normals calculated in the previous step
            {
                mesh.vertices.get(i).normal.normalize();
            }

            mesh.hasNormals = true;
        }
        catch (Exception ex)
        {
            throw new OBJException("Normal calculation failed:\n" + ex.getMessage());
        }
    }

    public static void reverseWinding(OBJMesh mesh) throws Exception
    {
        for (int i = 0; i < mesh.indices.size(); i += 3)
        {
            //even uglier...
            Integer tmp = mesh.indices.get(i + 1);
            mesh.indices.set(i + 1, mesh.indices.get(i + 2));
            mesh.indices.set(i + 2, tmp);
        }
    }

    //private parsing helpers

    private static OBJObject parseObject(DataCache cache, Scanner scanner) throws OBJException
    {
        try
        {
            OBJObject object = new OBJObject();
            String command;
            if(!scanner.hasNext())
                throw new OBJException("Error parsing Object.");

            if(scanner.hasNext("o"))
            {
                command = scanner.next();
                if(scanner.hasNextLine())
                    object.name = scanner.nextLine().trim();
                else
                    throw new OBJException("Error parsing object name.");
            }
            else
            {
                object.name = "UNNAMED";
            }

            while (scanner.hasNext())
            {
                //command = scanner.next();
                //Fill cache
                if (scanner.hasNext("v"))			//position
                {
                    cache.positions.add(parsePosition(scanner));
                }
                else if (scanner.hasNext("vt"))	//uv
                {
                    cache.uvs.add(parseUV(scanner));
                }
                else if (scanner.hasNext("vn"))	//normal
                {
                    cache.normals.add(parseNormal(scanner));
                }

                //meshes, groups and faces
                else if (scanner.hasNext("g") || scanner.hasNext("f")) //grouped or ungrouped mesh
                {
                    object.meshes.add(parseMesh(cache, scanner));
                }
                //stop condition
                else if (scanner.hasNext("o")) //next object found
                {
                    return object;
                }

                //ignore everything else
                else
                {
                    scanner.nextLine();
                }
            } //eof reached. model should be complete
            return object;
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing object:\n" + ex.getMessage());
        }
    }

    private static Vector3f parsePosition(Scanner scanner) throws OBJException
    {
        try
        {
            double x, y, z;

            if (!scanner.hasNext("v"))
                throw new OBJException("Error parsing v command.");
            scanner.next();

            x = scanner.nextDouble();
            y = scanner.nextDouble();
            z = scanner.nextDouble();

            return new Vector3f((float)x, (float)y, (float)z);
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing v command:\n" + ex.getMessage());
        }
    }

    private static Vector3f parseNormal(Scanner scanner) throws OBJException
    {
        try
        {
            double x, y, z;

            if (!scanner.hasNext("vn"))
                throw new OBJException("Error parsing vn command.");
            scanner.next();

            x = scanner.nextDouble();
            y = scanner.nextDouble();
            z = scanner.nextDouble();

            return new Vector3f((float)x, (float)y, (float)z);
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing vn command:\n" + ex.getMessage());
        }
    }

    private static Vector2f parseUV(Scanner scanner) throws  OBJException
    {
        try
        {
            double u, v;

            if (!scanner.hasNext("vt"))
                throw new OBJException("Error parsing vt command.");
            scanner.next();

            u = scanner.nextDouble();
            v = scanner.nextDouble();

            return new Vector2f((float)u, (float)v);
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing vt command:\n" + ex.getMessage());
        }
    }

    private static OBJMesh parseMesh(DataCache cache, Scanner scanner) throws OBJException
    {
        try
        {
            OBJMesh mesh = new OBJMesh();
            HashMap<VertexDef, Integer> meshvertset  = new HashMap<>();

            //later create actual vertices out of these and put them into the mesh
            ArrayList<VertexDef> meshverts = new ArrayList<>(); //for tracking order of insertion
            ArrayList<Integer> meshindices = new ArrayList<>(); //Vertex index of one of the Vertex defs above

            String command;
            if (scanner.hasNext("g")) //if we have a grouped mesh extract its name first
            {
                command = scanner.next();
                if(scanner.hasNextLine())
                {
                    mesh.name = scanner.nextLine().trim();
                }
                else
                    throw new OBJException("Error parsing mesh name.");
            }
            else
            {
                mesh.name = "UNGROUPED";
            }

            //now process faces
            while (scanner.hasNext())
            {
                //command = scanner.next();
                if (scanner.hasNext("f")) //yay we found a face
                {
                    Face face = parseFace(scanner);
                    //process face data and build mesh
                    for (int i = 0; i < 3; i++)
                    {
                        //add Vertexdefs and indices
                        if (meshvertset.containsKey(face.verts.get(i)))	//if Vertex def exists already, just get the index and push it onto index array
                        {
                            meshindices.add(meshvertset.get(face.verts.get(i)));
                        }
                        else //if not, push a index pointing to the last pushed Vertex def, push Vertex def and insert it into the set
                        {
                            meshindices.add(meshvertset.size());
                            meshverts.add(face.verts.get(i));
                            meshvertset.put(face.verts.get(i), meshverts.size() - 1);
                        }
                    }
                }
                else if (scanner.hasNext("g") || scanner.hasNext("o")) //found next mesh group
                {
                    fillMesh(mesh, cache, meshverts, meshindices);
                    return mesh;
                }
                //new vertex data
                else if (scanner.hasNext("v"))			//position
                {
                    cache.positions.add(parsePosition(scanner));
                }
                else if (scanner.hasNext("vt"))	//uv
                {
                    cache.uvs.add(parseUV(scanner));
                }
                else if (scanner.hasNext("vn"))	//normal
                {
                    cache.normals.add(parseNormal(scanner));
                }
                else
                {
                    scanner.nextLine();
                }
            }
            fillMesh(mesh, cache, meshverts, meshindices);
            return mesh;
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing mesh:\n" + ex.getMessage());
        }
    }

    private static Face parseFace(Scanner scanner) throws OBJException
    {
        try
        {
            String command;
            Face face = new Face();
            if (scanner.hasNext("f"))
            {
                command = scanner.next();
                face.verts.add(parseVertex(scanner.next()));
                face.verts.add(parseVertex(scanner.next()));
                face.verts.add(parseVertex(scanner.next()));
                return face;
            }
            else
            {
                throw new OBJException("Error parsing face");
            }
        }
        catch (Exception ex)
        {
            throw new OBJException("Error parsing Face: " + ex.getMessage());
        }
    }

    private static VertexDef parseVertex(String vstring) throws OBJException
    {
        try
        {
            //buffer for v, vt, vn index
            String[] att = vstring.split("/");
            if(att.length != 3)
                throw new OBJException("Error parsing vertex.");

            VertexDef vert = new VertexDef();
            vert.p_idx = (att[0].length() > 0 ? Integer.parseInt(att[0]) - 1 : 0);
            vert.p_defined = att[0].length() > 0;
            vert.uv_idx = (att[1].length() > 0 ? Integer.parseInt(att[1]) - 1 : 0);
            vert.uv_defined = att[1].length() > 0;
            vert.n_idx = (att[2].length() > 0 ? Integer.parseInt(att[2]) - 1 : 0);
            vert.n_defined = att[2].length() > 0;
            return vert;
        }
        catch (Exception ex)
        {
            throw new OBJException(ex.getMessage());
        }
    }

    private static void fillMesh(OBJMesh mesh, DataCache cache, ArrayList<VertexDef> vdefs, ArrayList<Integer> indices) throws OBJException
    {
        try
        {
            //assemble the mesh from the collected indices
            //create Vertex from cache data
            boolean hasverts = true;
            boolean hasuvs = true;
            boolean hasnormals = true;
            for (int i = 0; i < vdefs.size(); i++)
            {
                Vertex vert = new Vertex();

                if (vdefs.get(i).p_defined)
                {
                    if(vdefs.get(i).p_idx < cache.positions.size())
                        vert.position = new Vector3f(cache.positions.get(vdefs.get(i).p_idx));
                    else
                        throw new OBJException("Missing position in object definition");
                }
                else
                {
                    hasverts = false;
                }

                if (vdefs.get(i).uv_defined)
                {
                    if (vdefs.get(i).uv_idx < cache.uvs.size())
                        vert.uv = new Vector2f(cache.uvs.get(vdefs.get(i).uv_idx));
                    else
                        throw new OBJException("Missing texture coordinate in object definition");
                }
                else
                {
                    hasuvs = false;
                }

                if (vdefs.get(i).n_defined)
                {
                    if (vdefs.get(i).n_idx < cache.normals.size())
                        vert.normal = new Vector3f(cache.normals.get(vdefs.get(i).n_idx));
                    else
                        throw new OBJException("Missing normal in object definition");
                }
                else
                {
                    hasnormals = false;
                }

                mesh.vertices.add(vert);
            }
            mesh.indices = indices;
            mesh.hasPositions = hasverts;
            mesh.hasUVs = hasuvs;
            mesh.hasNormals = hasnormals;
        }
        catch (Exception ex)
        {
            throw new OBJException("Error filling mesh:\n" + ex.getMessage());
        }
    }


}
