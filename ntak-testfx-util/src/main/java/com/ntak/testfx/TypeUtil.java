/*
 * Copyright © 2022 92AK
 */

package com.ntak.testfx;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.testfx.api.FxRobot;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ntak.testfx.TestFXConstants.PLATFORM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TypeUtil {

    private static final Map<String,KeyCodeCombination> MAC_CHARMAP = new ConcurrentHashMap<>();
    private static final Locale locale = Locale.getDefault();

    static {
        if (!locale.getCountry().equals("GB") || !locale.getLanguage().equals("en")) {
            throw new ExceptionInInitializerError("Only en-GB Locale is currently supported!");
        }

        MAC_CHARMAP.put("ENTER", new KeyCodeCombination(KeyCode.ENTER));
        MAC_CHARMAP.put("BACK_SPACE", new KeyCodeCombination(KeyCode.BACK_SPACE));
        MAC_CHARMAP.put("TAB", new KeyCodeCombination(KeyCode.TAB));
        MAC_CHARMAP.put(" ", new KeyCodeCombination(KeyCode.SPACE));
        MAC_CHARMAP.put(",", new KeyCodeCombination(KeyCode.COMMA));
        MAC_CHARMAP.put(".", new KeyCodeCombination(KeyCode.PERIOD));
        MAC_CHARMAP.put("/", new KeyCodeCombination(KeyCode.SLASH));
        MAC_CHARMAP.put("0", new KeyCodeCombination(KeyCode.DIGIT0));
        MAC_CHARMAP.put("1", new KeyCodeCombination(KeyCode.DIGIT1));
        MAC_CHARMAP.put("2", new KeyCodeCombination(KeyCode.DIGIT2));
        MAC_CHARMAP.put("3", new KeyCodeCombination(KeyCode.DIGIT3));
        MAC_CHARMAP.put("4", new KeyCodeCombination(KeyCode.DIGIT4));
        MAC_CHARMAP.put("5", new KeyCodeCombination(KeyCode.DIGIT5));
        MAC_CHARMAP.put("6", new KeyCodeCombination(KeyCode.DIGIT6));
        MAC_CHARMAP.put("7", new KeyCodeCombination(KeyCode.DIGIT7));
        MAC_CHARMAP.put("8", new KeyCodeCombination(KeyCode.DIGIT8));
        MAC_CHARMAP.put("9", new KeyCodeCombination(KeyCode.DIGIT9));
        MAC_CHARMAP.put(";", new KeyCodeCombination(KeyCode.SEMICOLON));
        MAC_CHARMAP.put("=", new KeyCodeCombination(KeyCode.EQUALS));
        MAC_CHARMAP.put("a", new KeyCodeCombination(KeyCode.A));
        MAC_CHARMAP.put("b", new KeyCodeCombination(KeyCode.B));
        MAC_CHARMAP.put("c", new KeyCodeCombination(KeyCode.C));
        MAC_CHARMAP.put("d", new KeyCodeCombination(KeyCode.D));
        MAC_CHARMAP.put("e", new KeyCodeCombination(KeyCode.E));
        MAC_CHARMAP.put("f", new KeyCodeCombination(KeyCode.F));
        MAC_CHARMAP.put("g", new KeyCodeCombination(KeyCode.G));
        MAC_CHARMAP.put("h", new KeyCodeCombination(KeyCode.H));
        MAC_CHARMAP.put("i", new KeyCodeCombination(KeyCode.I));
        MAC_CHARMAP.put("j", new KeyCodeCombination(KeyCode.J));
        MAC_CHARMAP.put("k", new KeyCodeCombination(KeyCode.K));
        MAC_CHARMAP.put("l", new KeyCodeCombination(KeyCode.L));
        MAC_CHARMAP.put("m", new KeyCodeCombination(KeyCode.M));
        MAC_CHARMAP.put("n", new KeyCodeCombination(KeyCode.N));
        MAC_CHARMAP.put("o", new KeyCodeCombination(KeyCode.O));
        MAC_CHARMAP.put("p", new KeyCodeCombination(KeyCode.P));
        MAC_CHARMAP.put("q", new KeyCodeCombination(KeyCode.Q));
        MAC_CHARMAP.put("r", new KeyCodeCombination(KeyCode.R));
        MAC_CHARMAP.put("s", new KeyCodeCombination(KeyCode.S));
        MAC_CHARMAP.put("t", new KeyCodeCombination(KeyCode.T));
        MAC_CHARMAP.put("u", new KeyCodeCombination(KeyCode.U));
        MAC_CHARMAP.put("v", new KeyCodeCombination(KeyCode.V));
        MAC_CHARMAP.put("w", new KeyCodeCombination(KeyCode.W));
        MAC_CHARMAP.put("x", new KeyCodeCombination(KeyCode.X));
        MAC_CHARMAP.put("y", new KeyCodeCombination(KeyCode.Y));
        MAC_CHARMAP.put("z", new KeyCodeCombination(KeyCode.Z));
        MAC_CHARMAP.put("A", new KeyCodeCombination(KeyCode.A,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("B", new KeyCodeCombination(KeyCode.B,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("C", new KeyCodeCombination(KeyCode.C,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("D", new KeyCodeCombination(KeyCode.D,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("E", new KeyCodeCombination(KeyCode.E,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("F", new KeyCodeCombination(KeyCode.F,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("G", new KeyCodeCombination(KeyCode.G,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("H", new KeyCodeCombination(KeyCode.H,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("I", new KeyCodeCombination(KeyCode.I,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("J", new KeyCodeCombination(KeyCode.J,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("K", new KeyCodeCombination(KeyCode.K,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("L", new KeyCodeCombination(KeyCode.L,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("M", new KeyCodeCombination(KeyCode.M,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("N", new KeyCodeCombination(KeyCode.N,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("O", new KeyCodeCombination(KeyCode.O,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("P", new KeyCodeCombination(KeyCode.P,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("Q", new KeyCodeCombination(KeyCode.Q,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("R", new KeyCodeCombination(KeyCode.R,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("S", new KeyCodeCombination(KeyCode.S,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("T", new KeyCodeCombination(KeyCode.T,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("U", new KeyCodeCombination(KeyCode.U,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("V", new KeyCodeCombination(KeyCode.V,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("W", new KeyCodeCombination(KeyCode.W,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("X", new KeyCodeCombination(KeyCode.X,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("Y", new KeyCodeCombination(KeyCode.Y,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("Z", new KeyCodeCombination(KeyCode.Z,
                                                    KeyCombination.ModifierValue.DOWN,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP,
                                                    KeyCombination.ModifierValue.UP));
        MAC_CHARMAP.put("[", new KeyCodeCombination(KeyCode.OPEN_BRACKET));
        MAC_CHARMAP.put("\\", new KeyCodeCombination(KeyCode.BACK_SLASH));
        MAC_CHARMAP.put("]", new KeyCodeCombination(KeyCode.CLOSE_BRACKET));
        MAC_CHARMAP.put("+", new KeyCodeCombination(KeyCode.EQUALS, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("-", new KeyCodeCombination(KeyCode.SUBTRACT));
        MAC_CHARMAP.put("`", new KeyCodeCombination(KeyCode.BACK_QUOTE));
        MAC_CHARMAP.put("'", new KeyCodeCombination(KeyCode.QUOTE));
        MAC_CHARMAP.put("&", new KeyCodeCombination(KeyCode.DIGIT7, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("*", new KeyCodeCombination(KeyCode.DIGIT8, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("\"", new KeyCodeCombination(KeyCode.QUOTEDBL));
        MAC_CHARMAP.put("<", new KeyCodeCombination(KeyCode.COMMA, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put(">", new KeyCodeCombination(KeyCode.PERIOD, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("{", new KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("}", new KeyCodeCombination(KeyCode.CLOSE_BRACKET, KeyCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("@", new KeyCodeCombination(KeyCode.DIGIT2, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put(":", new KeyCodeCombination(KeyCode.SEMICOLON, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("^", new KeyCodeCombination(KeyCode.DIGIT6, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("$", new KeyCodeCombination(KeyCode.DIGIT4, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("€", new KeyCodeCombination(KeyCode.EURO_SIGN));
        MAC_CHARMAP.put("!", new KeyCodeCombination(KeyCode.DIGIT1, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("¡", new KeyCodeCombination(KeyCode.INVERTED_EXCLAMATION_MARK));
        MAC_CHARMAP.put("(", new KeyCodeCombination(KeyCode.DIGIT9, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put(")", new KeyCodeCombination(KeyCode.DIGIT0, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("_", new KeyCodeCombination(KeyCode.MINUS, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("£", new KeyCodeCombination(KeyCode.DIGIT3, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("~", new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN));
        MAC_CHARMAP.put("#", new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN));
        MAC_CHARMAP.put("%", new KeyCodeCombination(KeyCode.DIGIT5, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("?", new KeyCodeCombination(KeyCode.SLASH, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("|", new KeyCodeCombination(KeyCode.BACK_SLASH, KeyCodeCombination.SHIFT_DOWN));
        MAC_CHARMAP.put("–", new KeyCodeCombination(KeyCode.MINUS));
    }

    public static void typeString(FxRobot robot, String value) {
        typeString(robot, PLATFORM, value);
    }

    public static void typeString(FxRobot robot, TestFXConstants.Platform platform, String value) {
        switch (platform) {
            case MAC:   typeMacString(robot, value);
                        break;
            case WIN:
            case LINUX:
            default:    throw new RuntimeException(String.format("Platform %s not supported", platform));
        }
    }

    private static void typeMacString(FxRobot robot, String value) {
        for (char c : value.toCharArray()) {
            KeyCodeCombination combo = MAC_CHARMAP.get(String.valueOf(c));
            if (combo != null) {
                robot.push(combo);
                robot.sleep(5, MILLISECONDS);
            }
        }
    }
}
