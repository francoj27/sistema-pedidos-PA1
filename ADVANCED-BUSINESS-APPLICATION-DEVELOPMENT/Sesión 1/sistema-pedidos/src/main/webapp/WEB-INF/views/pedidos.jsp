<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!doctype html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - ISIL</title>

    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <c:url var="pedidosUrl" value="/pedidos"/>

    <link rel="stylesheet" href="${cssUrl}">
</head>

<body>

<h1>Sistema de Pedidos</h1>

<p class="nota">
    Flujo: Navegador → PedidoServlet → PedidoService (EJB) → JPA → H2
</p>

<!-- =========================
     MENSAJES
     ========================= -->

<c:if test="${not empty error}">
    <div class="error">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:if test="${not empty param.creado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.creado}"/>
        registrado correctamente.
    </div>
</c:if>

<c:if test="${not empty param.actualizado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.actualizado}"/>
        actualizado correctamente.
    </div>
</c:if>

<c:if test="${not empty param.eliminado}">
    <div class="mensaje">
        Pedido #
        <c:out value="${param.eliminado}"/>
        eliminado correctamente.
    </div>
</c:if>


<!-- =========================
     REGISTRAR PEDIDO
     ========================= -->

<h2>Registrar pedido</h2>

<form method="post"
      action="${pedidosUrl}"
      class="form-grid">

    <label>
        Cliente
        <input
                name="cliente"
                required
                maxlength="120"
                placeholder="Ej. Ana Torres"
                value="${clienteIngresado}">
    </label>

    <label>
        Producto
        <select name="productoId" required>

            <c:forEach var="producto" items="${productos}">
                <option value="${producto.id}">

                    <c:out value="${producto.nombre}"/>

                    - S/

                    <fmt:formatNumber
                            value="${producto.precio}"
                            minFractionDigits="2"
                            maxFractionDigits="2"/>

                    - stock:

                    <c:out value="${producto.stock}"/>

                </option>
            </c:forEach>

        </select>
    </label>

    <label>
        Cantidad
        <input
                name="cantidad"
                type="number"
                min="1"
                value="${empty cantidadIngresada ? 1 : cantidadIngresada}"
                required>
    </label>

    <button type="submit">
        Registrar
    </button>

</form>


<!-- =========================
     FORMULARIO DE EDICIÓN
     ========================= -->

<div id="formularioEdicion" style="display: none;">

    <h2>Editar pedido</h2>

    <form id="formEditarPedido"
          class="form-grid">

        <input
                type="hidden"
                id="editarPedidoId">

        <label>
            Cliente
            <input
                    id="editarCliente"
                    type="text"
                    maxlength="120"
                    required>
        </label>

        <label>
            Producto
            <select
                    id="editarProducto"
                    required>

                <c:forEach var="producto" items="${productos}">

                    <option value="${producto.id}">
                        <c:out value="${producto.nombre}"/>
                        - S/
                        <fmt:formatNumber
                                value="${producto.precio}"
                                minFractionDigits="2"
                                maxFractionDigits="2"/>
                        - stock:
                        <c:out value="${producto.stock}"/>
                    </option>

                </c:forEach>

            </select>
        </label>

        <label>
            Cantidad
            <input
                    id="editarCantidad"
                    type="number"
                    min="1"
                    required>
        </label>

        <button type="submit">
            Guardar cambios
        </button>

        <button
                type="button"
                id="cancelarEdicion">
            Cancelar
        </button>

    </form>

</div>


<!-- =========================
     PEDIDOS REGISTRADOS
     ========================= -->

<h2>Pedidos registrados</h2>

<table>

    <thead>

    <tr>
        <th>ID</th>
        <th>Cliente</th>
        <th>Producto</th>
        <th>Cantidad</th>
        <th>Total</th>
        <th>Fecha</th>
        <th>Acciones</th>
    </tr>

    </thead>

    <tbody>

    <c:forEach var="pedido" items="${pedidos}">

        <tr>

            <td>
                <c:out value="${pedido.id}"/>
            </td>

            <td>
                <c:out value="${pedido.cliente}"/>
            </td>

            <td>
                <c:out value="${pedido.producto.nombre}"/>
            </td>

            <td>
                <c:out value="${pedido.cantidad}"/>
            </td>

            <td>
                S/

                <fmt:formatNumber
                        value="${pedido.total}"
                        minFractionDigits="2"
                        maxFractionDigits="2"/>
            </td>

            <td>
                <c:out value="${pedido.fecha}"/>
            </td>

            <td>

                <button
                        type="button"
                        class="btn-editar"
                        data-pedido-id="${pedido.id}"
                        data-producto-id="${pedido.producto.id}">
                    Editar
                </button>

                <button
                        type="button"
                        class="btn-eliminar"
                        data-pedido-id="${pedido.id}">
                    Eliminar
                </button>

            </td>

        </tr>

    </c:forEach>

    <c:if test="${empty pedidos}">

        <tr>

            <td colspan="7">
                Aún no hay pedidos.
            </td>

        </tr>

    </c:if>

    </tbody>

</table>


<!-- =========================
     JAVASCRIPT
     ========================= -->

<script>

    /*
     * URL del Servlet.
     *
     * JSP genera algo como:
     *
     * /sistema-pedidos/pedidos
     */
    const pedidosUrl = '${pedidosUrl}';


    /*
     * =========================
     * EDITAR PEDIDO
     * =========================
     */

    const botonesEditar =
        document.querySelectorAll('.btn-editar');

    const formularioEdicion =
        document.getElementById('formularioEdicion');

    const formEditarPedido =
        document.getElementById('formEditarPedido');

    const editarPedidoId =
        document.getElementById('editarPedidoId');

    const editarCliente =
        document.getElementById('editarCliente');

    const editarProducto =
        document.getElementById('editarProducto');

    const editarCantidad =
        document.getElementById('editarCantidad');

    const cancelarEdicion =
        document.getElementById('cancelarEdicion');


    /*
     * Cuando hacemos clic en "Editar",
     * obtenemos los datos de la fila.
     */

    botonesEditar.forEach(function(boton) {

        boton.addEventListener('click', function() {

            const fila =
                boton.closest('tr');

            const pedidoId =
                boton.dataset.pedidoId;

            const productoId =
                boton.dataset.productoId;

            const cliente =
                fila.children[1].textContent.trim();

            const cantidad =
                fila.children[3].textContent.trim();


            /*
             * Colocamos los datos actuales
             * dentro del formulario de edición.
             */

            editarPedidoId.value =
                pedidoId;

            editarCliente.value =
                cliente;

            editarProducto.value =
                productoId;

            editarCantidad.value =
                cantidad;


            /*
             * Mostramos el formulario.
             */

            formularioEdicion.style.display =
                'block';


            /*
             * Movemos la pantalla hacia
             * el formulario de edición.
             */

            formularioEdicion.scrollIntoView({
                behavior: 'smooth'
            });

        });

    });


    /*
     * =========================
     * CANCELAR EDICIÓN
     * =========================
     */

    cancelarEdicion.addEventListener(
        'click',
        function() {

            formularioEdicion.style.display =
                'none';

            formEditarPedido.reset();

        }
    );


    /*
     * =========================
     * GUARDAR EDICIÓN
     * =========================
     */

    formEditarPedido.addEventListener(
        'submit',
        async function(event) {

            /*
             * Evitamos que el formulario
             * haga un POST tradicional.
             */

            event.preventDefault();


            const pedidoId =
                editarPedidoId.value;

            const cliente =
                editarCliente.value.trim();

            const productoId =
                editarProducto.value;

            const cantidad =
                editarCantidad.value;


            /*
             * Validación básica.
             */

            if (cliente === '') {

                alert(
                    'El cliente es obligatorio.'
                );

                return;
            }

            if (Number(cantidad) <= 0) {

                alert(
                    'La cantidad debe ser mayor que cero.'
                );

                return;
            }


            /*
             * Construimos los parámetros
             * que recibirá doPut().
             */

            const parametros =
                new URLSearchParams();

            parametros.append(
                'id',
                pedidoId
            );

            parametros.append(
                'cliente',
                cliente
            );

            parametros.append(
                'productoId',
                productoId
            );

            parametros.append(
                'cantidad',
                cantidad
            );


            try {

                /*
                 * PETICIÓN HTTP PUT REAL
                 */

                const respuesta =
                    await fetch(
                        pedidosUrl
                        + '?'
                        + parametros.toString(),
                        {
                            method: 'PUT'
                        }
                    );


                /*
                 * Obtenemos el mensaje
                 * enviado por el Servlet.
                 */

                const mensaje =
                    await respuesta.text();


                /*
                 * Si todo salió bien,
                 * recargamos la página.
                 */

                if (respuesta.ok) {

                    window.location.href =
                        pedidosUrl
                        + '?actualizado='
                        + encodeURIComponent(
                            pedidoId
                        );

                    return;
                }


                /*
                 * Si hubo un error,
                 * mostramos el mensaje.
                 */

                alert(mensaje);

            } catch (error) {

                console.error(error);

                alert(
                    'No se pudo conectar con el servidor.'
                );

            }

        }
    );


    /*
     * =========================
     * ELIMINAR PEDIDO
     * =========================
     */

    const botonesEliminar =
        document.querySelectorAll('.btn-eliminar');


    botonesEliminar.forEach(function(boton) {

        boton.addEventListener(
            'click',
            async function() {

                const pedidoId =
                    boton.dataset.pedidoId;


                /*
                 * Confirmación antes de eliminar.
                 */

                const confirmar =
                    confirm(
                        '¿Está seguro de eliminar el pedido #'
                        + pedidoId
                        + '?'
                    );


                if (!confirmar) {
                    return;
                }


                try {

                    /*
                     * PETICIÓN HTTP DELETE REAL
                     */

                    const respuesta =
                        await fetch(
                            pedidosUrl
                            + '?id='
                            + encodeURIComponent(
                                pedidoId
                            ),
                            {
                                method: 'DELETE'
                            }
                        );


                    /*
                     * Obtenemos el mensaje
                     * enviado por el Servlet.
                     */

                    const mensaje =
                        await respuesta.text();


                    /*
                     * Si la eliminación fue exitosa,
                     * recargamos la página.
                     */

                    if (respuesta.ok) {

                        window.location.href =
                            pedidosUrl
                            + '?eliminado='
                            + encodeURIComponent(
                                pedidoId
                            );

                        return;
                    }


                    /*
                     * Si hubo un error.
                     */

                    alert(mensaje);

                } catch (error) {

                    console.error(error);

                    alert(
                        'No se pudo conectar con el servidor.'
                    );

                }

            }
        );

    });

</script>

</body>
</html>