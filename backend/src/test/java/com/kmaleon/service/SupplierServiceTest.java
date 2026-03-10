package com.kmaleon.service;

import com.kmaleon.dto.SupplierRequest;
import com.kmaleon.dto.SupplierResponse;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenCreate_thenSavesAndReturnsSupplierWithName() {
        SupplierRequest request = new SupplierRequest();
        request.setName("China Trading Co.");

        when(supplierRepository.save(any())).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        SupplierResponse response = supplierService.create(CALLER_ID, request);

        assertThat(response.getName()).isEqualTo("China Trading Co.");
        assertThat(response.getId()).isNotNull();
    }

    @Test
    void whenFindAll_asSuperAdmin_thenReturnsAllSuppliers() {
        Supplier s1 = supplierWithName("Supplier A");
        Supplier s2 = supplierWithName("Supplier B");
        when(supplierRepository.findAll()).thenReturn(List.of(s1, s2));

        List<SupplierResponse> responses = supplierService.findAll(null, "super_admin");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Supplier A");
        assertThat(responses.get(1).getName()).isEqualTo("Supplier B");
    }

    @Test
    void whenFindAll_asAdmin_thenReturnsOnlyOwnSuppliers() {
        UUID callerId = UUID.randomUUID();
        Supplier s1 = supplierWithName("My Supplier");
        when(supplierRepository.findByOwnerId(callerId)).thenReturn(List.of(s1));

        List<SupplierResponse> responses = supplierService.findAll(callerId, "admin");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("My Supplier");
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenRepositoryThrows_thenExceptionPropagates() {
        SupplierRequest request = new SupplierRequest();
        request.setName("Failing Supplier");
        when(supplierRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> supplierService.create(CALLER_ID, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenFindAll_andNoSuppliers_thenReturnsEmptyList() {
        when(supplierRepository.findAll()).thenReturn(List.of());

        List<SupplierResponse> responses = supplierService.findAll(null, "super_admin");

        assertThat(responses).isEmpty();
    }

    @Test
    void whenCreate_thenResponseContainsExactNameFromRequest() {
        String name = "  Proveedor Con Espacios  ";
        SupplierRequest request = new SupplierRequest();
        request.setName(name);

        when(supplierRepository.save(any())).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        SupplierResponse response = supplierService.create(CALLER_ID, request);

        assertThat(response.getName()).isEqualTo(name);
    }

    private Supplier supplierWithName(String name) {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName(name);
        return supplier;
    }
}
