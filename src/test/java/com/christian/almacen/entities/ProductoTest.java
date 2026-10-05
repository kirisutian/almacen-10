package com.christian.almacen.entities;

import com.christian.almacen.enums.Categoria;
import com.christian.almacen.exceptions.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

public class ProductoTest {

    @Test
    void aumentarCantidad_debeIncrementarStock_cuandoCantidadEsPositiva() {
        // Arrange
        Producto producto = Producto.builder().cantidad(10).build();
        // Act
        producto.aumentarCantidad(5);
        // Assert
        assertThat(producto.getCantidad()).isEqualTo(15);
    }

    @Test
    void aumentarCantidad_debeLanzarExcepcion_cuandoCantidadEsNegativa() {
        // Arrange
        Producto producto = Producto.builder().cantidad(10).build();
        // Act + Assert
        assertThatThrownBy(() -> producto.aumentarCantidad(-3))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessage("La cantidad debe ser positiva");
    }

    @Test
    void descontarCantidad_debeReducirStock_cuandoHaySuficiente() {
        // Arrange
        Producto producto = Producto.builder().cantidad(10).build();
        // Act
        producto.descontarCantidad(4);
        // Assert
        assertThat(producto.getCantidad()).isEqualTo(6);
    }

    @Test
    void descontarCantidad_debeLanzarExcepcion_cuandoNoHaySuficienteStock() {
        // Arrange
        Producto producto = Producto.builder().cantidad(3).build();

        // Act + Assert
        assertThatThrownBy(() -> producto.descontarCantidad(10))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessage("La cantidad debe ser menor o igual a la cantidad actual");
    }

    @Test
    void actualizar_debeModificarDatos_cuandoTodoEsValido() {
        // Arrange
        Producto producto = Producto.builder()
                .nombre("Nombre viejo")
                .categoria(Categoria.JUGUETE)
                .precio(BigDecimal.TEN)
                .cantidad(1)
                .build();
        // Act
        producto.actualizar("Nombre Nuevo", Categoria.ELECTRONICA,
                new BigDecimal("99.90"), 20);
        // Assert
        assertThat(producto.getNombre()).isEqualTo("Nombre Nuevo");
        assertThat(producto.getCategoria()).isEqualTo(Categoria.ELECTRONICA);
        assertThat(producto.getPrecio()).isEqualByComparingTo("99.90");
        assertThat(producto.getCantidad()).isEqualTo(20);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNombreEsMuyCorto() {
        // Arrange
        Producto producto = Producto.builder()
                .nombre("Nombre válido")
                .categoria(Categoria.JUGUETE)
                .precio(BigDecimal.TEN)
                .cantidad(1)
                .build();
        // Act + Assert: "Ab" tiene menos de 5 caracteres
        assertThatThrownBy(() -> producto.actualizar(
                "Ab", Categoria.ELECTRONICA, BigDecimal.TEN, 5))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("entre 5 y 30 caracteres");
    }
}
