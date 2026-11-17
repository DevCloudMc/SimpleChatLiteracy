package org.devcloud.simplechatliteracy.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
            "привет мир, привет мир, Привет мир.",
            "./tell Bob https://example.org/A?key=AbC, ./tell Bob https://example.org/A?key=AbC, /tell Bob https://example.org/A?key=AbC",
            "./, ./, /"
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
    void formatsStyledChildOfEmptyRoot() {
        Component message = Component.empty().append(Component.text("привет мир", NamedTextColor.RED));
        AsyncChatEvent event = chatEvent(message, message);

        listener.onAsyncChat(event);

        assertEquals("Привет мир.", PlainTextComponentSerializer.plainText().serialize(event.message()));
        assertEquals(Component.empty().append(Component.text("Привет мир.", NamedTextColor.RED)), event.message());
    }

    @Test
    void respectsPunctuationInStyledChild() {
        Component punctuation = Component.text("!", NamedTextColor.RED);
        Component message = Component.text("привет мир").append(punctuation);
        AsyncChatEvent event = chatEvent(message, message);

        listener.onAsyncChat(event);

        assertEquals("Привет мир!", PlainTextComponentSerializer.plainText().serialize(event.message()));
        assertEquals(Component.text("Привет мир").append(punctuation), event.message());
    }

    @Test
    void editsDifferentNodesAndPreservesChildEvent() {
        TextComponent child = Component.text("мир", NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/example"));
        Component message = Component.text("привет ").append(child);
        AsyncChatEvent event = chatEvent(message, message);

        listener.onAsyncChat(event);

        assertEquals("Привет мир.", PlainTextComponentSerializer.plainText().serialize(event.message()));
        assertEquals(Component.text("Привет ").append(child.content("мир.")), event.message());
    }

    @Test
    void formatsNestedBranchBeforeSibling() {
        Component message = Component.text("пр").append(Component.text("ивет ").append(Component.text("ми").append(Component.text("р")))).append(Component.text(" друг"));
        AsyncChatEvent event = chatEvent(message, message);

        listener.onAsyncChat(event);

        assertEquals("Привет мир друг.", PlainTextComponentSerializer.plainText().serialize(event.message()));
        assertEquals(Component.text("Пр").append(Component.text("ивет ").append(Component.text("ми").append(Component.text("р")))).append(Component.text(" друг.")), event.message());
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
