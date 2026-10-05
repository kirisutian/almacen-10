package com.christian.almacen.services.productos;

import com.christian.almacen.dto.productos.ProductoRequest;
import com.christian.almacen.dto.productos.ProductoResponse;
import com.christian.almacen.entities.Producto;
import com.christian.almacen.enums.Categoria;
import com.christian.almacen.exceptions.DatoInvalidoException;
import com.christian.almacen.exceptions.RecursoNoEncontradoException;
import com.christian.almacen.mappers.ProductoMapper;
import com.christian.almacen.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        // Datos base que reutilizamos en varias pruebas.
        producto = Producto.builder()
                .id(1L)
                .nombre("Laptop Gamer")
                .categoria(Categoria.ELECTRONICA)
                .precio(new BigDecimal("15999.99"))
                .cantidad(10)
                .build();

        productoResponse = new ProductoResponse(
                1L, "Laptop Gamer", "Electrónica",
                new BigDecimal("15999.99"), 10);
    }

    // ---------- listar() ----------

    @Test
    void listar_debeRetornarListaDeProductos_cuandoExistenRegistros() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(productoMapper.entidadAResponse(producto)).thenReturn(productoResponse);
        // Act
        List<ProductoResponse> resultado = productoService.listar(null, null, null, null);
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Laptop Gamer");
        verify(productoRepository).findAll();
    }

    @Test
    void listar_debeRetornarListaVacia_cuandoNoHayProductosRegistrados() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(List.of());
        // Act
        List<ProductoResponse> resultado = productoService.listar(null, null, null, null);
        // Assert
        assertThat(resultado).isEmpty();
    }

    // ---------- obtenerPorId() ----------

    @Test
    void obtenerPorId_debeRetornarProducto_cuandoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoMapper.entidadAResponse(producto)).thenReturn(productoResponse);

        ProductoResponse resultado = productoService.obtenerPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.categoria()).isEqualTo("Electrónica");
    }

    @Test
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        // Arrange: el repositorio "no encuentra nada", como pasaría en la vida real
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Producto no encontrado con id: 99");
    }

    // ---------- registrar() ----------

    @Test
    void registrar_debeGuardarYRetornarProducto_cuandoDatosSonValidos() {
        // Arrange
        ProductoRequest request = new ProductoRequest(
                "Laptop Gamer", "Electrónica", new BigDecimal("15999.99"), 10);

        when(productoMapper.requestAEntidad(request, Categoria.ELECTRONICA)).thenReturn(producto);
        when(productoMapper.entidadAResponse(producto)).thenReturn(productoResponse);

        // Act
        ProductoResponse resultado = productoService.registrar(request);

        // Assert
        assertThat(resultado).isEqualTo(productoResponse);
        verify(productoRepository).save(producto);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoLaCategoriaNoExiste() {
        // Arrange: "CategoriaInventada" no está en el enum Categoria.
        // Nota: aquí NO mockeamos nada del mapper porque el fallo ocurre
        // antes, en Categoria.obtenerCategoriaPorDescripcion (código real, no un mock).
        ProductoRequest request = new ProductoRequest(
                "Producto random", "CategoriaInventada", BigDecimal.TEN, 5);

        // Act + Assert
        assertThatThrownBy(() -> productoService.registrar(request))
                .isInstanceOf(DatoInvalidoException.class);

        verify(productoRepository, never()).save(any());
    }

    // ---------- actualizar() ----------

    @Test
    void actualizar_debeActualizarDatos_cuandoProductoExisteYDatosSonValidos() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoRequest request = new ProductoRequest(
                "Laptop Pro Max", "Electrónica", new BigDecimal("18999.99"), 15);

        ProductoResponse respuestaEsperada = new ProductoResponse(
                1L, "Laptop Pro Max", "Electrónica", new BigDecimal("18999.99"), 15);
        when(productoMapper.entidadAResponse(producto)).thenReturn(respuestaEsperada);

        // Act
        ProductoResponse resultado = productoService.actualizar(request, 1L);

        // Assert
        assertThat(resultado.nombre()).isEqualTo("Laptop Pro Max");
        assertThat(producto.getCantidad()).isEqualTo(15);

        verify(productoRepository).saveAndFlush(producto);
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElProductoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        ProductoRequest request = new ProductoRequest(
                "Cualquier Nombre", "Electrónica", BigDecimal.TEN, 5);

        assertThatThrownBy(() -> productoService.actualizar(request, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Producto no encontrado con id: 99");
    }

    @Test
    void actualizar_debeLanzarExcepcion_cuandoElNombreEsMuyCorto() {
        // Arrange: el producto SÍ existe, pero el nuevo nombre viola la regla de negocio
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoRequest request = new ProductoRequest(
                "Ab", "Electrónica", BigDecimal.TEN, 5);

        assertThatThrownBy(() -> productoService.actualizar(request, 1L))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("entre 5 y 30 caracteres");
    }

    // ---------- eliminar() ----------

    @Test
    void eliminar_debeEliminarProducto_cuandoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository).delete(producto);
        // eliminar() también llama a flush() explícitamente (para forzar el DELETE
        // de inmediato en vez de esperar al final de la transacción). Como es
        // parte del comportamiento real del método, lo verificamos también.
        verify(productoRepository).flush();
    }

    @Test
    void eliminar_debeLanzarExcepcion_cuandoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(productoRepository, never()).delete(any());
    }
}
