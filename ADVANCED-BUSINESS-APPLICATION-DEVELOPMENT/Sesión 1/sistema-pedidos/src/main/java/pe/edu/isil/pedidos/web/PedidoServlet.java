package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.service.PedidoException;
import pe.edu.isil.pedidos.service.PedidoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/pedidos")
public class PedidoServlet extends HttpServlet {

  @EJB
  private PedidoService pedidoService;


  // =========================================================
  // GET
  // =========================================================

  @Override
  protected void doGet(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    cargarDatosVista(request);

    request.getRequestDispatcher(
            "/WEB-INF/views/pedidos.jsp"
    ).forward(request, response);
  }


  // =========================================================
  // POST
  // REGISTRAR / SOLUCIÓN BASE
  // =========================================================

  @Override
  protected void doPost(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    request.setCharacterEncoding(
            StandardCharsets.UTF_8.name()
    );

    String action =
            request.getParameter("action");


    // =====================================================
    // SOLUCIÓN BASE:
    // POST + action=actualizar
    // =====================================================

    if ("actualizar".equalsIgnoreCase(action)) {

      procesarActualizacionPost(
              request,
              response
      );

      return;
    }


    // =====================================================
    // SOLUCIÓN BASE:
    // POST + action=eliminar
    // =====================================================

    if ("eliminar".equalsIgnoreCase(action)) {

      procesarEliminacionPost(
              request,
              response
      );

      return;
    }


    // =====================================================
    // REGISTRAR PEDIDO
    // POST normal
    // =====================================================

    try {

      String cliente =
              request.getParameter("cliente");

      Long productoId =
              Long.valueOf(
                      request.getParameter("productoId")
              );

      int cantidad =
              Integer.parseInt(
                      request.getParameter("cantidad")
              );


      Pedido pedido =
              pedidoService.registrarPedido(
                      cliente,
                      productoId,
                      cantidad
              );


      // PRG:
      // Post / Redirect / Get

      response.sendRedirect(
              request.getContextPath()
                      + "/pedidos?creado="
                      + pedido.getId()
      );


    } catch (NumberFormatException e) {

      mostrarError(
              request,
              response,
              "Los datos numéricos del pedido no son válidos.",
              HttpServletResponse.SC_BAD_REQUEST
      );


    } catch (PedidoException e) {

      mostrarErrorNegocio(
              request,
              response,
              e.getMessage()
      );


    } catch (RuntimeException e) {

      mostrarError(
              request,
              response,
              "Ocurrió un error interno al registrar el pedido.",
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      );
    }
  }


  // =========================================================
  // SOLUCIÓN BASE
  // POST + action=actualizar
  // =========================================================

  private void procesarActualizacionPost(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    try {

      Long pedidoId =
              Long.valueOf(
                      request.getParameter("id")
              );

      String cliente =
              request.getParameter("cliente");

      Long productoId =
              Long.valueOf(
                      request.getParameter("productoId")
              );

      int cantidad =
              Integer.parseInt(
                      request.getParameter("cantidad")
              );


      pedidoService.actualizarPedido(
              pedidoId,
              cliente,
              productoId,
              cantidad
      );


      /*
       * Redirección después del POST.
       *
       * Evita que el navegador vuelva a enviar
       * el formulario al actualizar la página.
       */

      response.sendRedirect(
              request.getContextPath()
                      + "/pedidos?actualizado="
                      + pedidoId
      );


    } catch (NumberFormatException e) {

      mostrarError(
              request,
              response,
              "Los datos enviados para actualizar no son válidos.",
              HttpServletResponse.SC_BAD_REQUEST
      );


    } catch (PedidoException e) {

      if (e.getMessage() != null
              && e.getMessage()
              .toLowerCase()
              .contains("no existe")) {

        mostrarError(
                request,
                response,
                e.getMessage(),
                HttpServletResponse.SC_NOT_FOUND
        );

      } else {

        mostrarErrorNegocio(
                request,
                response,
                e.getMessage()
        );
      }


    } catch (RuntimeException e) {

      mostrarError(
              request,
              response,
              "Ocurrió un error interno al actualizar el pedido.",
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      );
    }
  }


  // =========================================================
  // SOLUCIÓN BASE
  // POST + action=eliminar
  // =========================================================

  private void procesarEliminacionPost(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    try {

      Long pedidoId =
              Long.valueOf(
                      request.getParameter("id")
              );


      pedidoService.eliminarPedido(
              pedidoId
      );


      response.sendRedirect(
              request.getContextPath()
                      + "/pedidos?eliminado="
                      + pedidoId
      );


    } catch (NumberFormatException e) {

      mostrarError(
              request,
              response,
              "El ID del pedido no es válido.",
              HttpServletResponse.SC_BAD_REQUEST
      );


    } catch (PedidoException e) {

      if (e.getMessage() != null
              && e.getMessage()
              .toLowerCase()
              .contains("no existe")) {

        mostrarError(
                request,
                response,
                e.getMessage(),
                HttpServletResponse.SC_NOT_FOUND
        );

      } else {

        mostrarErrorNegocio(
                request,
                response,
                e.getMessage()
        );
      }


    } catch (RuntimeException e) {

      mostrarError(
              request,
              response,
              "Ocurrió un error interno al eliminar el pedido.",
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      );
    }
  }


  // =========================================================
  // SOLUCIÓN AVANZADA
  // PUT
  // =========================================================

  @Override
  protected void doPut(
          HttpServletRequest request,
          HttpServletResponse response)
          throws IOException {

    try {

      Long pedidoId =
              Long.valueOf(
                      request.getParameter("id")
              );

      String cliente =
              request.getParameter("cliente");

      Long productoId =
              Long.valueOf(
                      request.getParameter("productoId")
              );

      int cantidad =
              Integer.parseInt(
                      request.getParameter("cantidad")
              );


      pedidoService.actualizarPedido(
              pedidoId,
              cliente,
              productoId,
              cantidad
      );


      response.setStatus(
              HttpServletResponse.SC_OK
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().write(
              "Pedido actualizado correctamente."
      );


    } catch (NumberFormatException e) {

      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Los datos enviados no son válidos."
      );


    } catch (PedidoException e) {

      if (e.getMessage() != null
              && e.getMessage()
              .toLowerCase()
              .contains("no existe")) {

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                e.getMessage()
        );

      } else {

        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
        );
      }


    } catch (RuntimeException e) {

      response.sendError(
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Error interno al actualizar el pedido."
      );
    }
  }


  // =========================================================
  // SOLUCIÓN AVANZADA
  // DELETE
  // =========================================================

  @Override
  protected void doDelete(
          HttpServletRequest request,
          HttpServletResponse response)
          throws IOException {

    try {

      Long pedidoId =
              Long.valueOf(
                      request.getParameter("id")
              );


      pedidoService.eliminarPedido(
              pedidoId
      );


      response.setStatus(
              HttpServletResponse.SC_OK
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().write(
              "Pedido eliminado correctamente."
      );


    } catch (NumberFormatException e) {

      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "El ID del pedido no es válido."
      );


    } catch (PedidoException e) {

      if (e.getMessage() != null
              && e.getMessage()
              .toLowerCase()
              .contains("no existe")) {

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                e.getMessage()
        );

      } else {

        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
        );
      }


    } catch (RuntimeException e) {

      response.sendError(
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Error interno al eliminar el pedido."
      );
    }
  }


  // =========================================================
  // CARGAR DATOS PARA LA JSP
  // =========================================================

  private void cargarDatosVista(
          HttpServletRequest request) {

    request.setAttribute(
            "productos",
            pedidoService.listarProductos()
    );

    request.setAttribute(
            "pedidos",
            pedidoService.listarPedidos()
    );
  }


  // =========================================================
  // ERROR DE NEGOCIO
  // =========================================================

  private void mostrarErrorNegocio(
          HttpServletRequest request,
          HttpServletResponse response,
          String mensaje)
          throws ServletException, IOException {

    mostrarError(
            request,
            response,
            mensaje,
            HttpServletResponse.SC_BAD_REQUEST
    );
  }


  // =========================================================
  // MOSTRAR ERROR EN JSP
  // =========================================================

  private void mostrarError(
          HttpServletRequest request,
          HttpServletResponse response,
          String mensaje,
          int status)
          throws ServletException, IOException {

    response.setStatus(status);

    request.setAttribute(
            "error",
            mensaje
    );

    cargarDatosVista(request);

    request.getRequestDispatcher(
            "/WEB-INF/views/pedidos.jsp"
    ).forward(request, response);
  }
}