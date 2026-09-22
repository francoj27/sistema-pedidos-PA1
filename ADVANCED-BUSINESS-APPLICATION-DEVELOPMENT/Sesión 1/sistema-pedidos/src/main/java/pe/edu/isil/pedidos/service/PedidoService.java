package pe.edu.isil.pedidos.service;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

@Stateless
public class PedidoService {

  @PersistenceContext(unitName = "PedidosPU")
  private EntityManager entityManager;

  @EJB
  private ProductoInitializer productoInitializer;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido registrarPedido(
          String cliente,
          Long productoId,
          int cantidad) {

    validarDatos(cliente, productoId, cantidad);

    Producto producto =
            entityManager.find(Producto.class, productoId);

    if (producto == null) {
      throw new PedidoException("El producto no existe.");
    }

    try {
      producto.descontarStock(cantidad);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new PedidoException(e.getMessage());
    }

    BigDecimal total =
            producto.getPrecio()
                    .multiply(BigDecimal.valueOf(cantidad));

    Pedido pedido =
            new Pedido(
                    cliente.trim(),
                    producto,
                    cantidad,
                    total
            );

    entityManager.persist(pedido);

    return pedido;
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Pedido buscarPedido(Long pedidoId) {

    if (pedidoId == null) {
      throw new PedidoException(
              "El ID del pedido es obligatorio."
      );
    }

    Pedido pedido =
            entityManager.find(Pedido.class, pedidoId);

    if (pedido == null) {
      throw new PedidoException(
              "El pedido no existe."
      );
    }

    return pedido;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido actualizarPedido(
          Long pedidoId,
          String cliente,
          Long productoId,
          int cantidad) {

    validarDatos(cliente, productoId, cantidad);

    if (pedidoId == null) {
      throw new PedidoException(
              "El ID del pedido es obligatorio."
      );
    }

    Pedido pedido =
            entityManager.find(Pedido.class, pedidoId);

    if (pedido == null) {
      throw new PedidoException(
              "El pedido no existe."
      );
    }

    Producto productoNuevo =
            entityManager.find(
                    Producto.class,
                    productoId
            );

    if (productoNuevo == null) {
      throw new PedidoException(
              "El producto no existe."
      );
    }

    Producto productoActual =
            pedido.getProducto();

    int cantidadActual =
            pedido.getCantidad();

    /*
     * CASO 1:
     * Se mantiene el mismo producto.
     */
    if (productoActual.getId()
            .equals(productoNuevo.getId())) {

      int diferencia =
              cantidad - cantidadActual;

      // Aumentó la cantidad solicitada.
      if (diferencia > 0) {

        try {
          productoActual.descontarStock(
                  diferencia
          );
        } catch (
                IllegalArgumentException |
                IllegalStateException e) {

          throw new PedidoException(
                  e.getMessage()
          );
        }

        // Disminuyó la cantidad solicitada.
      } else if (diferencia < 0) {

        productoActual.reponerStock(
                -diferencia
        );
      }

      /*
       * CASO 2:
       * Se cambia de producto.
       */
    } else {

      // Primero verificamos el stock del nuevo producto.
      if (cantidad > productoNuevo.getStock()) {

        throw new PedidoException(
                "Stock insuficiente. Disponible: "
                        + productoNuevo.getStock()
        );
      }

      // Descontamos el nuevo producto.
      try {
        productoNuevo.descontarStock(
                cantidad
        );
      } catch (
              IllegalArgumentException |
              IllegalStateException e) {

        throw new PedidoException(
                e.getMessage()
        );
      }

      // Devolvemos al stock el producto anterior.
      productoActual.reponerStock(
              cantidadActual
      );
    }

    BigDecimal totalNuevo =
            productoNuevo.getPrecio()
                    .multiply(
                            BigDecimal.valueOf(cantidad)
                    );

    pedido.actualizarDatos(
            cliente.trim(),
            productoNuevo,
            cantidad,
            totalNuevo
    );

    return pedido;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void eliminarPedido(Long pedidoId) {

    if (pedidoId == null) {
      throw new PedidoException(
              "El ID del pedido es obligatorio."
      );
    }

    Pedido pedido =
            entityManager.find(
                    Pedido.class,
                    pedidoId
            );

    if (pedido == null) {
      throw new PedidoException(
              "El pedido no existe."
      );
    }

    /*
     * Antes de eliminar el pedido,
     * devolvemos su cantidad al stock.
     */
    Producto producto =
            pedido.getProducto();

    producto.reponerStock(
            pedido.getCantidad()
    );

    entityManager.remove(pedido);
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Producto> listarProductos() {

    /*
     * La inicialización de productos ahora
     * está separada del PedidoService.
     */
    productoInitializer.inicializarSiEsNecesario();

    return entityManager
            .createQuery(
                    """
                    select p
                    from Producto p
                    order by p.id
                    """,
                    Producto.class
            )
            .getResultList();
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Pedido> listarPedidos() {

    return entityManager
            .createQuery(
                    """
                    select p
                    from Pedido p
                    join fetch p.producto
                    order by p.id desc
                    """,
                    Pedido.class
            )
            .getResultList();
  }

  private void validarDatos(
          String cliente,
          Long productoId,
          int cantidad) {

    if (cliente == null || cliente.isBlank()) {

      throw new PedidoException(
              "El cliente es obligatorio."
      );
    }

    if (productoId == null) {

      throw new PedidoException(
              "Debe seleccionar un producto."
      );
    }

    if (cantidad <= 0) {

      throw new PedidoException(
              "La cantidad debe ser mayor que cero."
      );
    }
  }
}