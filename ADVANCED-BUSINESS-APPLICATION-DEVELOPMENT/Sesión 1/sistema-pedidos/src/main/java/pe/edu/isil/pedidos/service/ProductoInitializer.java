package pe.edu.isil.pedidos.service;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pe.edu.isil.pedidos.domain.Producto;

import java.math.BigDecimal;

@Singleton
public class ProductoInitializer {

    @PersistenceContext(unitName = "PedidosPU")
    private EntityManager entityManager;

    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void inicializarSiEsNecesario() {

        Long cantidad =
                entityManager
                        .createQuery(
                                """
                                select count(p)
                                from Producto p
                                """,
                                Long.class
                        )
                        .getSingleResult();

        if (cantidad == 0) {

            entityManager.persist(
                    new Producto(
                            "Laptop",
                            new BigDecimal("2500.00"),
                            5
                    )
            );

            entityManager.persist(
                    new Producto(
                            "Monitor",
                            new BigDecimal("850.00"),
                            8
                    )
            );

            entityManager.persist(
                    new Producto(
                            "Teclado",
                            new BigDecimal("120.00"),
                            15
                    )
            );
        }
    }
}