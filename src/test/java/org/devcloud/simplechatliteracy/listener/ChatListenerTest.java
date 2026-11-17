package org.devcloud.simplechatliteracy.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.RegisteredListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChatListenerTest {
    private final ChatListener listener = new ChatListener();

    @ParameterizedTest
    @CsvSource({
            "badword here, [removed], [removed]",
            "привет мир, привет мир, Привет мир."
    })
    void formatsCurrentMessage(String originalMessage, String currentMessage, String expected) {
        AsyncChatEvent event = chatEvent(Component.text(originalMessage), Component.text(currentMessage));

        listener.onAsyncChat(event);

        assertEquals(expected, ((TextComponent) event.message()).content());
    }

    @Test
    void preservesRootStyle() {
        AsyncChatEvent event = chatEvent(Component.text("привет мир"), Component.text("привет мир", NamedTextColor.RED));

        listener.onAsyncChat(event);

        assertEquals(Component.text("Привет мир.", NamedTextColor.RED), event.message());
    }

    @Test
    void skipsCancelledEventWhenDispatched() throws Exception {
        Component message = Component.text("привет мир");
        AsyncChatEvent event = chatEvent(message, message);
        event.setCancelled(true);
        EventHandler handler = ChatListener.class.getMethod("onAsyncChat", AsyncChatEvent.class).getAnnotation(EventHandler.class);
        RegisteredListener registeredListener = new RegisteredListener(listener,
                (registered, dispatched) -> ((ChatListener) registered).onAsyncChat((AsyncChatEvent) dispatched),
                handler.priority(), null, handler.ignoreCancelled());

        registeredListener.callEvent(event);

        assertSame(message, event.message());
    }

    private static AsyncChatEvent chatEvent(Component originalMessage, Component message) {
        return new AsyncChatEvent(true, null, Set.of(), ChatRenderer.defaultRenderer(), message, originalMessage, null);
    }
}
