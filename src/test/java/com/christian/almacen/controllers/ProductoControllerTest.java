package com.christian.almacen.controllers;

import com.christian.almacen.dto.productos.ProductoRequest;
import com.christian.almacen.dto.productos.ProductoResponse;
import com.christian.almacen.services.productos.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @MockitoBean reemplaza al antiguo @MockBean en Spring Boot 4.
    // Registra un mock de ProductoService dentro del contexto de Spring
    // para que el controller lo reciba por inyección de dependencias.
    @MockitoBean
    private ProductoService productoService;

    @Test
    void registrar_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        // Arrange
        ProductoRequest request = new ProductoRequest(
                "Mouse Inalámbrico", "Electrónica", new BigDecimal("299.99"), 50);
        ProductoResponse response = new ProductoResponse(
                1L, "Mouse Inalámbrico", "Electrónica", new BigDecimal("299.99"), 50);

        when(productoService.registrar(any(ProductoRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mouse Inalámbrico"));
    }

    @Test
    void registrar_debeRetornar400_cuandoElNombreEsMuyCorto() throws Exception {
        // Arrange: "Ab" viola @Size(min = 5) del ProductoRequest
        ProductoRequest request = new ProductoRequest(
                "Ab", "Electrónica", new BigDecimal("299.99"), 50);

        // Act + Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // La validación @Valid falla ANTES de llegar al service.
        verify(productoService, never()).registrar(any());
    }
}
