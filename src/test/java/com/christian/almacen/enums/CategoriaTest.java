package com.christian.almacen.enums;

import com.christian.almacen.exceptions.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CategoriaTest {

    @Test
    void obtenerCategoriaPorDescripcion_debeEncontrarCategoria_ignorandoAcentosYMayusculas() {
        // Act
        Categoria categoria = Categoria.obtenerCategoriaPorDescripcion("electronica");
        // Assert
        assertThat(categoria).isEqualTo(Categoria.ELECTRONICA);
    }

    @Test
    void obtenerCategoriaPorDescripcion_debeEncontrarCategoria_conMayusculasYTilde() {
        Categoria categoria = Categoria.obtenerCategoriaPorDescripcion("ELECTRÓNICA");

        assertThat(categoria).isEqualTo(Categoria.ELECTRONICA);
    }

    @Test
    void obtenerCategoriaPorDescripcion_debeLanzarExcepcion_cuandoNoExisteLaCategoria() {
        assertThatThrownBy(() -> Categoria.obtenerCategoriaPorDescripcion("Mueble"))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("No existe una categoría con la descripción: Mueble");
    }

    @Test
    void obtenerCategoriaPorDescripcion_debeLanzarExcepcion_cuandoLaDescripcionEsVacia() {
        assertThatThrownBy(() -> Categoria.obtenerCategoriaPorDescripcion("   "))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessage("La descripción es requerida");
    }
}
