package lpc.javaTools.minecraft.replay;

// time均以秒为单位
public record KeyFrame(Camera camera, double gameTime, double videoTime) {
}
