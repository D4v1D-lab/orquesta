package com.mygdx.game;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;

public final class GestorMetricas {

    private static final String PREFS_NAME = "Orquesta_Metricas_Save";

    private static final String KEY_RETENCION_TOTAL = "retencion_total_ms";
    private static final String KEY_RETENCION_MUESTRAS = "retencion_muestras";
    private static final String KEY_JUEGOS = "juegos";
    private static final String KEY_PULSACIONES_JUEGO = "pulsaciones_juego";
    private static final String KEY_DURACION_JUEGO = "duracion_juego";

    private static final String PLANTILLA_METRICAS = "artifacts/orquesta_metricas.html";
    private static final String PLANTILLA_GUIA = "artifacts/orquesta_guia.html";
    private static final String ARCHIVO_METRICAS = "orquesta_metricas.html";
    private static final String ARCHIVO_GUIA = "orquesta_guia.html";

    private static final long PRESS_NONE = -1L;

    private static Preferences prefs;
    private static long pressStartMs = PRESS_NONE;
    private static boolean juegoActivo;
    private static long pendienteRetencionTotalMs;
    private static int pendienteRetencionMuestras;

    private GestorMetricas() {
    }

    private static Preferences prefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences(PREFS_NAME);
        }
        return prefs;
    }

    public static void iniciarJuego() {
        juegoActivo = true;
        pressStartMs = PRESS_NONE;
        pendienteRetencionTotalMs = 0L;
        pendienteRetencionMuestras = 0;
    }

    public static void suspenderJuego() {
        juegoActivo = false;
        pressStartMs = PRESS_NONE;
    }

    public static void reanudarJuego() {
        juegoActivo = true;
        pressStartMs = PRESS_NONE;
    }

    public static void finalizarJuego() {
        juegoActivo = false;
        pressStartMs = PRESS_NONE;
        pendienteRetencionTotalMs = 0L;
        pendienteRetencionMuestras = 0;
    }

    public static void registrarInicioPulsacion() {
        if (!juegoActivo) {
            return;
        }
        pressStartMs = TimeUtils.millis();
    }

    public static void registrarFinPulsacion() {
        if (!juegoActivo || pressStartMs == PRESS_NONE) {
            pressStartMs = PRESS_NONE;
            return;
        }
        long duracion = TimeUtils.millis() - pressStartMs;
        pressStartMs = PRESS_NONE;
        if (duracion >= 10L && duracion <= 10000L) {
            pendienteRetencionTotalMs += duracion;
            pendienteRetencionMuestras++;
        }
    }

    public static void registrarJuegoCompletado(float duracionSegundos, int pulsaciones) {
        if (duracionSegundos <= 0f) {
            return;
        }
        Preferences p = prefs();
        p.putInteger(KEY_JUEGOS, p.getInteger(KEY_JUEGOS, 0) + 1);
        p.putInteger(KEY_PULSACIONES_JUEGO,
                p.getInteger(KEY_PULSACIONES_JUEGO, 0) + Math.max(0, pulsaciones));
        p.putFloat(KEY_DURACION_JUEGO,
                p.getFloat(KEY_DURACION_JUEGO, 0f) + duracionSegundos);
        if (pendienteRetencionMuestras > 0) {
            p.putLong(KEY_RETENCION_TOTAL,
                    p.getLong(KEY_RETENCION_TOTAL, 0L) + pendienteRetencionTotalMs);
            p.putInteger(KEY_RETENCION_MUESTRAS,
                    p.getInteger(KEY_RETENCION_MUESTRAS, 0) + pendienteRetencionMuestras);
        }
        p.flush();
        juegoActivo = false;
        pressStartMs = PRESS_NONE;
        pendienteRetencionTotalMs = 0L;
        pendienteRetencionMuestras = 0;
    }

    public static void reiniciarMetricas() {
        Preferences p = prefs();
        p.clear();
        p.flush();
        juegoActivo = false;
        pressStartMs = PRESS_NONE;
        pendienteRetencionTotalMs = 0L;
        pendienteRetencionMuestras = 0;
        generarPaginasHtml();
    }

    public static void abrirPaginaMetricas() {
        guardarMetricas();
        generarPaginasHtml();
        FileHandle archivoMetricas = Gdx.files.local(ARCHIVO_METRICAS);
        if (!archivoMetricas.exists()) {
            Gdx.app.error("GestorMetricas", "No se pudo generar " + ARCHIVO_METRICAS);
            return;
        }
        Gdx.net.openURI(archivoMetricas.file().toURI().toString());
    }

    public static void guardarMetricas() {
        prefs().flush();
    }

    private static void generarPaginasHtml() {
        Preferences p = prefs();

        long retencionTotal = p.getLong(KEY_RETENCION_TOTAL, 0L);
        int retencionMuestras = p.getInteger(KEY_RETENCION_MUESTRAS, 0);
        int juegos = p.getInteger(KEY_JUEGOS, 0);
        int pulsacionesJuego = p.getInteger(KEY_PULSACIONES_JUEGO, 0);
        float duracionJuego = p.getFloat(KEY_DURACION_JUEGO, 0f);

        int pulsacionesPorJuego = juegos > 0
                ? Math.round((float) pulsacionesJuego / juegos) : 0;
        float retencionMedia = retencionMuestras > 0
                ? (retencionTotal / (float) retencionMuestras) / 1000f : 0f;
        float duracionMedia = juegos > 0 ? duracionJuego / juegos : 0f;

        String html = leerPlantilla(PLANTILLA_METRICAS);
        if (html != null) {
            html = html.replace("{{PULSACIONES_POR_JUEGO}}",
                    String.format(Locale.US, "%02d", pulsacionesPorJuego));
            html = html.replace("{{PULSACIONES_TOTALES}}",
                    String.format(Locale.US, "%02d", pulsacionesJuego));
            html = html.replace("{{RETENCION_BOTON}}",
                    String.format(Locale.US, "%.1fs", retencionMedia));
            html = html.replace("{{DURACION_MEDIA}}",
                    String.format(Locale.US, "%.1fs", duracionMedia));
            escribirLocal(ARCHIVO_METRICAS, html);
        }

        String guia = leerPlantilla(PLANTILLA_GUIA);
        if (guia != null) {
            escribirLocal(ARCHIVO_GUIA, guia);
        }
    }

    private static String leerPlantilla(String ruta) {
        try {
            return Gdx.files.internal(ruta).readString("UTF-8");
        } catch (RuntimeException e) {
            Gdx.app.error("GestorMetricas", "No se pudo leer " + ruta, e);
            return null;
        }
    }

    private static void escribirLocal(String nombre, String contenido) {
        try {
            FileHandle destino = Gdx.files.local(nombre);
            Writer writer = destino.writer(false, "UTF-8");
            try {
                writer.write(contenido);
            } catch (IOException e) {
                Gdx.app.error("GestorMetricas", "No se pudo escribir " + nombre, e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    Gdx.app.error("GestorMetricas", "No se pudo cerrar " + nombre, e);
                }
            }
        } catch (RuntimeException e) {
            Gdx.app.error("GestorMetricas", "No se pudo crear " + nombre, e);
        }
    }
}
