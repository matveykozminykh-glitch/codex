package moscow.rockstar.systems.modules.modules.visuals;

import java.util.List;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

final class VisualRenderHelper {
   private VisualRenderHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static Vec3d interpolated(Entity entity, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
         MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
         MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ())
      );
   }

   static Vec3d relative(Vec3d worldPos, Camera camera) {
      return worldPos.subtract(camera.getPos());
   }

   static Box relative(Box box, Camera camera) {
      return box.offset(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
   }

   static Box scale(Box box, float scale) {
      Vec3d center = box.getCenter();
      double halfX = box.getLengthX() * 0.5 * scale;
      double halfY = box.getLengthY() * 0.5 * scale;
      double halfZ = box.getLengthZ() * 0.5 * scale;
      return new Box(center.x - halfX, center.y - halfY, center.z - halfZ, center.x + halfX, center.y + halfY, center.z + halfZ);
   }

   static void line(Matrix4f matrix, BufferBuilder builder, Vec3d from, Vec3d to, ColorRGBA color) {
      builder.vertex(matrix, (float)from.x, (float)from.y, (float)from.z).color(color.getRGB());
      builder.vertex(matrix, (float)to.x, (float)to.y, (float)to.z).color(color.getRGB());
   }

   static void lineLoop(Matrix4f matrix, BufferBuilder builder, List<Vec3d> points, ColorRGBA color) {
      if (points.size() < 2) {
         return;
      }

      for (int i = 0; i < points.size(); i++) {
         Vec3d current = points.get(i);
         Vec3d next = points.get((i + 1) % points.size());
         line(matrix, builder, current, next, color);
      }
   }

   static void lineStrip(Matrix4f matrix, BufferBuilder builder, List<Vec3d> points, List<ColorRGBA> colors) {
      if (points.size() < 2) {
         return;
      }

      for (int i = 0; i < points.size() - 1; i++) {
         line(matrix, builder, points.get(i), points.get(i + 1), colors.get(Math.min(i, colors.size() - 1)));
      }
   }

   static void fillBox(Matrix4f matrix, BufferBuilder builder, Box box, ColorRGBA color) {
      quad(matrix, builder, new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.minX, box.maxY, box.minZ), color);
      quad(matrix, builder, new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), color);
      quad(matrix, builder, new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.minZ), color);
      quad(matrix, builder, new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.minZ), color);
      quad(matrix, builder, new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ), color);
      quad(matrix, builder, new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), color);
   }

   static void outlineBox(Matrix4f matrix, BufferBuilder builder, Box box, ColorRGBA color, boolean dashed, float dashDensity) {
      Vec3d[] points = new Vec3d[]{
         new Vec3d(box.minX, box.minY, box.minZ),
         new Vec3d(box.maxX, box.minY, box.minZ),
         new Vec3d(box.maxX, box.minY, box.maxZ),
         new Vec3d(box.minX, box.minY, box.maxZ),
         new Vec3d(box.minX, box.maxY, box.minZ),
         new Vec3d(box.maxX, box.maxY, box.minZ),
         new Vec3d(box.maxX, box.maxY, box.maxZ),
         new Vec3d(box.minX, box.maxY, box.maxZ)
      };
      int[][] edges = new int[][]{
         {0, 1}, {1, 2}, {2, 3}, {3, 0},
         {4, 5}, {5, 6}, {6, 7}, {7, 4},
         {0, 4}, {1, 5}, {2, 6}, {3, 7}
      };

      for (int[] edge : edges) {
         if (dashed) {
            dashedLine(matrix, builder, points[edge[0]], points[edge[1]], color, dashDensity);
         } else {
            line(matrix, builder, points[edge[0]], points[edge[1]], color);
         }
      }
   }

   private static void quad(Matrix4f matrix, BufferBuilder builder, Vec3d a, Vec3d b, Vec3d c, Vec3d d, ColorRGBA color) {
      builder.vertex(matrix, (float)a.x, (float)a.y, (float)a.z).color(color.getRGB());
      builder.vertex(matrix, (float)b.x, (float)b.y, (float)b.z).color(color.getRGB());
      builder.vertex(matrix, (float)c.x, (float)c.y, (float)c.z).color(color.getRGB());
      builder.vertex(matrix, (float)d.x, (float)d.y, (float)d.z).color(color.getRGB());
   }

   private static void dashedLine(Matrix4f matrix, BufferBuilder builder, Vec3d from, Vec3d to, ColorRGBA color, float density) {
      int segments = Math.max(4, (int)(8.0F * density));
      for (int i = 0; i < segments; i += 2) {
         float start = (float)i / (float)segments;
         float end = (float)(i + 1) / (float)segments;
         Vec3d segStart = from.lerp(to, start);
         Vec3d segEnd = from.lerp(to, end);
         line(matrix, builder, segStart, segEnd, color);
      }
   }
}
