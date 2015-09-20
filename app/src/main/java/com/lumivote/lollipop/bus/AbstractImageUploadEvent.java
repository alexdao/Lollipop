package com.lumivote.lollipop.bus;

/**
 * Created by alex on 8/30/15.
 */
public class AbstractImageUploadEvent {

    private Enum type;

    protected AbstractImageUploadEvent(Enum type) {
        this.type = type;
    }

    public Enum getType() {
        return this.type;
    }

}