package com.lavamelo.service;

import java.util.Locale;

final class Util {

    private Util() {
    }

    /** Recorta espacios; cadena vacía = null. */
    static String limpio(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Placa en mayúsculas y sin espacios ni guiones (ABC-123 → ABC123). */
    static String placa(String s) {
        return s.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }
}
