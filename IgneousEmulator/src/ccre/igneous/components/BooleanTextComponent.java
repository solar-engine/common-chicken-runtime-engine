package ccre.igneous.components;

import java.awt.Color;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;

public class BooleanTextComponent extends TextComponent implements BooleanOutput, BooleanInputPoll {

    private final String off, on;
    private boolean state = false;
    private boolean editable = false;

    public BooleanTextComponent(String off, String on) {
        super(off, new String[] {off, on});
        this.off = off;
        this.on = on;
        setColor(Color.RED.darker());
    }
    
    public BooleanTextComponent(String string) {
        this(string, string);
    }

    public BooleanTextComponent setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public boolean get() {
        return state;
    }

    public void set(boolean value) {
        state = value;
        this.setLabel(value ? on : off);
        this.setColor(value ? Color.green : Color.RED.darker());
    }

    @Override
    public void onPress(int x, int y) {
        if (editable) {
            set(!get());
        }
    }
}
