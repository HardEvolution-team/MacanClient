package com.ded.macanclient.render;



import org.lwjgl.opengl.GL11;

/**
 * Утилитарный класс для рендеринга с использованием OpenGL.
 */
public class RenderUtils {
    /**
     * Рендерит градиент между двумя цветами.
     */
    public static void renderGradient(int x1, int y1, int x2, int y2, int startColor, int endColor) {
        float a1 = (startColor >> 24 & 255) / 255.0f;
        float r1 = (startColor >> 16 & 255) / 255.0f;
        float g1 = (startColor >> 8 & 255) / 255.0f;
        float b1 = (startColor & 255) / 255.0f;

        float a2 = (endColor >> 24 & 255) / 255.0f;
        float r2 = (endColor >> 16 & 255) / 255.0f;
        float g2 = (endColor >> 8 & 255) / 255.0f;
        float b2 = (endColor & 255) / 255.0f;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(r1, g1, b1, a1);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x1, y2);
        GL11.glColor4f(r2, g2, b2, a2);
        GL11.glVertex2i(x2, y2);
        GL11.glVertex2i(x2, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Линейная интерполяция.
     */
    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }
}
