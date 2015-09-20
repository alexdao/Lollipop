package com.lumivote.lollipop.bus;

/**
 * Created by alex on 9/20/15.
 */
public class ImageUploadEvent extends AbstractImageUploadEvent{

    String tag;

    public enum Type {
        COMPLETED,
        STARTED
    }

    public ImageUploadEvent(Type type, String tag) {
        super(type);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
