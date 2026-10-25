# ChemLab

Juego/laboratorio químico educativo desarrollado con **Java 25 + JavaFX + Maven**.

La aplicación inicia ahora en un prototipo 2D jugable: puedes recorrer el laboratorio, acercarte a los puestos y ejecutar desde ellos los procesos del dominio químico.

## Arquitectura inicial

El proyecto se organizará por responsabilidades, evitando que la interfaz gráfica contenga reglas químicas o lógica de negocio.

- `model/`: entidades y objetos del dominio.
- `model/enums/`: enumeraciones del dominio.
- `engine/`: motor y reglas de ejecución de experimentos.
- `automata/`: autómata químico, estados y transiciones.
- `grammar/`: gramáticas regulares para protocolos, conversión a autómatas y el parser libre de contexto de fórmulas; consulta [GRAMATICAS_Y_AUTOMATAS.md](GRAMATICAS_Y_AUTOMATAS.md).
- `game/`: sesión, coordinación, mapa, personaje y estaciones del juego.
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

La vista 2D observa `SesionJuego`; los puestos se construyen a partir del equipo asignado por `FabricaExperimento`, y las acciones siguen pasando por los autómatas y el motor químico existentes. `ui/game/RenderizadorLaboratorio2D` dibuja el mapa y al personaje; `ui/game/VistaJuego2D` coordina controles y menús.

El cuaderno E incluye prácticas de gramáticas regulares para los protocolos de agua y diamante CVD, con el autómata equivalente avanzando en paralelo, y un analizador libre de contexto de fórmulas que muestra su árbol de derivación y el conteo de átomos. Los detalles y límites están en [GRAMATICAS_Y_AUTOMATAS.md](GRAMATICAS_Y_AUTOMATAS.md).

Al pulsar F, `ui/workbench/VistaPuestoLaboratorio` abre una mesa completa con el equipo propio de la estación. Los reactivos se arrastran al recipiente dibujado sobre la mesa; los procesos se inician accionando los mandos o herramientas visibles. Calor, llama, frío, mezcla, presión, plasma e ignición se animan en el instrumento. `game/procedure/PlanificadorEstacionesProcedimiento` determina cuándo mover el proceso a otro puesto. `ui/components/IconoSustancia` representa cada reactivo según su estado físico, y el cuaderno E conserva su diseño digital separado del almacén M.

## Controles del prototipo 2D

- **WASD** o flechas: mover al personaje.
- **F**: abrir la mesa del puesto requerido para el procedimiento.
- Arrastra reactivos al recipiente o acciona la herramienta resaltada en la mesa; observa la animación hasta que termine.
- **E**: abrir inventario y cuaderno de laboratorio.
- **M**: abrir almacén y seleccionar/iniciar una práctica.
- **Esc**: cerrar el menú o el panel de acciones.

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

Requisitos: **JDK 25** y **Maven 3.9+**.

```bash
mvn clean test
mvn javafx:run
```
