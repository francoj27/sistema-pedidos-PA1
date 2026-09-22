# Sistema de Pedidos — Jakarta EE + Servlet + JSP + Maven

Proyecto académico desarrollado para la evaluación PA1 del curso de Desarrollo de Aplicaciones Empresariales Avanzado.

La aplicación permite registrar, editar y eliminar pedidos, aplicando reglas de negocio sobre el stock de productos y utilizando una arquitectura empresarial basada en Jakarta EE.

## Tecnologías

- Java 21
- Maven
- Jakarta EE 11
- Jakarta Servlet
- JSP / Jakarta Server Pages
- Jakarta Tags (JSTL)
- Jakarta Enterprise Beans (EJB)
- Jakarta Persistence (JPA)
- Hibernate ORM provisto por WildFly
- H2
- WildFly 41
- WildFly Maven Plugin
- IntelliJ IDEA

## Arquitectura

La aplicación utiliza una separación por capas:

```text
Navegador
    |
    | HTTP
    v
PedidoServlet
    |
    | @EJB
    v
PedidoService
    |
    | JPA / EntityManager
    v
H2 / ExampleDS

Capas principales

Client Tier

Navegador web.
HTML y JavaScript.
Formularios para registrar y editar pedidos.
Botones para editar y eliminar.

Web Tier

PedidoServlet
pedidos.jsp
Recepción y coordinación de solicitudes HTTP.

Business Tier

PedidoService
Reglas de negocio.
Validación de datos.
Control del stock.
Cálculo del total.
Transacciones.

Persistence / Data Tier

JPA.
EntityManager.
Entidades Pedido y Producto.
H2 mediante el DataSource de WildFly.
Inicialización de productos

El componente ProductoInitializer se encarga de crear los productos iniciales cuando la base de datos no contiene productos.

Se utiliza un @Singleton con bloqueo de escritura para evitar inicializaciones concurrentes y la creación duplicada de productos.

Requisitos
JDK 21
Maven 3.9 o superior
Acceso a Internet la primera vez para descargar dependencias
Puerto 8081 disponible
IntelliJ IDEA o un IDE compatible

Verificar Java:

java -version

Verificar Maven:

mvn -version
Ejecución

Desde la raíz del proyecto:

mvn clean wildfly:run

El plugin de WildFly provisiona el servidor dentro de:

target/server

La aplicación queda disponible en:

http://localhost:8081/sistema-pedidos/

También se puede acceder directamente a:

http://localhost:8081/sistema-pedidos/pedidos

Para detener WildFly:

Ctrl + C
Flujo de la aplicación

El flujo general de una operación es:

Navegador
    ↓
Solicitud HTTP
    ↓
PedidoServlet
    ↓
PedidoService (EJB)
    ↓
JPA / EntityManager
    ↓
H2
    ↓
Respuesta HTTP
    ↓
Interfaz JSP
Registro de pedido
El navegador envía un POST al Servlet.
PedidoServlet#doPost() recibe los datos.
PedidoService valida los datos.
Se busca el producto mediante JPA.
Se valida y descuenta el stock.
Se calcula el total.
Se registra el pedido.
La operación se ejecuta dentro de una transacción.
El Servlet utiliza el patrón PRG (Post / Redirect / Get).
La JSP muestra el resultado actualizado.
Edición de pedido

Para la solución avanzada se utiliza:

PUT /pedidos

El navegador envía los datos mediante JavaScript fetch().

El Servlet recibe la solicitud en:

doPut()

y delega la operación a:

PedidoService.actualizarPedido()

El servicio controla:

Cambio solamente del cliente.
Cambio de cantidad.
Cambio de producto.
Disponibilidad de stock.
Reposición del stock anterior.
Descuento del nuevo stock.
Recalculo del total.
Eliminación de pedido

Para la solución avanzada se utiliza:

DELETE /pedidos

El navegador envía la solicitud mediante JavaScript fetch().

El Servlet recibe la operación en:

doDelete()

y delega a:

PedidoService.eliminarPedido()

Antes de eliminar el pedido se repone el stock correspondiente.

Métodos HTTP utilizados
Solución base

La solución base utiliza POST junto con el parámetro action.

Ejemplo para actualizar:

POST /pedidos
action=actualizar

Ejemplo para eliminar:

POST /pedidos
action=eliminar

El parámetro action permite que el Servlet determine qué operación debe ejecutar.

Solución avanzada

Para cumplir con la implementación avanzada de PA1 se utilizan los métodos HTTP reales:

PUT     → actualizar pedido
DELETE  → eliminar pedido

La aplicación mantiene ambas estrategias.

La solución avanzada fue probada mediante las herramientas de desarrollador del navegador, verificando las solicitudes HTTP y sus códigos de respuesta.

Reglas de negocio

El sistema implementa las siguientes reglas:

El cliente es obligatorio.
Debe seleccionarse un producto.
La cantidad debe ser mayor que cero.
No se puede registrar una cantidad superior al stock disponible.
Al registrar un pedido se descuenta stock.
Al aumentar la cantidad de un pedido se descuenta solamente la diferencia.
Al disminuir la cantidad se repone la diferencia.
Al cambiar de producto se descuenta stock del nuevo producto.
Al cambiar de producto se repone el stock del producto anterior.
Al eliminar un pedido se repone su stock.
El total se recalcula cuando cambia la cantidad o el producto.
Un pedido inexistente genera una respuesta controlada.
Un producto inexistente genera una respuesta controlada.
Las operaciones de modificación de pedido y stock se realizan dentro de transacciones.
Casos funcionales de PA1

La implementación fue validada mediante los siguientes casos:

Caso 1 — Editar únicamente el cliente

Se modifica el cliente manteniendo:

mismo producto;
misma cantidad;
mismo total;
mismo stock.
Caso 2 — Modificar la cantidad

Se modifica la cantidad de un pedido.

El sistema:

recalcula el total;
descuenta stock adicional cuando corresponde;
repone stock cuando la cantidad disminuye.
Caso 3 — Cambiar el producto

Se cambia el producto de un pedido.

El sistema:

repone el stock del producto anterior;
descuenta el stock del nuevo producto;
recalcula el total.
Caso 4 — Rechazar una edición inválida

Se prueba una cantidad superior al stock disponible.

El sistema rechaza la operación y muestra un mensaje controlado:

Stock insuficiente. Disponible: X

El pedido y el stock permanecen sin cambios.

También se controla el caso de un pedido inexistente mediante una respuesta HTTP apropiada.

Caso 5 — Eliminar pedido y reponer stock

Al eliminar un pedido:

Se identifica el producto asociado.
Se repone la cantidad correspondiente al stock.
Se elimina el pedido.
La operación se realiza dentro de una transacción.
Transacciones

Las operaciones que modifican pedidos y stock utilizan transacciones JTA mediante EJB.

Por ejemplo:

Actualizar pedido
      ↓
Modificar stock
      ↓
Modificar pedido
      ↓
Calcular total
      ↓
COMMIT

Si ocurre un error durante la operación, la transacción puede revertirse evitando que quede una actualización parcial de los datos.

Manejo de errores HTTP

El Servlet controla diferentes situaciones:

400 → datos inválidos
404 → pedido o recurso inexistente
500 → error interno del servidor
200 → operación realizada correctamente
Base de datos

Se utiliza el DataSource de ejemplo proporcionado por WildFly:

java:jboss/datasources/ExampleDS

La persistencia utiliza JPA y Hibernate.

La base de datos utilizada es H2.

El archivo persistence.xml utiliza:

drop-and-create

Por esta razón, al recrearse la instancia de la aplicación se vuelven a crear las tablas y los datos iniciales.

Productos iniciales

La aplicación utiliza los siguientes productos:

Producto	Precio	Stock inicial
Laptop	S/ 2,500.00	5
Monitor	S/ 850.00	8
Teclado	S/ 120.00	15
Estructura principal del proyecto
src/main/java/pe/edu/isil/pedidos/
├── domain/
│   ├── Pedido.java
│   └── Producto.java
│
├── service/
│   ├── PedidoException.java
│   ├── PedidoService.java
│   └── ProductoInitializer.java
│
└── web/
    └── PedidoServlet.java

src/main/resources/META-INF/
└── persistence.xml

src/main/webapp/
├── index.jsp
├── assets/
│   └── css/
│       └── app.css
└── WEB-INF/
    └── views/
        └── pedidos.jsp
Forward y Redirect

La aplicación utiliza dos mecanismos diferentes según la situación.

Forward

Cuando el Servlet necesita mostrar una JSP en la misma solicitud utiliza:

request.getRequestDispatcher(
    "/WEB-INF/views/pedidos.jsp"
).forward(request, response);

El procesamiento ocurre en el servidor y el navegador recibe finalmente el HTML generado por la JSP.

La JSP se encuentra dentro de WEB-INF/views, por lo que no se accede directamente desde el navegador.

Redirect

Después de operaciones exitosas realizadas mediante POST, se utiliza redirección:

POST → Redirect → GET

Esto implementa el patrón:

PRG — Post / Redirect / Get

El objetivo es evitar que el navegador vuelva a enviar el formulario al actualizar la página.


