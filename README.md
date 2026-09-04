# ChemLab

Juego/laboratorio químico educativo 2D desarrollado con **Java 21 + JavaFX + Maven**.

## Arquitectura inicial

El proyecto se organizará por responsabilidades, evitando que la interfaz gráfica contenga reglas químicas o lógica de negocio.

- `model/`: entidades y objetos del dominio.
- `model/enums/`: enumeraciones del dominio.
- `engine/`: motor y reglas de ejecución de experimentos.
- `automata/`: autómata químico, estados y transiciones.
- `game/`: coordinación del flujo del juego.
- `ui/`: interfaz 2D JavaFX.
- `factory/`: creación controlada de objetos complejos.
- `repository/`: acceso a datos/configuración.
- `service/`: casos de uso y servicios de dominio.
- `resources/data/`: datos externos (JSON, posteriormente).
- `resources/images/`: recursos gráficos 2D.
- `resources/styles/`: estilos CSS de JavaFX.
- `test/`: pruebas automatizadas.

## Patrones que se incorporarán cuando aporten valor

- **State:** estados del experimento y del autómata químico.
- **Strategy:** reglas/reacciones químicas intercambiables.
- **Factory / Factory Method:** creación de sustancias, equipos y acciones.
- **Observer:** actualización desacoplada de la interfaz ante cambios del experimento.
- **Repository:** separación entre dominio y almacenamiento de datos.

No se aplicarán patrones "por obligación"; cada uno se introducirá cuando reduzca acoplamiento o facilite la extensión.

## Principios de desarrollo

- SOLID.
- Responsabilidad única.
- Bajo acoplamiento y alta cohesión.
- Inmutabilidad cuando sea conveniente.
- Validaciones en los límites del dominio.
- Excepciones específicas para errores de negocio.
- Pruebas unitarias para reglas químicas y transiciones.
- La UI no modifica directamente el estado interno del dominio.

## Ejecución

Requisitos: **JDK 21** y **Maven 3.9+**.

```bash
mvn clean test
mvn javafx:run
```
