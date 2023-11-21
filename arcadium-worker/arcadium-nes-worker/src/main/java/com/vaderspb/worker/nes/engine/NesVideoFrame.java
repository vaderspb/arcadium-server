package com.vaderspb.worker.nes.engine;

public class NesVideoFrame {
    private final int[] pixels;

    public NesVideoFrame(int[] pixels) {
        this.pixels = pixels;
    }

    public int[] getPixels() {
        return pixels;
    }
}
