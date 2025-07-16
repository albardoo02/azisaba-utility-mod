package net.azisaba.azisabautilitymod.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class AUtil {

    public static Text stacktraceComponent(Throwable t) {
        Text component = new LiteralText(t.getClass().getName() + ": " + t.getMessage() + "\n");

        for (StackTraceElement element : t.getStackTrace()) {
            component = component.copy().append(new LiteralText("  at " + element.toString()));
            component = component.copy().append(new LiteralText("\n"));
        }
        if (t.getCause() != null) {
            component = component.copy().append(new LiteralText("Caused by: "));
            component = component.copy().append(stacktraceComponent(t.getCause()));
        }
        return component;
    }
}
