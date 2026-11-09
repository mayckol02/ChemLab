package com.chemlab;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.game.GestorMisiones;
import com.chemlab.ui.game.VistaJuego2D;
import com.chemlab.ui.IntroChemLab;
import com.chemlab.ui.SelectorPerfil;
import com.chemlab.game.PerfilJugador;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.repository.RepositorioPerfilesJson;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Punto de entrada de ChemLab.
 *
 * <p>La clase {@link Main} solo instancia la aplicación JavaFX;
 * toda la lógica de UI vive en {@link VistaJuego2D} y el
 * dominio en los paquetes correspondientes.</p>
 */
public final class Main extends Application {

    private static final double ANCHO = 1280;
    private static final double ALTO = 720;

    @Override
    public void start(Stage stage) {
        URL logoResource = Main.class.getResource("/images/chemlab-logo.png");
        if (logoResource == null) {
            throw new IllegalStateException("No se encontró el icono de ChemLab");
        }
        stage.getIcons().add(new Image(logoResource.toExternalForm()));

        stage.setTitle("ChemLab - Laboratorio 2D");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        RepositorioPerfilesJson repositorioPerfiles = new RepositorioPerfilesJson();
        SelectorPerfil selector = crearSelectorPerfil(stage, repositorioPerfiles, true);
        Scene scene = new Scene(selector, ANCHO, ALTO);
        stage.setScene(scene);
        stage.show();
    }

    private static SelectorPerfil crearSelectorPerfil(
            Stage stage,
            RepositorioPerfilesJson repositorioPerfiles,
            boolean mostrarIntro
    ) {
        return new SelectorPerfil(
                repositorioPerfiles,
                perfil -> seleccionarPerfil(stage, repositorioPerfiles, perfil, mostrarIntro)
        );
    }

    private static void seleccionarPerfil(
            Stage stage,
            RepositorioPerfilesJson repositorioPerfiles,
            PerfilJugador perfil,
            boolean mostrarIntro
    ) {
        try {
            new GestorMisiones(
                    perfil.progreso().misionesCompletadas(),
                    perfil.progreso().rutaMisionesIniciada()
            );
            perfil.progreso().restaurarCuaderno(
                    new CuadernoLaboratorio(),
                    CatalogoQuimico.predeterminado()
            );
            if (mostrarIntro) {
                iniciarIntro(stage, repositorioPerfiles, perfil);
            } else {
                iniciarJuego(stage, repositorioPerfiles, perfil);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("No se pudo cargar el progreso");
            alerta.setHeaderText("El perfil \"" + perfil.nombre() + "\" contiene datos inválidos.");
            alerta.setContentText(e.getMessage());
            alerta.show();
        }
    }

    private static void iniciarIntro(
            Stage stage,
            RepositorioPerfilesJson repositorioPerfiles,
            PerfilJugador perfil
    ) {
        IntroChemLab intro = new IntroChemLab(() -> {
            iniciarJuego(stage, repositorioPerfiles, perfil);
        });
        stage.getScene().setRoot(intro);
        intro.iniciar();
    }

    private static void iniciarJuego(
            Stage stage,
            RepositorioPerfilesJson repositorioPerfiles,
            PerfilJugador perfil
    ) {
        VistaJuego2D vista = new VistaJuego2D(
                perfil,
                repositorioPerfiles,
                () -> stage.getScene().setRoot(crearSelectorPerfil(stage, repositorioPerfiles, false))
        );
        stage.getScene().setRoot(vista);
        vista.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
